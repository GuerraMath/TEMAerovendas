// Caminho: app/src/main/java/com/temaerovendas/ui/screens/list/AircraftListScreen.kt
package com.temaerovendas.ui.screens.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.temaerovendas.R
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.AircraftCategory
import com.temaerovendas.ui.theme.*
import com.temaerovendas.ui.util.isLandscape
import java.text.NumberFormat
import java.util.Locale

private val BottomBarClearancePortrait = 104.dp
private val BottomBarClearanceLandscape = 90.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftListScreen(
    onAircraftClick: (String) -> Unit,
    onAddAircraftClick: () -> Unit,
    viewModel: AircraftListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val landscape = isLandscape()

    Box(modifier = Modifier.fillMaxSize().background(NavyDeep)) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldLight)
                }
            }
            uiState.aircraft.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma aeronave encontrada", color = SilverAccent)
                }
            }
            landscape -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 64.dp,
                        bottom = BottomBarClearanceLandscape,
                        start = 12.dp,
                        end = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.aircraft, key = { it.id }) { aircraft ->
                        AircraftCoverCard(
                            aircraft = aircraft,
                            modifier = Modifier.fillParentMaxHeight(0.92f),
                            onClick = { onAircraftClick(aircraft.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(aircraft.id) }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 156.dp,
                        bottom = BottomBarClearancePortrait,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.aircraft, key = { it.id }) { aircraft ->
                        AircraftCard(
                            aircraft = aircraft,
                            isAdmin = uiState.isAdmin,
                            onClick = { onAircraftClick(aircraft.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(aircraft.id) }
                        )
                    }
                }
            }
        }

        TEMListHeader(
            compact = landscape,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            selectedCategory = uiState.selectedCategory,
            onCategorySelect = viewModel::onCategorySelected,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun TEMListHeader(
    compact: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: AircraftCategory?,
    onCategorySelect: (AircraftCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    var searchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(shape)
            .glassSurface(shape = shape, tint = NavyDeep, alpha = 0.6f)
            .padding(horizontal = 14.dp, vertical = if (compact) 8.dp else 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo_tem),
                contentDescription = "TEM Aerovendas",
                modifier = Modifier
                    .height(if (compact) 22.dp else 28.dp)
                    .padding(end = 8.dp)
            )
            if (!compact && !searchExpanded) {
                Text(
                    "TEM Aerovendas",
                    fontWeight = FontWeight.Bold,
                    color = WhitePrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            if (compact) {
                CompactSearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.weight(1.4f)
                )
            } else if (searchExpanded) {
                CompactSearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    searchExpanded = false
                    onSearchQueryChange("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar busca", tint = SilverAccent)
                }
            } else {
                IconButton(onClick = { searchExpanded = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = SilverAccent)
                }
            }
        }

        if (!compact) {
            Spacer(modifier = Modifier.height(8.dp))
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelect = onCategorySelect
            )
        }
    }
}

@Composable
private fun CompactSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar...", color = SilverAccent, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SilverAccent, modifier = Modifier.size(18.dp)) },
        modifier = modifier.height(48.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldLight,
            unfocusedBorderColor = NavyLight,
            focusedTextColor = WhitePrimary,
            unfocusedTextColor = WhitePrimary,
            cursorColor = GoldLight,
            focusedContainerColor = NavyMid.copy(alpha = 0.4f),
            unfocusedContainerColor = NavyMid.copy(alpha = 0.4f)
        ),
        singleLine = true
    )
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: AircraftCategory?,
    onCategorySelect: (AircraftCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldLight,
                    selectedLabelColor = NavyDeep
                )
            )
        }
        items(AircraftCategory.values()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) },
                label = { Text(category.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldLight,
                    selectedLabelColor = NavyDeep
                )
            )
        }
    }
}

/**
 * Card "capa cheia" (modo paisagem).
 */
@Composable
private fun AircraftCoverCard(
    aircraft: Aircraft,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = aircraft.mainPhotoUrl,
            contentDescription = "${aircraft.model} - ${aircraft.registration}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(50))
                .glassSurface(shape = RoundedCornerShape(50), tint = NavyDeep, alpha = 0.45f)
        ) {
            Icon(
                imageVector = if (aircraft.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favoritar",
                tint = if (aircraft.isFavorite) GoldLight else WhitePrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .glassSurface(shape = RoundedCornerShape(16.dp), tint = NavyDeep, alpha = 0.6f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = aircraft.model,
                        style = MaterialTheme.typography.titleLarge,
                        color = WhitePrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = aircraft.registration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilverAccent,
                        maxLines = 1
                    )
                }
                Text(
                    text = formatPrice(aircraft.price, aircraft.currency),
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = SilverAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${aircraft.category.displayName} · ${aircraft.year} · ${aircraft.flightHours}h · ${aircraft.passengers} pax",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AircraftCard(
    aircraft: Aircraft,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyMid),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = aircraft.mainPhotoUrl,
                    contentDescription = "${aircraft.model} - ${aircraft.registration}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = aircraft.registration,
                            style = MaterialTheme.typography.titleMedium,
                            color = WhitePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatPrice(aircraft.price, aircraft.currency),
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (aircraft.isFavorite) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (aircraft.isFavorite) GoldLight else SilverAccent
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = aircraft.model,
                    style = MaterialTheme.typography.headlineMedium,
                    color = WhitePrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = SilverAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = aircraft.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverAccent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = NavyLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AircraftDataItem(label = "Ano", value = aircraft.year.toString())
                    AircraftDataItem(label = "Disponível", value = "${aircraft.flightHours}h")
                }
            }
        }
    }
}

@Composable
private fun AircraftDataItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = WhitePrimary, fontWeight = FontWeight.SemiBold)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = SilverAccent)
    }
}

private fun formatPrice(price: Double, currency: String): String {
    val locale = if (currency == "BRL") Locale("pt", "BR") else Locale.US
    val formatter = NumberFormat.getCurrencyInstance(locale)
    return formatter.format(price)
}
