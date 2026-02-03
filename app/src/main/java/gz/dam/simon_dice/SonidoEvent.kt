package gz.dam.simon_dice

/**
 * Eventos de sonido para la UI
 */
sealed class SonidoEvent {
    data class ColorSound(val colorInt: Int) : SonidoEvent()
    object Error : SonidoEvent()
    object Victory : SonidoEvent()
}