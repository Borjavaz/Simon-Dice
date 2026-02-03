package gz.dam.simon_dice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RecordRepository(private val recordDao: RecordDao) {

    suspend fun getRecord(): RecordEntity? {
        return recordDao.getRecord()
    }

    suspend fun getRecordScore(): Int {
        return recordDao.getRecordScore() ?: 0
    }

    suspend fun saveRecord(score: Int) {
        val record = RecordEntity(id = 1, score = score)
        recordDao.insertRecord(record)
    }

    fun getRecordFlow(): Flow<RecordEntity?> = flow {
        val record = recordDao.getRecord()
        emit(record)
    }

    suspend fun clearRecord() {
        recordDao.deleteAll()
    }
}