package gz.dam.simon_dice

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application

@Composable
fun SimonDiceUI(
    gameViewModel: VM = viewModel(),
    miViewModel: MiViewModel = viewModel(factory = MiViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current

    // Estados del juego
    val gameState by gameViewModel.gameState.collectAsState()
    val ronda by gameViewModel.ronda.collectAsState()
    val record by gameViewModel.record.collectAsState()
    val text by gameViewModel.text.collectAsState()
    val colorActivo by gameViewModel.colorActivo.collectAsState()
    val botonesBrillantes by gameViewModel.botonesBrillantes.collectAsState()

    // Record persistente
    val recordPersistente by miViewModel.recordTexto.collectAsState()
    val recordPersistenteRecuadro by miViewModel.recordParaRecuadro.collectAsState()

    // NUEVO: Información de SQLite
    val dbInfo by miViewModel.dbInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // MODIFICADO: Pasamos dbInfo también
        HeaderInfo(ronda, recordPersistenteRecuadro, text, gameState, recordPersistente, dbInfo)
        BotonesColores(gameViewModel, colorActivo, botonesBrillantes, gameState)
        BotonControl(gameViewModel, gameState)

    }
}

@Composable
fun HeaderInfo(
    ronda: Int,
    record: String,
    text: String,
    gameState: GameState,
    recordPersistente: String,
    dbInfo: String // NUEVO parámetro
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "SIMÓN DICE (SQLite)",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // NUEVO: Información de la base de datos
        Text(
            text = dbInfo,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Record persistente
        Text(
            text = recordPersistente,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InfoBox("RONDA", ronda.toString())
            InfoBox("RÉCORD", record)
            InfoBox("ESTADO", when (gameState) {
                is GameState.Inicio -> "INICIO"
                is GameState.Preparando -> "PREPARADO"
                is GameState.MostrandoSecuencia -> "OBSERVA"
                is GameState.EsperandoJugador -> "TU TURNO"
                is GameState.ProcesandoInput -> "PROCESANDO"
                is GameState.SecuenciaCorrecta -> "¡BIEN!"
                is GameState.GameOver -> "GAME OVER"
            })
        }
    }
}


@Composable
fun InfoBox(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .border(2.dp, Color.DarkGray)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = valor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BotonesColores(
    viewModel: VM,
    colorActivo: Int,
    botonesBrillantes: Boolean,
    gameState: GameState
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            BotonColor(
                viewModel = viewModel,
                color = Colores.ROJO,
                colorActivo = colorActivo,
                enabled = botonesBrillantes,
                gameState = gameState
            )
            Spacer(modifier = Modifier.width(20.dp))
            BotonColor(
                viewModel = viewModel,
                color = Colores.VERDE,
                colorActivo = colorActivo,
                enabled = botonesBrillantes,
                gameState = gameState
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            BotonColor(
                viewModel = viewModel,
                color = Colores.AMARILLO,
                colorActivo = colorActivo,
                enabled = botonesBrillantes,
                gameState = gameState
            )
            Spacer(modifier = Modifier.width(20.dp))
            BotonColor(
                viewModel = viewModel,
                color = Colores.AZUL,
                colorActivo = colorActivo,
                enabled = botonesBrillantes,
                gameState = gameState
            )
        }
    }
}

@Composable
fun BotonColor(
    viewModel: VM,
    color: Colores,
    colorActivo: Int,
    enabled: Boolean,
    gameState: GameState
) {
    val estaActivo = colorActivo == color.colorInt

    // USANDO TUS COLORES EXACTOS
    val colorBoton = when {
        estaActivo || enabled -> when (color) {
            Colores.ROJO -> Color(0xFFE53935)     // SimonRed
            Colores.VERDE -> Color(0xFF43A047)   // SimonGreen
            Colores.AZUL -> Color(0xFF1E88E5)    // SimonBlue
            Colores.AMARILLO -> Color(0xFFFDD835) // SimonYellow
        }
        else -> when (color) {
            Colores.ROJO -> Color(0xFFB71C1C)     // SimonRedDark
            Colores.VERDE -> Color(0xFF1B5E20)   // SimonGreenDark
            Colores.AZUL -> Color(0xFF0D47A1)    // SimonBlueDark
            Colores.AMARILLO -> Color(0xFFF57F17) // SimonYellowDark
        }
    }

    Button(
        onClick = { viewModel.procesarClickUsuario(color.colorInt) },
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorBoton,
            disabledContainerColor = colorBoton
        ),
        modifier = Modifier
            .size(140.dp)
            .border(4.dp, Color.Black, CircleShape)
    ) {
        // Botón sin contenido adicional
    }
}

@Composable
fun BotonControl(viewModel: VM, gameState: GameState) {
    val textoBoton = when (gameState) {
        is GameState.Inicio, is GameState.GameOver -> "START"
        else -> "RESTART"
    }

    Button(
        onClick = {
            when (gameState) {
                is GameState.Inicio, is GameState.GameOver -> viewModel.comenzarJuego()
                else -> viewModel.reiniciarJuego()
            }
        },
        modifier = Modifier
            .width(200.dp)
            .height(60.dp)
    ) {
        Text(
            text = textoBoton,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


