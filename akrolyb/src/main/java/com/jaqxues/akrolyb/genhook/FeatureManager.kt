package com.jaqxues.akrolyb.genhook

import android.app.Activity
import android.content.Context
import com.jaqxues.akrolyb.genhook.states.StateDispatcher
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 17.03.20 - Time 11:05.
 */

/**
 * The FeatureManager class controls the registered [Feature]s and requests them to perform actions like injecting the
 * hooks.
 */
class FeatureManager<T: FeatureHelper>(featureProvider: FeatureProvider<T>) {
    private val forcedFeatures = featureProvider.forcedFeatures
    private val optionalFeatures = featureProvider.optionalFeatures
    private val featureNames = forcedFeatures + optionalFeatures
    private val disabledFeatures = featureProvider.disabledFeatures
    private var hookDefs = featureProvider.hookDefs

    val stateDispatcher = StateDispatcher()

    /**
     * Filters the features from the [FeatureProvider] to check which optional features are enabled. In case no optional
     * feature is active, forced features will be ignored too by default.
     *
     * @param alwaysReturnForced Enabling this will always return the Forced Features, even if there are no optional
     * features currently enabled
     * @return The features defined in the [FeatureProvider].
     */
    fun getActiveFeatures(alwaysReturnForced: Boolean = false): List<T> {
        val disabled = disabledFeatures()
        val activeOptionals = optionalFeatures.mapNotNull { (name, clazz) ->
            if (name in disabled) null else clazz
        }

        val list = if (activeOptionals.isEmpty()) {
            if (alwaysReturnForced)
                forcedFeatures.values
            else emptyList()
        } else
            activeOptionals + forcedFeatures.values
        return list.map { it.java.newInstance() }
    }

    fun addHookDefs(vararg any: Any) {
        hookDefs = (hookDefs.toList() + any.toList()).toTypedArray()
    }

    /**
     * Requests that all loaded features inject their hooks
     */
    fun loadAll(classLoader: ClassLoader, context: Context) {
        FeatureHelper.loadAll(classLoader, context, this, stateDispatcher, * hookDefs)
    }

    /**
     * Allows features an entry point that gives the target application time to initialize things like preferences or
     * other values. Usually called from a hooked [Activity.onCreate].
     */
    fun lateInitAll(classLoader: ClassLoader, activity: Activity) {
        FeatureHelper.lateInitAll(classLoader, activity, stateDispatcher)
    }

    fun unhookFeatureByClass(feature: KClass<out T>) = FeatureHelper.unhookByFeature(feature)
    fun unhookFeatureByName(feature: String) = FeatureHelper.unhookByFeature(getFeatureClassByName(feature))

    fun getFeatureClassByName(featureName: String): KClass<out T> =
        featureNames[featureName] ?: throw IllegalArgumentException("Feature $featureName not resolved")

    fun getFeatureNameByClass(feature: KClass<out T>) =
        featureNames.entries.find { (_, v) -> v == feature }?.key
            ?: throw IllegalArgumentException("Feature ${feature.qualifiedName} not resolved")

    fun isFeatureEnabled(key: String): Boolean {
        checkOptionalFeatureKey(key)
        return key !in disabledFeatures()
    }

    private fun checkOptionalFeatureKey(key: String) {
        require(key in optionalFeatures.keys) { "Key not bound to optional Feature" }
    }
}