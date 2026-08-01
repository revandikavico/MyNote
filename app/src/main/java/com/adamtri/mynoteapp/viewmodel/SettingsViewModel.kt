package com.adamtri.mynoteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.adamtri.mynoteapp.data.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository.getInstance(application)

    val themeMode: StateFlow<Int> = repository.themeMode
    val themeColor: StateFlow<Long> = repository.themeColor

    fun setThemeMode(mode: Int) {
        repository.setThemeMode(mode)
    }

    fun setThemeColor(color: Long) {
        repository.setThemeColor(color)
    }
}
