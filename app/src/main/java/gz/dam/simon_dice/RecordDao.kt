package gz.dam.simon_dice

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("SELECT * FROM records ORDER BY score DESC, timestamp ASC")
    suspend fun getAllRecords(): List<RecordEntity>


    @Query("SELECT * FROM records WHERE playerName = :playerName ORDER BY score DESC")
    suspend fun getRecordsByPlayer(playerName: String): List<RecordEntity>


    @Query("SELECT * FROM records ORDER BY score DESC LIMIT 1")
    suspend fun getBestRecord(): RecordEntity?


    @Query("SELECT playerName, MAX(score) as bestScore FROM records GROUP BY playerName ORDER BY bestScore DESC LIMIT 1")
    suspend fun getBestPlayerRecord(): PlayerBestRecord?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity)

    @Update
    suspend fun updateRecord(record: RecordEntity)


    @Query("DELETE FROM records")
    suspend fun deleteAll()


    @Query("SELECT COUNT(*) FROM records")
    suspend fun getRecordCount(): Int


    //query que me vale para mantener solo los 10 mejores records
    @Query("DELETE FROM records WHERE id NOT IN (SELECT id FROM records ORDER BY score DESC, timestamp ASC LIMIT 10)")
    suspend fun keepOnlyTop10()


    //Flow para observar el mejor record
    @Query("SELECT * FROM records ORDER BY score DESC LIMIT 1")
    fun observeBestRecord(): Flow<RecordEntity?>
}

// clase para obtener el mejor record por jugador
data class PlayerBestRecord(
    val playerName: String,
    val bestScore: Int
)