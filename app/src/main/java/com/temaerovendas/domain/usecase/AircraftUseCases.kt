// Caminho: app/src/main/java/com/temaerovendas/domain/usecase/AircraftUseCases.kt
package com.temaerovendas.domain.usecase

import android.net.Uri
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.AircraftCategory
import com.temaerovendas.domain.model.Report
import com.temaerovendas.domain.model.ReportReason
import com.temaerovendas.domain.repository.AircraftRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import androidx.annotation.Keep

@Keep
class GetAllAircraftUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    operator fun invoke(): Flow<List<Aircraft>> = repository.getAllAircraft()
}

class GetAircraftByIdUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(id: String): Aircraft? = repository.getAircraftById(id)
}

class SearchAircraftUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    operator fun invoke(query: String): Flow<List<Aircraft>> = repository.searchAircraft(query)
}

class FilterAircraftByCategoryUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    operator fun invoke(category: AircraftCategory): Flow<List<Aircraft>> =
        repository.filterByCategory(category)
}

class AddFavoriteUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(userId: String, aircraftId: String) =
        repository.addFavorite(userId, aircraftId)
}

class RemoveFavoriteUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(userId: String, aircraftId: String) =
        repository.removeFavorite(userId, aircraftId)
}

class SubmitContactRequestUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(aircraftId: String, userId: String, message: String) =
        repository.submitContactRequest(aircraftId, userId, message)
}

class CreateAircraftUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(
        aircraft: Aircraft,
        mainPhotoUri: Uri?,
        galleryUris: List<Uri>,
        ownerId: String
    ): Result<String> = repository.createAircraft(aircraft, mainPhotoUri, galleryUris, ownerId)
}

class UpdateAircraftUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(
        aircraftId: String,
        aircraft: Aircraft,
        newMainPhotoUri: Uri?,
        newGalleryUris: List<Uri>,
        existingGalleryUrls: List<String>,
        ownerId: String
    ): Result<Unit> = repository.updateAircraft(
        aircraftId, aircraft, newMainPhotoUri, newGalleryUris, existingGalleryUrls, ownerId
    )
}

class GetFavoritesUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    operator fun invoke(userId: String): Flow<List<Aircraft>> = repository.getFavorites(userId)
}

class GetMyListingsUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    operator fun invoke(userId: String): Flow<List<Aircraft>> = repository.getMyListings(userId)
}

class DeleteAircraftUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(aircraftId: String): Result<Unit> = repository.deleteAircraft(aircraftId)
}

/**
 * Registra a denúncia de um anúncio (Política de UGC). Disponível para
 * qualquer usuário autenticado a partir da ficha técnica da aeronave.
 */
class SubmitReportUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(
        aircraftId: String,
        reporterId: String,
        reporterEmail: String,
        reason: ReportReason,
        details: String
    ): Result<Unit> = repository.submitReport(aircraftId, reporterId, reporterEmail, reason, details)
}

/** Lista de denúncias pendentes, para a tela de administração. */
class GetPendingReportsUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    operator fun invoke(): Flow<List<Report>> = repository.getPendingReports()
}

/** Marca uma denúncia como tratada, removendo o anúncio quando procedente. */
class ResolveReportUseCase @Inject constructor(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(
        reportId: String,
        adminUid: String,
        removeAircraft: Boolean,
        aircraftId: String
    ): Result<Unit> = repository.resolveReport(reportId, adminUid, removeAircraft, aircraftId)
}
