package gz.dam.simon_dice

/**
 * Clase sellada que representa todos los estados posibles del juego.
 * Documentación: https://developer.android.com/kotlin/learn#sealed-classes
 */
sealed class GameState {
    object Inicio : GameState()
    object Preparando : GameState()
    object MostrandoSecuencia : GameState()
    object EsperandoJugador : GameState()
    object ProcesandoInput : GameState()
    object SecuenciaCorrecta : GameState()
    data class GameOver(val rondaAlcanzada: Int) : GameState()
}