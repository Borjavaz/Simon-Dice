package gz.dam.simon_dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel del juego Simon Dice
 */
class VM : ViewModel() {

    // Estados reactivos usando StateFlow
    private val _gameState = MutableStateFlow<GameState>(GameState.Inicio)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _ronda = MutableStateFlow(0)
    val ronda: StateFlow<Int> = _ronda.asStateFlow()

    private val _record = MutableStateFlow(0)
    val record: StateFlow<Int> = _record.asStateFlow()

    private val _text = MutableStateFlow("PRESIONA START")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _colorActivo = MutableStateFlow(-1)
    val colorActivo: StateFlow<Int> = _colorActivo.asStateFlow()

    private val _botonesBrillantes = MutableStateFlow(false)
    val botonesBrillantes: StateFlow<Boolean> = _botonesBrillantes.asStateFlow()

    private val _sonidoEvent = MutableStateFlow<SonidoEvent?>(null)
    val sonidoEvent: StateFlow<SonidoEvent?> = _sonidoEvent.asStateFlow()

    // Para record persistente
    private var recordViewModel: MiViewModel? = null

    // Velocidades para mejor visibilidad
    private val velocidadMostrarColor = 800L
    private val velocidadPausaEntreColores = 400L
    private val velocidadTiempoApagado = 200L
    private val velocidadPausaEntreRondas = 1200L

    // Secuencias del juego
    private val secuencia = mutableListOf<Int>()
    private val secuenciaUsuario = mutableListOf<Int>()

    init {
        // Inicializar el juego usando Datos
        Datos.reiniciarJuego()
    }

    // Conectar con MiViewModel
    fun setRecordViewModel(viewModel: MiViewModel) {
        this.recordViewModel = viewModel
    }

    fun generaNumero(): Int = (0..3).random()

    fun comenzarJuego() {
        if (_gameState.value == GameState.Inicio || _gameState.value is GameState.GameOver) {
            reiniciarDatos()
            _gameState.value = GameState.Preparando
            _text.value = "PREPARADO..."
            _botonesBrillantes.value = false

            viewModelScope.launch {
                delay(1000)
                comenzarNuevaRonda()
            }
        }
    }

    private fun reiniciarDatos() {
        secuencia.clear()
        secuenciaUsuario.clear()
        _ronda.value = 0
        _colorActivo.value = -1
        _botonesBrillantes.value = false
        Datos.reiniciarJuego()
    }

    private fun comenzarNuevaRonda() {
        viewModelScope.launch {
            _gameState.value = GameState.MostrandoSecuencia
            _text.value = "OBSERVA LA SECUENCIA"
            _botonesBrillantes.value = false

            delay(500)

            // Agregar nuevo color a la secuencia
            val nuevoColor = generaNumero()
            secuencia.add(nuevoColor)
            _ronda.value = secuencia.size

            // Mostrar secuencia completa
            mostrarSecuenciaCompleta()
        }
    }

    private suspend fun mostrarSecuenciaCompleta() {
        for ((index, colorInt) in secuencia.withIndex()) {
            //ILUMINA Y DISPARA EVENTO DE SONIDO
            _colorActivo.value = colorInt
            _sonidoEvent.value = SonidoEvent.ColorSound(colorInt)
            delay(velocidadMostrarColor)

            // APAGA
            _colorActivo.value = -1

            //PAUSA (Tiempo de apagado mínimo)
            delay(velocidadTiempoApagado)

            // PAUSA ENTRE COLORES (Solo si no es el último)
            if (index < secuencia.size - 1) {
                delay(velocidadPausaEntreColores)
            }
        }

        delay(500)
        prepararTurnoJugador()
    }

    private fun prepararTurnoJugador() {
        secuenciaUsuario.clear()
        _gameState.value = GameState.EsperandoJugador
        _text.value = "TU TURNO - REPITE LA SECUENCIA"
        _botonesBrillantes.value = true
    }

    fun procesarClickUsuario(colorInt: Int) {
        if (_gameState.value != GameState.EsperandoJugador) return

        viewModelScope.launch {
            _gameState.value = GameState.ProcesandoInput
            _botonesBrillantes.value = false

            // ILUMINA Y DISPARA EVENTO DE SONIDO
            _colorActivo.value = colorInt
            _sonidoEvent.value = SonidoEvent.ColorSound(colorInt)
            delay(400)
            _colorActivo.value = -1

            secuenciaUsuario.add(colorInt)
            verificarSecuenciaUsuario()
        }
    }

    private fun verificarSecuenciaUsuario() {
        val indiceActual = secuenciaUsuario.size - 1

        if (secuenciaUsuario[indiceActual] != secuencia[indiceActual]) {
            _sonidoEvent.value = SonidoEvent.Error
            verificarRecordPersistente()
            gameOver()
            return
        }

        if (secuenciaUsuario.size == secuencia.size) {
            _sonidoEvent.value = SonidoEvent.Victory
            verificarRecordPersistente()
            secuenciaCorrecta()
        } else {
            _gameState.value = GameState.EsperandoJugador
            _text.value = "CONTINÚA... ${secuenciaUsuario.size}/${secuencia.size}"
            _botonesBrillantes.value = true
        }
    }

    private fun verificarRecordPersistente() {
        recordViewModel?.let { mvvm ->
            if (mvvm.verificarYActualizarRecord(_ronda.value)) {
                _record.value = _ronda.value  // Actualiza también el record local
            }
        }
    }

    private fun secuenciaCorrecta() {
        viewModelScope.launch {
            _gameState.value = GameState.SecuenciaCorrecta
            _text.value = "¡BIEN! SIGUIENTE RONDA"

            if (_ronda.value > _record.value) {
                _record.value = _ronda.value
            }

            efectoCelebracion()

            delay(velocidadPausaEntreRondas)

            comenzarNuevaRonda()
        }
    }

    private suspend fun efectoCelebracion() {
        repeat(2) {
            for (i in 0..3) {
                _colorActivo.value = i
                _sonidoEvent.value = SonidoEvent.ColorSound(i)
                delay(150)
            }
            _colorActivo.value = -1
            delay(200)
        }
    }

    private fun gameOver() {
        viewModelScope.launch {
            _gameState.value = GameState.GameOver(_ronda.value)
            _text.value = "GAME OVER - RONDA ${_ronda.value}"
            _botonesBrillantes.value = false

            delay(2000)
            _text.value = "RÉCORD: ${_record.value} - PRESIONA START"
        }
    }

    fun reiniciarJuego() {
        if (_gameState.value != GameState.Inicio) {
            viewModelScope.launch {
                _gameState.value = GameState.GameOver(_ronda.value)
                _text.value = "JUEGO REINICIADO"
                _botonesBrillantes.value = false

                // Efecto visual de reinicio
                repeat(2) {
                    for (i in 0..3) {
                        _colorActivo.value = i
                        _sonidoEvent.value = SonidoEvent.ColorSound(i)
                        delay(150)
                    }
                    _colorActivo.value = -1
                    delay(200)
                }

                _text.value = "RÉCORD: ${_record.value} - PRESIONA START"
            }
        }
    }

    fun clearSoundEvent() {
        _sonidoEvent.value = null
    }
}

/*
📁 ARCHIVOS NUEVOS (Room):
AppDatabase.kt - Configuración principal de Room Database

RecordEntity.kt - Entidad de datos con @Entity (tabla "records")

RecordDao.kt - DAO con consultas SQL (CRUD operations)

RecordRepository.kt - Patrón Repository (capa de abstracción)

🔄 ARCHIVO MODIFICADO CRÍTICO:
MiViewModel.kt:
ANTES:

kotlin
val (score, timestamp) = ControladorPreference.obtenerRecordCompleto(getApplication())
AHORA:

kotlin
private val database = AppDatabase.getDatabase(application)
private val repository = RecordRepository(database.recordDao())
val recordEntity = repository.getRecord()
❌ ARCHIVO ELIMINADO:
ControladorPreference.kt - Eliminado completamente

🏗️ ARQUITECTURA NUEVA:
text
ViewModel → Repository → DAO → Room Database (SQLite)
⚙️ CAMBIO EN EL FLUJO DE DATOS:
Operación	Master (SharedPreferences)	Room
Guardar	ControladorPreference.actualizarRecord()	repository.saveRecord()
Cargar	ControladorPreference.obtenerRecordCompleto()	repository.getRecord()
Base	Archivo XML	Base de datos SQLite
✅ LO QUE NO CAMBIA:
La interfaz del ViewModel (verificarYActualizarRecord())

La UI (Compose) - no hay cambios visuales

La lógica del juego en VM.kt

La experiencia del usuario

🎯 CAMBIO REAL:
Solo la capa de persistencia - de un simple archivo XML (SharedPreferences) a una base de datos SQLite con ORM (Room).

RESUMEN FINAL: Se reemplazó SharedPreferences por Room manteniendo 100% de la funcionalidad existente. Solo cambia la implementación interna de cómo se guardan los datos.
 */