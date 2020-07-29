package com.jaqxues.akrolyb.genhook.states

import com.jaqxues.akrolyb.genhook.Feature
import com.jaqxues.akrolyb.genhook.decs.AddInsField
import com.jaqxues.akrolyb.genhook.decs.ClassDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec
import com.jaqxues.akrolyb.genhook.decs.VariableDec
import timber.log.Timber

class StateLogger: StateListener() {
    override fun addUnresolvedClass(dec: ClassDec, ex: ClassNotFoundException) {
        Timber.e(ex, "Unresolved ClassDec (${dec.className}")
    }

    override fun addUnresolvedMember(dec: MemberDec, ex: NoSuchMethodError?) {
        Timber.e(ex, "Unresolved MemberDec ${dec.prettyFormat()}")
    }

    override fun addCallError(feature: Feature, dec: MemberDec, t: Throwable) {
        Timber.e(t, "Error while invoking Member ${dec.prettyFormat()} in Feature $feature")
    }

    override fun addHookError(feature: Feature, dec: MemberDec, t: Throwable) {
        Timber.e(t, "Error while hooking Member ${dec.prettyFormat()} in Feature $feature")
    }

    override fun addVarError(feature: Feature, dec: VariableDec<*>, t: Throwable) {
        Timber.e(t, "Error while using Variable Dec ${dec.name} in Feature $feature")
    }

    override fun addAddInsFieldError(feature: Feature, addInsField: AddInsField<*>, t: Throwable) {
        Timber.e(t, "Error while using an AdditionalInstanceField in Feature $feature")
    }

    override fun addLateInitAbort(feature: Feature, t: Throwable) {
        Timber.e(t, "Aborting LateInit Process of Pack in Feature $feature")
    }

    override fun addHookAbort(feature: Feature, t: Throwable) {
        Timber.e(t, "Aborting Hook Process of Pack in Feature $feature")
    }
}