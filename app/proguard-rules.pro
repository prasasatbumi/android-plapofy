# ProGuard Rules for Plapofy Android App
# =======================================
# This file contains rules for code obfuscation and shrinking

# Keep application class
-keep class com.finprov.plapofy.PlapofyApplication { *; }

# ==================== Data Classes (Gson/Retrofit) ====================
# Keep all data classes used for API responses and Room entities
-keep class com.finprov.plapofy.data.remote.dto.** { *; }
-keep class com.finprov.plapofy.data.local.entity.** { *; }
-keep class com.finprov.plapofy.domain.model.** { *; }

# Keep Gson serialized fields
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ==================== Retrofit ====================
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ==================== OkHttp ====================
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ==================== Hilt/Dagger ====================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ==================== Room ====================
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ==================== Firebase ====================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ==================== RootBeer (Root Detection) ====================
-keep class com.scottyab.rootbeer.** { *; }

# ==================== Kotlin Coroutines ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ==================== Compose ====================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ==================== General Android ====================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable implementations
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
