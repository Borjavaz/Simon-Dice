package gz.dam.simon_dice

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // Solo guardaremos un récord, siempre con ID = 1
    val score: Int,
    val timestamp: Long = Date().time
)