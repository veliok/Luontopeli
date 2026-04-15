# proguard-rules.pro

# Room – säilytä entiteetit ja DAO:t
-keep class com.example.luontopeli.data.local.** { *; }

# Firebase – ei minifioida Firebase SDK:ta
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ML Kit – säilytä merkinnät
-keep class com.google.mlkit.** { *; }

# Retrofit / Gson (jos käytössä)
-keepattributes Signature
-keepattributes *Annotation*

# Health Connect SDK:n suojaaminen
-keep class androidx.health.connect.client.** { *; }
-keep interface androidx.health.connect.client.** { *; }

# Estä Record-luokkien nimenmuutokset
-keep class androidx.health.connect.client.records.** { *; }
-keep class androidx.health.connect.client.permission.** { *; }
-keep interface androidx.health.connect.client.permission.** { *; }

# Suojaa Health Connectin sisäiset metadata- ja Serialization-luokat
-keepclassmembers class ** extends androidx.health.connect.client.records.Record {
    <fields>;
    <init>(...);
}