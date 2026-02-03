package gz.dam.simon_dice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import gz.dam.simon_dice.ui.theme.SimonDiceTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SimonDiceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    // ViewModels
                    val miViewModel: MiViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = MiViewModelFactory(application)
                    )
                    val gameViewModel: VM = androidx.lifecycle.viewmodel.compose.viewModel()

                    // Conectar ViewModels
                    gameViewModel.setRecordViewModel(miViewModel)

                    // UI sin modifier (usa padding interno en UI.kt)
                    SimonDiceUI(
                        gameViewModel = gameViewModel,
                        miViewModel = miViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}