package gz.dam.simon_dice

import android.content.Context
import androidx.core.content.edit
import java.util.Date

/**
 * Controlador para manejar las preferencias compartidas (SharedPreferences).
 * Documentación: https://developer.android.com/training/data-storage/shared-preferences
 */
object ControladorPreference {
    private const val PREFS_NAME = "simon_dice_prefs"
    private const val KEY_RECORD_SCORE = "record_score"
    private const val KEY_RECORD_TIMESTAMP = "record_timestamp"

    fun actualizarRecord(context: Context, nuevoRecord: Int) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putInt(KEY_RECORD_SCORE, nuevoRecord)
            putLong(KEY_RECORD_TIMESTAMP, Date().time)
        }
    }

    fun obtenerRecordScore(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(KEY_RECORD_SCORE, 0)
    }

    fun obtenerRecordTimestamp(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(KEY_RECORD_TIMESTAMP, 0L)
    }

    fun obtenerRecordCompleto(context: Context): Pair<Int, Long> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val score = sharedPreferences.getInt(KEY_RECORD_SCORE, 0)
        val timestamp = sharedPreferences.getLong(KEY_RECORD_TIMESTAMP, 0L)
        return Pair(score, timestamp)
    }
}