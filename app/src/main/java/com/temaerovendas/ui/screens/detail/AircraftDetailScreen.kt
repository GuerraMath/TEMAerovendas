// Caminho: app/src/main/java/com/temaerovendas/ui/screens/detail/AircraftDetailScreen.kt
package com.temaerovendas.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.ReportReason
import com.temaerovendas.ui.components.ImageViewerDialog
import com.temaerovendas.ui.theme.*
import com.temaerovendas.ui.util.isLandscape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftDetailScreen(
    aircraftId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: AircraftDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(aircraftId) {
        viewModel.loadAircraft(aircraftId)
    }

    LaunchedEffect(uiState.reportSubmitted) {
        if (uiState.reportSubmitted) {
            snackbarHostState.showSnackbar("Denúncia enviada. Nossa equipe vai analisar este anúncio.")
            viewModel.consumeReportSubmitted()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.aircraft?.model ?: "Ficha Técnica",
                        color = WhitePrimary,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = SilverAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Outlined.Flag, contentDescription = "Denunciar anúncio", tint = SilverAccent)
                    }
                    if (uiState.isOwner || uiState.isAdmin) {
                        IconButton(onClick = { onEditClick(aircraftId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar anúncio", tint = SilverAccent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyMid)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NavyDeep
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldLight)
                }
            }
            uiState.aircraft != null -> {
                AircraftDetailContent(
                    aircraft = uiState.aircraft!!,
                    isAdmin = uiState.isAdmin,
                    modifier = Modifier.padding(padding),
                    onFavoriteToggle = { viewModel.toggleFavorite(aircraftId) },
                    onContactRequest = { message -> viewModel.submitContactRequest(aircraftId, message) }
                )
            }
            else -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aeronave não encontrada", color = SilverAccent)
                }
            }
        }
    }

    if (showReportDialog) {
        ReportAircraftDialog(
            isSubmitting = uiState.isSubmittingReport,
            onConfirm = { reason, details ->
                viewModel.submitReport(aircraftId, reason, details)
                showReportDialog = false
            },
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
private fun AircraftDetailContent(
    aircraft: Aircraft,
    isAdmin: Boolean,
    modifier: Modifier = Modifier,
    onFavoriteToggle: () -> Unit,
    onContactRequest: (String) -> Unit
) {
    var showContactDialog by remember { mutableStateOf(false) }

    // Estado do viewer de imagens
    // allPhotos = capa + galeria (sem duplicatas)
    val allPhotos = remember(aircraft) {
        buildList {
            if (aircraft.mainPhotoUrl.isNotBlank()) add(aircraft.mainPhotoUrl)
            aircraft.photoUrls.forEach { url ->
                if (url.isNotBlank() && url != aircraft.mainPhotoUrl) add(url)
            }
        }
    }
    var viewerInitialIndex by remember { mutableStateOf(0) }
    var showImageViewer by remember { mutableStateOf(false) }

    if (isLandscape()) {
        Row(modifier = modifier.fillMaxSize()) {
            AircraftHeroImage(
                aircraft = aircraft,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onImageClick = {
                    viewerInitialIndex = 0
                    showImageViewer = true
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                AircraftInfoSections(
                    aircraft = aircraft,
                    allPhotos = allPhotos,
                    compact = true,
                    onFavoriteToggle = onFavoriteToggle,
                    onContactClick = { showContactDialog = true },
                    onImageClick = { index ->
                        viewerInitialIndex = index
                        showImageViewer = true
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AircraftHeroImage(
                aircraft = aircraft,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                onImageClick = {
                    viewerInitialIndex = 0
                    showImageViewer = true
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyMid)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("✈  ${aircraft.category.displayName}", style = MaterialTheme.typography.bodyMedium, color = SilverAccent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AircraftInfoSections(
                aircraft = aircraft,
                allPhotos = allPhotos,
                compact = false,
                onFavoriteToggle = onFavoriteToggle,
                onContactClick = { showContactDialog = true },
                onImageClick = { index ->
                    viewerInitialIndex = index
                    showImageViewer = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog de contato
    if (showContactDialog) {
        ContactRequestDialog(
            aircraftModel = aircraft.model,
            onConfirm = { message ->
                onContactRequest(message)
                showContactDialog = false
            },
            onDismiss = { showContactDialog = false }
        )
    }

    // Viewer fullscreen
    if (showImageViewer && allPhotos.isNotEmpty()) {
        ImageViewerDialog(
            images = allPhotos,
            initialIndex = viewerInitialIndex,
            onDismiss = { showImageViewer = false }
        )
    }
}

/**
 * Foto principal (capa) do anúncio.
 * Agora recebe [onImageClick] para abrir o viewer ao ser tocada.
 */
@Composable
private fun AircraftHeroImage(
    aircraft: Aircraft,
    modifier: Modifier = Modifier,
    onImageClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onImageClick)) {
        AsyncImage(
            model = aircraft.mainPhotoUrl,
            contentDescription = "${aircraft.model} - ${aircraft.registration}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column {
                Text(aircraft.registration, style = MaterialTheme.typography.titleLarge, color = WhitePrimary, fontWeight = FontWeight.Bold)
                Text(formatPrice(aircraft.price, aircraft.currency), style = MaterialTheme.typography.headlineMedium, color = GoldLight, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Todas as seções de informação do anúncio.
 *
 * [allPhotos] é a lista completa (capa + galeria) usada pelo viewer.
 * [onImageClick] recebe o índice dentro de [allPhotos] da foto tocada.
 */
@Composable
private fun AircraftInfoSections(
    aircraft: Aircraft,
    allPhotos: List<String>,
    compact: Boolean,
    onFavoriteToggle: () -> Unit,
    onContactClick: () -> Unit,
    onImageClick: (index: Int) -> Unit
) {
    // Visão Geral do Mercado
    DetailSection(title = "VISÃO GERAL DO MERCADO") {
        DetailGrid(
            items = listOf(
                "Fabricante" to aircraft.manufacturer,
                "Ano" to aircraft.year.toString(),
                "Passageiros" to aircraft.passengers.toString(),
                "Horas disponíveis" to "${aircraft.flightHours}h",
                "Configuração" to aircraft.configuration
            )
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Condições de Pagamento
    if (aircraft.installmentCount > 0) {
        DetailSection(title = "CONDIÇÕES DE PAGAMENTO") {
            val symbol = if (aircraft.currency == "BRL") "R$ " else "US$ "
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Entrada:", color = SilverAccent, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "$symbol${"%,.0f".format(aircraft.downPayment)}",
                        color = WhitePrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Parcelamento:", color = SilverAccent, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${aircraft.installmentCount}x de $symbol${"%,.0f".format(aircraft.installmentValue)}",
                        color = WhitePrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Destaques e Melhorias
    if (aircraft.highlights.isNotEmpty()) {
        DetailSection(title = "DESTAQUES E MELHORIAS") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                aircraft.highlights.forEach { highlight ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("•", color = GoldLight, style = MaterialTheme.typography.bodyMedium)
                        Text(highlight, color = WhitePrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Galeria de Fotos — miniaturas clicáveis que abrem o viewer
    // O índice passado a onImageClick corresponde à posição em allPhotos:
    //   índice 0 = capa (mainPhotoUrl), índices 1..N = photoUrls
    if (!compact && allPhotos.isNotEmpty()) {
        DetailSection(title = "GALERIA") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(allPhotos) { index, url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Foto ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 140.dp, height = 90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(index) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Descrição
    if (aircraft.description.isNotBlank()) {
        DetailSection(title = "DESCRIÇÃO") {
            Text(aircraft.description, color = WhitePrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Localização e Contato
    DetailSection(title = "LOCALIZAÇÃO E CONTATO") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (aircraft.location.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Localização:", color = SilverAccent, style = MaterialTheme.typography.bodyMedium)
                    Text(aircraft.location, color = WhitePrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (aircraft.avionicsPackage.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Aviônicos:", color = SilverAccent, style = MaterialTheme.typography.bodyMedium)
                    Text(aircraft.avionicsPackage, color = WhitePrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mantida em hangar:", color = SilverAccent, style = MaterialTheme.typography.bodyMedium)
                Text(if (aircraft.hangarKept) "Sim" else "Não", color = WhitePrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            }
            if (aircraft.contactEmail.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("E-mail:", color = SilverAccent, style = MaterialTheme.typography.bodyMedium)
                    Text(aircraft.contactEmail, color = WhitePrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (aircraft.contactPhone.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Telefone:", color = SilverAccent, style = MaterialTheme.typography.bodyMedium)
                    Text(aircraft.contactPhone, color = WhitePrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Botão de Contato
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(
            onClick = onContactClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldLight,
                contentColor = NavyDeep
            )
        ) {
            Text("SOLICITAR PROPOSTA / MAIS INFORMAÇÕES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (aircraft.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (aircraft.isFavorite) GoldLight else SilverAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (aircraft.isFavorite) "Salvo nos Favoritos" else "Adicionar aos Favoritos",
                    color = SilverAccent
                )
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMid)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = GoldLight, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$label:", style = MaterialTheme.typography.bodyMedium, color = SilverAccent)
                        Text(value, style = MaterialTheme.typography.bodyMedium, color = WhitePrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRequestDialog(
    aircraftModel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyMid,
        title = {
            Text("Solicitar Proposta", color = WhitePrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(aircraftModel, color = SilverAccent, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mensagem (opcional)", color = SilverAccent) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldLight,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = WhitePrimary,
                        unfocusedTextColor = WhitePrimary,
                        cursorColor = GoldLight
                    ),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(message) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldLight, contentColor = NavyDeep)
            ) {
                Text("Enviar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverAccent)
            }
        }
    )
}

@Composable
private fun ReportAircraftDialog(
    isSubmitting: Boolean,
    onConfirm: (ReportReason, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf(ReportReason.OTHER) }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyMid,
        title = {
            Text("Denunciar Anúncio", color = WhitePrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Selecione o motivo da denúncia. Nossa equipe vai analisar o anúncio e poderá removê-lo caso a denúncia seja procedente.",
                    color = SilverAccent,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))

                ReportReason.values().forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = GoldLight,
                                unselectedColor = SilverAccent
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(reason.displayName, color = WhitePrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Detalhes (opcional)", color = SilverAccent) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldLight,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = WhitePrimary,
                        unfocusedTextColor = WhitePrimary,
                        cursorColor = GoldLight
                    ),
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedReason, details.trim()) },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = GoldLight, contentColor = NavyDeep)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NavyDeep, strokeWidth = 2.dp)
                } else {
                    Text("Enviar Denúncia", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancelar", color = SilverAccent)
            }
        }
    )
}

private fun formatPrice(price: Double, currency: String): String {
    val symbol = if (currency == "BRL") "R$ " else "US$ "
    val formatted = "%,.0f".format(price)
    return "$symbol$formatted"
}
