-keepattributes SourceFile,LineNumberTable
-keep class com.liquidbubble.launcher.data.model.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-dontwarn java.awt.**
