<div dir="rtl">

# معماری پیش‌فایل

## نگاه کلی

معماری **آفلاین‌اول** (Offline-first) با ساختار لایه‌ای ساده و بدون کتابخانه‌های سنگین:

```
┌─────────────────────────────────────────────┐
│  UI (Jetpack Compose, RTL, Vazirmatn)       │
│  screens/ ──► viewmodel/                    │
└────────────────────┬────────────────────────┘
                     │ StateFlow / Flow
┌────────────────────▼────────────────────────┐
│  Repository (منطق کاری)                     │
│  PreFileRepository · ProjectRepository · …  │
└────────────────────┬────────────────────────┘
                     │ suspend / Flow
┌────────────────────▼────────────────────────┐
│  Room (SQLite) — ۶ جدول                     │
│  projects · units · customers ·             │
│  pre_files · installments · follow_ups      │
└────────────────────┬────────────────────────┘
                     │ (فاز ۵)
┌────────────────────▼────────────────────────┐
│  Sync (قرارداد آماده)                       │
│  SyncEngine ◄── DemoSyncEngine (غیرفعال)    │
└─────────────────────────────────────────────┘
```

## چرا این انتخاب‌ها؟

| تصمیم | دلیل |
| --- | --- |
| **Kotlin + Jetpack Compose** | تنها زبان و فریم‌ورک رسمی و آینده‌دار اندروید؛ پشتیبانی کامل راست‌چین و فونت فارسی |
| **Room به‌جای فایل/SharedPreferences** | کوئری‌های تجمیعی (جمع مطالبات، سررسیدها) با SQL ساده‌تر و سریع‌تر است |
| **آفلاین‌اول** | دفاتر املاک در محل پروژه یا دفتر همیشه اینترنت پایدار ندارند؛ داده باید همیشه در دسترس باشد |
| **DI دستی** | برای شروع، بدون annotation processing و وابستگی اضافه؛ انتقال به Hilt در آینده ساده است |
| **حذف نرم (Soft delete)** | هیچ داده‌ای واقعاً پاک نمی‌شود تا برای همگام‌سازی آینده قابل ارسال باشد |

## لایه‌ها

### ۱) لایه‌ی داده (`data/local`)
- `PishFileDatabase` — دیتابیس Room با نسخه‌بندی و داده‌ی نمونه‌ی اولیه (فقط بار اول).
- ۶ موجودیت با فیلدهای تخصصی املاک + سه فیلد همگام‌سازی در همه‌ی جدول‌ها:
  `remoteId`, `syncState`, `serverUpdatedAt`.
- DAOها هم عملیات CRUD دارند و هم کوئری‌های تجمیعی مخصوص داشبورد
  (مثلاً `observeFinanceSummary()` و `observeOverdueAmount()`).

### ۲) لایه‌ی Repository (`data/repository`)
منطق کاری این‌جاست، نه در UI:

- **پیش‌فرض‌های پروژه (نسخه ۰.۳.۰):** `ProjectEntity` همه‌ی فیلدهای مشترک ثبت فایل را نگه می‌دارد
  (امتیاز پیش‌فرض، رتبه، شرایط فروش، اقساط). `PreFileEditViewModel.applyProjectToForm()` این مقادیر را
  روی فرم می‌نهد و `PreFileWizardViewModel` ثبت فایل را قدم‌به‌قدم انجام می‌دهد: فقط فیلدهای خالی پرسیده
  می‌شوند و «قیمت کل» (= واریزی + امتیاز) همیشه به‌عنوان یک فیلد مشخص به‌روز می‌ماند.
- `PreFileRepository` — مهم‌ترین کلاس:
  - `nextDraftNumber()` → شماره‌ی خودکار `PF-1405-0007`
  - `generateInstallments()` → ساخت جدول اقساط از شرایط توافق (شامل پیش‌پرداخت به‌عنوان قسط صفر)
  - `markInstallmentPaid()` → ثبت پرداخت و به‌روزرسانی خودکار «دریافتی/مانده» قرارداد
  - `save()` → همگام‌سازی خودکار وضعیت واحد (آزاد → رزرو → پیش‌فروش → تحویل)
  - `markOverdueInstallments()` → شناسایی اقساط سررسیدگذشته
- `UnitRepository.createBatch()` → ساخت گروهی واحدهای یک بلوک.
- `BackupManager` (در `core/`) → بکاپ/بازیابی JSON و خروجی CSV.

### ۳) لایه‌ی UI (`ui`)
- **فونت و راست‌چینی:** `PishFileTheme` مستقل از زبان دستگاه، کل رابط را RTL می‌کند؛ فونت وزیرمتن با چهار وزن.
- **کامپوننت‌های فارسی:** `JalaliDateField` (تاریخ شمسی با میان‌بر)، `MoneyField` (جداکننده هزارگان + معادل میلیون)، `StatusChip`، `PaymentProgress` و…
- **ViewModelها:** هر صفحه یک ViewModel با `StateFlow`؛ فرم‌ها به‌صورت `data class` نگه‌داری و در یک نقطه به موجودیت تبدیل می‌شوند (`ProjectForm.toEntity()` و مشابه‌ها) — همین باعث می‌شود فرم‌ها تست‌پذیر باشند.

## تاریخ شمسی

هیچ کتابخانه‌ی خارجی استفاده نشده. تبدیل در `core/Formatters.kt` انجام می‌شود:

- `gregorianToJalali` / `jalaliToGregorian` — الگوریتم چرخه‌ی ۳۳ ساله
- `addJalaliMonths` / `addJalaliDays` — برای ساخت سررسید اقساط
- `epochToJalali` / `jalaliStringToEpoch` — مرتب‌سازی و یادآوری

تاریخ‌ها به شکل **`YYYY/MM/DD` با رقم لاتین و صفر ابتدایی** ذخیره می‌شوند (مثل `1405/06/29`) تا مقایسه و مرتب‌سازی رشته‌ای درست کار کند، و فقط در نمایش به رقم فارسی تبدیل می‌شوند.

## آماده‌سازی برای سرور (فاز ۵)

همه‌ی رکوردها سه میدان همگام‌سازی دارند و DAOها متدهای زیر را ارائه می‌دهند:

```kotlin
suspend fun getPendingSync(): List<T>
suspend fun getChangedSince(since: Long): List<T>
suspend fun updateSyncState(id: String, state: String, remoteId: String?)
suspend fun softDelete(id: String, timestamp: Long)
```

برای اتصال سرور:

1. یک کلاس `HttpSyncEngine : SyncEngine` بسازید (Retrofit/Ktor + احراز هویت).
2. در `DefaultAppContainer` آن را جای `DemoSyncEngine` بگذارید.
3. آدرس سرور را از `SettingsRepository.serverUrl` بخوانید.
4. در `SyncRepository.markPushedIfSynced()` وضعیت رکوردهای ارسال‌شده را `CLEAN` کنید.

هیچ صفحه یا ViewModelی نیاز به تغییر ندارد.

## تصمیم‌های آگاهانه (و بدهی فنی)

- `fallbackToDestructiveMigrationOnDowngrade()` فقط برای راحتی توسعه است؛ **قبل از انتشار نسخه‌ی نهایی** باید مهاجرت‌های واقعی نوشته شود.
- `PreFileRepository.refreshPreFilePaidAmount()` با جمع ساده‌ی اقساط کار می‌کند؛ اگر پرداخت‌های خارج از قسط اضافه شود، باید با پرداخت‌های مستقیم (`addPayment`) ترکیب شود.
- بارگذاری تصاویر اسناد (فاز ۴) هنوز پیاده نشده؛ فیلد `documentPaths` رزرو شده است.

</div>
