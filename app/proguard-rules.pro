# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ======== General Android Rules ========

# Preserve line numbers for debugging crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ======== Firebase Rules ========

# Keep Firebase model classes (they use reflection for serialization)
-keep class com.brosh.finance.monthlybudgetsync.objects.** { *; }

# Firebase Database
-keepattributes Signature
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.** {
    *;
}

# Firebase Auth
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ======== Gson (if used by Firebase) ========
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# ======== Google Play Services ========
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# ======== Keep Serializable classes ========
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ======== Prevent stripping of model classes ========
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.User { *; }
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.Budget { *; }
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.Category { *; }
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.Transaction { *; }
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.Month { *; }
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.Share { *; }
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.UserSettings { *; }
-keepclassmembers class com.brosh.finance.monthlybudgetsync.objects.ContactUs { *; }

# ======== Remove logging in release ========
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ======== Optimization Settings ========
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# ======== Debugging (remove for production) ========
# -dontobfuscate
