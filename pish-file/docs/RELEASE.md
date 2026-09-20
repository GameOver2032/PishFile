<div dir="rtl">

# انتشار نسخه (Release)

## نسخه‌ی آزمایشی (Debug) — برای تست روی گوشی خودتان

```bash
./gradlew assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

نسخه‌ی debug با شناسه‌ی `ir.pishfile.app.debug` نصب می‌شود، پس می‌تواند در کنار نسخه‌ی اصلی روی گوشی بماند.

## نسخه‌ی انتشار (Release) — برای ارسال به دیگران

### ۱) ساخت کلید امضا (فقط یک‌بار)

```bash
keytool -genkeypair -v \
  -keystore pishfile-release.jks \
  -alias pishfile \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Pish File, OU=Sales, O=Pish File, L=Tehran, C=IR"
```

> ⚠️ این فایل و رمزهایش را در جای امن نگه دارید و **هرگز** داخل مخزن گیت کامیت نکنید.
> اگر کلید را گم کنید، دیگر نمی‌توانید برای همان برنامه در بازار/گوگل‌پلی به‌روزرسانی منتشر کنید.

### ۲) تعریف رمزها (خارج از مخزن)

در `~/.gradle/gradle.properties`:

```properties
PISHFILE_STORE_FILE=/absolute/path/pishfile-release.jks
PISHFILE_STORE_PASSWORD=***
PISHFILE_KEY_ALIAS=pishfile
PISHFILE_KEY_PASSWORD=***
```

### ۳) فعال‌سازی امضا در `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(project.findProperty("PISHFILE_STORE_FILE") as String)
            storePassword = project.findProperty("PISHFILE_STORE_PASSWORD") as String
            keyAlias = project.findProperty("PISHFILE_KEY_ALIAS") as String
            keyPassword = project.findProperty("PISHFILE_KEY_PASSWORD") as String
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

### ۴) ساخت

```bash
./gradlew clean assembleRelease

# بررسی امضا
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## چک‌لیست قبل از انتشار

- [ ] اجرای تست‌ها: `./gradlew test`
- [ ] افزایش `versionCode` و `versionName` در `app/build.gradle.kts`
- [ ] به‌روزرسانی `CHANGELOG.md`
- [ ] تست روی گوشی واقعی: ثبت پروژه → واحد → مشتری → پیش‌فایل → پرداخت قسط → بکاپ و بازیابی
- [ ] بررسی text و اعداد فارسی در حالت تاریک و روشن
- [ ] مهاجرت دیتابیس: اگر ساختار جدول‌ها تغییر کرده، `Migration` نوشته شده باشد (نه destructive)
- [ ] گرفتن بکاپ از داده‌ی گوشی آزمایشی قبل از نصب نسخه‌ی جدید

## نصب روی گوشی دیگران

فایل APK را می‌توانید از طریق تلگرام/ایمیل/درایو بفرستید. کاربر باید در تنظیمات گوشی، «نصب از منابع نامشخص» (Install unknown apps) را برای همان پیام‌رسان/مرورگر فعال کند.

</div>
