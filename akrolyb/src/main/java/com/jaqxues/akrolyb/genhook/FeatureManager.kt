package com.jaqxues.akrolyb.genhook

import android.app.Activity
import android.content.Context
import com.jaqxues.akrolyb.genhook.states.StateManager
import com.jaqxues.akrolyb.utils.CollectableDec
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
    private val reversedFeatureMap = featureNames.map { (k, v) -> v.java.canonicalName!! to k }.toMap()
    private val disabledFeatures = featureProvider.disabledFeatures

    val stateManager = StateManager()

    /**
     * Filters the features from the [FeatureProvider] to check which optional features are enabled. In case no optional
     * feature is active, forced features will be ignored too by default.
     *
     * @param alwaysReturnForced Enabling this will always return the Forced Features, even if there are no optional
     * features currently enabled
     * @return The features defined in the [FeatureProvider].
     */
    fun getActiveFeatures(alwaysReturnForced: Boolean = false): List<T> {
        val disabled = disabledFeatures.list
        val activeOptionals = optionalFeatures.mapNotNull { (name, clazz) ->
            if (disabled.contains(name)) null else clazz
        }

        val list = if (activeOptionals.isEmpty()) {
            if (alwaysReturnForced)
                forcedFeatures.values
            else emptyList()
        } else
            activeOptionals + forcedFeatures.values
        return list.map { it.java.newInstance() }
    }

    /**
     * Requests that all loaded features inject their hooks
     */
    fun loadAll(classLoader: ClassLoader, context: Context, vararg collectables: CollectableDec) {
        FeatureHelper.loadAll(classLoader, context, this, stateManager, *collectables)
    }

    /**
     * Allows features an entry point that gives the target application time to initialize things like preferences or
     * other values. Usually called from a hooked [Activity.onCreate].
     */
    fun lateInitAll(classLoader: ClassLoader, activity: Activity) {
        FeatureHelper.lateInitAll(classLoader, activity, stateManager)
    }

    fun unhookByFeature(feature: KClass<out T>) = FeatureHelper.unhookByFeature(feature)

    fun resolveName(featureName: String): KClass<out T> =
        featureNames[featureName] ?: throw IllegalArgumentException("Feature $featureName not resolved")

    fun getFeatureName(canonicalName: String) =
        reversedFeatureMap[canonicalName] ?: throw IllegalArgumentException("Feature $canonicalName not resolved")

    fun isFeatureEnabled(key: String): Boolean {
        checkOptionalFeatureKey(key)
        return key !in disabledFeatures.list
    }

    fun toggleFeature(featureClass: KClass<out T>, active: Boolean) {
        val key = optionalFeatures.entries.find { (_, kClass) ->
            kClass == featureClass
        }?.key ?: throw IllegalStateException("Feature not registered")

        togglePref(key, active)
    }

    fun toggleFeature(key: String, active: Boolean) {
        checkOptionalFeatureKey(key)
        togglePref(key, active)
    }

    private fun togglePref(key: String, active: Boolean) {
        if (active)
            disabledFeatures.enable(key)
        else {
            disabledFeatures.disable(key)
        }
    }

    private fun checkOptionalFeatureKey(key: String) {
        require(key in optionalFeatures.keys) { "Key not bound to optional Feature" }
    }
}