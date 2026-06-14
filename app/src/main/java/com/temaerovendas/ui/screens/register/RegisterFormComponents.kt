// Caminho: app/src/main/java/com/temaerovendas/ui/screens/register/RegisterFormComponents.kt
package com.temaerovendas.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.temaerovendas.ui.theme.*

/**
 * Campo de texto padronizado com tema escuro/dourado e exibição de erro.
 */
@Composable
fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    suffix: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = SilverAccent) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            isError = error != null,
            suffix = suffix?.let { { Text(it, color = SilverAccent) } },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldLight,
                unfocusedBorderColor = NavyLight,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = WhitePrimary,
                unfocusedTextColor = WhitePrimary,
                cursorColor = GoldLight,
                focusedContainerColor = NavyMid,
                unfocusedContainerColor = NavyMid
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Título de seção do formulário (ex: "DADOS GERAIS", "ESPECIFICAÇÕES").
 */
@Composable
fun RegisterSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = GoldLight,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

/**
 * Seletor de foto principal — exibe preview ou placeholder clicável.
 * Quando em modo edição e nenhuma foto nova foi selecionada, exibe a foto
 * já existente do anúncio (existingPhotoUrl) como preview.
 */
@Composable
fun MainPhotoPicker(
    photoUri: android.net.Uri?,
    onClick: () -> Unit,
    existingPhotoUrl: String = "",
    error: String? = null
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavyMid)
                .border(
                    width = 1.dp,
                    color = if (error != null) MaterialTheme.colorScheme.error else NavyLight,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val previewModel = photoUri ?: existingPhotoUrl.ifBlank { null }
            if (previewModel != null) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Foto principal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = SilverAccent, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Adicionar foto principal", color = SilverAccent)
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Galeria horizontal de fotos adicionais com botão de adicionar e remover.
 * Exibe tanto as fotos já existentes do anúncio (existingPhotoUrls, modo edição)
 * quanto as novas fotos locais selecionadas (photoUris).
 */
@Composable
fun GalleryPhotosPicker(
    photoUris: List<android.net.Uri>,
    onAddClick: () -> Unit,
    onRemove: (android.net.Uri) -> Unit,
    existingPhotoUrls: List<String> = emptyList(),
    onRemoveExisting: (String) -> Unit = {}
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(existingPhotoUrls) { url ->
            Box(modifier = Modifier.size(100.dp)) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = { onRemoveExisting(url) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remover", tint = WhitePrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
        items(photoUris) { uri ->
            Box(modifier = Modifier.size(100.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remover", tint = WhitePrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyMid)
                    .border(width = 1.dp, color = NavyLight, shape = RoundedCornerShape(8.dp))
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Adicionar fotos", tint = SilverAccent)
            }
        }
    }
}

/**
 * Seletor de categoria de aeronave via menu dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selected: com.temaerovendas.domain.model.AircraftCategory,
    onSelect: (com.temaerovendas.domain.model.AircraftCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria", color = SilverAccent) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldLight,
                unfocusedBorderColor = NavyLight,
                focusedTextColor = WhitePrimary,
                unfocusedTextColor = WhitePrimary,
                focusedContainerColor = NavyMid,
                unfocusedContainerColor = NavyMid
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NavyMid)
        ) {
            com.temaerovendas.domain.model.AircraftCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName, color = WhitePrimary) },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Seletor de moeda (USD/BRL) via SegmentedButton simples.
 */
@Composable
fun CurrencySelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = NavyLight, shape = RoundedCornerShape(8.dp))
    ) {
        listOf("USD", "BRL").forEach { currency ->
            val isSelected = selected == currency
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSelected) GoldLight else NavyMid)
                    .clickable { onSelect(currency) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currency == "USD") "US$ (Dólar)" else "R$ (Real)",
                    color = if (isSelected) NavyDeep else SilverAccent,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
