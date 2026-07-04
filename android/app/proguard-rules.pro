# Supabase / Ktor / kotlinx.serialization use reflection for serializers
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class kotlinx.serialization.**$$serializer { *; }
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class **.*$Companion { *; }

# Ktor
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# Hilt-generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
