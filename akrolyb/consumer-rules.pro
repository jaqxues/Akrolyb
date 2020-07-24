# Keep the PackImpl Class (instantiated by reflection)
-keep public final class * extends com.jaqxues.akrolyb.pack.ModPackBase
# Keep everything in the PackImpl Class (but allow obfuscation and optimization). These are all the entry points of a
# Pack and hence keeping all the Members and Fields of this class makes it safe to allow minifying the pack.
-keep ,allowobfuscation, allowoptimization public final class * extends com.jaqxues.akrolyb.pack.ModPackBase { *; }


# Starting with BuildTools 'com.android.tools.build:gradle:4.0.0', the Xposed Entry Points are no
# longer automatically kept by Proguard.
-keep, allowoptimization public final class * extends de.robv.android.xposed.IXposedHookZygoteInit
-keep, allowoptimization public final class * extends de.robv.android.xposed.IXposedHookLoadPackage
-keep, allowoptimization public final class * extends de.robv.android.xposed.IXposedHookInitPackageResources