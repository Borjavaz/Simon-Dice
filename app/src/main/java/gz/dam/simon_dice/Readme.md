# Implementacion en Room para guardar el nombre del jugador

## 1. RecordEntity

Campo playerName agregado le puse el valor de: "Borja"

id ahora auto-generado para múltiples records:

```kotlin
@PrimaryKey(autoGenerate = true){}
```

## 2. RecordDAO

Nuevas consultas para manejar nombres


Query para mantener solo top 10

```kotlin
@Query("DELETE FROM records WHERE id NOT IN (SELECT id FROM records ORDER BY score DESC, timestamp ASC LIMIT 10)")
suspend fun keepOnlyTop10()
```

Flow para observar el mejor record:

```kotlin
@Query("SELECT * FROM records ORDER BY score DESC LIMIT 1")
fun observeBestRecord(): Flow<RecordEntity?>
```

## 3. RecordRepository

Manejo del nombre del jugador


Lógica de top 10 integrada


Métodos para diferentes operaciones con nombres

## 4. MiViewModel

Estados para nombre e info del jugador


Métodos para cambiar nombre


Observación reactiva del mejor record


## Comprobacion

Nombre se genera aleatoriamente al iniciar la app


Se puede cambiar manualmente con changePlayerName()


Nombre Mostrado Junto al Record:

interfaz:
![img.png](img.png)

actualizacion de logs cuando se va consiguiendo un record:
![img_1.png](img_1.png)

aunq esta ronda la esta jugando Isabel_7 le avisa que Laura_99 tiene un recor de 5
![img_2.png](img_2.png)

Si quiero cambiar manualmente el nombre del jugador solo tengo que modificar esta linea de codigo en MyViewModel:
```kotlin
private val _playerName = MutableStateFlow("Borja")
```

YYYY comentar esto en MyViewModel:
```kotlin
//nombre aleatorio al iniciar
setupRandomPlayerName()
```

Ya que si no se genera un nombre aleatorio



