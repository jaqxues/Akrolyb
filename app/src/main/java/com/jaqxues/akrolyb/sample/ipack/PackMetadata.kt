package com.jaqxues.akrolyb.sample.ipack

import android.content.Context
import com.jaqxues.akrolyb.pack.IPackMetadata
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 20.04.20 - Time 12:25.
 */
class PackMetadata(
    override val devPack: Boolean,
    override val packVersion: String,
    override val packVersionCode: Int,
    override val packImplClass: String,
    override val minApkVersionCode: Int,
    override val isEncrypted: Boolean
) : IPackMetadata
{
    companion object {
        fun toPackMetadata(attributes: Attributes, context: Context, file: File): PackMetadata {
            fun safeGetValue(name: String) =
                attributes.getValue(name) ?: throw IllegalStateException("Pack did not include \"$name\" Attribute")

            val devPack = safeGetValue("Development").equals("true", true)
            val packImpl = safeGetValue("PackImpl")
            val packVersionCode = safeGetValue("PackVersionCode").toInt()
            val packVersion = safeGetValue("PackVersion")
            val minApkVersion = safeGetValue("MinApkVersionCode").toInt()
            val isEncrypted = attributes.getValue("Encrypted")?.equals("true", true) ?: false

            return PackMetadata(devPack, packVersion, packVersionCode, packImpl, minApkVersion, isEncrypted)
        }
    }
}
