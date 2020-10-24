package com.jaqxues.akrolyb.genhook.decs

import com.jaqxues.akrolyb.genhook.Feature
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 21.07.2019 - Time 00:02.
 * Pulled out to SocialLib on 16.03.2020 - Time 15:00.
 *
 * Actions on the Declarations are in [com.jaqxues.akrolyb.genhook.FeatureHelper] to ensure that the Xposed Part of the
 * code is only called in an appropriate Xposed Context and to have access to the private caching in FeatureHelper.
 */

private val classCache = mutableMapOf<String, Class<*>>()

open class ClassDec(val className: String) {
    /**
     * Find Classes by ClassDec and ClassLoader.
     *
     * Caching is disabled while initializing Members. [com.jaqxues.akrolyb.genhook.FeatureHelper] has its own little
     * caching. This functionality is reserved for actions that require Classes after resolving Methods, such as
     * accessing Additional Static Fields or "Regular" Static Fields
     *
     * Open: Allows Returning Class by direct reference (e.g. if Class is on BootClassLoader)
     */
    open fun findClass(classLoader: ClassLoader, cache: Boolean = true): Class<*> {
        return if (cache && className in classCache) {
            classCache[className] ?: throw IllegalStateException("Value of key $className null")
        } else {
            XposedHelpers.findClass(className, classLoader).also {
                if (cache)
                    classCache[className] = it
            }
        }
    }
}

/**
 * Part of the Abstraction Layer that allows to separate the declarations of target hooks in case of obfuscation and
 * also allows for proper reflection caching.
 */
sealed class MemberDec(
    open val classDec: ClassDec,
    open val usedInFeature: Array<Class<out Feature>>,
    open vararg val params: Any
) {
    class ConstructorDec(
        override val classDec: ClassDec,
        override val usedInFeature: Array<Class<out Feature>>,
        override vararg val params: Any
    ) : MemberDec(classDec, usedInFeature, params) {
        fun findConstructor(resolvedClass: Class<*>): Constructor<*> =
            XposedHelpers.findConstructorExact(
                resolvedClass,
                *params.map {
                    if (it is ClassDec)
                        it.className
                    else
                        it
                }.toTypedArray()
            )
    }

    class MethodDec(
        override val classDec: ClassDec,
        val methodName: String,
        override val usedInFeature: Array<Class<out Feature>>,
        override vararg val params: Any
    ) : MemberDec(classDec, usedInFeature, params) {
        fun findMethod(resolvedClass: Class<*>): Method {
            return XposedHelpers.findMethodExact(
                resolvedClass,
                methodName,
                *params.map {
                    if (it is ClassDec)
                        it.className
                    else
                        it
                }.toTypedArray()
            )
        }
    }

    // fixme Explicit "Useless" Cast to Member. Kotlin will otherwise cast to Executable, which is only available in Oreo+
    @Suppress("USELESS_CAST")
    fun findMember(clazz: Class<*>) =
        when (this) {
            is ConstructorDec -> findConstructor(clazz) as Member
            is MethodDec -> findMethod(clazz) as Member
        }

    fun prettyFormat() = buildString {
        append(classDec.className)
        append(".")
        append(if (this@MemberDec is MethodDec) methodName else "<init>")
        append("(")
        params.joinTo(buffer = this, separator = ", ") {
            when (it) {
                is String -> it
                is ClassDec -> it.className
                is Class<*> -> it.name
                else -> error("Illegal Type for Parameter")
            }
        }
        append(")")
    }
}

/**
 * Avoid having to manually enter nullable. Function acting as a Constructor
 * @param T the type to both initialize the VariableDec and, since it is a reified type, check for nullability
 * automatically
 */
@Suppress("FunctionName")
inline fun <reified T> VariableDec(name: String) = VariableDec<T>(name, null is T)

/**
 * Since Generics in Kotlin cannot check for nullability, but we want null-safety, add a [nullable] field. Calling this
 * directly should be avoided, use the inline function [VariableDec] that deals with this with a reified type.
 */
class VariableDec<T>(val name: String, val nullable: Boolean)

class AddInsField<T> {
    val id get() = toString()
}
