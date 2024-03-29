# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

# Get rid of package names, makes file smaller
-repackageclasses

# Required for classes created and used from JNI code (on C/C++ side)
#-keep, includedescriptorclasses class in.uncod.android.bypass.Document { *; }
#-keep, includedescriptorclasses class in.uncod.android.bypass.Element { *; }

# A resource is loaded with a relative path so the package of this class must be preserved.
#-keepnames class org.jsoup.nodes.Entities
