-keepattributes SourceFile,LineNumberTable
-keep class com.liquidbubble.launcher.data.** { *; }
-keep class com.liquidbubble.launcher.RiiLauncherApp { *; }
-keep class com.liquidbubble.launcher.MainActivity { *; }
-keep class com.liquidbubble.launcher.settings.SettingsActivity { *; }
-keep class com.liquidbubble.launcher.system.LiquidNotificationListenerService { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-dontwarn java.awt.**
