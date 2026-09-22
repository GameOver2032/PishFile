<div dir="rtl">

# مدل داده — فیلدهای تخصصی پیش‌فروش املاک

دیتابیس: SQLite با Room — نام فایل: `pishfile.db` (نسخه ۳).

> **مهاجرت ۲ به ۳:** جدول `projects` با `ALTER TABLE … ADD COLUMN` به‌روزرسانی شده است (مهاجرت واقعی در `PishFileDatabase.MIGRATION_2_3`)؛ یعنی **داده‌های کاربر هنگام به‌روزرسانی حفظ می‌شود** و نیازی به بکاپ و نصب مجدد نیست.
همه‌ی تاریخ‌ها **شمسی** و با قالب `YYYY/MM/DD` و رقم لاتین ذخیره می‌شوند (مثال: `1405/06/29`) و به تومان هستند.

## فهرست جدول‌ها

| جدول | کلاس | کاربرد |
| --- | --- | --- |
| `projects` | `ProjectEntity` | پروژه‌ی ساختمانی |
| `units` | `UnitEntity` | واحد (آپارتمان/مغازه/اداری/پارکینگ/انباری/ویلا/زمین) |
| `customers` | `CustomerEntity` | مشتری حقیقی/حقوقی |
| `pre_files` | `PreFileEntity` | برگه‌ی پیش‌فایل (قرارداد پیش‌فروش) |
| `installments` | `InstallmentEntity` | اقساط و سررسیدها |
| `follow_ups` | `FollowUpEntity` | پیگیری‌ها |

## رابطه‌ها

```
projects ──▲── units ──▲── pre_files ──▲── installments
                                    └── follow_ups
customers ─────────────────────────────┘
```

- حذف پروژه ⇒ حذف واحدها و پیش‌فایل‌ها و اقساط (CASCADE)
- حذف واحد ⇒ پیش‌فایل باقی می‌ماند (`unitId` تهی می‌شود) چون **اسنپ‌شات** واحد در پیش‌فایل ذخیره شده است
- حذف مشتری ⇒ پیش‌فایل‌های او هم حذف می‌شوند (CASCADE)

## جدول `projects` (پروژه)

| گروه | فیلدها |
| --- | --- |
| شناسه | `id`, `name`, `code`, `projectType` |
| موقعیت | `province`, `city`, `district`, `address`, `postalCode`, `latitude`, `longitude` |
| فنی | `landArea`, `totalBuiltArea`, `blockCount`, `floorCount`, `unitCount`, `unitsPerFloor`, `parkingCount`, `elevatorCount`, `structureType`, `heatingSystem` |
| مجوز | `permitNumber`, `permitIssueDate`, `permitExpiryDate`, `landDeedNumber` |
| مالی | `costPerMeter`, `salePricePerMeter`, `defaultDepositAmount`, `shareMeterArea`, `sharePrice`, `totalBudget` |
| **پیش‌فرض‌های ثبت فایل** | `defaultBonusAmount`, `hasRanking`, `defaultRanking`, `saleConditionCash/Installment/Exchange`, `saleConditionNotes`, `installmentCount`, `remainingInstallmentsCount`, `installmentAmount`, `installmentPeriod`, `nextInstallmentDueDate` |
| پیشرفت | `phase`, `progressPercent`, `startDate`, `deliveryDate`, `deliveryFrom` |
| افراد | `contractorName/Phone`, `supervisorName/Phone`, `developerName`, `salesManagerPhone` |
| سایر | `facilities`, `description`, `tags`, `isFavorite`, `isArchived` |

مراحل پروژه (`phase`): `PLANNING`، `EXCAVATION`، `STRUCTURE`، `FINISHING`، `DELIVERED`.

> **پیش‌فرض‌های ثبت فایل (نسخه ۰.۳.۰):** این فیلدها همه‌ی مقادیری هستند که قبلاً برای **هر فایل** جداگانه پر می‌شد و در واحدهای یک پروژه با هم یکسان‌اند
> (واریزی تا امروز، امتیاز، رتبه، شرایط فروش، اقساط). حالا یک‌بار روی پروژه ثبت می‌شوند و در «ثبت سریع فایل»
> خودکار پر می‌شوند؛ برنامه فقط فیلدهای خالی (مثلاً نام مالک، امتیاز خاصِ آن فایل و رتبه) را می‌پرسد.
> **قیمت کل** هر فایل همیشه `واریزی + امتیاز` (یا معادل آن در مدل‌های متری/سهامی) است و با همین عنوان در همه‌ی نماها نمایش داده می‌شود.

## جدول `units` (واحد)

| گروه | فیلدها |
| --- | --- |
| شناسه | `block`, `unitNumber`, `floor`, `unitType`, `bedrooms`, `bathrooms`, `kitchens` |
| متراژ | `grossArea`, `netArea`, `balconyArea`, `commonAreaShare`, `ceilingHeight` |
| نور و موقعیت | `direction`, `lighting`, `view`, `positionType` |
| ضمائم | `parkingCount`, `parkingNumber`, `storageCount`, `storageNumber`, `facilities` |
| مالی | `pricePerMeter`, `totalPrice`, `finalPrice`, `extraCosts`, `discount`, `vatAmount`, `costPrice` |
| پیشنهاد پرداخت | `prepaymentSuggestion`, `suggestedInstallment`, `suggestedInstallmentCount` |
| وضعیت | `status`, `deliveryDate`, `deliveryStatus`, `technicalNotes` |

وضعیت واحد (`status`): `AVAILABLE` (آزاد)، `RESERVED` (رزرو)، `SOLD` (پیش‌فروش‌شده)، `DELIVERED` (تحویل‌شده).

> **محاسبه‌ی خودکار:** اگر «قیمت کل» خالی بماند، `grossArea × pricePerMeter` محاسبه می‌شود.

## جدول `customers` (مشتری)

| گروه | فیلدها |
| --- | --- |
| هویت | `title`, `firstName`, `lastName`, `fatherName`, `nationalId`, `idNumber`, `birthDate`, `entityType` |
| حقوقی | `companyName`, `registrationNumber`, `economicCode` |
| تماس | `phonePrimary`, `phoneSecondary`, `whatsapp`, `email` |
| نشانی | `province`, `city`, `address`, `postalCode`, `job`, `workAddress`, `workPhone` |
| بانکی | `iban`, `bankName` |
| اعتبار | `status`, `source`, `referredBy`, `creditScore`, `creditLimit`, `hasBouncedCheque`, `isReturningCustomer`, `totalPurchases` |

وضعیت مشتری (`status`): `LEAD` (سرنخ)، `ACTIVE`، `INSTALLMENT` (در حال پرداخت اقساط)، `SETTLED` (تسویه)، `INACTIVE`.

منبع آشنایی (`source`): `REFERRAL`, `INSTAGRAM`, `SITE`, `BANNER`, `EXHIBITION`, `WALK_IN`, `CALL`, `OTHER`.

## جدول `pre_files` (پیش‌فایل) — قلب برنامه

| گروه | فیلدها |
| --- | --- |
| شناسه | `draftNumber` (خودکار `PF-سال-شماره`)، `draftDate`, `trackingCode` |
| طرفین | `projectId`, `unitId`, `customerId`, `salesAgentName`, `salesAgentPhone` |
| اسنپ‌شات واحد | `unitBlock`, `unitNumber`, `unitFloor`, `unitArea` |
| مالی | `totalPrice`, `pricePerMeter`, `discount`, `finalPrice`, `prepayment`, `paidAmount`, `remainingAmount` |
| قسط‌بندی | `installmentCount`, `installmentAmount`, `installmentPeriod`, `installmentStartDate`, `paymentType` |
| تعهدات | `sellerCommitment`, `buyerCommitment`, `penaltyClause`, `cancellationTerms` |
| سند و تحویل | `deedDate`, `deedOffice`, `deliveryDate`, `isUnitMortgaged` |
| ضمانت | `guaranteeType`, `chequeCount`, `chequeAmount` |
| وضعیت | `status`, `confirmedDate`, `cancelDate`, `cancelReason`, `cancelPenaltyAmount` |
| سایر | `exchangeDetails`, `notes`, `documentPaths`, `contractPhotoPath` |

وضعیت پیش‌فایل (`status`): `DRAFT` (پیش‌نویس)، `RESERVED` (رزرو اولیه)، `PENDING_PAYMENT` (در انتظار پرداخت)، `CONFIRMED` (قطعی)، `COMPLETED` (تسویه/تحویل)، `CANCELED` (لغو).

نوع پرداخت (`paymentType`): `CASH`, `INSTALLMENT`, `EXCHANGE` (تهاتر), `FACILITY` (تسهیلات), `MIXED`.

**فیلدهای محاسبه‌شده در کد (نه در دیتابیس):**
- `effectivePrice = finalPrice ?: (totalPrice - discount)`
- `dueAmount = effectivePrice - paidAmount`
- `progressPercent = paidAmount / effectivePrice × 100`

## جدول `installments` (اقساط)

`installmentNumber` (۰ = پیش‌پرداخت)، `title`، `amount`، `dueDate`، `status`، `paidAmount`، `paidDate`، `paymentMethod`، `referenceNumber`، `bankName`، `chequeOwner`، `chequeDate`، `latePenalty`، `reminderEnabled`، `reminderDate`.

وضعیت قسط (`status`): `UNPAID`, `PAID`, `PARTIAL` (پرداخت جزئی), `OVERDUE` (معوق).

**ساخت خودکار:** با ذخیره‌ی پیش‌فایل، برای هر قسط ردیفی ساخته می‌شود:
`dueDate = installmentStartDate + (period × (i-1))` که `period` بر اساس «ماهانه/دو ماهه/فصلی/شش‌ماهه/سالانه» یک، دو، سه، شش یا دوازده ماه است.

## جدول `follow_ups` (پیگیری)

`type` (`CALL`/`VISIT`/`MEETING`/`MESSAGE`/`REMINDER`)، `priority` (`LOW`/`NORMAL`/`HIGH`/`URGENT`)، `title`، `description`، `outcome`، `result`، `customerId`، `preFileId`، `projectId`، `dueDate`، `dueTime`، `durationMinutes`، `status`، `completedDate`، `assignee`، `contactPhone`، `remindDaysBefore`.

## فیلدهای همگام‌سازی (در همه‌ی جدول‌ها)

| فیلد | معنا |
| --- | --- |
| `remoteId` | شناسه‌ی رکورد روی سرور (خالی تا قبل از اولین ارسال) |
| `syncState` | `CLEAN` / `PENDING_UPLOAD` / `PENDING_DELETE` / `CONFLICT` |
| `serverUpdatedAt` | زمان آخرین تغییر روی سرور (برای حل تعارض) |
| `deletedAt` | حذف نرم — رکورد از فهرست‌ها پنهان می‌شود ولی برای ارسال باقی می‌ماند |

## فیلدهایی که ممکن است بخواهید اضافه کنید

اگر کار شما این موارد را لازم دارد، اضافه‌کردنشان ساده است (یک فیلد در موجودیت + یک فیلد در فرم + نمایش):

- شماره پایان‌کار/گواهی عدم خلاف
- کد رهگیری کاداستر و پلاک ثبتی
- سهم‌الارض / تعداد دانگ
- متره‌ی دقیق: تعداد پریز، کولر گازی، رادیاتور
- فهرست کامل ساکنان و تحویل کلید
- کمیسیون مشاور و سهم مشارکت

</div>
