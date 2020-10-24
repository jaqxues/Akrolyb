package com.jaqxues.akrolyb.genhook.states

import com.jaqxues.akrolyb.genhook.Feature
import com.jaqxues.akrolyb.genhook.decs.AddInsField
import com.jaqxues.akrolyb.genhook.decs.ClassDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec
import com.jaqxues.akrolyb.genhook.decs.VariableDec


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 09.04.20 - Time 00:06.
 */
abstract class StateListener {
    open fun addUnresolvedClass(dec: ClassDec, ex: ClassNotFoundException) {}
    open fun addUnresolvedMember(dec: MemberDec, ex: NoSuchMethodError? = null) {}
    open fun addCallError(feature: Feature, dec: MemberDec, t: Throwable) {}
    open fun addHookError(feature: Feature, dec: MemberDec, t: Throwable) {}
    open fun addVarError(feature: Feature, dec: VariableDec<*>, t: Throwable) {}
    open fun addAddInsFieldError(feature: Feature, addInsField: AddInsField<*>, t: Throwable) {}
    open fun addLateInitAbort(feature: Feature, t: Throwable) {}
    open fun addHookAbort(feature: Feature, t: Throwable) {}
}

class StateDispatcher: StateListener() {
    private val stateListeners = mutableListOf<StateListener>()

    fun addStateListener(stateListener: StateListener) = stateListeners.add(stateListener)
    fun removeStateListener(stateListener: StateListener) = stateListeners.remove(stateListener)
    fun removeAllStateListeners() = stateListeners.clear()

    override fun addUnresolvedClass(dec: ClassDec, ex: ClassNotFoundException) =
        stateListeners.forEach { it.addUnresolvedClass(dec, ex) }
    override fun addUnresolvedMember(dec: MemberDec, ex: NoSuchMethodError?) =
        stateListeners.forEach { it.addUnresolvedMember(dec, ex) }
    override fun addCallError(feature: Feature, dec: MemberDec, t: Throwable) =
        stateListeners.forEach { it.addCallError(feature, dec, t) }
    override fun addHookError(feature: Feature, dec: MemberDec, t: Throwable) =
        stateListeners.forEach { it.addHookError(feature, dec, t) }
    override fun addVarError(feature: Feature, dec: VariableDec<*>, t: Throwable) =
        stateListeners.forEach { it.addVarError(feature, dec, t) }
    override fun addAddInsFieldError(feature: Feature, addInsField: AddInsField<*>, t: Throwable) =
        stateListeners.forEach { it.addAddInsFieldError(feature, addInsField, t) }
    override fun addLateInitAbort(feature: Feature, t: Throwable) =
        stateListeners.forEach { it.addLateInitAbort(feature, t) }
    override fun addHookAbort(feature: Feature, t: Throwable) =
        stateListeners.forEach { it.addHookAbort(feature, t) }
}
