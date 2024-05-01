-keepclasseswithmembers public class ua.besf0r.cubauncher.MainKt {
    public static void main(java.lang.String[]);
}

-dontwarn kotlinx.coroutines.debug.*

-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

-keep class io.kamel.** {  *;}
-keep class org.to2mbn.** {  *;}

-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer,java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer,int,java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}
-ignorewarnings

-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-dontwarn org.jetbrains.skiko.**
-dontwarn io.kamel.**
-dontwarn org.to2mbn.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.conscrypt.**
-dontwarn org.osgi.framework.**
-dontskipnonpubliclibraryclasses