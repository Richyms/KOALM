package com.example.koalm.repository

import android.content.Context
import android.util.Log
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.services.NotificationService
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.services.notifications.ReadingNotificationService
import com.example.koalm.services.notifications.WritingNotificationService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HabitoRepository {
    private val TAG = "HabitoRepository"
    private val db = FirebaseFirestore.getInstance()
    private val habitosCollection = db.collection("habitos")

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
                        fechaModificacion = data["fechaModificacion"] as? String
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
                fechaModificacion = data["fechaModificacion"] as? String
            )
            
            Log.d(TAG, "Hábito obtenido exitosamente: ${habito.titulo}")
            Result.success(habito)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener hábito: ${e.message}", e)
            Result.failure(e)
        }
    }
} 