-dontwarn kotlinx.coroutines.debug.*

-keep class kotlin.** { *; }
-keep class ua.besf0r.** { *; }
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer,java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer,int,java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}
-ignorewarnings

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

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

