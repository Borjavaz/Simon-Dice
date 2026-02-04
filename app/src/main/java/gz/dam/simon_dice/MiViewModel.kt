package gz.dam.simon_dice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para manejar el record persistente con SQLite.
 */
class MiViewModel(application: Application) : AndroidViewModel(application) {

    private val _record = MutableStateFlow(0)
    val record: StateFlow<Int> = _record.asStateFlow()

    private val _recordTexto = MutableStateFlow("Sin récord (SQLite)")
    val recordTexto: StateFlow<String> = _recordTexto.asStateFlow()

    // Para mostrar en el recuadro de RÉCORD
    private val _recordParaRecuadro = MutableStateFlow("0")
    val recordParaRecuadro: StateFlow<String> = _recordParaRecuadro.asStateFlow()

    // Nuevo: Para mostrar información adicional de SQLite
    private val _dbInfo = MutableStateFlow("Base de datos: SQLite")
    val dbInfo: StateFlow<String> = _dbInfo.asStateFlow()

    init {
        cargarRecordGuardado()
        // Opcional: Mostrar todos los records en Logcat
        mostrarTodosRecordsEnLogcat()
    }

    private fun cargarRecordGuardado() {
        viewModelScope.launch {
            val (score, timestamp, playerName) = ControladorSQLite.obtenerMejorRecordCompleto(getApplication())
            _record.value = score
            _recordParaRecuadro.value = score.toString()

            if (score > 0) {
                val date = Date(timestamp)
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                _recordTexto.value = "Récord SQLite: $score ($fecha $hora)"
                _dbInfo.value = "Jugador: $playerName"
            } else {
                _recordTexto.value = "Sin récord (SQLite)"
                _dbInfo.value = "Base de datos: SQLite - Sin datos"
            }
        }
    }

    fun verificarYActualizarRecord(posibleRecord: Int): Boolean {
        val resultado = ControladorSQLite.insertarRecord(getApplication(), posibleRecord)

        if (resultado) {
            viewModelScope.launch {
                // Recargar el mejor record
                cargarRecordGuardado()
                _record.value = posibleRecord
                _recordParaRecuadro.value = posibleRecord.toString()

                // Mostrar mensaje de éxito
                val now = Date()
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                _recordTexto.value = "¡NUEVO RÉCORD SQLite! $posibleRecord ($fecha $hora)"

                // Mostrar todos los records actualizados
                mostrarTodosRecordsEnLogcat()
            }
            return true
        }
        return false
    }

    private fun mostrarTodosRecordsEnLogcat() {
        viewModelScope.launch {
            val todosRecords = ControladorSQLite.obtenerTodosRecords(getApplication())
            android.util.Log.d("SQLite_ViewModel", "=== TODOS LOS RECORDS EN DB ===")
            todosRecords.forEachIndexed { index, (score, timestamp, player) ->
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(timestamp))
                android.util.Log.d("SQLite_ViewModel", "${index + 1}. $player: $score ($fecha)")
            }
            android.util.Log.d("SQLite_ViewModel", "============================")
        }
    }

    // Función adicional para testing desde la UI
    fun testSQLiteOperations() {
        viewModelScope.launch {
            android.util.Log.d("SQLite_Test", "=== INICIANDO PRUEBAS SQLite ===")

            // 1. Insertar datos de prueba
            ControladorSQLite.insertarRecord(getApplication(), 5)
            ControladorSQLite.insertarRecord(getApplication(), 3)
            ControladorSQLite.insertarRecord(getApplication(), 8)
            ControladorSQLite.insertarRecord(getApplication(), 2)

            // 2. Obtener mejor record
            val mejor = ControladorSQLite.obtenerMejorRecordScore(getApplication())
            android.util.Log.d("SQLite_Test", "Mejor record después de inserciones: $mejor")

            // 3. Obtener todos los records
            val todos = ControladorSQLite.obtenerTodosRecords(getApplication())
            android.util.Log.d("SQLite_Test", "Total de records en DB: ${todos.size}")

            // 4. Recargar en ViewModel
            cargarRecordGuardado()

            android.util.Log.d("SQLite_Test", "=== PRUEBAS COMPLETADAS ===")
        }
    }
}
