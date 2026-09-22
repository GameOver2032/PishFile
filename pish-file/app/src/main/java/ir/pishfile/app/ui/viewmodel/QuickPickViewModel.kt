package ir.pishfile.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import ir.pishfile.app.data.local.dao.PreFileRow
import ir.pishfile.app.data.local.entity.ProjectEntity
import ir.pishfile.app.data.local.entity.UnitEntity
import ir.pishfile.app.data.repository.PreFileRepository
import ir.pishfile.app.data.repository.ProjectRepository
import ir.pishfile.app.data.repository.UnitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyList
import kotlinx.coroutines.flow.stateIn

/**
 * صفحه‌ی «از کجا شروع کنیم؟» — انتخاب واحد آماده/پیش‌فروش (و برای مشتری، فایل)
 * قبل از شروع فرایند ثبت.
 */
class QuickPickViewModel(
    unitRepository: UnitRepository,
    preFileRepository: PreFileRepository,
    projectRepository: ProjectRepository,
) : ViewModel() {

    val units: StateFlow<List<UnitEntity>> = unitRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = projectRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preFileRows: StateFlow<List<PreFileRow>> = preFileRepository.observeAllRows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
