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
 * ViewModel para manejar el record persistente con SQLite (Top 10).
 */
class MiViewModel(application: Application) : AndroidViewModel(application) {


    private val _record = MutableStateFlow(0)
    val record: StateFlow<Int> = _record.asStateFlow()

    private val _recordTexto = MutableStateFlow("Sin récord (SQLite Top 10)")
    val recordTexto: StateFlow<String> = _recordTexto.asStateFlow()
    private val _recordParaRecuadro = MutableStateFlow("0")
    val recordParaRecuadro: StateFlow<String> = _recordParaRecuadro.asStateFlow()


    //Para mostrar información del top 10
    private val _top10Info = MutableStateFlow("Top 10 Records")
    val top10Info: StateFlow<String> = _top10Info.asStateFlow()


    init {
        cargarRecordGuardado()
        //muestro top 10 en Logcat al inicio
        mostrarTop10EnLogcat()
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
                _recordTexto.value = "Récord: $score ($fecha $hora)"
                _top10Info.value = "Top 10: $score puntos (1º)"
            } else {
                _recordTexto.value = "Sin récord (Top 10)"
                _top10Info.value = "Top 10: Vacío"
            }
        }
    }


    fun verificarYActualizarRecord(posibleRecord: Int): Boolean {
        // siempre guardar el score (se limitará a top 10)
        val estaEnTop10 = ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), posibleRecord)


        if (estaEnTop10) {
            viewModelScope.launch {
                //Recargar el mejor record
                cargarRecordGuardado()


                //Actualizo flows
                val mejorRecord = ControladorSQLite.obtenerMejorRecordScore(getApplication())
                _record.value = mejorRecord
                _recordParaRecuadro.value = mejorRecord.toString()


                //muestro un mensaje de éxito
                val now = Date()
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)


                //obtener posición del top
                val posicion = ControladorSQLite.obtenerPosicionDeScore(getApplication(), posibleRecord, now.time)


                if (posicion == 1) {
                    _recordTexto.value = "¡NUEVO RÉCORD! $posibleRecord ($fecha $hora)"
                    _top10Info.value = "¡TOP 1! $posibleRecord puntos"
                } else if (posicion in 2..10) {
                    _recordTexto.value = "Top $posicion: $posibleRecord ($fecha $hora)"
                    _top10Info.value = "Top $posicion: $posibleRecord puntos"
                }


                // mostrar top 10 actualizado en Logcat
                mostrarTop10EnLogcat()


                android.util.Log.i("TOP10_MENSAJE",
                    "¡FELICIDADES! Score $posibleRecord está en el TOP 10 (posición $posicion)")
            }
            return true
        } else {
            // Aunque no esté en top 10, actualizar si es mejor record
            viewModelScope.launch {
                val mejorRecord = ControladorSQLite.obtenerMejorRecordScore(getApplication())
                if (posibleRecord > mejorRecord) {
                    _record.value = posibleRecord
                    _recordParaRecuadro.value = posibleRecord.toString()
                    cargarRecordGuardado()
                }
            }


            android.util.Log.d("TOP10_MENSAJE",
                "Score $posibleRecord no está en el TOP 10")
            return false
        }
    }


    //funcion para mostrar el top 10 en el Logcat
    private fun mostrarTop10EnLogcat() {
        viewModelScope.launch {
            val top10 = ControladorSQLite.obtenerTop10Records(getApplication())
            android.util.Log.d("SQLite_Top10", "=== TOP 10 RECORDS ===")


            if (top10.isEmpty()) {
                android.util.Log.d("SQLite_Top10", "No hay records aún")
            } else {
                top10.forEachIndexed { index, (score, timestamp, player) ->
                    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(timestamp))
                    android.util.Log.d("SQLite_Top10",
                        "${index + 1}. $player: $score puntos ($fecha)")
                }
            }
            android.util.Log.d("SQLite_Top10", "============================")
        }
    }


    //función para testing desde la UI
    fun testTop10Operations() {
        viewModelScope.launch {
            android.util.Log.d("Top10_Test", "=== PRUEBAS TOP 10 SQLite ===")


            // Limpiar primero
            ControladorSQLite.eliminarTodosRecords(getApplication())


            // Insertar datos de prueba
            android.util.Log.d("Top10_Test", "Insertando scores de prueba...")
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 5)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 8)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 3)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 12)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 7)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 15)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 9)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 6)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 10)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 4)
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 11)

            //este record no deberí entrar porq hay 10 mejores
            ControladorSQLite.guardarScoreYLimitarTop10(getApplication(), 2)


            //Mostrar top 10
            mostrarTop10EnLogcat()


            android.util.Log.d("Top10_Test", "=== PRUEBAS COMPLETADAS ===")
        }
    }
}