# Kotlin
-keepattributes *Annotation*, InnerClasses, Signature, SourceFile, LineNumberTable, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations
-keepclassmembers class kotlinx.* {
    *;
}

# kotlinx-serialization
-keep class **$$serializer {
    *;
}
-keep class **Serializer {
    *;
}
-keep,allowobfuscation @kotlinx.serialization.Serializable class **
-keep,allowobfuscation class dev.terrakok.cozyspace.Preset {
    <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Java Sound SPI - Vorbis OGG decoder (must stay unobfuscated for ServiceLoader)
-keep class javazoom.spi.** { *; }
-keep class javazoom.spi.vorbis.** { *; }
-keep class com.jcraft.jogg.** { *; }

# Tritonus audio infrastructure
-keep class org.tritonus.** { *; }

# TinySound
-keep class kuusisto.tinysound.** { *; }

# Keep SPI service file references correct when merging JARs
-adaptresourcefilecontents **/*.properties, META-INF/services/**

# Suppress warnings for missing optional classes
-dontwarn org.tritonus.**
-dontwarn org.vorbis.**
-dontwarn net.engie.**

# Duplicate META-INF entries
-adaptclassstrings

# Nucleus platform-specific bridges (native only, not on classpath)
-dontwarn io.github.kdroidfilter.nucleus.**
