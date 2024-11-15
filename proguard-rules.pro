-dontwarn kotlinx.coroutines.debug.*

-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

-keep class ua.besf0r.** { *; }
-keep class io.ktor.** { *; }

-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer,java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer,int,java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}
-ignorewarnings

-dontwarn android.os.**
-dontwarn android.view.**
-dontwarn org.slf4j.impl.**
-dontwarn androidx.compose.runtime.**
-dontwarn io.ktor.**
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-dontwarn org.jetbrains.skiko.**
-dontwarn io.kamel.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.conscrypt.**
-dontwarn org.osgi.framework.**
-dontskipnonpubliclibraryclasses

