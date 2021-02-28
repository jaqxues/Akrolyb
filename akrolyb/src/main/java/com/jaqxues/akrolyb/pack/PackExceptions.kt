package com.jaqxues.akrolyb.pack

import java.io.File


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 15.04.20 - Time 19:05.
 */

sealed class PackException : Exception {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
}

class PackNotFoundException(file: File) :
    PackException("Specified PackFile (${file.absolutePath}) does not exist")

class PackAttributesException(cause: Throwable) :
    PackException("Failed to load PackAttributes", cause)

class PackMetadataException(cause: Throwable) :
    PackException("Failed to extract PackMetadata", cause)

class PackSecurityException(message: String, cause: Throwable) : PackException(message, cause)

class PackClassLoaderException(cause: Throwable) :
    PackException("Failed to instantiate DexClassLoader instance", cause)

class PackReflectionException(message: String, cause: Throwable) : PackException(message, cause)

class PackEnvironmentException(message: String, cause: Throwable) : PackException(message, cause)


class DeveloperPackException : Exception("Developer Pack with non-debuggable Apk")
class CheckMinApkVersionException(val installedVersionCode: Int, val requiredVersionCode: Int) :
    Exception("Pack requires newer Apk")
class FoundPackClassloader :
    Exception("Detected Pack in ClassLoader. You might want to adjust your run configuration to avoid installing the pack as a dynamic feature")