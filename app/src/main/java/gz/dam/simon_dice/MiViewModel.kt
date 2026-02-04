package gz.dam.simon_dice


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MiViewModel(application: Application) : AndroidViewModel(application) {


    private val database = AppDatabase.getDatabase(application)
    private val repository = RecordRepository(database.recordDao())


    // estados para el record
    private val _record = MutableStateFlow(0)
    val record: StateFlow<Int> = _record.asStateFlow()
    private val _recordTexto = MutableStateFlow("Sin récord")
    val recordTexto: StateFlow<String> = _recordTexto.asStateFlow()
    private val _recordParaRecuadro = MutableStateFlow("0")
    val recordParaRecuadro: StateFlow<String> = _recordParaRecuadro.asStateFlow()


    //Estados para el nombre del jugador
    private val _playerName = MutableStateFlow("Borja")
    val playerName: StateFlow<String> = _playerName.asStateFlow()
    private val _playerInfo = MutableStateFlow("Jugador: Sin datos")
    val playerInfo: StateFlow<String> = _playerInfo.asStateFlow()


    init {

        //nombre aleatorio al iniciar
        setupRandomPlayerName()


        //cargo record guardado
        cargarRecordGuardado()


        //miro cambios en el mejor record
        observarMejorRecord()
    }


    private fun setupRandomPlayerName() {
        // Generao nombre aleatorio cada vez que se inicia la app
        val randomNames = listOf(
            "Ana", "Carlos", "Pedro", "Elena", "Diego",
            "Gloria", "Pablo", "Isabel", "Javier", "Laura"
        )
        val randomIndex = (System.currentTimeMillis() % randomNames.size).toInt()
        val randomName = "${randomNames[randomIndex]}_${(System.currentTimeMillis() % 100)}"


        repository.changePlayerName(randomName)
        _playerName.value = randomName
        _playerInfo.value = "Jugando como: $randomName"


        android.util.Log.d("Room_Player", "Nombre del jugador asignado: $randomName")
    }


    private fun cargarRecordGuardado() {
        viewModelScope.launch {
            val bestRecord = repository.getBestRecord()


            if (bestRecord != null) {
                _record.value = bestRecord.score
                _recordParaRecuadro.value = bestRecord.score.toString()


                val date = Date(bestRecord.timestamp)
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)


                // Muestro el nombre del jugador junto al record
                _recordTexto.value = "${bestRecord.playerName}: ${bestRecord.score} ($fecha $hora)"
            } else {
                _record.value = 0
                _recordParaRecuadro.value = "0"
                _recordTexto.value = "Sin récord"
            }
        }
    }


    private fun observarMejorRecord() {
        viewModelScope.launch {
            repository.observeBestRecord().collectLatest { bestRecord ->
                bestRecord?.let { record ->
                    _record.value = record.score
                    _recordParaRecuadro.value = record.score.toString()


                    val date = Date(record.timestamp)
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)


                    // actualizo con nombre del jugador
                    _recordTexto.value = "${record.playerName}: ${record.score} ($fecha $hora)"
                }
            }
        }
    }


    fun verificarYActualizarRecord(posibleRecord: Int): Boolean {
        viewModelScope.launch {
            //Guardar record con el nombre del jugador actual
            val estaEnTop10 = repository.saveRecordWithCurrentPlayer(posibleRecord)


            if (estaEnTop10) {
                // recargar el mejor record
                cargarRecordGuardado()


                //log para top 10
                android.util.Log.i("Room_Top10_Msg",
                    "¡${_playerName.value} consiguió un score en TOP 10! Puntos: $posibleRecord")
            }


            //actualizo el record en local, si es mejor
            if (posibleRecord > _record.value) {
                _record.value = posibleRecord
                _recordParaRecuadro.value = posibleRecord.toString()


                // actualizar texto con nombre del jugador
                val now = Date()
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                _recordTexto.value = "${_playerName.value}: $posibleRecord ($fecha $hora)"
            }
        }
        return true
    }
}

