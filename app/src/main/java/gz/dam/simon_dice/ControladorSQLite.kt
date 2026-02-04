package gz.dam.simon_dice


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Controlador para manejar la base de datos SQLite con top 10 records.
 * Documentación: https://developer.android.com/training/data-storage/sqlite
 */
object ControladorSQLite {
    private const val DATABASE_NAME = "simon_dice_top10.db"
    private const val DATABASE_VERSION = 1
    private const val TABLE_RECORDS = "records"

    // Columnas de la tabla
    private const val KEY_ID = "_id"
    private const val KEY_SCORE = "score"
    private const val KEY_TIMESTAMP = "timestamp"
    private const val KEY_PLAYER_NAME = "player_name"

    class SimonDiceDBHelper(context: Context) : SQLiteOpenHelper(
        context, DATABASE_NAME, null, DATABASE_VERSION
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            val createTable = """
               CREATE TABLE $TABLE_RECORDS (
                   $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                   $KEY_SCORE INTEGER NOT NULL,
                   $KEY_TIMESTAMP INTEGER NOT NULL,
                   $KEY_PLAYER_NAME TEXT DEFAULT 'Jugador'
               )
           """.trimIndent()

            db.execSQL(createTable)
            Log.d("SQLite", "Tabla $TABLE_RECORDS creada (Top 10)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
            onCreate(db)
            Log.d("SQLite", "Base de datos actualizada de v$oldVersion a v$newVersion")
        }
    }

    /**
     * Guardar score y mantener solo top 10 records
     */
    fun guardarScoreYLimitarTop10(context: Context, score: Int): Boolean {
        return try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.writableDatabase

            //insertar el nuevo score
            val values = ContentValues().apply {
                put(KEY_SCORE, score)
                put(KEY_TIMESTAMP, Date().time)
                put(KEY_PLAYER_NAME, "Jugador")
            }


            val nuevoId = db.insert(TABLE_RECORDS, null, values)
            Log.d("SQLite_Top10", "Score insertado: $score, ID=$nuevoId")


            //obtenengo todos los records ordenados
            val cursor = db.query(
                TABLE_RECORDS,
                arrayOf(KEY_ID, KEY_SCORE, KEY_TIMESTAMP),
                null, null, null, null,
                //selecciono el mayor y en caso de empate el más antiguo
                "$KEY_SCORE DESC, $KEY_TIMESTAMP ASC"
            )


            val todosRecords = mutableListOf<Pair<Long, Int>>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID))
                val recordScore = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE))
                todosRecords.add(Pair(id, recordScore))
            }
            cursor.close()


            //mantengo solo los primeros 10 records
            if (todosRecords.size > 10) {
                val idsParaEliminar = todosRecords.drop(10).map { it.first }


                idsParaEliminar.forEach { id ->
                    db.delete(TABLE_RECORDS, "$KEY_ID = ?", arrayOf(id.toString()))
                    Log.d("SQLite_Top10", "Record eliminado (fuera del top 10): ID=$id")
                }


                Log.d("SQLite_Top10", "Tabla limitada a 10 records. Eliminados: ${idsParaEliminar.size}")
            }


            //Verifico si el nuevo score está en el top 10
            val posicionEnTop10 = obtenerPosicionEnTop10(db, score, Date().time)
            val estaEnTop10 = posicionEnTop10 != null && posicionEnTop10 <= 10


            db.close()


            if (estaEnTop10) {
                Log.i("SQLite_Top10", "¡NUEVO RECORD EN TOP 10! Score: $score - Posición: $posicionEnTop10")
            } else {
                Log.d("SQLite_Top10", "Score $score no está en el top 10")
            }


            estaEnTop10


        } catch (e: Exception) {
            Log.e("SQLite", "Error al guardar score: ${e.message}")
            false
        }
    }


    /**
     * Obtener la posición de un score en el ranking
     */
    private fun obtenerPosicionEnTop10(db: SQLiteDatabase, score: Int, timestamp: Long): Int? {
        val cursor = db.query(
            TABLE_RECORDS,
            arrayOf(KEY_ID, KEY_SCORE, KEY_TIMESTAMP),
            null, null, null, null,
            "$KEY_SCORE DESC, $KEY_TIMESTAMP ASC"
        )


        var posicion = 1
        var encontrado = false


        while (cursor.moveToNext() && posicion <= 10) {
            val recordScore = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE))
            val recordTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP))


            // Verificar si es el score que acabamos de insertar
            if (recordScore == score && recordTimestamp == timestamp) {
                encontrado = true
                break
            }
            posicion++
        }
        cursor.close()


        return if (encontrado && posicion <= 10) posicion else null
    }


    /**
     * Obtener el mejor record (mayor score)
     */
    fun obtenerMejorRecordScore(context: Context): Int {
        return try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                TABLE_RECORDS,
                arrayOf("MAX($KEY_SCORE) as max_score"),
                null, null, null, null, null
            )

            var maxScore = 0
            if (cursor.moveToFirst()) {
                maxScore = cursor.getInt(cursor.getColumnIndexOrThrow("max_score"))
            }

            cursor.close()
            db.close()


            Log.d("SQLite_Top10", "Mejor record obtenido: $maxScore")
            maxScore
        } catch (e: Exception) {
            Log.e("SQLite", "Error al obtener mejor record: ${e.message}")
            0
        }
    }

    /**
     * Obtener el mejor record completo con timestamp
     */
    fun obtenerMejorRecordCompleto(context: Context): Triple<Int, Long, String> {
        return try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                TABLE_RECORDS,
                arrayOf(KEY_SCORE, KEY_TIMESTAMP, KEY_PLAYER_NAME),
                null, null, null, null,
                "$KEY_SCORE DESC, $KEY_TIMESTAMP ASC", // Score mayor primero
                "1" // Limitar a 1 resultado
            )


            var score = 0
            var timestamp = 0L
            var playerName = "Jugador"


            if (cursor.moveToFirst()) {
                score = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE))
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP))
                playerName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PLAYER_NAME))
            }


            cursor.close()
            db.close()


            Log.d("SQLite_Top10", "Record #1 obtenido: $score puntos")
            Triple(score, timestamp, playerName)
        } catch (e: Exception) {
            Log.e("SQLite", "Error al obtener record completo: ${e.message}")
            Triple(0, 0L, "Jugador")
        }
    }


    /**
     * Obtener todos los records ordenados (top 10)
     */
    fun obtenerTop10Records(context: Context): List<Triple<Int, Long, String>> {
        val records = mutableListOf<Triple<Int, Long, String>>()


        return try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.readableDatabase


            val cursor = db.query(
                TABLE_RECORDS,
                arrayOf(KEY_SCORE, KEY_TIMESTAMP, KEY_PLAYER_NAME),
                null, null, null, null,
                "$KEY_SCORE DESC, $KEY_TIMESTAMP ASC", // Score mayor primero, en empate el más antiguo
                "10" // Limitar a 10 resultados
            )


            var posicion = 1
            while (cursor.moveToNext()) {
                val score = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP))
                val playerName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PLAYER_NAME))


                records.add(Triple(score, timestamp, playerName))
                Log.d("SQLite_Top10", "Posición $posicion: $score puntos - ${formatearFecha(timestamp)}")
                posicion++
            }


            cursor.close()
            db.close()


            Log.d("SQLite_Top10", "Total records en top 10: ${records.size}")
            records
        } catch (e: Exception) {
            Log.e("SQLite", "Error al obtener top 10: ${e.message}")
            records
        }
    }


    /**
     * Obtener posición actual de un score específico
     */
    fun obtenerPosicionDeScore(context: Context, score: Int, timestamp: Long): Int {
        return try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.readableDatabase


            val cursor = db.query(
                TABLE_RECORDS,
                arrayOf(KEY_SCORE, KEY_TIMESTAMP),
                null, null, null, null,
                "$KEY_SCORE DESC, $KEY_TIMESTAMP ASC"
            )


            var posicion = 1
            var encontrado = false


            while (cursor.moveToNext()) {
                val recordScore = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE))
                val recordTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP))


                if (recordScore == score && recordTimestamp == timestamp) {
                    encontrado = true
                    break
                }
                posicion++
            }


            cursor.close()
            db.close()


            if (encontrado) posicion else -1
        } catch (e: Exception) {
            Log.e("SQLite", "Error al obtener posición: ${e.message}")
            -1
        }
    }


    /**
     * Eliminar todos los records (para testing)
     */
    fun eliminarTodosRecords(context: Context) {
        try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.writableDatabase


            db.delete(TABLE_RECORDS, null, null)
            db.close()


            Log.d("SQLite_Top10", "Todos los records eliminados")
        } catch (e: Exception) {
            Log.e("SQLite", "Error al eliminar records: ${e.message}")
        }
    }

    /**
     * Formatear fecha para logging
     */
    private fun formatearFecha(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            "Fecha desconocida"
        }
    }
}