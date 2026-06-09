# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# WorkManager core + impl
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }

# Keep WorkManager's Room database
-keep class androidx.work.impl.WorkDatabase { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }

# Keep Room internals
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }

# Keep AndroidX Startup initializer
-keep class androidx.work.WorkManagerInitializer { *; }
