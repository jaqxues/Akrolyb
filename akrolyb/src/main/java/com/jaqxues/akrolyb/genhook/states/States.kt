package com.jaqxues.akrolyb.genhook.states

import com.jaqxues.akrolyb.genhook.decs.AddInsField
import com.jaqxues.akrolyb.genhook.decs.ClassDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec
import com.jaqxues.akrolyb.genhook.decs.VariableDec
import de.robv.android.xposed.XposedHelpers


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 08.04.20 - Time 22:53.
 */
typealias ClassNotFoundException = XposedHelpers.ClassNotFoundError

sealed class StateReason : Comparable<StateReason> {
    override fun compareTo(other: StateReason) =
        if (this is AbortSignal) {
            if (other is AbortSignal) 0 else 1
        } else {
            if (other is AbortSignal) -1 else 1
        }
}

sealed class WarnSignal : StateReason() {
    data class UnresolvedClass(val dec: ClassDec, val ex: ClassNotFoundException) : WarnSignal()
    data class UnresolvedMember(override val member: MemberDec, val cause: Cause) : WarnSignal(), MemberIssue
    data class MethodCallError(override val member: MemberDec, val ex: Throwable): WarnSignal(), MemberIssue
    data class MethodHookError(override val member: MemberDec, val ex: Throwable): WarnSignal(), MemberIssue
    data class UnresolvedVar(val dec: VariableDec<*>, val ex: Throwable): WarnSignal()
    data class AddInsFieldError(val addInsField: AddInsField<*>, val ex: Throwable): WarnSignal()

    interface MemberIssue {
        val member: MemberDec
    }
}

sealed class AbortSignal : StateReason() {
    data class Hooks(val hookError: Throwable) : AbortSignal()
    data class LateInit(val lateInitError: Throwable) : AbortSignal()
}

sealed class State {
    class Aborted(val signals: List<StateReason>) : State()
    class Warning(val signals: List<WarnSignal>) : State()
    object Success : State()
}

sealed class Cause {
    object Class: Cause()
    class Member(val cause: NoSuchMethodError): Cause()
}
