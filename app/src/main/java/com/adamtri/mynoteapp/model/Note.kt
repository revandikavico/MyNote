package com.adamtri.mynoteapp.model

/**
 * Note adalah "cetakan" (blueprint) satu catatan.
 * Disimpan menggunakan SharedPreferences (lihat NoteRepository),
 * sehingga tidak lagi memerlukan anotasi Room.
 */
data class Note(
    val id: Long = 0,             // 0 = catatan baru, akan diberi ID otomatis oleh NoteRepository
    val content: String,
    val color: Long = 0xFFFFF9C4, // Default Kuning Muda
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
