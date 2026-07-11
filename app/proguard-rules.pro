# Core App Protection
-keep class com.eliasgreen18.vocabularytracker.data.local.entity.** { *; }
-keep class com.eliasgreen18.vocabularytracker.domain.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class com.eliasgreen18.vocabularytracker.di.** { *; }
-dontwarn dagger.hilt.**

# ML Kit / CameraX
-keep class com.google.mlkit.** { *; }
-keep class androidx.camera.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn androidx.camera.**

# Google API Client
-keep class com.google.api.** { *; }
-keep class com.google.auth.** { *; }
-dontwarn com.google.api.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Fix for missing classes in Google API / Apache HTTP
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.SslError
-dontwarn com.google.common.flogger.backend.system.DefaultPlatform
