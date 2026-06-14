// Caminho: app/src/main/java/com/temaerovendas/domain/repository/AircraftRepository.kt
package com.temaerovendas.domain.repository

import android.net.Uri
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.AircraftCategory
import com.temaerovendas.domain.model.Report
import com.temaerovendas.domain.model.ReportReason
import kotlinx.coroutines.flow.Flow

interface AircraftRepository {
    fun getAllAircraft(): Flow<List<Aircraft>>
    suspend fun getAircraftById(id: String): Aircraft?
    fun searchAircraft(query: String): Flow<List<Aircraft>>
    fun filterByCategory(category: AircraftCategory): Flow<List<Aircraft>>
    fun filterByPrice(minPrice: Double, maxPrice: Double): Flow<List<Aircraft>>
    fun getFavorites(userId: String): Flow<List<Aircraft>>
    suspend fun addFavorite(userId: String, aircraftId: String)
    suspend fun removeFavorite(userId: String, aircraftId: String)
    suspend fun submitContactRequest(aircraftId: String, userId: String, message: String)

    /**
     * Retorna os anúncios cadastrados pelo próprio usuário (campo ownerId no Firestore).
     */
    fun getMyListings(userId: String): Flow<List<Aircraft>>

    /**
     * Cadastra uma nova aeronave no Firestore, fazendo upload prévio
     * das imagens fornecidas para o Firebase Storage.
     *
     * @param aircraft dados da aeronave (sem photoUrls/mainPhotoUrl preenchidos)
     * @param mainPhotoUri URI local da imagem principal (galeria do dispositivo)
     * @param galleryUris lista de URIs locais das fotos da galeria
     * @param ownerId UID do usuário que está cadastrando o anúncio
     * @return id do documento criado no Firestore
     */
    suspend fun createAircraft(
        aircraft: Aircraft,
        mainPhotoUri: Uri?,
        galleryUris: List<Uri>,
        ownerId: String
    ): Result<String>

    /**
     * Atualiza um anúncio já existente no Firestore.
     * Se mainPhotoUri ou novas galleryUris forem fornecidas, faz o upload
     * e substitui/complementa as URLs. Caso contrário, mantém as fotos atuais.
     *
     * @param aircraftId id do documento a ser atualizado
     * @param aircraft dados atualizados do anúncio
     * @param newMainPhotoUri nova URI local da foto principal (null = mantém a atual)
     * @param newGalleryUris novas URIs locais a adicionar à galeria
     * @param existingGalleryUrls URLs da galeria já existentes que devem ser mantidas
     * @param ownerId UID do usuário dono do anúncio (usado no caminho de upload)
     */
    suspend fun updateAircraft(
        aircraftId: String,
        aircraft: Aircraft,
        newMainPhotoUri: Uri?,
        newGalleryUris: List<Uri>,
        existingGalleryUrls: List<String>,
        ownerId: String
    ): Result<Unit>

    /**
     * Remove definitivamente um anúncio (usado pelo painel de administração
     * ao tratar uma denúncia procedente). Não remove as imagens do Storage
     * por simplicidade — o documento do anúncio é o que importa para a
     * visibilidade do conteúdo na plataforma.
     */
    suspend fun deleteAircraft(aircraftId: String): Result<Unit>

    /**
     * Registra a denúncia de um anúncio (Política de UGC), feita por
     * qualquer usuário autenticado a partir da ficha técnica. Grava na
     * coleção "reports" do Firestore e dispara um e-mail de alerta ao
     * administrador através da extensão Firebase "Trigger Email"
     * (mesmo mecanismo já usado em submitContactRequest).
     */
    suspend fun submitReport(
        aircraftId: String,
        reporterId: String,
        reporterEmail: String,
        reason: ReportReason,
        details: String
    ): Result<Unit>

    /** Stream de denúncias pendentes, para a tela de administração. */
    fun getPendingReports(): Flow<List<Report>>

    /**
     * Marca uma denúncia como tratada.
     * @param removeAircraft se true, também remove o anúncio denunciado
     */
    suspend fun resolveReport(
        reportId: String,
        adminUid: String,
        removeAircraft: Boolean,
        aircraftId: String
    ): Result<Unit>
}

