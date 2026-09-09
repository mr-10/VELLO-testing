# Supabase and Ktor ProGuard rules
-keep class io.github.jan.supabase.** { *; }
-keep interface io.github.jan.supabase.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
}

# Keep Vello Data Models
-keep class com.mr10.vello.data.model.** { *; }
-keep class com.mr10.vello.ui.navigation.NavKey** { *; }

# Ktor
-keep class io.ktor.** { *; }
-dontwarn java.lang.management.**

# OkHttp (if used)
-keepattributes Signature
-keepattributes AnnotationDefault
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
