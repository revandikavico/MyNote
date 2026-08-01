package com.adamtri.mynoteapp.data

import android.content.Context
import android.content.SharedPreferences
import com.adamtri.mynoteapp.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * NoteRepository menyimpan semua catatan menggunakan SharedPreferences
 * (bukan Room/SQLite). Seluruh daftar catatan disimpan sebagai satu
 * string JSON di dalam SharedPreferences, lalu dibaca ulang & di-parse
 * setiap kali aplikasi dibuka.
 *
 * Alasan pakai SharedPreferences:
 * - Sederhana untuk data yang tidak terlalu besar (catatan pribadi).
 * - Tidak butuh setup database/skema seperti Room.
 */
class NoteRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Sumber kebenaran (source of truth) di memori, disinkronkan dengan
    // SharedPreferences setiap kali ada perubahan (simpan/hapus).
    private val _notes = MutableStateFlow(loadNotesFromPrefs())
    val notes: StateFlow<List<Note>> = _notes

    /** Membaca & mem-parsing JSON dari SharedPreferences menjadi List<Note>. */
    private fun loadNotesFromPrefs(): List<Note> {
        val json = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { index ->
                val obj = array.getJSONObject(index)
                Note(
                    id = obj.getLong("id"),
                    content = obj.getString("content"),
                    color = obj.optLong("color", 0xFFFFF9C4),
                    isPinned = obj.optBoolean("isPinned", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Mengubah List<Note> menjadi JSON lalu menuliskannya ke SharedPreferences. */
    private fun persist(notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject()
            obj.put("id", note.id)
            obj.put("content", note.content)
            obj.put("color", note.color)
            obj.put("isPinned", note.isPinned)
            obj.put("createdAt", note.createdAt)
            obj.put("updatedAt", note.updatedAt)
            array.put(obj)
        }
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
        _notes.value = notes
    }

    fun getNoteById(id: Long): Note? = _notes.value.find { it.id == id }

    /** Menyimpan catatan baru (id = 0) atau memperbarui catatan yang sudah ada. */
    fun upsertNote(note: Note) {
        val current = _notes.value.toMutableList()

        val finalNote = if (note.id == 0L) {
            val newId = (current.maxOfOrNull { it.id } ?: 0L) + 1
            note.copy(id = newId)
        } else {
            note
        }

        val existingIndex = current.indexOfFirst { it.id == finalNote.id }
        if (existingIndex >= 0) {
            current[existingIndex] = finalNote
        } else {
            current.add(finalNote)
        }

        persist(current)
    }

    fun deleteNoteById(id: Long) {
        val current = _notes.value.filterNot { it.id == id }
        persist(current)
    }

    companion object {
        private const val PREFS_NAME = "note_prefs"
        private const val KEY_NOTES = "notes_json"

        @Volatile
        private var INSTANCE: NoteRepository? = null

        fun getInstance(context: Context): NoteRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = NoteRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
