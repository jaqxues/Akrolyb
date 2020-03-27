package com.jaqxues.akrolyb.utils

import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 17.03.20 - Time 09:48.
 */
sealed class CollectableDec {
    class Class(val value: KClass<*>): CollectableDec()
    class Object(val value: Any): CollectableDec()
}
