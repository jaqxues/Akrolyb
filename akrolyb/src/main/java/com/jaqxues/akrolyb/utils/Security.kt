package com.jaqxues.akrolyb.utils

import android.content.res.AssetManager
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
    fun checkSecurity(jarFile: JarFile, certificate: X509Certificate) {
        val toCheck = Vector<JarEntry>()

        for (entry in jarFile.entries()) {
            // Do not check directories
            if (entry.isDirectory) continue

            toCheck.addElement(entry)

            // [JarFile.getInputStream] throws Security Exception if any of the entries are signed incorrectly
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
                    if (certChain[0] == certificate) {
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

    fun certificateFromAssets(assetManager: AssetManager, name: String): X509Certificate {
        return assetManager.open(name).use {
            return@use CertificateFactory.getInstance("X.509")
                .generateCertificate(it) as X509Certificate
        }
    }
}


/**
 * Iterator through (possibly multiple) certificate chains in the receiver certificate array.
 * (Grouping the chain by checking the current certificate's [X509Certificate.getSubjectX500Principal] and the
 * following certificate's [X509Certificate.getIssuerX500Principal] until both differ).
 */
fun Array<Certificate>.getSingleChains() = sequence {
    val certs = this@getSingleChains
    var startIdx = 0
    while (startIdx < size - 1) {
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