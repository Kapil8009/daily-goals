# Room and WorkManager ship consumer rules. Keep database constructors used by Room.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
