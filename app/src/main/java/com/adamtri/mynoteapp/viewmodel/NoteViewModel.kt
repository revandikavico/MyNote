package com.adamtri.mynoteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adamtri.mynoteapp.data.NoteRepository
import com.adamtri.mynoteapp.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * NoteViewModel = "otak" aplikasi yang menyimpan dan mengelola semua catatan.
 * Sumber datanya adalah NoteRepository yang menyimpan catatan
 * di SharedPreferences (bukan Room/SQLite).
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository.getInstance(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // notes bersifat PUBLIC dan READ-ONLY — UI hanya boleh MEMBACA.
    // data mengalir turun dari NoteRepository (SharedPreferences → UI).
    // Digabungkan dengan searchQuery untuk fitur pencarian instan.
    val notes: StateFlow<List<Note>> = repository.notes
        .combine(_searchQuery) { notes, query ->
            val sorted = notes.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
            )
            if (query.isBlank()) {
                sorted
            } else {
                sorted.filter { it.content.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Mencari catatan berdasarkan id — dipakai EditorScreen saat mode edit. */
    fun getNoteById(id: Long): Note? {
        return repository.getNoteById(id)
    }

    /**
     * Menyimpan catatan ke SharedPreferences (via NoteRepository).
     */
    fun saveNote(
        id: Long? = null,
        content: String,
        color: Long = 0xFFFFF9C4,
        isPinned: Boolean = false
    ) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val existingNote = id?.let { repository.getNoteById(it) }
            val note = Note(
                id = id ?: 0,
                content = content.trim(),
                color = color,
                isPinned = isPinned,
                createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.upsertNote(note)
        }
    }

    /** Menghapus catatan. */
    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }
}
