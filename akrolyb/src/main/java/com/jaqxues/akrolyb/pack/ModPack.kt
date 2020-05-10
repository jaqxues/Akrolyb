package com.jaqxues.akrolyb.pack

import android.content.Context
import com.jaqxues.akrolyb.genhook.FeatureManager
import com.jaqxues.akrolyb.utils.Security
import dalvik.system.DexClassLoader
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.InvocationTargetException
import java.security.cert.X509Certificate
import java.util.jar.JarFile


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 14.04.20 - Time 21:53.
 */

abstract class ModPack<T : PackMetadata>(private val metadata: T) {
    abstract fun loadFeatureManager(): FeatureManager

    companion object {
        @Throws(PackException::class)
        @Suppress("UNCHECKED_CAST")
        fun <T : PackMetadata, M: ModPack<T>> buildPack(
            context: Context,
            packFile: File,
            certificate: X509Certificate? = null,
            packBuilder: PackFactory<T>
        ): M {
            Timber.plant(Timber.DebugTree())
            if (!packFile.exists()) throw PackNotFoundException(packFile)

            val attributes = try {
                JarFile(packFile).use { jarFile ->
                    val manifest = jarFile.manifest ?: throw NullPointerException("No Manifest in specified Jar")
                    if (certificate == null) {
                        Timber.i("Skipping Security Checks on Pack. Strongly advised to provide a valid Certificate to check the Packs Signature")
                    } else {
                        try {
                            Security.checkSecurity(jarFile, certificate)
                        } catch (t: Throwable) {
                            throw PackSecurityException("Security Check and Signature Verification Failed", t)
                        }
                    }
                    manifest.mainAttributes
                }
            } catch (t: Throwable) {
                Timber.e(t)
                throw PackMetadataException(t)
            }

            val metadata: T
            try {
                metadata = packBuilder.buildMeta(attributes, context, packFile)
                packBuilder.performChecks(metadata)
            } catch (t: Throwable) {
                Timber.e(t)
                throw PackAttributesException(t)
            }

            val classLoader = try {
                with(context.codeCacheDir) {
                    if (!exists() && !mkdirs())
                        throw FileNotFoundException("Optimised CodeCache dir not found and could not be created")
                    DexClassLoader(
                        packFile.absolutePath, absolutePath,
                        null, ModPack::class.java.classLoader)
                }
            } catch (t: Throwable) {
                Timber.e(t)
                throw PackClassLoaderException(t)
            }

            return try {
                val clazz = classLoader.loadClass(metadata.packImplClass) as Class<out M>
                val constructor = clazz.getConstructor(metadata.javaClass)

                constructor.newInstance(metadata) as M
            } catch (t: Throwable) {
                val message = when (t) {
                    is ClassNotFoundException -> "Pack Implementation Class (${metadata.packImplClass}) not found"
                    is NoSuchMethodException -> "Matching constructor not found."
                    is InvocationTargetException -> "Pack Constructor Invocation failed"
                    else -> "Unknown Error while instantiating Pack"
                }

                Timber.e(t, message)
                throw PackReflectionException(message, t)
            }
        }
    }
}