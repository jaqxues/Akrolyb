package com.jaqxues.akrolyb.pack

import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 15.04.20 - Time 01:01.
 */
interface PackMetadata {
    val devPack: Boolean
    val packVersion: String
    val packVersionCode: Int
    val packImplClass: String
    val minApkVersionCode: Int
}
