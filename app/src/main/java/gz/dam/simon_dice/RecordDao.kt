package gz.dam.simon_dice

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDao {

    @Query("SELECT * FROM records WHERE id = 1")
    suspend fun getRecord(): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity)

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Query("SELECT score FROM records WHERE id = 1")
    suspend fun getRecordScore(): Int?

    @Query("DELETE FROM records")
    suspend fun deleteAll()
}