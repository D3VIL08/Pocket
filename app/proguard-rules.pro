# Room generates classes referenced only by reflection at migration time.
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# kotlinx.serialization keeps generated serializers on the companion; R8 needs the hint.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
