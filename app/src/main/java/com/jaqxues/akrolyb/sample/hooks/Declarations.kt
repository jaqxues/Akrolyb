package com.jaqxues.akrolyb.sample.hooks

import android.os.Bundle
import com.jaqxues.akrolyb.genhook.decs.ClassDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec
import com.jaqxues.akrolyb.sample.hooks.features.Forced
import com.jaqxues.akrolyb.sample.hooks.features.SavingFeature
import java.io.File


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 27.03.20 - Time 20:24.
 */
object Declarations {
    val EXAMPLE_CLASS = ClassDec("com.example.SomeClass")

    val FORCED_METHOD = MemberDec.MethodDec(EXAMPLE_CLASS, "onCreate", arrayOf(Forced::class.java), Bundle::class.java)

    val INTERCEPT_SAVING = MemberDec.ConstructorDec(EXAMPLE_CLASS, arrayOf(SavingFeature::class.java), File::class.java)

    // usedInFeature is empty, since this will be used as "hookAllMethods". (Does not need to be resolved)
    val HOOK_ALL_EXAMPLE = MemberDec.MethodDec(EXAMPLE_CLASS, "example", emptyArray())
}