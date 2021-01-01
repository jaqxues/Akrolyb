package com.jaqxues.akrolyb.pack


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 15.04.20 - Time 01:01.
 */
interface IPackMetadata {
    val devPack: Boolean
    val packVersion: String
    val packVersionCode: Int
    val packImplClass: String
    val minApkVersionCode: Int
    val isEncrypted: Boolean
}
