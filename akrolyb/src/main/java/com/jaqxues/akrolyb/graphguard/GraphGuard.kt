package com.jaqxues.akrolyb.graphguard

import com.jaqxues.akrolyb.genhook.decs.*
import java.lang.reflect.Field

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 12.08.20 - Time 15:21.
 */
object GraphGuard {
    fun generateNamedFDecs(fieldDecs: Any) =
        fieldDecs::class.java.declaredFields.mapNotNull { f ->
            f.isAccessible = true
            val x = f.get(fieldDecs)

            if (x !is VariableDec<*>) return@mapNotNull null

            "'${f.name}': ${x.formatToGraphGuard(f)}"
        }

    fun generateNamedMDecs(memberDecs: Any) =
        memberDecs::class.java.declaredFields.mapNotNull { f ->
            f.isAccessible = true
            val x = f.get(memberDecs)

            if (x !is MemberDec || f.isAnnotationPresent(Ignore::class.java))
                return@mapNotNull null

            "'${f.name}': ${x.formatToGraphGuard(f)}"
        }

    fun generateNamedCDecs(classDecs: Any) =
        classDecs::class.java.declaredFields.mapNotNull { f ->
            f.isAccessible = true
            val c = f.get(classDecs)
            if (c !is ClassDec)
                return@mapNotNull null

            "'${f.name}': '${c.className}'"
        }

    fun printGeneratedDecs(classDecs: Any, memberDecs: Any, fieldDecs: Any) =
        printGeneratedDecs(
            arrayOf(classDecs), arrayOf(memberDecs), arrayOf(fieldDecs)
        )

    fun printGeneratedDecs(classDecs: Array<Any>, memberDecs: Array<Any>, fieldDecs: Array<Any>) {

        println("named_c_decs = {")
        println(classDecs.flatMap(::generateNamedCDecs).joinToString(separator = ",\n") { "    $it" })
        println("}")

        println()

        println("named_m_decs = {")
        println(memberDecs.flatMap(::generateNamedMDecs).joinToString(separator = ",\n") { "    $it" })
        println("}")

        println()

        println("named_f_decs = {")
        println(fieldDecs.flatMap(::generateNamedFDecs).joinToString(separator = ",\n") { "    $it" })
        println("}")
    }
}

fun MemberDec.formatToGraphGuard(f: Field) = buildString {
    append("MethodDec('")
    append(classDec.className)
    append("', '")
    append(if (this@formatToGraphGuard is MemberDec.MethodDec) methodName else "<init>")
    append("'")
    for (param in params) {
        append(", '")
        if (param is String)
            append(param)
        else if (param is ClassDec)
            append(param.className)
        else if (param is Class<*>)
            append(param.canonicalName!!)
        else error("Illegal Type for param")
        append("'")
    }
    if (f.isAnnotationPresent(SkipParams::class.java))
        append(", skip_params=True")
    append(")")
}

fun VariableDec<*>.formatToGraphGuard(f: Field) = buildString {
    val classAnnotation = f.getAnnotation(FieldClass::class.java) ?: error(
        "Class Annotation must be present for FieldDeclaration and GraphGuard Compatibility"
    )

    val cls = classAnnotation.cls.map { cl -> "FieldDec('$cl', '$name')" }

    if (cls.size > 1) append("(")
    append(cls.joinToString(separator = ", "))
    if (cls.size > 1) append(")")
}