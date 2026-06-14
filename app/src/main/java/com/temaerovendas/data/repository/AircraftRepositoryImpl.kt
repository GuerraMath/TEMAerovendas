// Caminho: app/src/main/java/com/temaerovendas/data/repository/AircraftRepositoryImpl.kt
package com.temaerovendas.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.temaerovendas.data.local.dao.FavoriteDao
import com.temaerovendas.data.local.entity.FavoriteEntity
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.AircraftCategory
import com.temaerovendas.domain.model.Report
import com.temaerovendas.domain.model.ReportReason
import com.temaerovendas.domain.model.ReportStatus
import com.temaerovendas.domain.repository.AircraftRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AircraftRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val favoriteDao: FavoriteDao,
    private val firebaseAuth: FirebaseAuth
) : AircraftRepository {

    private val aircraftCollection = firestore.collection("aircraft")
    private val reportsCollection = firestore.collection("reports")

    override fun getAllAircraft(): Flow<List<Aircraft>> = callbackFlow {
        val listener = aircraftCollection
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Aircraft::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list.filterVisible())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getAircraftById(id: String): Aircraft? {
        return try {
            val doc = aircraftCollection.document(id).get().await()
            doc.toObject(Aircraft::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    override fun searchAircraft(query: String): Flow<List<Aircraft>> = callbackFlow {
        val listener = aircraftCollection
            .whereGreaterThanOrEqualTo("model", query)
            .whereLessThanOrEqualTo("model", query + "\uF7FF")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Aircraft::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list.filterVisible())
            }
        awaitClose { listener.remove() }
    }

    override fun filterByCategory(category: AircraftCategory): Flow<List<Aircraft>> = callbackFlow {
        val listener = aircraftCollection
            .whereEqualTo("category", category.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Aircraft::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list.filterVisible())
            }
        awaitClose { listener.remove() }
    }

    override fun filterByPrice(minPrice: Double, maxPrice: Double): Flow<List<Aircraft>> = callbackFlow {
        val listener = aircraftCollection
            .whereGreaterThanOrEqualTo("price", minPrice)
            .whereLessThanOrEqualTo("price", maxPrice)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Aircraft::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list.filterVisible())
            }
        awaitClose { listener.remove() }
    }

    override fun getFavorites(userId: String): Flow<List<Aircraft>> {
        return favoriteDao.getFavoritesByUser(userId).map { entities ->
            entities.mapNotNull { entity -> getAircraftById(entity.aircraftId) }
        }
    }

    override fun getMyListings(userId: String): Flow<List<Aircraft>> = callbackFlow {
        val listener = aircraftCollection
            .whereEqualTo("ownerId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Aircraft::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addFavorite(userId: String, aircraftId: String) {
        favoriteDao.insertFavorite(FavoriteEntity(userId = userId, aircraftId = aircraftId))
    }

    override suspend fun removeFavorite(userId: String, aircraftId: String) {
        favoriteDao.deleteFavorite(userId, aircraftId)
    }

    override suspend fun submitContactRequest(aircraftId: String, userId: String, message: String) {
        firestore.collection("contact_requests").add(
            mapOf(
                "aircraftId" to aircraftId,
                "userId" to userId,
                "message" to message,
                "timestamp" to System.currentTimeMillis(),
                "status" to "pending"
            )
        ).await()

        // Encaminha a solicitação por e-mail via extensão Firebase "Trigger Email"
        val aircraft = getAircraftById(aircraftId)
        val buyerEmail = firebaseAuth.currentUser?.email ?: "não informado"
        firestore.collection("mail").add(
            mapOf(
                "to" to listOf("ten.matheus.guerra@gmail.com"),
                "message" to mapOf(
                    "subject" to "Nova proposta - ${aircraft?.model ?: aircraftId}",
                    "text" to """
                        Aeronave: ${aircraft?.model ?: ""} (${aircraft?.registration ?: aircraftId})
                        Comprador (e-mail logado): $buyerEmail
                        Mensagem: $message
                    """.trimIndent()
                )
            )
        ).await()
    }

    override suspend fun createAircraft(
        aircraft: Aircraft,
        mainPhotoUri: Uri?,
        galleryUris: List<Uri>,
        ownerId: String
    ): Result<String> {
        return try {
            // Upload da foto principal (se houver)
            val mainPhotoUrl = mainPhotoUri?.let { uploadImage(it, ownerId) } ?: ""

            // Upload das fotos da galeria
            val galleryUrls = galleryUris.map { uri -> uploadImage(uri, ownerId) }

            val data = aircraft.copy(
                mainPhotoUrl = mainPhotoUrl,
                photoUrls = galleryUrls,
                createdAt = System.currentTimeMillis()
            )

            val document = aircraftCollection.document()
            document.set(
                mapOf(
                    "registration" to data.registration,
                    "model" to data.model,
                    "manufacturer" to data.manufacturer,
                    "year" to data.year,
                    "price" to data.price,
                    "currency" to data.currency,
                    "category" to data.category.name,
                    "flightHours" to data.flightHours,
                    "cycles" to data.cycles,
                    "passengers" to data.passengers,
                    "engines" to data.engines,
                    "configuration" to data.configuration,
                    "highlights" to data.highlights,
                    "mainPhotoUrl" to data.mainPhotoUrl,
                    "photoUrls" to data.photoUrls,
                    "contactEmail" to data.contactEmail,
                    "contactPhone" to data.contactPhone,
                    "description" to data.description,
                    "hangarKept" to data.hangarKept,
                    "avionicsPackage" to data.avionicsPackage,
                    "location" to data.location,
                    "downPayment" to data.downPayment,
                    "installmentCount" to data.installmentCount,
                    "installmentValue" to data.installmentValue,
                    "isApproved" to data.isApproved,
                    "ownerId" to ownerId,
                    "createdAt" to data.createdAt
                )
            ).await()

            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Faz upload de uma imagem local para o Firebase Storage
     * em /aircraft_photos/{ownerId}/{uuid}.jpg e retorna a URL pública de download.
     */
    private suspend fun uploadImage(uri: Uri, ownerId: String): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("aircraft_photos/$ownerId/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun updateAircraft(
        aircraftId: String,
        aircraft: Aircraft,
        newMainPhotoUri: Uri?,
        newGalleryUris: List<Uri>,
        existingGalleryUrls: List<String>,
        ownerId: String
    ): Result<Unit> {
        return try {
            // Só faz upload de uma nova foto principal se uma nova URI local foi selecionada;
            // caso contrário mantém a URL já existente no anúncio.
            val mainPhotoUrl = newMainPhotoUri?.let { uploadImage(it, ownerId) } ?: aircraft.mainPhotoUrl

            // Faz upload apenas das fotos novas da galeria e concatena com as URLs já existentes
            val newGalleryUrls = newGalleryUris.map { uri -> uploadImage(uri, ownerId) }
            val galleryUrls = existingGalleryUrls + newGalleryUrls

            aircraftCollection.document(aircraftId).update(
                mapOf(
                    "registration" to aircraft.registration,
                    "model" to aircraft.model,
                    "manufacturer" to aircraft.manufacturer,
                    "year" to aircraft.year,
                    "price" to aircraft.price,
                    "currency" to aircraft.currency,
                    "category" to aircraft.category.name,
                    "flightHours" to aircraft.flightHours,
                    "cycles" to aircraft.cycles,
                    "passengers" to aircraft.passengers,
                    "engines" to aircraft.engines,
                    "configuration" to aircraft.configuration,
                    "highlights" to aircraft.highlights,
                    "mainPhotoUrl" to mainPhotoUrl,
                    "photoUrls" to galleryUrls,
                    "contactEmail" to aircraft.contactEmail,
                    "contactPhone" to aircraft.contactPhone,
                    "description" to aircraft.description,
                    "hangarKept" to aircraft.hangarKept,
                    "avionicsPackage" to aircraft.avionicsPackage,
                    "location" to aircraft.location,
                    "downPayment" to aircraft.downPayment,
                    "installmentCount" to aircraft.installmentCount,
                    "installmentValue" to aircraft.installmentValue
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAircraft(aircraftId: String): Result<Unit> {
        return try {
            aircraftCollection.document(aircraftId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitReport(
        aircraftId: String,
        reporterId: String,
        reporterEmail: String,
        reason: ReportReason,
        details: String
    ): Result<Unit> {
        return try {
            val aircraft = getAircraftById(aircraftId)

            reportsCollection.add(
                mapOf(
                    "aircraftId" to aircraftId,
                    "aircraftModel" to (aircraft?.model ?: ""),
                    "aircraftRegistration" to (aircraft?.registration ?: ""),
                    "reporterId" to reporterId,
                    "reporterEmail" to reporterEmail,
                    "reason" to reason.name,
                    "details" to details,
                    "status" to ReportStatus.PENDING.name,
                    "createdAt" to System.currentTimeMillis(),
                    "resolvedAt" to 0L,
                    "resolvedBy" to ""
                )
            ).await()

            // Alerta o administrador por e-mail via extensão Firebase "Trigger Email"
            // (mesmo mecanismo já usado em submitContactRequest). O envio de push via
            // FCM ao token salvo em admin_config/fcm é responsabilidade de uma Cloud
            // Function que reage à criação de documentos em "reports" (ver função de
            // referência onReportCreated, mantida junto ao backend de Functions).
            firestore.collection("mail").add(
                mapOf(
                    "to" to listOf("ten.matheus.guerra@gmail.com"),
                    "message" to mapOf(
                        "subject" to "Nova denúncia - ${aircraft?.model ?: aircraftId}",
                        "text" to """
                            Aeronave: ${aircraft?.model ?: ""} (${aircraft?.registration ?: aircraftId})
                            Denunciante (e-mail logado): $reporterEmail
                            Motivo: ${reason.displayName}
                            Detalhes: ${details.ifBlank { "(nenhum detalhe adicional)" }}
                        """.trimIndent()
                    )
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingReports(): Flow<List<Report>> = callbackFlow {
        val listener = reportsCollection
            .whereEqualTo("status", ReportStatus.PENDING.name)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Report::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun resolveReport(
        reportId: String,
        adminUid: String,
        removeAircraft: Boolean,
        aircraftId: String
    ): Result<Unit> {
        return try {
            if (removeAircraft) {
                aircraftCollection.document(aircraftId).delete().await()
            }
            reportsCollection.document(reportId).update(
                mapOf(
                    "status" to (if (removeAircraft) ReportStatus.RESOLVED.name else ReportStatus.DISMISSED.name),
                    "resolvedAt" to System.currentTimeMillis(),
                    "resolvedBy" to adminUid
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun List<Aircraft>.filterVisible(): List<Aircraft> = this
}
