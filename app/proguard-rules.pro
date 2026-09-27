# --- Kotlinx Serialization: keep generated serializers for backup DTOs ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class dev.xykal.nabungin.data.backup.** {
    *** Companion;
}
-keepclasseswithmembers class dev.xykal.nabungin.data.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.xykal.nabungin.data.backup.**$$serializer { *; }

# --- Room: entities are accessed via generated code only ---
-keep class dev.xykal.nabungin.data.local.** { *; }

# --- WorkManager workers are instantiated reflectively ---
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Biometric prompt callback ---
-keep class androidx.biometric.** { *; }

# Remove verbose logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
