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

class MiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = RecordRepository(database.recordDao())

    private val _record = MutableStateFlow(0)
    val record: StateFlow<Int> = _record.asStateFlow()

    private val _recordTexto = MutableStateFlow("Sin récord")
    val recordTexto: StateFlow<String> = _recordTexto.asStateFlow()

    private val _recordParaRecuadro = MutableStateFlow("0")
    val recordParaRecuadro: StateFlow<String> = _recordParaRecuadro.asStateFlow()

    init {
        cargarRecordGuardado()
    }

    private fun cargarRecordGuardado() {
        viewModelScope.launch {
            val recordEntity = repository.getRecord()

            if (recordEntity != null) {
                _record.value = recordEntity.score
                _recordParaRecuadro.value = recordEntity.score.toString()

                val date = Date(recordEntity.timestamp)
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                _recordTexto.value = "Récord: ${recordEntity.score} ($fecha $hora)"
            } else {
                _record.value = 0
                _recordParaRecuadro.value = "0"
                _recordTexto.value = "Sin récord"
            }
        }
    }

    fun verificarYActualizarRecord(posibleRecord: Int): Boolean {
        val recordActual = _record.value

        if (posibleRecord > recordActual) {
            viewModelScope.launch {
                repository.saveRecord(posibleRecord)
                _record.value = posibleRecord
                _recordParaRecuadro.value = posibleRecord.toString()

                val now = Date()
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                _recordTexto.value = "Récord: $posibleRecord ($fecha $hora)"
            }
            return true
        }
        return false
    }

    // Método para limpiar el récord (opcional)
    fun clearRecord() {
        viewModelScope.launch {
            repository.clearRecord()
            _record.value = 0
            _recordParaRecuadro.value = "0"
            _recordTexto.value = "Sin récord"
        }
    }
}