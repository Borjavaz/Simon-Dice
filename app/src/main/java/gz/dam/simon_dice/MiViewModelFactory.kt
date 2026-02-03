package gz.dam.simon_dice

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MiViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MiViewModel::class.java)) {
            return MiViewModel(application) as T
        }
            throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}