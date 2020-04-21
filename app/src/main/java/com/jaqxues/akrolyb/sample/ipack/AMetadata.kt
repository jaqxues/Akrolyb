package com.jaqxues.akrolyb.sample.ipack

import android.content.Context
import com.jaqxues.akrolyb.pack.PackMetadata
import org.w3c.dom.Attr
import java.io.File
import java.util.jar.Attributes


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 20.04.20 - Time 12:25.
 */
class AMetadata(
    override val devPack: Boolean,
    override val packVersion: String,
    override val packVersionCode: Int,
    override val packImplClass: String,
    override val minApkVersionCode: Int
) : PackMetadata
{
    companion object {
        fun toPackMetadata(attributes: Attributes, context: Context, file: File): AMetadata {
            fun safeGetValue(name: String) =
                attributes.getValue(name) ?: throw IllegalStateException("Pack did not include \"$name\" Attribute")

            val devPack = safeGetValue("Development").equals("true", false)
            val packImpl = safeGetValue("PackImpl")
            val packVersionCode = safeGetValue("PackVersionCode").toInt()
            val packVersion = safeGetValue("PackVersion")
            val minApkVersion = safeGetValue("MinApkVersionCode").toInt()

            return AMetadata(devPack, packVersion, packVersionCode, packImpl, minApkVersion)
        }
    }
}
