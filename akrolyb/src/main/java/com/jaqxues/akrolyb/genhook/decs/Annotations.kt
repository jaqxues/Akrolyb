package com.jaqxues.akrolyb.genhook.decs


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 11.08.2020 - Time 22:22.
 *
 * Annotations primarily used by Akrolyb to generate GraphGuard compatible code to auto-update hooks
 */


/**
 * Usually MemberDeclarations used with `hookAllMethods` to avoid copying a lot of parameter types.
 * Allows GraphGuard to ignore the parameters and only resolve the Method by its class and name
 */
@Target(AnnotationTarget.FIELD)
annotation class SkipParams

/**
 * The Class where the Fields are declared. Since a single Field can have multiple classes (override),
 * the classes are represented by an Array of Strings
 */
@Target(AnnotationTarget.FIELD)
annotation class FieldClass(val cls: Array<String>)