package com.jaqxues.akrolyb.sample.hooks.features

import android.content.Context
import com.jaqxues.akrolyb.genhook.FeatureHelper
import com.jaqxues.akrolyb.genhook.decs.AddInsField
import com.jaqxues.akrolyb.genhook.decs.HookWrapper
import com.jaqxues.akrolyb.genhook.decs.VariableDec
import com.jaqxues.akrolyb.prefs.getPref
import com.jaqxues.akrolyb.sample.R
import com.jaqxues.akrolyb.sample.hooks.Declarations
import com.jaqxues.akrolyb.sample.prefs.Preferences
import java.io.File


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 27.03.20 - Time 20:30.
 */
class SavingFeature: FeatureHelper() {
    override val name = R.string.saving_title

    override fun loadFeature(classLoader: ClassLoader, context: Context) {
        hookConstructor(Declarations.INTERCEPT_SAVING, object : HookWrapper() {
            override fun after(param: MethodHookParam) {
                if (Preferences.SHOULD_SAVE.getPref()) {
                    println((param.args[0] as File).absolutePath)
                }
            }
        })

        hookAllMethods(Declarations.HOOK_ALL_EXAMPLE, classLoader, object: HookWrapper() {
            override fun before(param: MethodHookParam) {
                if (HAS_VIEWED.get(param.thisObject) == false) {
                    HAS_VIEWED.set(param.thisObject, true)
                }

                val user = param.args[0]
                val username = NAME_FIELD.getVar(user)
            }
        })
    }

    companion object {
        // Additional Field on Objects
        val HAS_VIEWED = AddInsField<Boolean>()
        val NAME_FIELD = VariableDec<String>("username")
    }
}