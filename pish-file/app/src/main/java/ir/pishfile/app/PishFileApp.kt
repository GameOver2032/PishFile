package ir.pishfile.app

import android.app.Application
import ir.pishfile.app.core.InstallmentAutoPayScheduler
import ir.pishfile.app.di.AppContainer
import ir.pishfile.app.di.DefaultAppContainer

/**
 * کلاس اصلی برنامه — نگه‌دارنده‌ی «ظرف وابستگی‌ها» (بدون کتابخانه‌ی DI خارجی، سبک و ساده).
 */
class PishFileApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        // بررسی خودکار اقساط سررسیدشده (هر روز + یک‌بار فوری هنگام استارت)
        InstallmentAutoPayScheduler.schedule(this)
    }
}
