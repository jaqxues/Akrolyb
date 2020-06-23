package com.jaqxues.akrolyb.genhook

import android.app.Activity
import android.content.Context
import com.jaqxues.akrolyb.genhook.decs.*
import com.jaqxues.akrolyb.genhook.decs.MemberDec.ConstructorDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec.MethodDec
import com.jaqxues.akrolyb.genhook.states.ClassNotFoundException
import com.jaqxues.akrolyb.genhook.states.StateManager
import com.jaqxues.akrolyb.utils.Predicate
import com.jaqxues.akrolyb.utils.collectAll
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import timber.log.Timber
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 16.03.20 - Time 15:42.
 */

/**
 * Abstractions for null safety, exception handling, ... Also contains caching mechanisms to cache reflection used to
 * find classes and hooks.
 */
abstract class FeatureHelper : Feature {
    private lateinit var stateManager: StateManager
    protected fun callMethod(obj: Any?, method: MethodDec, vararg params: Any?): Any? {
        try {
            // obj == null -> invoke Static Method, must be resolved
            if (obj == null || resolvedMembers.containsKey(method) &&
                // Inheritance, call method on child
                obj.javaClass.name == method.classDec.className
            ) {
                return (resolvedMembers[method] as? Method ?: throw AssertionError("Method not resolved"))
                    .invoke(obj, *params)
            } else {
                return XposedHelpers.callMethod(obj, method.methodName, *params)
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            stateManager.addCallError(this, method, ex)
            return null
        }
    }

    protected fun callStaticMethod(method: MethodDec, vararg params: Any?): Any? {
        return callMethod(null, method, *params)
    }

    protected fun hookMethod(method: MethodDec, hook: XC_MethodHook) {
        tryHook(method) { XposedBridge.hookMethod(resolvedMembers[method], hook).addToUnhooks() }
    }

    protected fun hookConstructor(constructor: ConstructorDec, hook: XC_MethodHook) {
        tryHook(constructor) { XposedBridge.hookMethod(resolvedMembers[constructor], hook).addToUnhooks() }
    }

    protected fun hookAllMethods(methodDec: MethodDec, classLoader: ClassLoader, hook: XC_MethodHook) {
        tryHook(methodDec) {
            XposedBridge.hookAllMethods(
                XposedHelpers.findClass(methodDec.classDec.className, classLoader),
                methodDec.methodName,
                hook
            ).addToUnhooks()
        }
    }

    protected fun newInstance(constructorDec: ConstructorDec, vararg params: Any?): Any? {
        try {
            return (resolvedMembers[constructorDec] as? Constructor<*>
                ?: throw AssertionError("Constructor not resolved"))
                .newInstance(*params)
        } catch (e: Exception) {
            Timber.e(e)
            stateManager.addHookError(constructorDec, this, e)
            return null
        }
    }

    // Entry Point Optional
    override fun lateInit(classLoader: ClassLoader, activity: Activity) {}

    private fun Set<XC_MethodHook.Unhook>.addToUnhooks() {
        val key = this@FeatureHelper::class
        val list = unhookMap[key] ?: (mutableListOf<XC_MethodHook.Unhook>().also {
            unhookMap[key] = it
        })
        list.addAll(this)
    }

    private fun XC_MethodHook.Unhook.addToUnhooks() {
        val key = this@FeatureHelper::class
        val list = unhookMap[key] ?: (mutableListOf<XC_MethodHook.Unhook>().also {
            unhookMap[key] = it
        })
        list.add(this)
    }

    private inline fun tryHook(dec: MemberDec, action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            Timber.e(e)
            stateManager.addHookError(dec, this, e)
        }
    }


    // - - - - - - - - VariableDec Methods

    protected fun <T> VariableDec<T>.setStaticVar(clazz: Class<*>, value: T) {
        tryVar {
            XposedHelpers.setStaticObjectField(clazz, this.name, checkVal { value })
        }
    }

    protected fun <T> VariableDec<T>.getStaticVar(clazz: Class<*>) =
        tryVar {
            checkVal { XposedHelpers.getStaticObjectField(clazz, name) as T }
        }

    protected fun <T> VariableDec<T>.setVar(obj: Any, value: T) {
        tryVar {
            XposedHelpers.setObjectField(obj, name, checkVal { value })
        }
    }

    protected fun <T> VariableDec<T>.getVar(obj: Any) =
        tryVar {
            checkVal { XposedHelpers.getObjectField(obj, name) as T }
        }

    // Convenience Methods
    protected fun <T> VariableDec<T>.setStaticVar(classDec: ClassDec, classLoader: ClassLoader, value: T) {
        tryVar { setStaticVar(classDec.findClass(classLoader), value) }
    }

    protected fun <T> VariableDec<T>.getStaticVar(classDec: ClassDec, classLoader: ClassLoader) =
        tryVar {
            getStaticVar(classDec.findClass(classLoader))
        }

    private inline fun <T> VariableDec<T>.checkVal(value: () -> T): T =
        if (nullable) value() else (value()
            ?: throw NullPointerException("Value of VariableDec defined as non-nullable, but received null value"))

    private inline fun <T> VariableDec<*>.tryVar(action: () -> T?): T? {
        return try {
            action()
        } catch (ex: Exception) {
            Timber.e(ex)
            stateManager.addVarError(this@FeatureHelper, this, ex)
            null
        }
    }


    // - - - - - - - - VariableDec Methods

    /**
     * @return The previously stored value for this instance/key combination, or null if there was none.
     */
    // === Additional Fields on Objects ==
    fun <T> AddInsField<T>.set(obj: Any, value: T?) =
        tryAddInsField { XposedHelpers.setAdditionalInstanceField(obj, id, value) as T? }

    fun <T> AddInsField<T>.get(obj: Any) =
        tryAddInsField { XposedHelpers.getAdditionalInstanceField(obj, id) as T? }

    // === Static Additional Fields ==
    fun <T> AddInsField<T>.get(clazz: Class<*>) =
        tryAddInsField { XposedHelpers.getAdditionalStaticField(clazz, id) as T? }

    /**
     * @return The previously stored value for this instance/key combination, or null if there was none.
     */
    fun <T> AddInsField<T>.set(clazz: Class<*>, value: T?) =
        tryAddInsField { XposedHelpers.setAdditionalStaticField(clazz, id, value) as T? }

    // Convenience methods
    fun <T> AddInsField<T>.get(classDec: ClassDec, classLoader: ClassLoader) =
        get(classDec.findClass(classLoader))

    fun <T> AddInsField<T>.set(classDec: ClassDec, classLoader: ClassLoader, value: T?) =
        set(classDec.findClass(classLoader), value)

    private inline fun <T> AddInsField<T>.tryAddInsField(action: () -> T?): T? {
        return try {
            action()
        } catch (ex: Exception) {
            Timber.e(ex)
            stateManager.addAddInsFieldError(this@FeatureHelper, this, ex)
            null
        }
    }


    internal companion object {
        private lateinit var resolvedMembers: Map<MemberDec, Member>
        private var unhookMap = mutableMapOf<KClass<out FeatureHelper>, MutableList<XC_MethodHook.Unhook>>()
        private lateinit var activeFeatures: List<FeatureHelper>

        fun loadAll(
            classLoader: ClassLoader,
            context: Context,
            featureManager: FeatureManager<out FeatureHelper>,
            stateManager: StateManager,
            vararg hookDefs: Any
        ) {
            check(unhookMap.isEmpty()) { "Some Features already loaded" }

            activeFeatures = featureManager.getActiveFeatures()
            if (activeFeatures.isEmpty())
                return

            resolveMembers(classLoader, activeFeatures.map { it::class.java }, stateManager, *hookDefs)

            for (feature in activeFeatures) {
                feature.stateManager = stateManager
                try {
                    feature.loadFeature(classLoader, context)
                } catch (ex: Exception) {
                    stateManager.addHookAbort(feature, ex)
                }
            }
        }

        fun lateInitAll(classLoader: ClassLoader, activity: Activity, stateManager: StateManager) {
            for (feature in activeFeatures) {
                try {
                    feature.lateInit(classLoader, activity)
                } catch (e: Exception) {
                    stateManager.addLateInitAbort(feature, e)
                }
            }
        }

        /** @return true if unhooks have been performed */
        fun unhookByFeature(feature: KClass<out FeatureHelper>): Boolean {
            return unhookMap[feature]?.let { list ->
                if (unhookMap.isEmpty())
                    return@let false
                list.forEach {
                    it.unhook()
                }
                list.clear()
                true
            } ?: false
        }

        /**
         * Resolves Members needed for active features.
         *
         * @param classLoader  Target ClassLoader
         * @param features     Currently active features to decide which Members to resolve
         * @param collectables Implemented ability to load Declarations either from static objects and from objects,
         *                     which allows for dynamically loading in Declarations from JSON Files or other input.
         */
        private fun resolveMembers(
            classLoader: ClassLoader,
            features: List<Class<out FeatureHelper>>,
            stateManager: StateManager,
            vararg hookDefs: Any
        ) {
            // Nullable Class means unresolved class, should be skipped for other members of class
            val cache = HashMap<String, Class<*>?>()
            val test: Predicate<MemberDec> = { memberDec ->
                memberDec.usedInFeature.any { it in features }
            }

            val members: List<MemberDec> = hookDefs.flatMap { c ->
                when (c) {
                    is KClass<*> -> c.collectAll(test = test)
                    else -> c::class.collectAll(
                        obj = c,
                        test = test
                    )
                }
            }
            val resolved = mutableMapOf<MemberDec, Member>()
            for (member in members) {
                val className = member.classDec.className
                val resolvedClass: Class<*>?
                if (className in cache) {
                    resolvedClass = cache[className]
                    if (resolvedClass == null) {
                        stateManager.addUnresolvedMember(member)
                        continue // continue if class could not be resolved previously
                    }
                } else {
                    try {
                        resolvedClass = member.classDec.findClass(classLoader, false)
                    } catch (ex: ClassNotFoundException) {
                        stateManager.addUnresolvedClass(member.classDec, ex)
                        stateManager.addUnresolvedMember(member)
                        // Mark as unresolved in caching
                        cache[className] = null
                        continue
                    }

                    cache[className] = resolvedClass
                }

                resolved[member] = try {
                    member.findMember(resolvedClass)
                } catch (ex: NoSuchMethodError) {
                    stateManager.addUnresolvedMember(member, ex)
                    continue
                }
            }
            resolvedMembers = resolved.toMap()
        }
    }
}