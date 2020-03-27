package com.jaqxues.akrolyb.genhook

import android.app.Activity
import android.content.Context
import com.jaqxues.akrolyb.genhook.decs.MemberDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec.ConstructorDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec.MethodDec
import com.jaqxues.akrolyb.utils.CollectableDec
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
abstract class FeatureHelper : Feature {
    protected fun callMethod(obj: Any?, method: MethodDec, vararg params: Any?): Any? {
        return try {
            // obj == null -> invoke Static Method, must be resolved
            if (obj == null || resolvedMembers.containsKey(method) &&
                    // Inheritance, call method on child
                    obj.javaClass.name == method.classDec.className) {
                (resolvedMembers[method] as? Method ?: throw AssertionError("Method not resolved"))
                        .invoke(obj, *params)
            } else {
                XposedHelpers.callMethod(obj, method.methodName, *params)
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    protected fun callStaticMethod(method: MethodDec, vararg params: Any?): Any? {
        return callMethod(null, method, *params)
    }

    protected fun hookMethod(method: MethodDec, hook: XC_MethodHook): XC_MethodHook.Unhook {
        return XposedBridge.hookMethod(resolvedMembers[method], hook).addToUnhooks()
    }

    protected fun hookConstructor(constructor: ConstructorDec, hook: XC_MethodHook): XC_MethodHook.Unhook {
        return XposedBridge.hookMethod(resolvedMembers[constructor], hook).addToUnhooks()
    }

    protected fun hookAllMethods(methodDec: MethodDec, classLoader: ClassLoader, hook: XC_MethodHook): Set<XC_MethodHook.Unhook> {
        return XposedBridge.hookAllMethods(
                XposedHelpers.findClass(methodDec.classDec.className, classLoader),
                methodDec.methodName,
                hook).addToUnhooks()
    }

    protected fun newInstance(constructorDec: ConstructorDec, vararg params: Any?): Any? {
        return try {
            (resolvedMembers[constructorDec] as? Constructor<*>
                    ?: throw AssertionError("Constructor not resolved"))
                    .newInstance(*params)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    // Entry Point Optional
    override fun lateInit(classLoader: ClassLoader, activity: Activity) {}

    private fun Set<XC_MethodHook.Unhook>.addToUnhooks(): Set<XC_MethodHook.Unhook> {
        val key = this@FeatureHelper::class
        val list = unhookMap[key] ?: (mutableListOf<XC_MethodHook.Unhook>().also {
            unhookMap[key] = it
        })
        list.addAll(this)
        return this
    }

    private fun XC_MethodHook.Unhook.addToUnhooks(): XC_MethodHook.Unhook {
        val key = this@FeatureHelper::class
        val list = unhookMap[key] ?: (mutableListOf<XC_MethodHook.Unhook>().also {
            unhookMap[key] = it
        })
        list.add(this)
        return this
    }

    internal companion object {
        private lateinit var resolvedMembers: Map<MemberDec, Member>
        private var unhookMap = mutableMapOf<KClass<out Feature>, MutableList<XC_MethodHook.Unhook>>()
        private lateinit var activeFeatures: List<Feature>

        fun loadAll(classLoader: ClassLoader, context: Context, featureManager: FeatureManager, vararg collectables: CollectableDec) {
            check(unhookMap.isEmpty()) { "Some Features already loaded" }

            activeFeatures = featureManager.getActiveFeatures()
            if (activeFeatures.isEmpty())
                return

            resolveMembers(classLoader, activeFeatures.map { it::class.java }, *collectables)

            for (feature in activeFeatures) {
                feature.loadFeature(classLoader, context)
            }
        }

        fun lateInitAll(classLoader: ClassLoader, activity: Activity) {
            for (feature in activeFeatures)
                feature.lateInit(classLoader, activity)
        }

        /** @return true if unhooks have been performed */
        fun unhookByFeature(feature: KClass<out Feature>): Boolean {
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
        private fun resolveMembers(classLoader: ClassLoader, features: List<Class<out Feature>>, vararg collectables: CollectableDec) {
            val cache = HashMap<String, Class<*>>()
            val test: Predicate<MemberDec> = { memberDec ->
                memberDec.usedInFeature.any { it in features }
            }

            val members: List<MemberDec> = collectables.flatMap { collectable ->
                when (collectable) {
                    is CollectableDec.Class -> collectable.value.collectAll(test = test)
                    is CollectableDec.Object -> collectable.value::class.collectAll(obj = collectable.value, test = test)
                }
            }
            val resolved = mutableMapOf<MemberDec, Member>()
            for (member in members) {
                val className = member.classDec.className
                val resolvedClass: Class<*>?
                if (cache.containsKey(className))
                    resolvedClass = cache[className]
                else {
                    resolvedClass = member.classDec.findClass(classLoader, false)
                    cache[className] = resolvedClass
                }

                resolvedClass ?: throw IllegalStateException("Unresolved Class")

                resolved[member] = when (member) {
                    is MethodDec -> member.findMethod(resolvedClass)
                    is ConstructorDec -> member.findConstructor(resolvedClass)
                }
            }
            resolvedMembers = resolved.toMap()
        }
    }
}