// Caminho: app/src/main/java/com/temaerovendas/domain/model/Aircraft.kt
package com.temaerovendas.domain.model
import androidx.annotation.Keep

@Keep
data class Aircraft(
    val id: String = "",
    val registration: String = "",          // ex: N789TA
    val model: String = "",                 // ex: Gulfstream G650ER
    val manufacturer: String = "",
    val year: Int = 0,
    val price: Double = 0.0,
    val currency: String = "USD",
    val category: AircraftCategory = AircraftCategory.EXECUTIVE_JET,
    val flightHours: Int = 0,
    val cycles: Int = 0,
    val passengers: Int = 0,
    val engines: String = "",               // ex: 2x RR BR725
    val configuration: String = "",         // ex: Executiva de Luxo
    val highlights: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList(),
    val mainPhotoUrl: String = "",
    val isFavorite: Boolean = false,
    val contactEmail: String = "",
    val contactPhone: String = "",
    val description: String = "",
    val hangarKept: Boolean = false,
    val avionicsPackage: String = "",
    val location: String = "",

    // Condições de pagamento (opcional) — preenchido quando o vendedor
    // oferece parcelamento, ex: entrada + N parcelas mensais
    val downPayment: Double = 0.0,      // valor da entrada
    val installmentCount: Int = 0,      // número de parcelas (0 = sem parcelamento)
    val installmentValue: Double = 0.0, // valor de cada parcela

    val isApproved: Boolean = false,    // status de aprovação

    val ownerId: String = "",           // UID do usuário que cadastrou o anúncio (dono)

    val createdAt: Long = System.currentTimeMillis()
)

enum class AircraftCategory(val displayName: String) {

    PISTON("Pistão"),
    HELICOPTER("Helicóptero"),
    TURBOPROP("Turboélice"),
    EXECUTIVE_JET("Jato Executivo"),
    LIGHT_JET("Jato Leve"),
    HEAVY_JET("Jato Pesado"),
    ULTRA_LONG_RANGE("Ultra Long Range")
}
