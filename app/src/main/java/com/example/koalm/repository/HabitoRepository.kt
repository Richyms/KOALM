package com.example.koalm.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.MetricasHabito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.example.koalm.services.timers.ReadingTimerService
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.services.notifications.ReadingNotificationService
import com.example.koalm.services.notifications.WritingNotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HabitoRepository {
    private val TAG = "HabitoRepository"
    private val db = FirebaseFirestore.getInstance()
    private val habitosCollection = db.collection("habitos")

    private val userEmail = FirebaseAuth.getInstance().currentUser?.email
        ?: throw IllegalStateException("Usuario no autenticado")

    suspend fun crearHabito(habito: Habito): Result<String> = try {
        Log.d(TAG, "Iniciando creación de hábito: ${habito.titulo}")
        Log.d(TAG, "Datos del hábito: userId=${habito.userId}, tipo=${habito.tipo}, clase=${habito.clase}")

        val habitoConFechas = habito.copy(
            fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            fechaModificacion = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        )

        Log.d(TAG, "Datos a guardar en Firebase: ${habitoConFechas.toMap()}")

        val docRef = habitosCollection.add(habitoConFechas.toMap()).await()
        Log.d(TAG, "Hábito creado exitosamente con ID: ${docRef.id}")
        Result.success(docRef.id)
    } catch (e: Exception) {
        Log.e(TAG, "Error al crear hábito: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun obtenerHabitosActivos(userId: String): Result<List<Habito>> = try {
        Log.d(TAG, "Iniciando búsqueda de hábitos para userId: $userId")

        val query = habitosCollection
            .whereEqualTo("userId", userId)

        Log.d(TAG, "Ejecutando query: ${query.toString()}")

        val snapshot = query.get().await()
        Log.d(TAG, "Query completada. Documentos encontrados: ${snapshot.documents.size}")

        val habitos = snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data
                if (data != null) {
                    Log.d(TAG, "Procesando documento ${doc.id}: $data")
                    Habito(
                        id = doc.id,
                        titulo = data["titulo"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        clase = try {
                            ClaseHabito.valueOf(data["clase"] as? String ?: ClaseHabito.MENTAL.name)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al convertir clase: ${data["clase"]}", e)
                            ClaseHabito.MENTAL
                        },
                        tipo = try {
                            TipoHabito.valueOf(data["tipo"] as? String ?: TipoHabito.ESCRITURA.name)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al convertir tipo: ${data["tipo"]}", e)
                            TipoHabito.ESCRITURA
                        },
                        diasSeleccionados = (data["diasSeleccionados"] as? List<*>)?.map { it as Boolean } ?: List(7) { false },
                        hora = data["hora"] as? String ?: "",
                        duracionMinutos = (data["duracionMinutos"] as? Number)?.toInt() ?: 15,
                        notasHabilitadas = data["notasHabilitadas"] as? Boolean ?: false,
                        userId = data["userId"] as? String,
                        fechaCreacion = data["fechaCreacion"] as? String,
                        fechaModificacion = data["fechaModificacion"] as? String,
                        objetivoPaginas = (data["objetivoPaginas"] as? Number)?.toInt() ?: 0,
                        objetivoHorasSueno = (data["objetivoHorasSueno"] as? Number)?.toFloat() ?: 8f,
                        metricasEspecificas = MetricasHabito()
                    )
                } else {
                    Log.w(TAG, "Documento ${doc.id} tiene datos nulos")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar documento ${doc.id}: ${e.message}", e)
                null
            }
        }

        val habitosOrdenados = habitos.sortedByDescending { it.fechaCreacion }

        Log.d(TAG, "Procesamiento completado. Hábitos válidos: ${habitosOrdenados.size}")
        Result.success(habitosOrdenados)
    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener hábitos: ${e.message}", e)
        Result.failure(e)
    }
    suspend fun obtenerHabitosMentales(userId: String): Result<List<Habito>> {
        return obtenerHabitosPorClase(userId, ClaseHabito.MENTAL)
    }

    suspend fun obtenerHabitosFisicos(userId: String): Result<List<Habito>> {
        return obtenerHabitosPorClase(userId, ClaseHabito.FISICO)
    }

    private suspend fun obtenerHabitosPorClase(userId: String, clase: ClaseHabito): Result<List<Habito>> {
        return try {
            Log.d(TAG, "Buscando hábitos de clase ${clase.name} para userId: $userId")

            val query = habitosCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("clase", clase.name)

            Log.d(TAG, "Ejecutando query filtrada: ${query.toString()}")

            val snapshot = query.get().await()
            Log.d(TAG, "Query completada. Documentos encontrados: ${snapshot.documents.size}")

            val habitos = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Log.d(TAG, "Procesando documento ${doc.id}: $data")

                    Habito(
                        id = doc.id,
                        titulo = data["titulo"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        clase = clase, // Ya sabemos que es de esta clase
                        tipo = try {
                            TipoHabito.valueOf(data["tipo"] as? String ?: throw Exception("Tipo no especificado"))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al convertir tipo: ${data["tipo"]}", e)
                            when(clase) {
                                ClaseHabito.MENTAL -> TipoHabito.ESCRITURA
                                ClaseHabito.FISICO -> TipoHabito.SUEÑO
                                else -> TipoHabito.ESCRITURA
                            }
                        },
                        diasSeleccionados = (data["diasSeleccionados"] as? List<*>)?.map { it as Boolean } ?: List(7) { false },
                        hora = data["hora"] as? String ?: "",
                        duracionMinutos = (data["duracionMinutos"] as? Number)?.toInt() ?: 15,
                        notasHabilitadas = data["notasHabilitadas"] as? Boolean ?: false,
                        userId = data["userId"] as? String,
                        fechaCreacion = data["fechaCreacion"] as? String,
                        fechaModificacion = data["fechaModificacion"] as? String,
                        objetivoPaginas = (data["objetivoPaginas"] as? Number)?.toInt() ?: 0,
                        objetivoHorasSueno = (data["objetivoHorasSueno"] as? Number)?.toFloat() ?: 8f,
                        metricasEspecificas = MetricasHabito()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar documento ${doc.id}: ${e.message}", e)
                    null
                }
            }

            val habitosOrdenados = habitos.sortedByDescending { it.fechaCreacion }
            Log.d(TAG, "Procesamiento completado. Hábitos ${clase.name} válidos: ${habitosOrdenados.size}")
            Result.success(habitosOrdenados)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener hábitos ${clase.name}: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun eliminarHabito(habitoId: String): Result<Unit> = try {
        Log.d(TAG, "Eliminando hábito $habitoId")
        habitosCollection.document(habitoId).delete().await()
        Log.d(TAG, "Hábito eliminado exitosamente")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar hábito: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun actualizarHabito(habito: Habito): Result<Unit> = try {
        Log.d(TAG, "Actualizando hábito ${habito.id}")
        val habitoActualizado = habito.copy(
            fechaModificacion = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        )
        habitosCollection.document(habito.id)
            .set(habitoActualizado.toMap())
            .await()
        Log.d(TAG, "Hábito actualizado exitosamente")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar hábito: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun obtenerHabito(habitoId: String): Result<Habito> {
        return try {
            Log.d(TAG, "Obteniendo hábito con ID: $habitoId")
            val doc = habitosCollection.document(habitoId).get().await()

            if (!doc.exists()) {
                Log.e(TAG, "Hábito no encontrado: $habitoId")
                return Result.failure(Exception("Hábito no encontrado"))
            }

            val data = doc.data
            if (data == null) {
                Log.e(TAG, "Datos del hábito son nulos: $habitoId")
                return Result.failure(Exception("Datos del hábito son nulos"))
            }
            
            val habito = Habito(
                id = doc.id,
                titulo = data["titulo"] as? String ?: "",
                descripcion = data["descripcion"] as? String ?: "",
                clase = try {
                    ClaseHabito.valueOf(data["clase"] as? String ?: ClaseHabito.MENTAL.name)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir clase: ${data["clase"]}", e)
                    ClaseHabito.MENTAL
                },
                tipo = try {
                    TipoHabito.valueOf(data["tipo"] as? String ?: TipoHabito.ESCRITURA.name)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir tipo: ${data["tipo"]}", e)
                    TipoHabito.ESCRITURA
                },
                diasSeleccionados = (data["diasSeleccionados"] as? List<*>)?.map { it as Boolean } ?: List(7) { false },
                hora = data["hora"] as? String ?: "",
                duracionMinutos = (data["duracionMinutos"] as? Number)?.toInt() ?: 15,
                notasHabilitadas = data["notasHabilitadas"] as? Boolean ?: false,
                userId = data["userId"] as? String,
                fechaCreacion = data["fechaCreacion"] as? String,
                fechaModificacion = data["fechaModificacion"] as? String,
                objetivoPaginas = (data["objetivoPaginas"] as? Number)?.toInt() ?: 0,
                objetivoHorasSueno = (data["objetivoHorasSueno"] as? Number)?.toFloat() ?: 8f,
                metricasEspecificas = MetricasHabito()
            )

            Log.d(TAG, "Hábito obtenido exitosamente: ${habito.titulo}")
            Result.success(habito)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener hábito: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        val db = FirebaseFirestore.getInstance()
        
        private suspend fun actualizarProgresoAcumulado(email: String, habito: Habito, incremento: Int) {
            val db = FirebaseFirestore.getInstance()
            val progresoMentalRef = db.collection("usuarios")
                .document(email)
                .collection("progresohabitomental")
                .document(habito.tipo.name.lowercase())

            try {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(progresoMentalRef)
                    val progresoActual = snapshot.getLong("total") ?: 0L
                    val metricaEspecifica = when (habito.tipo) {
                        TipoHabito.LECTURA -> "minutosLeidos"
                        TipoHabito.ESCRITURA -> "paginasEscritas"
                        TipoHabito.MEDITACION -> "minutosMeditados"
                        TipoHabito.DESCONEXION_DIGITAL -> "minutosDesconectado"
                        else -> null
                    }

                    val datosActualizados = mutableMapOf<String, Any>(
                        "total" to progresoActual + incremento,
                        "ultimaActualizacion" to LocalDate.now().toString(),
                        "tipoHabito" to habito.tipo.name
                    )

                    if (metricaEspecifica != null) {
                        val metricaActual = snapshot.getLong(metricaEspecifica) ?: 0L
                        datosActualizados[metricaEspecifica] = metricaActual + incremento
                    }

                    transaction.set(
                        progresoMentalRef,
                        datosActualizados,
                        SetOptions.merge()
                    )
                }.await()
                Log.d("HabitoRepository", "Progreso acumulado actualizado para ${habito.tipo.name}")
            } catch (e: Exception) {
                Log.e("HabitoRepository", "Error actualizando progreso acumulado: ${e.message}")
            }
        }

        // Incrementar el progreso de un hábito
        suspend fun incrementarProgresoHabito(email: String, habito: Habito, valor: Int) {
            val progresoRef = db.collection("habitos")
                .document(email)
                .collection("predeterminados")
                .document(habito.id)
                .collection("progreso")
                .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

            val snapshot = progresoRef.get().await()
            val progresoActual = snapshot.toObject(ProgresoDiario::class.java)

            // Inicializamos si no existía
            val nuevoProgreso = progresoActual ?: ProgresoDiario(
                realizados = 0,
                completado = false,
                totalObjetivoDiario = when (habito.tipo) {
                    TipoHabito.ESCRITURA -> habito.objetivoPaginas
                    TipoHabito.SUEÑO -> habito.objetivoHorasSueno.toInt()
                    else -> 1
                }
            )

            // No hacer nada si ya estaba completo
            if (nuevoProgreso.completado) return

            nuevoProgreso.realizados += valor
            nuevoProgreso.completado = when (habito.tipo) {
                TipoHabito.ESCRITURA -> nuevoProgreso.realizados >= habito.objetivoPaginas
                TipoHabito.SUEÑO -> nuevoProgreso.realizados >= habito.objetivoHorasSueno
                else -> nuevoProgreso.realizados >= 1
            }

            // Si es un hábito mental o físico, actualizamos el progreso acumulado
            if (habito.clase == ClaseHabito.MENTAL || habito.clase == ClaseHabito.FISICO) {
                actualizarProgresoAcumulado(email, habito, valor)
            }

            // Actualizamos la racha si se completó hoy
            if (nuevoProgreso.completado) {
                actualizarRacha(habito, nuevoProgreso)
            }

            // Guardamos progreso
            progresoRef.set(nuevoProgreso, SetOptions.merge()).await()

            // Guardamos cambios del hábito (racha y último día)
            db.collection("habitos")
                .document(email)
                .collection("predeterminados")
                .document(habito.id)
                .set(habito, SetOptions.merge())
                .await()
        }

        // Actualizar racha del hábito
        private fun actualizarRacha(habito: Habito, progreso: ProgresoDiario) {
            val hoy = LocalDate.now()
            val ultimoDia = habito.ultimoDiaCompletado?.let { LocalDate.parse(it) }

            if (ultimoDia == null || !ultimoDia.plusDays(1).isEqual(hoy)) {
                habito.rachaActual = 1
            } else {
                habito.rachaActual += 1
            }

            if (habito.rachaActual > habito.rachaMaxima) {
                habito.rachaMaxima = habito.rachaActual
            }

            habito.ultimoDiaCompletado = hoy.toString()
        }
    }
    suspend fun guardarHabitoHidratacion(
        userId: String,
        descripcion: String,
        cantlitros: Float,
        horarios: List<String>,
        recordatorios: Boolean,
        frecuenciaActiva: Boolean,
        minutosFrecuencia: Int
    ): Boolean {
        return try {
            val datos = hashMapOf(
                "descripcion" to descripcion,
                "litros" to cantlitros,
                "horarios" to horarios,
                "recordatorios" to recordatorios,
                "frecuenciaActiva" to frecuenciaActiva,
                "minutosFrecuencia" to minutosFrecuencia
            )

            db.collection("habitosHidratacion")
                .document(userId)
                .set(datos)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}