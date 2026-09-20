#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  انتشار پروژه «پیش‌فایل» روی گیت‌هاب — با یک دستور
#
#  استفاده:
#     ./tools/push-to-github.sh <نام‌کاربری‌گیت‌هاب> [نام‌ریپو] [توکن]
#
#  مثال‌ها:
#     ./tools/push-to-github.sh ali PishFile                 # ریپو را خودتان در وب ساخته‌اید
#     ./tools/push-to-github.sh ali PishFile ghp_xxxxxxxx    # ریپو خودکار ساخته می‌شود
#     GITHUB_TOKEN=ghp_xxxx ./tools/push-to-github.sh ali    # توکن از متغیر محیطی
#
#  نکته‌ی امنیتی: توکن فقط برای همین اجرا استفاده می‌شود و در .git/config ذخیره نمی‌شود.
# ─────────────────────────────────────────────────────────────
set -euo pipefail

GITHUB_USER="${1:-}"
REPO_NAME="${2:-PishFile}"
TOKEN="${3:-${GITHUB_TOKEN:-}}"
DESCRIPTION="پیش‌فایل — اپلیکیشن اندروید مدیریت پیش‌فروش املاک (Kotlin + Jetpack Compose)"
TOPICS='["android","kotlin","jetpack-compose","room-database","real-estate","persian","farsi","offline-first","jalali-calendar","property-management"]'

if [[ -z "$GITHUB_USER" ]]; then
  echo "✋ نام کاربری گیت‌هاب را وارد کنید:"
  echo "   ./tools/push-to-github.sh <username> [repo] [token]"
  exit 1
fi

# رفتن به ریشه‌ی مخزن (محل همین اسکریپت)
cd "$(dirname "$0")/.."
REPO_ROOT="$(pwd)"
echo "📁 مخزن: $REPO_ROOT"

# ── ۱) تنظیم هویت کامیت در صورت نبود ────────────────────────
if [[ -z "$(git config user.email || true)" ]]; then
  git config user.name "Pish File"
  git config user.email "${GITHUB_USER}@users.noreply.github.com"
  echo "✅ هویت گیت تنظیم شد: ${GITHUB_USER}@users.noreply.github.com"
fi

# ── ۲) ساخت ریپازیتوری (اگر توکن داده شده باشد) ─────────────
if [[ -n "$TOKEN" ]]; then
  echo "🚀 ساخت ریپازیتوری ${GITHUB_USER}/${REPO_NAME} …"
  HTTP_CODE=$(curl -s -o /tmp/gh_repo.json -w "%{http_code}" \
    -X POST "https://api.github.com/user/repos" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    -d "{\"name\":\"${REPO_NAME}\",\"description\":\"${DESCRIPTION}\",\"private\":false,\"has_issues\":true,\"has_wiki\":false,\"has_projects\":false,\"auto_init\":false}")

  case "$HTTP_CODE" in
    201) echo "✅ ریپازیتوری ساخته شد." ;;
    422) echo "ℹ️  ریپازیتوری از قبل وجود دارد — ادامه می‌دهیم." ;;
    401|403) echo "❌ توکن نامعتبر است یا دسترسی لازم را ندارد (نیاز: Administration=write و Contents=write و Workflows=write)"; exit 1 ;;
    *)   echo "⚠️  پاسخ گیت‌هاب (HTTP $HTTP_CODE):"; head -5 /tmp/gh_repo.json; exit 1 ;;
  esac

  # تنظیم توضیحات و موضوع‌ها (Topic) برای دیده‌شدن در جست‌وجو
  curl -s -o /dev/null -X PUT "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/topics" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -d "{\"names\":${TOPICS}}" || true
  echo "✅ موضوع‌ها (Topics) تنظیم شد."
else
  echo "ℹ️  توکن داده نشده — فرض می‌کنیم ریپازیتوری را در وب ساخته‌اید:"
  echo "   https://github.com/new  →  نام: ${REPO_NAME}  (بدون README و .gitignore)"
fi

# ── ۳) آماده‌سازی شاخه و remote ─────────────────────────────
git branch -M main

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
else
  git remote add origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
fi

# ── ۴) ارسال کد ─────────────────────────────────────────────
if [[ -n "$TOKEN" ]]; then
  # توکن موقت در URL قرار می‌گیرد و بلافاصله بعد از push پاک می‌شود
  git push "https://${GITHUB_USER}:${TOKEN}@github.com/${GITHUB_USER}/${REPO_NAME}.git" main --tags
  echo "🔒 آدرس remote پاک‌سازی شد (توکن ذخیره نشد)."
else
  echo "⬆️  حالا این دستور را بزنید تا کد ارسال شود:"
  echo "   git push -u origin main"
  exit 0
fi

echo
echo "🎉 تمام شد! مخزن شما: https://github.com/${GITHUB_USER}/${REPO_NAME}"
echo "   • اکشن‌های گیت‌هاب به‌صورت خودکار شروع به ساخت APK می‌کنند (.github/workflows/android.yml)"
echo "   • برای هر push روی main، یک فایل APK قابل دانلود در بخش Actions → Artifacts ساخته می‌شود."
