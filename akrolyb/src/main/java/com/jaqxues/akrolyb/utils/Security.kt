package com.jaqxues.akrolyb.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Build
import java.io.ByteArrayInputStream
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 10.05.20 - Time 18:39.
 */
object Security {
    fun checkSecurity(jarFile: JarFile, certificate: X509Certificate) = checkSecurity(jarFile, setOf(certificate))

    fun checkSecurity(jarFile: JarFile, certificates: Set<X509Certificate>) {
        val toCheck = Vector<JarEntry>()

        for (entry in jarFile.entries()) {
            // Do not check directories
            if (entry.isDirectory) continue

            toCheck.addElement(entry)

            // [JarFile.getInputStream] throws Security Exception if any of the entries are signed incorrectly and
            // [JarEntry.getCertificates] only returns the certificates once the InputStream has been read through until
            // the end of the stream.
            jarFile.getInputStream(entry).use {
                @Suppress("ControlFlowWithEmptyBody")
                while (it.read() != -1) {
                }
            }
        }

        // All the entries are signed correctly. Going through every entry in [toCheck] to verify the actual Signature
        // Every file must be signed except files in META-INF
        for (entry in toCheck) {
            val certs = entry.certificates

            if (certs.isNullOrEmpty()) {
                if (!entry.name.startsWith("META-INF"))
                    throw SecurityException("JarEntry is an unsigned file")
            } else {
                // Checking the actual certificates to check for corresponding certificate from the provided [X509Certificate]
                // The jar may be signed by multiple signers: see if one of them is the target certificate.

                if (entry.name != "classes.dex") continue

                var signedAsExpected = false

                for (certChain in certs.getSingleChains()) {
                    if (certChain[0] in certificates) {
                        // Trusted Signer has been found.
                        signedAsExpected = true
                        // Add Certification to Module Pack
                        break
                    }
                }
                if (!signedAsExpected) throw SecurityException("Classes.dex not signed by provided Certificate")
            }
        }
    }

    /**
     * Loads in a certificate file from the AssetManager
     */
    fun certificateFromAssets(assetManager: AssetManager, name: String): X509Certificate {
        return assetManager.open(name).use {
            return@use CertificateFactory.getInstance("X.509")
                .generateCertificate(it) as X509Certificate
        }
    }

    /**
     * Gets the X.509 Certificate used to sign this application.
     */
    fun certificateFromApk(context: Context, packageName: String) =
        CertificateFactory.getInstance("X.509")
            .generateCertificate(
                ByteArrayInputStream(
                    context.packageManager.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            it.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                                .signingInfo
                                .apkContentsSigners
                        } else {
                            @Suppress("DEPRECATION")
                            it.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                                .signatures
                        }
                    }[0].toByteArray()
                )
            ) as X509Certificate
}


/**
 * Iterator through (possibly multiple) certificate chains in the receiver certificate array.
 * (Grouping the chain by checking the current certificate's [X509Certificate.getSubjectX500Principal] and the
 * following certificate's [X509Certificate.getIssuerX500Principal] until both differ).
 * If only one certificate was found, return the single certificate
 */
fun Array<Certificate>.getSingleChains() = sequence {
    val certs = this@getSingleChains
    var startIdx = 0
    while (startIdx < size) {

        // Only one certificate left, cannot check for following certificate
        if (startIdx == size - 1) {
            yield(arrayOf(this@getSingleChains[startIdx]))
            startIdx += 1
            break
        }

        var i = startIdx
        // Loop through the chain until the next certificate is not the issuer of the current certificate.
        while (i < size - 1) {
            if ((certs[i + 1] as X509Certificate).subjectX500Principal != (certs[i] as X509Certificate).issuerX500Principal)
                break
            i++
        }
        yield(sliceArray(startIdx..i))
        startIdx += i
    }
}