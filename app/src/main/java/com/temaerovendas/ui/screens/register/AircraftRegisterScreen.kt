// Caminho: app/src/main/java/com/temaerovendas/ui/screens/register/AircraftRegisterScreen.kt
package com.temaerovendas.ui.screens.register

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.temaerovendas.domain.model.AircraftCategory
import com.temaerovendas.ui.theme.*

/**
 * Tela de cadastro/edição de aeronave — formulário completo de anúncio.
 * Qualquer usuário autenticado pode cadastrar (marketplace aberto).
 * Quando aberta a partir da ficha técnica (edição), carrega os dados
 * existentes do anúncio e passa a operar em modo de atualização.
 *
 * @param onBack volta para a tela anterior
 * @param onSubmitSuccess chamado após cadastro/edição bem-sucedida
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftRegisterScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: AircraftRegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher para selecionar a foto principal (single)
    val mainPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onMainPhotoSelected(uri) }

    // Launcher para selecionar fotos da galeria (multiple)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris -> viewModel.onGalleryPhotosSelected(uris) }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            snackbarHostState.showSnackbar(
                if (uiState.isEditMode) "Anúncio atualizado com sucesso!" else "Aeronave cadastrada com sucesso!"
            )
            onSubmitSuccess()
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
                        if (uiState.isEditMode) "Editar Anúncio" else "Anunciar Aeronave",
                        color = WhitePrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = SilverAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyMid)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NavyDeep
    ) { padding ->
        if (uiState.isLoadingExisting) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldLight)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // Foto principal
            RegisterSectionTitle("FOTO DE CAPA")
            MainPhotoPicker(
                photoUri = uiState.mainPhotoUri,
                existingPhotoUrl = uiState.existingMainPhotoUrl,
                onClick = {
                    mainPhotoLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                error = uiState.fieldErrors["mainPhoto"]
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Galeria
            RegisterSectionTitle("GALERIA DE FOTOS (opcional, até 8)")
            GalleryPhotosPicker(
                photoUris = uiState.galleryUris,
                existingPhotoUrls = uiState.existingGalleryUrls,
                onAddClick = {
                    galleryLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemove = viewModel::onRemoveGalleryPhoto,
                onRemoveExisting = viewModel::onRemoveExistingGalleryPhoto
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dados gerais
            RegisterSectionTitle("DADOS GERAIS")

            RegisterTextField(
                value = uiState.registration,
                onValueChange = viewModel::onRegistrationChange,
                label = "Matrícula (opcional, ex: PT-XXX)",
                error = uiState.fieldErrors["registration"]
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.model,
                onValueChange = viewModel::onModelChange,
                label = "Modelo (ex: Gulfstream G650ER)",
                error = uiState.fieldErrors["model"]
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.manufacturer,
                onValueChange = viewModel::onManufacturerChange,
                label = "Fabricante (ex: Gulfstream)",
                error = uiState.fieldErrors["manufacturer"]
            )
            Spacer(modifier = Modifier.height(12.dp))

            CategoryDropdown(
                selected = uiState.category,
                onSelect = viewModel::onCategoryChange
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.year,
                onValueChange = viewModel::onYearChange,
                label = "Ano de fabricação",
                keyboardType = KeyboardType.Number,
                error = uiState.fieldErrors["year"]
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preço
            RegisterSectionTitle("PREÇO")
            CurrencySelector(
                selected = uiState.currency,
                onSelect = viewModel::onCurrencyChange
            )
            Spacer(modifier = Modifier.height(12.dp))
            RegisterTextField(
                value = uiState.price,
                onValueChange = viewModel::onPriceChange,
                label = "Valor do anúncio",
                keyboardType = KeyboardType.Decimal,
                error = uiState.fieldErrors["price"]
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Condições de pagamento (parcelamento opcional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.offerInstallments,
                    onCheckedChange = viewModel::onOfferInstallmentsChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = GoldLight,
                        uncheckedColor = SilverAccent,
                        checkmarkColor = NavyDeep
                    )
                )
                Text("Oferecer parcelamento (entrada + parcelas)", color = WhitePrimary)
            }

            if (uiState.offerInstallments) {
                Spacer(modifier = Modifier.height(8.dp))
                RegisterTextField(
                    value = uiState.downPayment,
                    onValueChange = viewModel::onDownPaymentChange,
                    label = "Valor da entrada",
                    keyboardType = KeyboardType.Decimal,
                    error = uiState.fieldErrors["downPayment"]
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RegisterTextField(
                        value = uiState.installmentCount,
                        onValueChange = viewModel::onInstallmentCountChange,
                        label = "Número de parcelas",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        error = uiState.fieldErrors["installmentCount"]
                    )
                    RegisterTextField(
                        value = uiState.installmentValue,
                        onValueChange = viewModel::onInstallmentValueChange,
                        label = "Valor de cada parcela",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        error = uiState.fieldErrors["installmentValue"]
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Especificações técnicas
            RegisterSectionTitle("ESPECIFICAÇÕES TÉCNICAS")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RegisterTextField(
                    value = uiState.flightHours,
                    onValueChange = viewModel::onFlightHoursChange,
                    label = "Horas disponíveis",
                    keyboardType = KeyboardType.Number,
                    suffix = "h",
                    modifier = Modifier.weight(1f),
                    error = uiState.fieldErrors["flightHours"]
                )
                RegisterTextField(
                    value = uiState.cycles,
                    onValueChange = viewModel::onCyclesChange,
                    label = "Ciclos (opcional)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    error = uiState.fieldErrors["cycles"]
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                    value = uiState.passengers,
                    onValueChange = viewModel::onPassengersChange,
                    label = "Capacidade de passageiros (opcional)",
                    keyboardType = KeyboardType.Number,
                    error = uiState.fieldErrors["passengers"]
                )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.engines,
                onValueChange = viewModel::onEnginesChange,
                label = "Motores (opcional, ex: 2x RR BR725)",
                error = uiState.fieldErrors["engines"]
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.configuration,
                onValueChange = viewModel::onConfigurationChange,
                label = "Configuração (opcional, ex: Executiva de Luxo)",
                error = uiState.fieldErrors["configuration"]
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.avionicsPackage,
                onValueChange = viewModel::onAvionicsPackageChange,
                label = "Pacote de aviônicos (opcional)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Destaques
            RegisterSectionTitle("DESTAQUES E MELHORIAS")
            Text(
                "Digite um destaque por linha (ex: Sistema de Entretenimento Topo de Linha)",
                style = MaterialTheme.typography.bodySmall,
                color = SilverAccent,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            RegisterTextField(
                value = uiState.highlightsText,
                onValueChange = viewModel::onHighlightsChange,
                label = "Destaques",
                singleLine = false,
                minLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Estado / Condição
            RegisterSectionTitle("CONDIÇÃO DA AERONAVE")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.hangarKept,
                    onCheckedChange = viewModel::onHangarKeptChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = GoldLight,
                        uncheckedColor = SilverAccent,
                        checkmarkColor = NavyDeep
                    )
                )
                Text("Sempre mantida em hangar", color = WhitePrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            RegisterTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Descrição adicional (opcional)",
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Localização e contato
            RegisterSectionTitle("LOCALIZAÇÃO E CONTATO")

            RegisterTextField(
                value = uiState.location,
                onValueChange = viewModel::onLocationChange,
                label = "Localização (ex: São Paulo, SP)"
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.contactEmail,
                onValueChange = viewModel::onContactEmailChange,
                label = "E-mail de contato",
                keyboardType = KeyboardType.Email,
                error = uiState.fieldErrors["contact"]
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegisterTextField(
                value = uiState.contactPhone,
                onValueChange = viewModel::onContactPhoneChange,
                label = "Telefone de contato",
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de envio
            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldLight,
                    contentColor = NavyDeep
                )
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NavyDeep,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.isEditMode) "SALVAR ALTERAÇÕES" else "PUBLICAR ANÚNCIO",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(104.dp))
        }
    }
}
