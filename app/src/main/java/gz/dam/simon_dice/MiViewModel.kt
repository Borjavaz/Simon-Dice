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
 * ViewModel para manejar el record persistente.
 */
class MiViewModel(application: Application) : AndroidViewModel(application) {

    private val _record = MutableStateFlow(0)
    val record: StateFlow<Int> = _record.asStateFlow()

    private val _recordTexto = MutableStateFlow("Sin récord")
    val recordTexto: StateFlow<String> = _recordTexto.asStateFlow()

    // AÑADIDO: Para mostrar en el recuadro de RÉCORD
    private val _recordParaRecuadro = MutableStateFlow("0")
    val recordParaRecuadro: StateFlow<String> = _recordParaRecuadro.asStateFlow()

    init {
        cargarRecordGuardado()
    }

    private fun cargarRecordGuardado() {
        viewModelScope.launch {
            val (score, timestamp) = ControladorPreference.obtenerRecordCompleto(getApplication())
            _record.value = score

            // AÑADIDO: Actualizar también el record para el recuadro
            _recordParaRecuadro.value = score.toString()

            if (timestamp > 0) {
                val date = Date(timestamp)
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                _recordTexto.value = "Récord: $score ($fecha $hora)"
            } else {
                _recordTexto.value = "Sin récord"
            }
        }
    }

    fun verificarYActualizarRecord(posibleRecord: Int): Boolean {
        val recordActual = ControladorPreference.obtenerRecordScore(getApplication())

        if (posibleRecord > recordActual) {
            ControladorPreference.actualizarRecord(getApplication(), posibleRecord)
            _record.value = posibleRecord

            // AÑADIDO: Actualizar también el record para el recuadro
            _recordParaRecuadro.value = posibleRecord.toString()

            val now = Date()
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
            val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            _recordTexto.value = "Récord: $posibleRecord ($fecha $hora)"

            return true
        }

        return false
    }
}