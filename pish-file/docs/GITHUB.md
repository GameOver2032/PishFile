<div dir="rtl">

# انتشار پروژه روی گیت‌هاب

مخزن محلی پروژه آماده است (`/home/user/pish-file`) و همه‌ی کامیت‌ها روی شاخه‌ی `main` ثبت شده‌اند.
برای انتشار، **یکی** از دو روش زیر را انتخاب کنید.

---

## روش ۱ — بدون دادن توکن (امن‌ترین) ⭐

1. در مرورگر برو به <https://github.com/new>
2. نام مخزن: `PishFile` (یا `pish-file`)
3. **README و .gitignore و License را تیک نزنید** (چون در پروژه هستند)
4. `Create repository`
5. بعد این دستورها را بزنید:

```bash
cd ~/pish-file
git remote add origin https://github.com/<username>/PishFile.git
git branch -M main
git push -u origin main
```

اگر گیت رمز خواست، به‌جای رمز حساب از **Personal Access Token** استفاده کنید.

---

## روش ۲ — ساخت خودکار با اسکریپت

اسکریپت [`tools/push-to-github.sh`](../tools/push-to-github.sh) هم ریپو را می‌سازد، هم توضیحات و موضوع‌ها (Topics) را تنظیم می‌کند، هم کد را push می‌کند:

```bash
cd ~/pish-file
./tools/push-to-github.sh <username> PishFile <TOKEN>
```

و اگر ریپو را خودتان ساخته‌اید (بدون توکن):

```bash
./tools/push-to-github.sh <username> PishFile
```

> توکن فقط در همان لحظه استفاده می‌شود و در `.git/config` ذخیره نمی‌شود.

### ساخت توکن مناسب

**راه ساده‌تر (Classic Token):** <https://github.com/settings/tokens/new>
- Scopes لازم: ✅ `repo` و ✅ `workflow`

> اسکوپ `workflow` ضروری است، چون پروژه یک فایل `​.github/workflows/android.yml` دارد؛ بدون آن گیت‌هاب اجازه‌ی push این فایل را نمی‌دهد.

**راه دقیق‌تر (Fine-grained Token):** <https://github.com/settings/personal-access-tokens/new>
- Repository access: All repositories (یا فقط همین مخزن)
- Permissions: `Contents: Read and write` · `Workflows: Read and write` · `Administration: Read and write` (برای ساخت ریپو)

**بعد از پایان کار، توکن را حذف/باطل کنید:** <https://github.com/settings/tokens>

---

## بعد از انتشار — کارهای پیشنهادی

| کار | توضیح |
| --- | --- |
| تنظیم توضیحات مخزن | Settings ← Description: «اپلیکیشن اندروید مدیریت پیش‌فروش املاک — Kotlin + Jetpack Compose، آفلاین و فارسی» |
| افزودن Topics | Settings ← Topics: `android` `kotlin` `jetpack-compose` `room-database` `real-estate` `persian` `offline-first` `jalali-calendar` |
| سنجاق‌کردن مخزن | Profile ← Customize pins |
| فعال‌بودن Actions | تب Actions → اگر غیرفعال بود، «I understand my workflows, go ahead and enable them» |
| دریافت APK از CI | هر push روی `main` یک Workflow اجرا می‌کند؛ فایل APK در Actions ← آخرین اجرا ← Artifacts (نام: `pishfile-debug-apk`) قابل دانلود است |
| افزودن LICENSE در گیت‌هاب | چون فایل `LICENSE` وجود دارد، گیت‌هاب خودش MIT را تشخیص می‌دهد |

---

## ساخت Release رسمی روی گیت‌هاب

```bash
# تگ زدن روی آخرین کامیت
git tag -a v0.1.0 -m "نسخه ۰.۱.۰ — فاز اول: هسته‌ی آفلاین"
git push origin v0.1.0
```

سپس در گیت‌هاب: **Releases ← Draft a new release** → انتخاب تگ `v0.1.0` → آپلود فایل
`releases/PishFile-0.1.0-debug.apk` → متن از `CHANGELOG.md` کپی کنید → Publish.

---

## اگر بعداً خواستید مخزن خصوصی شود

Settings ← General ← پایین صفحه ← Danger Zone ← Change visibility.
(اگر مخزن عمومی بوده و کسی fork کرده باشد، فورک‌ها عمومی می‌مانند.)

</div>
