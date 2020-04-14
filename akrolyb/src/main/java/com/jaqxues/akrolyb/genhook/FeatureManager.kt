package com.jaqxues.akrolyb.genhook

import android.app.Activity
import android.content.Context
import com.jaqxues.akrolyb.utils.CollectableDec
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 17.03.20 - Time 11:05.
 */
class FeatureManager(featureProvider: FeatureProvider) {
    private val forcedFeatures = featureProvider.forcedFeatures
    private val optionalFeatures = featureProvider.optionalFeatures
    private val featureNames = forcedFeatures + optionalFeatures
    private val reversedFeatureMap = featureNames.map { (k, v) -> v.java.canonicalName!! to k }.toMap()
    private val disabledFeatures = featureProvider.disabledFeatures


    fun getActiveFeatures(): List<Feature> {
        val disabled = disabledFeatures.list
        val activeOptionals = optionalFeatures.mapNotNull { (name, clazz) ->
            if (disabled.contains(name)) null else clazz
        }

        return if (activeOptionals.isEmpty())
            emptyList()
        else
            (activeOptionals + forcedFeatures.values).map { it.java.newInstance() }
    }

    fun loadAll(classLoader: ClassLoader, context: Context, vararg collectables: CollectableDec) {
        FeatureHelper.loadAll(classLoader, context, this, *collectables)
    }

    fun lateInitAll(classLoader: ClassLoader, activity: Activity) {
        FeatureHelper.lateInitAll(classLoader, activity)
    }

    fun unhookByFeature(feature: KClass<out Feature>) = FeatureHelper.unhookByFeature(feature)

    fun resolveName(featureName: String): KClass<out Feature> =
            featureNames[featureName] ?: throw IllegalArgumentException("Feature $featureName not resolved")

    fun getFeatureName(canonicalName: String) =
            reversedFeatureMap[canonicalName] ?: throw IllegalArgumentException("Feature $canonicalName not resolved")

    fun isFeatureEnabled(key: String): Boolean {
        checkOptionalFeatureKey(key)
        return key !in disabledFeatures.list
    }

    fun toggleFeature(featureClass: KClass<out Feature>, active: Boolean) {
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