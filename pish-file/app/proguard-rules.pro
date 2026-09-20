# قواعد ProGuard/R8 برای پیش‌فایل

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# مدل‌های داده (برای بکاپ/بازیابی JSON با بازتاب)
-keep class ir.pishfile.app.data.local.entity.** { *; }
-keep class ir.pishfile.app.domain.model.** { *; }

# کاتلین
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
