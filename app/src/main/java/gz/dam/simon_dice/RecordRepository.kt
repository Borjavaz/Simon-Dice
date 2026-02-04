package gz.dam.simon_dice


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class RecordRepository(private val recordDao: RecordDao) {


    //variable para el nombre de jugador, se puede cambiar cada vez que iniciamos la app
    var playerName: String = "Jugador_${System.currentTimeMillis() % 1000}"
        private set


    fun changePlayerName(newName: String) {
        playerName = newName
    }


    suspend fun getCurrentPlayerName(): String {
        return playerName
    }


    suspend fun saveRecordWithCurrentPlayer(score: Int): Boolean {
        return try {
            //creo el record con el nombre del jugador que esta jugando
            val record = RecordEntity(
                score = score,
                playerName = playerName
            )


            //inserto el record
            recordDao.insertRecord(record)


            // Mantengo solo los 10 mejores records
            recordDao.keepOnlyTop10()


            // Verifico si está en el top 10
            val allRecords = recordDao.getAllRecords()
            val position = allRecords.indexOfFirst {
                it.score == score && it.playerName == playerName
            } + 1


            val isInTop10 = position in 1..10


            if (isInTop10) {
                android.util.Log.i("Room_Top10",
                    "¡$playerName está en TOP 10! Score: $score - Posición: $position")
            }


            isInTop10
        } catch (e: Exception) {
            android.util.Log.e("Room_Repository", "Error al guardar record: ${e.message}")
            false
        }
    }


    suspend fun getBestRecord(): RecordEntity? {
        return recordDao.getBestRecord()
    }


    suspend fun getBestScore(): Int {
        return recordDao.getBestRecord()?.score ?: 0
    }


    suspend fun getBestRecordWithPlayer(): Pair<String, Int> {
        val bestRecord = recordDao.getBestRecord()
        return if (bestRecord != null) {
            Pair(bestRecord.playerName, bestRecord.score)
        } else {
            Pair("Sin jugador", 0)
        }
    }


    fun observeBestRecord(): Flow<RecordEntity?> {
        return recordDao.observeBestRecord()
    }


    suspend fun getAllRecords(): List<RecordEntity> {
        return recordDao.getAllRecords()
    }


    suspend fun getRecordCount(): Int {
        return recordDao.getRecordCount()
    }


    suspend fun clearAllRecords() {
        recordDao.deleteAll()
    }


    suspend fun getPlayerRecords(): List<RecordEntity> {
        return recordDao.getRecordsByPlayer(playerName)
    }


    suspend fun getBestPlayer(): PlayerBestRecord? {
        return recordDao.getBestPlayerRecord()
    }
}
