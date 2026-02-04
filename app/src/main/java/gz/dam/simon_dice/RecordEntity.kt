package gz.dam.simon_dice


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "records")
data class RecordEntity(
    //lo cambié a autoGenerate para permitir múltiples records
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val timestamp: Long = Date().time,
    //nuevo nombre del jugador
    val playerName: String = "Borja",
)

