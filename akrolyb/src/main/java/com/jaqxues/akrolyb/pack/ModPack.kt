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

abstract class ModPack<T : IPackMetadata>(private val metadata: T) {
    abstract fun loadFeatureManager(): FeatureManager

    companion object {

        /**
         * Function to instantiate a Pack with all the given information. Performs basic checks with the given
         * PackFactory object, checks security aspects if a certificate is given. It instantiates the actual object by
         * creating a DexClassLoader on the given jar file and tries to load the [IPackMetadata.packImplClass] and invoke
         * its constructor by using reflection.
         *
         * @param context Context that is used to create the classLoader's cache
         * @param packFile JarFile that contains the classes.dex file. It must contain the Pack Implementation class
         * and it must be accessible with a simple DexClassLoader.
         * @param certificate Optional, but recommended Certificate to compare to the signatures of the given pack.
         * Verifies that the pack has not been tampered with, all the files have been signed correctly and that the pack
         * has been built by a trusted user. Leaving this null will kill all the above security mechanisms
         * @param packBuilder A PackFactory used to instantiate the Metadata associated to the Pack and retrieve
         * information needed to build the Pack.
         *
         * @return The [ModPack] object with the implementation provided by the Pack. Allows communication with the Pack
         * with an abstract class that the pack implementation extends.
         *
         * @see [PackFactory]
         */
        @Throws(PackException::class)
        fun <T : IPackMetadata, M : ModPack<T>> buildPack(
            context: Context,
            packFile: File,
            certificate: X509Certificate? = null,
            packBuilder: PackFactory<T>
        ): M {
            if (!packFile.exists()) throw PackNotFoundException(packFile)

            // Open Jar File and extract the Manifest to parse the attributes in order to build PackMetadata objects.
            // Since the jar is opened, it also checks security certificates etc. if a certificate is given.
            val attributes = try {
                JarFile(packFile).use { jarFile ->
                    val manifest = jarFile.manifest ?: throw NullPointerException("No Manifest in specified Jar")

                    if (certificate == null) {
                        Timber.i("Skipping Security Checks on Pack. Strongly advised to provide a valid Certificate to check the Packs Signature")
                    } else {
                        // Check Security to ensure the Pack has not been tampered with, the pack is signed correctly,
                        // and the pack has been signed with the given certificate to ensure a "trusted" and safe state.
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
                if (t is PackException)
                    throw t
                throw PackMetadataException(t)
            }

            val metadata: T
            try {
                // Parsing Attributes to a PackMetadata object
                metadata = packBuilder.buildMeta(attributes, context, packFile)
            } catch (t: Throwable) {
                Timber.e(t)
                throw PackAttributesException(t)
            }

            try {
                // Performing Checks if it safe to load the Pack in the current environment
                packBuilder.performChecks(metadata)
            } catch (t: Throwable) {
                throw PackEnvironmentException("Checks to ensure a safe environment have failed", t)
            }

            // Instantiating the actual ClassLoader
            val classLoader = try {
                with(context.codeCacheDir) {
                    if (!exists() && !mkdirs())
                        throw FileNotFoundException("Optimised CodeCache dir not found and could not be created")
                    DexClassLoader(
                        packFile.absolutePath, absolutePath,
                        null, ModPack::class.java.classLoader
                    )
                }
            } catch (t: Throwable) {
                Timber.e(t)
                throw PackClassLoaderException(t)
            }

            // Reflection Part: Try to load the pack implementation class on the classloader and try to invoke its
            // constructor
            return try {
                @Suppress("UNCHECKED_CAST") val clazz = classLoader.loadClass(metadata.packImplClass) as Class<out M>
                val constructor = clazz.getConstructor(metadata.javaClass)

                constructor.newInstance(metadata) as M
            } catch (t: Throwable) {
                val message = when (t) {
                    is ClassNotFoundException -> "Pack implementation class (${metadata.packImplClass}) not found"
                    is NoSuchMethodException -> "Matching pack implementation constructor not found."
                    is InvocationTargetException -> "Pack implementation constructor Invocation failed"
                    else -> "Unknown Error while instantiating Pack Implementation"
                }

                @Suppress("TimberExceptionLogging")
                Timber.e(t, message)
                throw PackReflectionException(message, t)
            }
        }
    }
}