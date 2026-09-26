<div dir="rtl">

# مدل داده — فیلدهای تخصصی پیش‌فروش املاک

دیتابیس: SQLite با Room — نام فایل: `pishfile.db` (**نسخه ۵**).

> **مهاجرت‌ها:**
> - **۲ → ۳** (`MIGRATION_2_3`): افزودن «پیش‌فرض‌های ثبت فایل» به `projects` با `ALTER TABLE … ADD COLUMN`
> - **۳ → ** (`MIGRATION_3_4`): ایجاد جدول‌های `customers` و `notes` + افزودن ستون `customerId` به `follow_ups`
> - **۴ → ۵** (`MIGRATION_4_5`): ایجاد جدول‌های `project_areas` (متراژهای پروژه) و `attachments` (پیوست فایل/عکس/ویدیو) + ستون `approx_total_price` روی `projects` + ستون‌های `fileType` و `areaId` روی `pre_files`
>
> همه‌ی مهاجرت‌ها **واقعی (غیرمخرب)** هستند؛ یعنی **داده‌های کاربر هنگام به‌روزرسانی حفظ می‌شود** و نیازی به بکاپ و نصب مجدد نیست.

همه‌ی تاریخ‌ها **شمسی** و با قالب `YYYY/MM/DD` و رقم لاتین ذخیره می‌شوند (مثال: `1405/06/29`). مبالغ به **تومان** هستند.

## فهرست جدول‌ها

| جدول | کلاس | کاربرد |
| --- | --- | --- |
| `projects` | `ProjectEntity` | پروژه‌ی ساختمانی (با پیش‌فرض‌های ثبت فایل) |
| `units` | `UnitEntity` | واحد (آپارتمان/مغازه/اداری/پارکینگ/انباری/ویلا/زمین) |
| `pre_files` | `PreFileEntity` | فایل پیش‌فروش (قرارداد پیش‌فروش یک واحد) |
| `customers` | `CustomerEntity` | مشتری / طرف‌مذاکره (خریدار/فروشنده) — ۰.۴.۰ |
| `notes` | `NoteEntity` | نوت/مکالمه‌ی تاریخ‌دار — ۰.۴.۰ |
| `project_areas` | `ProjectAreaEntity` | متراژهای پروژه با شرایط مالی مخصوص هرکدام — ۰.۵.۰ |
| `attachments` | `AttachmentEntity` | پیوست (فایل/عکس/ویدیو) متصل به فایل یا واحد — ۰.۵.۰ |
| `follow_ups` | `FollowUpEntity` | پیگیری‌ها (با آلارم) |

## رابطه‌ها

```
projects ──▲── units ──▲── pre_files ──▲── notes
                  └───┴───────────────┴──────────┘
customers ──▲ (preFileId/unitId، SET NULL)   │ (preFileId, CASCADE)
            └── follow_ups (customerId)      └── follow_ups (preFileId, CASCADE)
```

- حذف پروژه ⇒ حذف واحدها و فایل‌ها (CASCADE)
- حذف واحد ⇒ فایل باقی می‌ماند (`unitId` تهی می‌شود) چون **اسنپ‌شات** واحد در فایل ذخیره شده است
- حذف فایل ⇒ نوت‌ها و پیگیری‌های آن حذف می‌شوند (CASCADE) ولی **مشتری‌ها می‌مانند** (`preFileId` تهی می‌شود)
- حذف مشتری ⇒ نوت‌هایش حذف می‌شوند (CASCADE)؛ پیوندش روی فایل/واحد قطع می‌شود

## جدول `projects` (پروژه)

| گروه | فیلدها |
| --- | --- |
| شناسه | `id`, `name`, `code`, `projectType`, `pricingModel` |
| موقعیت | `province`, `city`, `district`, `address`, `postalCode`, `latitude`, `longitude` |
| فنی | `landArea`, `totalBuiltArea`, `blockCount`, `floorCount`, `unitCount`, `unitsPerFloor`, `parkingCount`, `elevatorCount`, `structureType`, `heatingSystem` |
| مجوز | `permitNumber`, `permitIssueDate`, `permitExpiryDate`, `landDeedNumber` |
| مالی | `costPerMeter`, `salePricePerMeter`, `defaultDepositAmount`, `shareMeterArea`, `sharePrice`, `totalBudget` |
| **پیش‌فرض‌های ثبت فایل** (۰.۳.۰) | `defaultBonusAmount`, `hasRanking`, `defaultRanking`, `saleConditionCash/Installment/Exchange`, `saleConditionNotes`, `installmentCount`, `remainingInstallmentsCount`, `installmentAmount`, `installmentPeriod`, `nextInstallmentDueDate` |
| پیشرفت | `phase`, `progressPercent`, `startDate`, `deliveryDate`, `deliveryFrom` |
| افراد/مالی | `contractorName/Phone`, `supervisorName/Phone`, `developerName`, `salesManagerPhone`, `iban`, `bankName` |
| سایر | `facilities`, `description`, `tags`, `isFavorite`, `isArchived` |

`pricingModel`: `METER` (متری) / `DEPOSIT_BONUS` (واریزی و امتیاز) / `SHARE` (سهامی).
`phase`: `PLANNING`، `EXCAVATION`، `STRUCTURE`، `FINISHING`، `DELIVERED`.

> **پیش‌فرض‌های ثبت فایل:** این فیلدها همه‌ی مقادیری هستند که در واحدهای یک پروژه با هم یکسان‌اند
> (واریزی تا امروز، امتیاز، رتبه، شرایط فروش، اقساط). یک‌بار روی پروژه ثبت می‌شوند و در
> «ثبت سریع فایل» خودکار پر می‌شوند. **قیمت کل** هر فایل همیشه `واریزی + امتیاز`
> (یا معادل آن در مدل‌های متری/سهامی) است و با همین عنوان در همه‌ی نماها نمایش داده می‌شود.

## جدول `units` (واحد)

| گروه | فیلدها |
| --- | --- |
| شناسه | `projectId`, `block`, `unitNumber`, `floor`, `unitType`, `bedrooms`, `bathrooms`, `kitchens` |
| متراژ | `grossArea`, `netArea`, `balconyArea`, `commonAreaShare`, `ceilingHeight` |
| نور و موقعیت | `direction`, `lighting`, `view`, `positionType` |
| ضمیمه‌ها | `facilities`, `parkingCount/Number`, `storageCount/Number` |
| مالی | `pricePerMeter`, `totalPrice`, `finalPrice`, `extraCosts`, `discount`, `vatAmount`, `prepaymentSuggestion`, `suggestedInstallment(Count)`, `costPrice` |
| وضعیت | `status`, `deliveryDate`, `deliveryStatus`, `technicalNotes`, `description` |
| فایل‌ها | `floorPlanPath`, `photoPaths` |

`status`: `AVAILABLE` (آزاد) / `RESERVED` (رزرو/پیش‌فروشی) / `SOLD` / `DELIVERED`.
`displayTitle` (محاسباتی): «بلوک A — واحد 502 — طبقه ۵».

## جدول `pre_files` (فایل پیش‌فروش)

| گروه | فیلدها |
| --- | --- |
| شناسه | `draftNumber` (`PF-1405-0001`), `draftDate` |
| اتصال | `projectId` (CASCADE), `unitId` (SET NULL) |
| مالک | `ownerName`, `ownerPhone` |
| قیمت | `pricingModel`, `depositAmount`, `bonusAmount`, `pricePerMeter`, `meterArea`, `shareMeterArea`, `shareCount`, `sharePrice`, `totalPrice` (نهایی) |
| رتبه | `hasRanking`, `ranking` |
| شرایط فروش | `saleConditionCash/Installment/Exchange`, `saleConditionNotes` |
| اقساط | `installmentCount`, `remainingInstallmentsCount`, `installmentAmount`, `installmentPeriod`, `nextInstallmentDueDate` |
| اسنپ‌شات واحد | `unitBlock`, `unitNumber`, `unitFloor` |
| وضعیت | `status`, `deliveryDate`, `notes`, `isFavorite` |

`status`: `DRAFT` (پیش‌نویس) / `URGENT` (فروش فوری) / `NORMAL` (غیر فوری) / `WITHDRAWN` (منصرف).

مقادیر محاسباتی (در کلاس، ذخیره نمی‌شوند):
- `computedTotal`: مجموع بر اساس مدل (واریزی + امتیاز / سهام × قیمت / متر × متری)
- `displayPrice`: `totalPrice` اگر پر شده باشد، وگرنه `computedTotal` — این همان **«قیمت کل»** است
- `saleConditionsSummary`: «نقد، شرایطی، تهاتر»

## جدول `customers` (مشتری) — ۰.۴.۰

| فیلدها | توضیح |
| --- | --- |
| `name` * | نام و نام خانوادگی |
| `phone` | شماره تماس (برای دکمه‌ی تماس) |
| `role` | `BUYER` (خریدار) / `SELLER` (فروشنده) / `OTHER` |
| `preFileId` | فایل مرتبط (SET NULL — حذف فایل، مشتری را نمی‌رود) |
| `unitId` | واحد مرتبط (SET NULL) |
| `notes` | یادداشت کلی |
| `status` | `ACTIVE` (در مذاکره) / `DONE` (نهایی شد) / `LOST` (رد شد) |

## جدول `notes` (نوت/مکالمه) — ۰.۴.۰

| فیلدها | توضیح |
| --- | --- |
| `preFileId` | فایل مرتبط (CASCADE) |
| `customerId` | مشتری مرتبط (CASCADE) |
| `type` | `CALL` (تلفنی) / `VISIT` (حضوری) / `MESSAGE` (پیامکی) / `OTHER` |
| `text` * | شرح مکالمه / پیگیری |
| `outcome` | نتیجه مکالمه |
| `noteDate` | تاریخ شمسی (پیش‌فرض امروز) |

نوت می‌تواند فقط به فایل، فقط به مشتری، به هر دو، یا بدون پیوند باشد.
تایم‌لاین‌ها روی صفحه‌ی فایل/مشتری و مرور کلی در تب پیگیری‌ها (حالت «مکالمات») نمایش داده می‌شوند.

## جدول `follow_ups` (پیگیری)

| فیلدها | توضیح |
| --- | --- |
| `type` | `CALL` / `VISIT` / `MEETING` / `OTHER` |
| `priority` | `LOW` / `NORMAL` / `HIGH` |
| `title` * | موضوع |
| `description` | شرح |
| `outcome` / `result` | نتیجه (پس از انجام) |
| `preFileId` | فایل مرتبط (CASCADE) |
| `projectId` | پروژه (اطلاعاتی) |
| `customerId` | **مشتری مرتبط — ۰.۴.۰** |
| `dueDate` / `dueTime` | سررسید شمسی + ساعت → **آلارم** |
| `status` | `PENDING` / `DONE` / `CANCELED` |
| `completedDate` | تاریخ انجام |
| `assignee` | مسئول |
| `contactPhone` | شماره تماس |
| `remindDaysBefore` | رزرو برای آینده |

### آلارم یادآوری (۰.۴.۰)

`ReminderScheduler` برای هر پیگیری `PENDING` که `dueDate` معتبر داشته باشد، یک آلارم
**دقیق یک‌بار** (`AlarmManager.setExactAndAllowWhileIdle`) روی تاریخ/ساعت شمسی سررسید ثبت می‌کند؛
`ReminderReceiver` در آن لحظه اعلان نشان می‌دهد (کانال «یادآوری پیگیری‌ها»).
اگر ساعتی نده شده باشد، ساعت ۰۹:۰۰ فرض می‌شود.
با `markDone` / `delete` / `save(غير-PENDING)` آلارم لغو می‌شود.
در اندروید ۱۲+ اگر مجوز آلارم دقیق داده نشده باشد، آلارم تقریبی (`set`) تنظیم می‌شود؛
در اندروید ۱۳+ مجوز `POST_NOTIFICATIONS` لازم است (هشدار + دکمه‌ی فعال‌سازی در UI).

## فیلدهای همگام‌سازی (همه‌ی جدول‌ها)

`remoteId`, `syncState`, `serverUpdatedAt`, `deletedAt` — رزرو برای فاز همگام‌سازی.
حذف در همه‌ی جدول‌ها **نرم** است (`deletedAt`) و از جداول وابسته با CASCADE واقعی حذف می‌شود.

## فیلدهایی که ممکن است بخواهید اضافه کنید

- **تاریخ آخرین تماس با مشتری** → روی `customers` یک `lastContactDate`
- **منبع مشتری** (اطلاعاتی/اینستاگرام/آغاز دهان) → `customers.source`
- **تسویه‌ی حساب** → جدول `payments` با `preFileId`
- اضافه‌کردن هر فیلد: موجودیت → `ALTER TABLE` در مهاجرت جدید → فرم → نمایش → بکاپ JSON.

</div>
