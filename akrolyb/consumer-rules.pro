# Keep the PackImpl Class (instantiated by reflection)
-keep public final class * extends com.jaqxues.akrolyb.pack.ModPackBase
# Keep everything in the PackImpl Class (but allow obfuscation and optimization). These are all the entry points of a
# Pack and hence keeping all the Members and Fields of this class makes it safe to allow minifying the pack.
-keep ,allowobfuscation, allowoptimization, public final class * extends com.jaqxues.akrolyb.pack.ModPackBase { *; }
