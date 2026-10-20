# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preserve Retrofit and Gson models and annotations
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn javax.annotation.**
-keep class kr.ac.waltdev29.hakplace.api.models.** { *; }
-keep interface kr.ac.waltdev29.hakplace.api.ApiService { *; }

# AndroidX Security Crypto
-keepclassmembers class * extends androidx.security.crypto.MasterKey { *; }

# WorkManager
-keep class kr.ac.waltdev29.hakplace.worker.** { *; }
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}