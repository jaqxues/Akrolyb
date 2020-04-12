package com.jaqxues.akrolyb.genhook.states

import com.jaqxues.akrolyb.genhook.Feature
import com.jaqxues.akrolyb.genhook.decs.ClassDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 09.04.20 - Time 00:06.
 */
class StateManager {
    private val unresolved = mutableListOf<WarnSignal>()
    private val warnings = mutableMapOf<Class<out Feature>, WarnSignal>()
    private val aborts = mutableMapOf<Class<out Feature>, AbortSignal>()

    internal fun addUnresolvedClass(dec: ClassDec, ex: ClassNotFoundException) {
        unresolved.add(WarnSignal.UnresolvedClass(dec, ex))
    }

    internal fun addUnresolvedMember(dec: MemberDec, ex: NoSuchMethodError? = null) {
        if (ex == null) {
            unresolved.add(WarnSignal.UnresolvedMember(dec, Cause.Class))
        } else {
            unresolved.add(WarnSignal.UnresolvedMember(dec, Cause.Member(ex)))
        }
    }

    internal fun addCallError(feature: Feature, dec: MemberDec, ex: Exception) {
        warnings[feature::class.java] = WarnSignal.MethodCallError(dec, ex)
    }

    internal fun addHookError(dec: MemberDec, feature: Feature, ex: Exception) {
        warnings[feature::class.java] = WarnSignal.MethodHookError(dec, ex)
    }

    internal fun addLateInitAbort(feature: Feature, ex: Exception) {
        aborts[feature::class.java] = AbortSignal.LateInit(ex)
    }

    internal fun addHookAbort(feature: Feature, ex: Exception) {
        aborts[feature::class.java] = AbortSignal.Hooks(ex)
    }

    fun getGlobalState(): State {
        if (aborts.isNotEmpty())
            return State.Aborted(aborts.values + warnings.values + unresolved)
        if (warnings.isNotEmpty() && unresolved.isNotEmpty())
            return State.Warning(warnings.values + unresolved)
        return State.Success
    }

    fun byFeature(features: List<Class<out Feature>>): Map<Class<out Feature>, State> {
        val reasonMap = features.associateWith { mutableListOf<StateReason>() }
        if (unresolved.isEmpty() && warnings.isEmpty() && aborts.isEmpty())
            return features.associateWith { State.Success }

        for ((k, v) in warnings) {
            (reasonMap[k] ?: error("Unknown Feature in Warn Signals")).add(v)
        }

        if (unresolved.isNotEmpty()) {
            val cls =
                unresolved.filterIsInstanceTo<WarnSignal.UnresolvedClass, MutableList<WarnSignal.UnresolvedClass>>(
                    mutableListOf()
                )
            for (item in unresolved) {
                if (item is WarnSignal.MemberIssue) {
                    if (item is WarnSignal.UnresolvedMember && item.cause is Cause.Class) {
                        val clazz = cls.find { item.member.classDec == it.dec }
                            ?: error("Could not find Unresolved Class that caused an Unresolved Method")
                        cls.remove(clazz)
                        item.member.usedInFeature.forEach {
                            val list = (reasonMap[it] ?: error("Unknown Feature in Unresolved Method Signal"))
                            list.add(clazz)
                            list.add(item)
                        }
                    } else {
                        item.member.usedInFeature.forEach {
                            (reasonMap[it] ?: error("Unknown Feature in Unresolved Warning Signals")).add(item)
                        }
                    }
                }
            }
            check(cls.isEmpty()) { "Unresolved Classes without matching" }
        }

        for ((k, v) in aborts) {
            (reasonMap[k] ?: error("Unknown Feature in Abort Signals")).add(v)
        }

        @Suppress("UNCHECKED_CAST")
        return reasonMap.map { (feature, reasons) ->
            feature to when (reasons.max()) {
                is AbortSignal -> State.Aborted(reasons)
                is WarnSignal -> State.Warning(reasons as List<WarnSignal>)
                null -> State.Success
            }
        }.toMap()
    }
}
