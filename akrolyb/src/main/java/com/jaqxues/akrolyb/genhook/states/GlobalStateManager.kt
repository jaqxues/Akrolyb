package com.jaqxues.akrolyb.genhook.states

import com.jaqxues.akrolyb.genhook.Feature
import com.jaqxues.akrolyb.genhook.decs.AddInsField
import com.jaqxues.akrolyb.genhook.decs.ClassDec
import com.jaqxues.akrolyb.genhook.decs.MemberDec
import com.jaqxues.akrolyb.genhook.decs.VariableDec
import com.jaqxues.akrolyb.utils.associateNotNull
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 24.07.20 - Time 14:05.
 */
class GlobalStateManager: StateListener() {
    private val unresolved = mutableListOf<WarnSignal>()
    private val warnings = mutableMapOf<Class<out Feature>, MutableList<WarnSignal>>()
    private val aborts = mutableMapOf<Class<out Feature>, AbortSignal>()

    override fun addUnresolvedClass(dec: ClassDec, ex: ClassNotFoundException) {
        unresolved.add(WarnSignal.UnresolvedClass(dec, ex))
    }

    override fun addUnresolvedMember(dec: MemberDec, ex: NoSuchMethodError?) {
        if (ex == null) {
            unresolved.add(WarnSignal.UnresolvedMember(dec, Cause.Class))
        } else {
            unresolved.add(WarnSignal.UnresolvedMember(dec, Cause.Member(ex)))
        }
    }

    private inline fun safeAddWarn(feature: KClass<out Feature>, signal: () -> WarnSignal) {
        var list = warnings[feature.java]
        if (list == null) {
            list = mutableListOf()
            warnings[feature.java] = list
        }
        list.add(signal())
    }

    override fun addCallError(feature: Feature, dec: MemberDec, t: Throwable) {
        safeAddWarn(feature::class) { WarnSignal.MethodCallError(dec, t) }
    }

    override fun addHookError(feature: Feature, dec: MemberDec, t: Throwable) {
        safeAddWarn(feature::class) { WarnSignal.MethodHookError(dec, t) }
    }

    override fun addVarError(feature: Feature, dec: VariableDec<*>, t: Throwable) {
        safeAddWarn(feature::class) { WarnSignal.UnresolvedVar(dec, t) }
    }

    override fun addAddInsFieldError(feature: Feature, addInsField: AddInsField<*>, t: Throwable) {
        safeAddWarn(feature::class) { WarnSignal.AddInsFieldError(addInsField, t) }
    }

    override fun addLateInitAbort(feature: Feature, t: Throwable) {
        aborts[feature::class.java] = AbortSignal.LateInit(t)
    }

    override fun addHookAbort(feature: Feature, t: Throwable) {
        aborts[feature::class.java] = AbortSignal.Hooks(t)
    }

    fun getGlobalState(): State {
        if (aborts.isNotEmpty())
            return State.Aborted(aborts.values + warnings.values.flatten() + unresolved)
        if (warnings.isNotEmpty() || unresolved.isNotEmpty())
            return State.Warning(warnings.values.flatten() + unresolved)
        return State.Success
    }

    fun byFeature(vararg features: Class<out Feature>) = byFeature(features.toList())

    fun byFeature(features: List<Class<out Feature>>): Map<Class<out Feature>, State> {
        val reasonMap = features.associateWith { mutableListOf<StateReason>() }
        if (unresolved.isEmpty() && warnings.isEmpty() && aborts.isEmpty())
            return features.associateWith { State.Success }

        for ((k, v) in warnings) {
            (reasonMap[k] ?: error("Unknown Feature in Warn Signals")).addAll(v)
        }

        if (unresolved.isNotEmpty()) {
            val cls =
                unresolved.associateNotNull {
                    if (it is WarnSignal.UnresolvedClass)
                        it.dec to (it to false)
                    else null
                }
            for (item in unresolved) {
                if (item is WarnSignal.MemberIssue) {
                    if (item is WarnSignal.UnresolvedMember && item.cause is Cause.Class) {
                        val clsDec = item.member.classDec
                        val cause = cls[clsDec]
                            ?: error("Could not find Unresolved Class that caused an Unresolved Method")
                        if (!cause.second)
                            cls[clsDec] = cause.copy(second = true)
                        item.member.usedInFeature.forEach {
                            val list = (reasonMap[it]
                                ?: error("Unknown Feature in Unresolved Method Signal"))
                            if (cause.first !in list)
                                list.add(cause.first)
                            list.add(item)
                        }
                    } else {
                        item.member.usedInFeature.forEach {
                            (reasonMap[it]
                                ?: error("Unknown Feature in Unresolved Warning Signals")).add(item)
                        }
                    }
                }
            }
            check(cls.values.all { (_, v) -> v }) { "Unresolved Classes without matching" }
        }

        for ((k, v) in aborts) {
            (reasonMap[k] ?: error("Unknown Feature in Abort Signals")).add(v)
        }

        @Suppress("UNCHECKED_CAST")
        return reasonMap.map { (feature, reasons) ->
            feature to when (reasons.maxOrNull()) {
                is AbortSignal -> State.Aborted(reasons)
                is WarnSignal -> State.Warning(reasons as List<WarnSignal>)
                null -> State.Success
            }
        }.toMap()
    }
}
