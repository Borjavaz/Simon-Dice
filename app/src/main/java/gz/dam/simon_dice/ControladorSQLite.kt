package gz.dam.simon_dice


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Date

/**
 * Controlador para manejar la base de datos SQLite.
 * Documentación: https://developer.android.com/training/data-storage/sqlite
 */
object ControladorSQLite {
    private const val DATABASE_NAME = "simon_dice.db"
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
            Log.d("SQLite", "Tabla $TABLE_RECORDS creada")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
            onCreate(db)
            Log.d("SQLite", "Base de datos actualizada de v$oldVersion a v$newVersion")
        }
    }

    /**
     * Insertar o actualizar record (solo si es mayor al actual)
     */
    fun insertarRecord(context: Context, nuevoScore: Int): Boolean {
        return try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.writableDatabase

            // Verificar si hay record previo
            val recordActual = obtenerMejorRecordScore(context)

            if (nuevoScore > recordActual) {
                val values = ContentValues().apply {
                    put(KEY_SCORE, nuevoScore)
                    put(KEY_TIMESTAMP, Date().time)
                    put(KEY_PLAYER_NAME, "Jugador")
                }

                val nuevoId = db.insert(TABLE_RECORDS, null, values)
                Log.d("SQLite", "Nuevo record insertado: ID=$nuevoId, Score=$nuevoScore")

                db.close()
                true
            } else {
                Log.d("SQLite", "Score $nuevoScore no supera el record actual $recordActual")
                db.close()
                false
            }
        } catch (e: Exception) {
            Log.e("SQLite", "Error al insertar record: ${e.message}")
            false
        }
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

            Log.d("SQLite", "Mejor record obtenido: $maxScore")
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
                "$KEY_SCORE DESC", // Ordenar por score descendente
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

            Log.d("SQLite", "Record completo obtenido: $score, $timestamp, $playerName")
            Triple(score, timestamp, playerName)
        } catch (e: Exception) {
            Log.e("SQLite", "Error al obtener record completo: ${e.message}")
            Triple(0, 0L, "Jugador")
        }
    }

    /**
     * Obtener todos los records ordenados
     */
    fun obtenerTodosRecords(context: Context): List<Triple<Int, Long, String>> {
        val records = mutableListOf<Triple<Int, Long, String>>()

        return try {
            val dbHelper = SimonDiceDBHelper(context)
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                TABLE_RECORDS,
                arrayOf(KEY_SCORE, KEY_TIMESTAMP, KEY_PLAYER_NAME),
                null, null, null, null,
                "$KEY_SCORE DESC" // Ordenar por score descendente
            )

            while (cursor.moveToNext()) {
                val score = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP))
                val playerName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PLAYER_NAME))

                records.add(Triple(score, timestamp, playerName))
                Log.d("SQLite", "Record: $score - $playerName")
            }

            cursor.close()
            db.close()

            Log.d("SQLite", "Total records obtenidos: ${records.size}")
            records
        } catch (e: Exception) {
            Log.e("SQLite", "Error al obtener todos los records: ${e.message}")
            records
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

            Log.d("SQLite", "Todos los records eliminados")
        } catch (e: Exception) {
            Log.e("SQLite", "Error al eliminar records: ${e.message}")
        }
    }
}
