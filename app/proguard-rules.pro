# Phigros Tracker ProGuard Rules

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes
-keep,includedescriptorclasses class org.kasumi321.ushio.phitracker.**$$serializer { *; }
-keepclassmembers class org.kasumi321.ushio.phitracker.** {
    *** Companion;
}
-keepclasseswithmembers class org.kasumi321.ushio.phitracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Ktor
-keep class io.ktor.** { *; }
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Google Tink / ErrorProne (used by EncryptedSharedPreferences)
-dontwarn com.google.errorprone.annotations.**
