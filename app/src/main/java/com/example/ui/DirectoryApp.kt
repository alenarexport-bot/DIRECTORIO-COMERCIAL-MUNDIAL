package com.example.ui

import com.example.R
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Business
import com.example.data.Review
import com.example.data.Offer
import com.example.data.LiveStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryApp(viewModel: DirectoryViewModel) {
    val currentCategory by viewModel.currentCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredBusinesses by viewModel.filteredBusinesses.collectAsStateWithLifecycle()
    val selectedBusiness by viewModel.selectedBusiness.collectAsStateWithLifecycle()
    val selectedReviews by viewModel.selectedBusinessReviews.collectAsStateWithLifecycle()
    val syncingStates by viewModel.syncingStates.collectAsStateWithLifecycle()
    val favoriteBusinesses by viewModel.favoriteBusinesses.collectAsStateWithLifecycle()
    val offers by viewModel.allOffers.collectAsStateWithLifecycle()
    val fairProducts by viewModel.allFairProducts.collectAsStateWithLifecycle()

    // Authentication States
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val loggedInEmail by viewModel.loggedInEmail.collectAsStateWithLifecycle()
    val simulatedNotification by viewModel.simulatedIncomingNotification.collectAsStateWithLifecycle()
    
    // UI flow control
    var activeTab by remember { mutableStateOf("home") } // "home", "ai_search", "favorites"
    var showAddDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showAddOfferDialog by remember { mutableStateOf(false) }
    var showAddLiveDialog by remember { mutableStateOf(false) }
    var showAddFairProductDialog by remember { mutableStateOf(false) }
    
    // Toast-like notification
    var bannerMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            AppleLoginScreen(viewModel = viewModel)
        } else {
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = "Logo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "DIRECTORIO COMERCIAL",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                    Text(
                                        text = "Directorio Comercial y LOPII Sincronizado",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = loggedInEmail.substringBefore("@"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.logout() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Cerrar sesión",
                                            tint = Color(0xFFFF3B30),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                // App header elegant divider line matching Apple's sleek layout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        },
        bottomBar = {
            Column {
                // Subtle top divider for iOS TabBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .height(54.dp)
                ) {
                    NavigationBarItem(
                        selected = activeTab == "home",
                        onClick = { activeTab = "home" },
                        icon = { GlobeNotificationIcon(selected = activeTab == "home") },
                        label = { Text("Inicio", fontSize = 9.5.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = activeTab == "offers",
                        onClick = { activeTab = "offers" },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Ofertas", modifier = Modifier.size(20.dp)) },
                        label = { Text("Ofertas", fontSize = 9.5.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_offers")
                    )
                    NavigationBarItem(
                        selected = activeTab == "feria",
                        onClick = { activeTab = "feria" },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Feria", modifier = Modifier.size(20.dp)) },
                        label = { Text("Feria", fontSize = 9.5.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_feria")
                    )
                    NavigationBarItem(
                        selected = activeTab == "live",
                        onClick = { activeTab = "live" },
                        icon = { Icon(Icons.Default.Videocam, contentDescription = "Live", modifier = Modifier.size(20.dp)) },
                        label = { Text("Live", fontSize = 9.5.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_live")
                    )
                    NavigationBarItem(
                        selected = activeTab == "favorites",
                        onClick = { activeTab = "favorites" },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos", modifier = Modifier.size(20.dp)) },
                        label = { Text("Favoritos", fontSize = 9.5.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_favs")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    filteredBusinesses = filteredBusinesses,
                    currentCategory = currentCategory,
                    searchQuery = searchQuery,
                    syncingStates = syncingStates
                )
                "offers" -> OffersScreen(viewModel = viewModel)
                "feria" -> FairScreen(viewModel = viewModel)
                "live" -> LiveStreamsScreen(viewModel = viewModel)
                "favorites" -> FavoritesScreen(
                    viewModel = viewModel,
                    favorites = favoriteBusinesses
                )
            }

            // Syncing Status Banner (Shows when local business is syncing to "LOPII")
            if (syncingStates.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Actualizando directorio en la nube LOPII...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Business Detail Sheets Override (Displays when card is clicked)
            if (selectedBusiness != null) {
                DetailOverlay(
                    business = selectedBusiness!!,
                    reviews = selectedReviews,
                    onDismiss = { viewModel.selectBusiness(null) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onAddReviewClick = { showReviewDialog = true }
                )
            }

            // Registration Dialog for registering custom businesses
            if (showAddDialog) {
                AddBusinessDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { name, cat, address, desc, phone, hours ->
                        viewModel.addNewBusiness(name, cat, address, desc, phone, hours) {
                            showAddDialog = false
                            bannerMessage = "¡Negocio registrado de manera local! Sincronizando con LOPII..."
                        }
                    }
                )
            }

            // Review Adding Dialog
            if (showReviewDialog && selectedBusiness != null) {
                AddReviewDialog(
                    businessName = selectedBusiness!!.name,
                    onDismiss = { showReviewDialog = false },
                    onSave = { author, rating, comment ->
                        viewModel.submitReview(author, rating, comment)
                        showReviewDialog = false
                    }
                )
            }

            // Publish Offer Dialog
            if (showAddOfferDialog) {
                AddOfferDialog(
                    onDismiss = { showAddOfferDialog = false },
                    onPublish = { title, bizName, desc, city, addr, discount, cat ->
                        viewModel.publishOffer(title, bizName, desc, city, addr, discount, cat) {
                            showAddOfferDialog = false
                            bannerMessage = "¡Oferta publicada exitosamente!"
                        }
                    }
                )
            }

            // Publish Live Stream Broadcast Dialog
            if (showAddLiveDialog) {
                AddLiveDialog(
                    onDismiss = { showAddLiveDialog = false },
                    onPublish = { bizName, title, platform, handle, desc ->
                        viewModel.publishLiveStream(bizName, title, platform, handle, desc) {
                            showAddLiveDialog = false
                            bannerMessage = "¡Transmisión en vivo iniciada y enlazada!"
                        }
                    }
                )
            }

            // Publish Fair Product Dialog
            if (showAddFairProductDialog) {
                AddFairProductDialog(
                    onDismiss = { showAddFairProductDialog = false },
                    businesses = filteredBusinesses,
                    onPublish = { selectedBiz, prodName, price, desc, imgStyle ->
                        viewModel.publishFairProduct(selectedBiz, prodName, price, desc, imgStyle) {
                            showAddFairProductDialog = false
                            bannerMessage = "¡Producto publicado con éxito en la Feria!"
                        }
                    }
                )
            }
        }
    }
}

    // Custom Sliding push-style Notification Banner for simulated incoming verification codes on iOS/Apple
    AnimatedVisibility(
        visible = simulatedNotification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        simulatedNotification?.let { msg ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .clickable { viewModel.dismissIncomingNotification() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEC1C1C1E)), // Translucent dark style
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(0.5.dp, Color(0x3EFFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF007AFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Mail",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mail • Hace un momento",
                            color = Color(0xFFAAAAAA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = { viewModel.dismissIncomingNotification() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
}

// ==========================================
// HOME SCREEN VIEW
// ==========================================
@Composable
fun HomeScreen(
    viewModel: DirectoryViewModel,
    filteredBusinesses: List<Business>,
    currentCategory: String,
    searchQuery: String,
    syncingStates: Set<Int>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar (Material 3 Sleek Search View)
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Buscar en el directorio...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("local_search_input"),
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Dynamic Category Chips Scroll
        val categories = listOf("Todos", "Restaurantes", "Servicios", "Tiendas", "Salud", "Educación")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = currentCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outline,
                        borderWidth = 1.dp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Businesses List section
        if (filteredBusinesses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron negocios",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prueba con otra palabra clave o agrega un nuevo comercio pulsando el botón +",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.wrapContentSize()
                    )
                }
            }
        } else {
            Text(
                text = "RESULTADOS DE LOPII (${filteredBusinesses.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("business_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                items(filteredBusinesses, key = { it.id }) { business ->
                    val isSyncing = syncingStates.contains(business.id)
                    BusinessCard(
                        business = business,
                        isSyncing = isSyncing,
                        onClick = { viewModel.selectBusiness(business) },
                        onFavoriteClick = { viewModel.toggleFavorite(business) }
                    )
                }
            }
        }
    }
}

// ==========================================
// INDIVIDUAL BUSINESS LISTING CARD
// ==========================================
@Composable
fun BusinessCard(
    business: Business,
    isSyncing: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val categoryColor = when (business.category) {
        "Restaurantes" -> Color(0xFFFF5722)
        "Servicios" -> Color(0xFF2196F3)
        "Tiendas" -> Color(0xFF9C27B0)
        "Salud" -> Color(0xFF4CAF50)
        "Educación" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("business_card_${business.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon with colored background disc
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val categoryIcon = when (business.category) {
                        "Restaurantes" -> Icons.Default.Place
                        "Servicios" -> Icons.Default.Settings
                        "Tiendas" -> Icons.Default.ShoppingCart
                        "Salud" -> Icons.Default.Favorite
                        "Educación" -> Icons.Default.Info
                        else -> Icons.Default.Place
                    }
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = business.category,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Midsection: texts & names
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = business.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // Cloud Sync Badge
                        if (isSyncing) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(2.dp)
                            ) {
                                CircularProgressIndicator(strokeWidth = 1.5.dp)
                            }
                        } else if (!business.isSynced) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Offline", fontSize = 9.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Sincronizado",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${business.category} • ${business.address}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stars rating representation
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Puntuación",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(business.rating),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = business.hours,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Toggle heart favorite
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (business.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (business.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// ==========================================
// GEMINI INTELLIGENT SEARCH SCREEN VIEW
// ==========================================
@Composable
fun AiSearchScreen(viewModel: DirectoryViewModel) {
    val query by viewModel.aiSearchQuery.collectAsStateWithLifecycle()
    val searchResult by viewModel.aiSearchResult.collectAsStateWithLifecycle()
    val isSearching by viewModel.isAiSearching.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Búsqueda Comercial con IA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Busca de forma abierta. Ejemplo: 'Necesito tacos deliciosos que cierren tarde' o 'Encuéntrame un dentista recomendado'. Gemini consultará los comercios para darte las mejores opciones locales.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateAiSearchQuery(it) },
                placeholder = { Text("Pregúntale a la IA...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.performAiSearch() },
                enabled = query.isNotBlank() && !isSearching,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("ai_search_submit_button")
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI search output presentation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(16.dp)
        ) {
            if (isSearching) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Gemini está consultando el directorio comercial...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (searchResult != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = searchResult!!,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Realiza una consulta a la IA",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// ==========================================
// FAVORITES SCREEN VIEW
// ==========================================
@Composable
fun FavoritesScreen(viewModel: DirectoryViewModel, favorites: List<Business>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Mis Negocios Favoritos",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Planifica tus visitas guardando los mejores comercios con el símbolo del corazón",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tienes favoritos guardados",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(favorites, key = { it.id }) { business ->
                    BusinessCard(
                        business = business,
                        isSyncing = false,
                        onClick = { viewModel.selectBusiness(business) },
                        onFavoriteClick = { viewModel.toggleFavorite(business) }
                    )
                }
            }
        }
    }
}

// ==========================================
// BUSINESS DETAIL OVERLAY SHEET (WITH REVIEWS)
// ==========================================
@Composable
fun DetailOverlay(
    business: Business,
    reviews: List<Review>,
    onDismiss: () -> Unit,
    onToggleFavorite: (Business) -> Unit,
    onAddReviewClick: () -> Unit
) {
    val categoryColor = when (business.category) {
        "Restaurantes" -> Color(0xFFFF5722)
        "Servicios" -> Color(0xFF2196F3)
        "Tiendas" -> Color(0xFF9C27B0)
        "Salud" -> Color(0xFF4CAF50)
        "Educación" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clickable(enabled = false, onClick = {}) // prevent dismissing when tapping content
                .testTag("business_detail_sheet"),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Hero Color band with category visual weight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(categoryColor, categoryColor.copy(alpha = 0.8f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = { onToggleFavorite(business) },
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (business.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (business.isFavorite) Color.Red else Color.White
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = business.category,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (business.isSynced) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Conectado a LOPII (Nube)",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = business.name,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Star ranking & contact information pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "%.1f".format(business.rating),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Text("Calificación", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = business.hours,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text("Horas de Atención", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description paragraph
                        Text(
                            text = "Sobre el comercio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = business.description,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Contact attributes
                        Text(
                            text = "Contacto y Ubicación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = business.address, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = business.phone, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Simulated Map Placeholder Screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0F2F1))
                                .drawBehind {
                                    // Drawing a simple, cute map mock visualization
                                    drawCircle(
                                        color = Color(0xFFA5D6A7),
                                        radius = size.width * 0.15f,
                                        center = Offset(size.width * 0.3f, size.height * 0.5f)
                                    )
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(0f, size.height * 0.5f),
                                        end = Offset(size.width, size.height * 0.5f),
                                        strokeWidth = 8f
                                    )
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(size.width * 0.5f, 0f),
                                        end = Offset(size.width * 0.5f, size.height),
                                        strokeWidth = 8f
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = categoryColor, // Show pin in category color
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Mapa Sincronizado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Reviews Headings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Comentarios (${reviews.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = onAddReviewClick) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Agregar Reseña")
                                }
                            }
                        }
                    }

                    if (reviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Aún no hay opiniones. ¡Sé el primero en calificar este comercio!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        items(reviews) { review ->
                            ReviewItem(review = review)
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// INDIVIDUAL REVIEW LIST ITEM
// ==========================================
@Composable
fun ReviewItem(review: Review) {
    val dateString = remember(review.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(review.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = review.author,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = dateString,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Stars rated
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                val ratingIndex = index + 1
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (ratingIndex <= review.rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = review.comment,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 18.sp
        )
    }
}

// ==========================================
// ADDING A BUSINESS DIALOG MODAL
// ==========================================
@Composable
fun AddBusinessDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Restaurantes") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("9:00 AM - 6:00 PM") }

    val categories = listOf("Restaurantes", "Servicios", "Tiendas", "Salud", "Educación")
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Registrar Comercio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ingresa los detalles para agregarlo al directorio. Se sincronizará inmediatamente en LOPII.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Negocio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("add_input_name"),
                    singleLine = true
                )

                // Category Selection Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Categoría") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("add_input_address"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono de Contacto") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("add_input_phone"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Horario de Atención") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("add_input_hours"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción literaria del comercio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .testTag("add_input_desc"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && address.isNotBlank() && description.isNotBlank()) {
                                onSave(name, category, address, description, phone.ifBlank { "Sin tlf." }, hours.ifBlank { "9:00 AM - 6:00 PM" })
                            }
                        },
                        enabled = name.isNotBlank() && address.isNotBlank() && description.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_business_submit_btn")
                    ) {
                        Text("Registrar")
                    }
                }
            }
        }
    }
}

// ==========================================
// ADDING A REVIEW DIALOG MODAL
// ==========================================
@Composable
fun AddReviewDialog(
    businessName: String,
    onDismiss: () -> Unit,
    onSave: (String, Float, String) -> Unit
) {
    var author by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Dar tu Opinión",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "¿Cómo fue tu experiencia en '$businessName'?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Tu Nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_input_author"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Rating Stars selector
                Text(text = "Selecciona tu Calificación:", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) { index ->
                        val ratingValue = (index + 1).toFloat()
                        IconButton(onClick = { rating = ratingValue }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (ratingValue <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Escribe tu opinión...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("review_input_comment"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (comment.isNotBlank()) {
                                onSave(author.ifBlank { "Anónimo" }, rating, comment)
                            }
                        },
                        enabled = comment.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("review_submit_btn")
                    ) {
                        Text("Enviar")
                    }
                }
            }
        }
    }
}

// ==========================================
// OFFERS TAB SCREEN VIEW
// ==========================================
@Composable
fun OffersScreen(viewModel: DirectoryViewModel) {
    val offers by viewModel.allOffers.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ofertas Especiales",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Descubre las mejores promociones publicadas por comercios locales en tu ciudad",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (offers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay ofertas publicadas por el momento",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(offers, key = { it.id }) { offer ->
                    val imageResId = when (offer.imageResName) {
                        "img_offer_food" -> R.drawable.img_offer_food
                        "img_offer_shopping" -> R.drawable.img_offer_shopping
                        "img_offer_services" -> R.drawable.img_offer_services
                        else -> R.drawable.ic_launcher_background
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("offer_card_${offer.id}")
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                                Image(
                                    painter = painterResource(id = imageResId),
                                    contentDescription = offer.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Discount Percent Badge
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${offer.discountPercent}% OFF",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = offer.businessName.uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = offer.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = offer.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Ubicación",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = offer.city,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = offer.address,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ADD OFFER DIALOG VIEW
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOfferDialog(
    onDismiss: () -> Unit,
    onPublish: (title: String, businessName: String, description: String, city: String, address: String, discountPercent: Int, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("15") }
    var category by remember { mutableStateOf("Gastronomía") } // "Gastronomía", "Moda", "Servicios"

    var isMenuExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Gastronomía", "Moda", "Servicios")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_offer_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Publicar Oferta Especial",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la Oferta (ej. Hamburguesas 3x2)") },
                    modifier = Modifier.fillMaxWidth().testTag("offer_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nombre del Comercio") },
                    modifier = Modifier.fillMaxWidth().testTag("offer_business_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción / Condiciones") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Ciudad") },
                        modifier = Modifier.weight(1.2f).testTag("offer_city_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = discountPercent,
                        onValueChange = { discountPercent = it },
                        label = { Text("% Descuento") },
                        modifier = Modifier.weight(0.8f).testTag("offer_discount_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth().testTag("offer_address_input"),
                    singleLine = true
                )

                // Select image theme category
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = isMenuExpanded,
                        onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría de Imagen") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        isMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && businessName.isNotBlank() && city.isNotBlank() && address.isNotBlank()) {
                                onPublish(
                                    title,
                                    businessName,
                                    description,
                                    city,
                                    address,
                                    discountPercent.toIntOrNull() ?: 15,
                                    category
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = title.isNotBlank() && businessName.isNotBlank() && city.isNotBlank() && address.isNotBlank()
                    ) {
                        Text("Publicar")
                    }
                }
            }
        }
    }
}

// ==========================================
// LIVE STREAMS TAB SCREEN VIEW
// ==========================================
@Composable
fun LiveStreamsScreen(viewModel: DirectoryViewModel) {
    val lives by viewModel.activeLiveStreams.collectAsStateWithLifecycle()
    var selectedLiveForPlayback by remember { mutableStateOf<LiveStream?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Directos y Transmisiones",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Accede a las cuentas de TikTok, Facebook e Instagram de comercios locales en directo",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (lives.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay transmisiones en directo por ahora",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(lives, key = { it.id }) { live ->
                    // Platform Theme Configuration
                    val platformColor = when (live.socialPlatform.lowercase(Locale.getDefault())) {
                        "tiktok" -> Color(0xFF00F2FE) // TikTok Neon Cyan
                        "instagram" -> Color(0xFFE1306C) // Instagram Pink
                        "facebook" -> Color(0xFF1877F2) // Facebook Blue
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLiveForPlayback = live }
                            .testTag("live_card_${live.id}")
                    ) {
                        Column {
                            // Simulated Video Frame Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(Color(0xFF121212))
                            ) {
                                // Draw dynamic abstract network camera pattern using drawBehind
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            val w = size.width
                                            val h = size.height
                                            // Draw simulated audio soundwaves / mesh
                                            for (i in 1..8) {
                                                val waveH = ((kotlin.math.sin((i * 0.5f) + System.currentTimeMillis() * 0.001f) + 1f) * 20f)
                                                drawLine(
                                                    color = Color.White.copy(alpha = 0.15f),
                                                    start = Offset(x = (w / 9f) * i, y = h / 2f - waveH),
                                                    end = Offset(x = (w / 9f) * i, y = h / 2f + waveH),
                                                    strokeWidth = 4f
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = "TOCA PARA INSTANTE EN VIVO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.5f),
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                // Live Pulse Badge
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.TopStart)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Red)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "LIVE",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // Viewer count badge
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.BottomStart)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "👁  ${live.viewerCount} espectadores",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Platform Badge Indicator
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(platformColor)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = live.socialPlatform.uppercase(Locale.getDefault()),
                                        color = if (live.socialPlatform.lowercase() == "tiktok") Color.Black else Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // Info details
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = live.businessName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = live.socialHandle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = platformColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = live.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                if (live.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = live.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { selectedLiveForPlayback = live },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Unirse a Transmisión", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Open dynamic stream simulator overlay when clicked
    val activePlayback = selectedLiveForPlayback
    if (activePlayback != null) {
        LiveStreamPlayerOverlay(
            live = activePlayback,
            onClose = { selectedLiveForPlayback = null }
        )
    }
}

// ==========================================
// IMMERSIVE LIVE STREAM SIMULATOR VIEW (OVERLAY)
// ==========================================
@Composable
fun LiveStreamPlayerOverlay(
    live: LiveStream,
    onClose: () -> Unit
) {
    val platformColor = when (live.socialPlatform.lowercase(Locale.getDefault())) {
        "tiktok" -> Color(0xFF00F2FE)
        "instagram" -> Color(0xFFE1306C)
        "facebook" -> Color(0xFF1877F2)
        else -> MaterialTheme.colorScheme.primary
    }

    // Dynamic dynamic remarks & chat entries
    val mockCommentsList = listOf(
        "¡Hola! Saludos desde la zona antigua, su comida es lo máximo ❤️",
        "¿Hacen envíos a domicilio en toda la ciudad?",
        "¡Se ve súper sabrosa esa preparación! 😍",
        "¡Súper recomendado! Visité ayer y excelente trato.",
        "¿Tienen tallas disponibles para la ropa veraniega?",
        "Ese color me atrapó, ¿tienen stock?",
        "El servicio es insuperable de veras. ¡Qué buen live!",
        "¡Qué grande Giuseppe con las recetas!",
        "Transmitiendo con súper calidad de audio y video. 👏",
        "¿A qué hora cierran hoy?",
        "Totalmente de acuerdo con el chat, ¡lo mejoor!",
        "¿Hay descuentos adicionales si compramos directo del live?",
        "¡Uff esa parrilla de tacos se ve brutal! 😋",
        "Quiero ir este fin de semana sin falta con mis amigos.",
        "¡Se me hace agua la boca!"
    )

    val activeComments = remember { mutableStateListOf<String>() }
    // Initialize with two comments
    LaunchedEffect(Unit) {
        activeComments.add("¡Inició la transmisión en vivo! 🎉")
        activeComments.add("Conectando con la cuenta de ${live.socialPlatform}...")
        
        // Loop to post comments every 1.5 - 2.5 seconds
        while (true) {
            delay((1500..3000).random().toLong())
            val author = listOf("LuisG", "Marta_v", "Pedro99", "SofiaS", "Carlos_R", "Elena_M", "Juanito").random()
            val comment = mockCommentsList.random()
            activeComments.add("$author: $comment")
            if (activeComments.size > 20) {
                activeComments.removeAt(0)
            }
        }
    }

    // Floating heart coordinates
    val floatingHearts = remember { mutableStateListOf<FloatingHeart>() }
    var heartCounter by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F0F11))
        ) {
            // Simulated LIVE visual feed background drawing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val w = size.width
                        val h = size.height

                        // Dynamic animated circle waves
                        val time = (System.currentTimeMillis() * 0.0015).toFloat()
                        val radius = (kotlin.math.sin(time) * 40f + 180f)
                        
                        drawCircle(
                            color = platformColor.copy(alpha = 0.04f),
                            radius = radius,
                            center = Offset(w / 2f, h * 0.4f)
                        )
                        drawCircle(
                            color = platformColor.copy(alpha = 0.07f),
                            radius = radius * 0.6f,
                            center = Offset(w / 2f, h * 0.4f)
                        )

                        // Outer camera frame corner design lines
                        val pad = 40f
                        val len = 60f
                        val stroke = 4f
                        // Top Left
                        drawLine(Color.White.copy(0.4f), Offset(pad, pad), Offset(pad + len, pad), stroke)
                        drawLine(Color.White.copy(0.4f), Offset(pad, pad), Offset(pad, pad + len), stroke)
                        // Top Right
                        drawLine(Color.White.copy(0.4f), Offset(w - pad, pad), Offset(w - pad - len, pad), stroke)
                        drawLine(Color.White.copy(0.4f), Offset(w - pad, pad), Offset(w - pad, pad + len), stroke)
                        // Bottom Left
                        drawLine(Color.White.copy(0.4f), Offset(pad, h - pad), Offset(pad + len, h - pad), stroke)
                        drawLine(Color.White.copy(0.4f), Offset(pad, h - pad), Offset(pad, h - pad - len), stroke)
                        // Bottom Right
                        drawLine(Color.White.copy(0.4f), Offset(w - pad, h - pad), Offset(w - pad - len, h - pad), stroke)
                        drawLine(Color.White.copy(0.4f), Offset(w - pad, h - pad), Offset(w - pad, h - pad - len), stroke)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Audio Wave Visualizer in the Center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(platformColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = platformColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "TRANSMITIENDO STREAM EN VIVO",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Vínculo social: ${live.socialPlatform} [${live.socialHandle}]",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Top Header: Video Info Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Back/Close Button
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(0.5f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = live.businessName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EN VIVO",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "👁 ${live.viewerCount + heartCounter} espectadores",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Banner Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(0.6f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = live.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        if (live.description.isNotEmpty()) {
                            Text(
                                text = live.description,
                                color = Color.White.copy(0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Floating Hearts Screen Overlay
            Box(modifier = Modifier.fillMaxSize()) {
                floatingHearts.forEach { heart ->
                    // Animated Upward Motion for hearts
                    val animatedY = remember { Animatable(0f) }
                    val animatedAlpha = remember { Animatable(1f) }

                    LaunchedEffect(heart.id) {
                        launch {
                            animatedY.animateTo(
                                targetValue = -400f,
                                animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
                            )
                            // Remove heart when finished
                            floatingHearts.remove(heart)
                        }
                        launch {
                            delay(1200)
                            animatedAlpha.animateTo(0f, tween(800))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(
                                x = heart.startX.dp - 60.dp,
                                y = animatedY.value.dp - 100.dp
                            )
                            .alpha(animatedAlpha.value)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = heart.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Chat Panel & Quick Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // Real-time Chat List Box (Takes most height in bottom area)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(0.5f))
                        .padding(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        reverseLayout = false
                    ) {
                        items(activeComments) { line ->
                            val parts = line.split(": ", limit = 2)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(0.08f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row {
                                    if (parts.size == 2) {
                                        Text(
                                            text = "${parts[0]}: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = platformColor
                                        )
                                        Text(
                                            text = parts[1],
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(
                                            text = line,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Light
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Open External Channel Links
                    Button(
                        onClick = {
                            // simulated redirection
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, 
                            contentDescription = null, 
                            tint = if (live.socialPlatform.lowercase() == "tiktok") Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Abrir en ${live.socialPlatform}",
                            fontWeight = FontWeight.Bold,
                            color = if (live.socialPlatform.lowercase() == "tiktok") Color.Black else Color.White,
                            fontSize = 12.sp
                        )
                    }

                    // Floating Heart Reaction Trigger Button
                    IconButton(
                        onClick = {
                            heartCounter++
                            val colors = listOf(Color(0xFFE1306C), Color(0xFF00F2FE), Color(0xFF1877F2), Color.Red, Color.Yellow)
                            floatingHearts.add(
                                FloatingHeart(
                                    id = System.nanoTime(),
                                    startX = (-60..20).random().toFloat(),
                                    color = colors.random()
                                )
                            )
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(0.15f)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Reacción de Corazón",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

data class FloatingHeart(val id: Long, val startX: Float, val color: Color)

// ==========================================
// ADD STREAM / BROADCAST DIALOG VIEW
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLiveDialog(
    onDismiss: () -> Unit,
    onPublish: (businessName: String, title: String, socialPlatform: String, socialHandle: String, description: String) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var socialPlatform by remember { mutableStateOf("TikTok") }  // "TikTok", "Instagram", "Facebook"
    var socialHandle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var isMenuExpanded by remember { mutableStateOf(false) }
    val platforms = listOf("TikTok", "Instagram", "Facebook")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_live_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enlazar y Transmitir En Vivo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Vincula la cuenta de tu negocio para mostrar un directo interactivo en la pestaña de la comunidad",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nombre del Comercio") },
                    modifier = Modifier.fillMaxWidth().testTag("live_business_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Directo (ej. Cocinando pizza en vivo)") },
                    modifier = Modifier.fillMaxWidth().testTag("live_title_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Platform Dropdown Selection
                    Box(modifier = Modifier.weight(1.2f)) {
                        ExposedDropdownMenuBox(
                            expanded = isMenuExpanded,
                            onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = socialPlatform,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Plataforma") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false }
                            ) {
                                platforms.forEach { plat ->
                                    DropdownMenuItem(
                                        text = { Text(plat) },
                                        onClick = {
                                            socialPlatform = plat
                                            isMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = socialHandle,
                        onValueChange = { socialHandle = it },
                        label = { Text("Usuario/Enlace") },
                        placeholder = { Text("@usuario") },
                        modifier = Modifier.weight(1.2f).testTag("live_handle_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción de la transmisión") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (businessName.isNotBlank() && title.isNotBlank() && socialHandle.isNotBlank()) {
                                onPublish(
                                    businessName,
                                    title,
                                    socialPlatform,
                                    socialHandle,
                                    description
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = businessName.isNotBlank() && title.isNotBlank() && socialHandle.isNotBlank()
                    ) {
                        Text("Iniciar")
                    }
                }
            }
        }
    }
}

// ==========================================
// VIRTUAL FAIR (FERIA) VIEW
// ==========================================
@Composable
fun FairScreen(viewModel: DirectoryViewModel) {
    val fairProducts by viewModel.allFairProducts.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleTab by remember { mutableStateOf(0) } // 0: Espectador, 1: Sobre la Feria

    val filteredProducts = remember(fairProducts, searchQuery) {
        if (searchQuery.isBlank()) {
            fairProducts
        } else {
            fairProducts.filter {
                it.productName.contains(searchQuery, ignoreCase = true) ||
                it.productDescription.contains(searchQuery, ignoreCase = true) ||
                it.businessName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic Fair Hero Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(vertical = 20.dp, horizontal = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Feria",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Feria Virtual de Productos 🎪",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Los comercios publican sus productos en tiempo real. ¡Tú especteas interactuando!",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${fairProducts.size}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                        Text(text = "Productos", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🟢 145",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            fontSize = 15.sp
                        )
                        Text(text = "Vecinos Mirando", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Custom tabs for role exploration
        TabRow(
            selectedTabIndex = selectedRoleTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedRoleTab == 0,
                onClick = { selectedRoleTab = 0 },
                text = { Text("🛒 Catálogo (Espectador)") }
            )
            Tab(
                selected = selectedRoleTab == 1,
                onClick = { selectedRoleTab = 1 },
                text = { Text("ℹ️ ¿Cómo funciona?") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedRoleTab == 0) {
            // Search Input Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar productos o marcas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("fair_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Aún no hay productos expuestos" else "No se encontraron resultados",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Los productos son actualizados en vivo por los comercios locales.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    gridItems(filteredProducts) { product ->
                        FairProductCard(product = product, onLike = { viewModel.likeFairProduct(product.id) })
                    }
                }
            }
        } else {
            // "About the Virtual Fair" detailed guidelines page
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Una Feria Interactiva Digital 🚀",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                item {
                    Text(
                        text = "La Feria Virtual de Productos de Barrio permite conectar a los negocios representativos registrados de la comunidad directamente con los clientes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🔒 Herencia Automática de Datos",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Al registrar un nuevo producto en los stands del mercado virtual, el sistema hereda automáticamente la marca o nombre comercial, la ubicación del local y el teléfono oficial directo del negocio seleccionado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "👀 Los Usuarios sólo Espectean",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mantenemos una vista limpia de espectador. Los clientes exploran el mural del mercado local, observando productos increíbles asiduamente y usando las interacciones instantáneas como 'Me Interesa 💖' para apoyar el comercio local.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun FairProductCard(product: com.example.data.FairProduct, onLike: () -> Unit) {
    val context = LocalContext.current
    var isLiked by remember(product.id) { mutableStateOf(false) }
    val animatedLikesScale = remember { Animatable(1f) }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fair_product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column {
            // Product visual image box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        when (product.productImageResName.lowercase()) {
                            "pizza" -> Brush.verticalGradient(listOf(Color(0xFFFF9800), Color(0xFFE65100)))
                            "tacos" -> Brush.verticalGradient(listOf(Color(0xFFF44336), Color(0xFFD84315)))
                            "cafe" -> Brush.verticalGradient(listOf(Color(0xFF8D6E63), Color(0xFF4E342E)))
                            "herramientas" -> Brush.verticalGradient(listOf(Color(0xFF78909C), Color(0xFF37474F)))
                            "dientes" -> Brush.verticalGradient(listOf(Color(0xFF00ACC1), Color(0xFF006064)))
                            "gimnasio" -> Brush.verticalGradient(listOf(Color(0xFF7E57C2), Color(0xFF4527A0)))
                            "clases" -> Brush.verticalGradient(listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)))
                            else -> Brush.verticalGradient(listOf(Color(0xFF009688), Color(0xFF004D40)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Large product icon representing the product creatively and responsively
                Icon(
                    imageVector = when (product.productImageResName.lowercase()) {
                        "pizza" -> Icons.Default.LocalPizza
                        "tacos" -> Icons.Default.Restaurant
                        "cafe" -> Icons.Default.Coffee
                        "herramientas" -> Icons.Default.Build
                        "dientes" -> Icons.Default.HealthAndSafety
                        "gimnasio" -> Icons.Default.FitnessCenter
                        "clases" -> Icons.Default.School
                        else -> Icons.Default.Store
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(68.dp)
                )

                // Floating Price badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = product.productPrice,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Floating "Me interesa" dynamic badge on the bottom left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${product.likesCount + if (isLiked) 1 else 0}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Info body
            Column(modifier = Modifier.padding(16.dp)) {
                // Product Name
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Description
                Text(
                    text = product.productDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(12.dp))

                // Inherited official credentials (colocando automáticamente la marca, ubicación y contacto)
                Text(
                    text = "STAND DE MARCA LOCAL AUTENTICADA",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 🏷️ Marca Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.businessName,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 📍 Ubicación Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.businessAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // 📞 Contacto Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:${product.businessPhone}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Copy to clipboard fallback if dial is not available
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Teléfono Comercio", product.businessPhone)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Teléfono ${product.businessPhone} copiado", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.businessPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(Llamar)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Spectator interaction button ("Me interesa 💖")
                    Button(
                        onClick = {
                            if (!isLiked) {
                                isLiked = true
                                onLike()
                                // Play short scale pop effect
                                kotlinx.coroutines.GlobalScope.launch {
                                    animatedLikesScale.animateTo(1.25f, tween(150, easing = FastOutSlowInEasing))
                                    animatedLikesScale.animateTo(1.0f, tween(150, easing = LinearOutSlowInEasing))
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiked) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isLiked) Color(0xFFC62828) else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .scale(animatedLikesScale.value)
                            .testTag("fair_like_btn_${product.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isLiked) "¡Me Interesa!" else "Me Interesa", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFairProductDialog(
    onDismiss: () -> Unit,
    businesses: List<Business>,
    onPublish: (business: Business, productName: String, price: String, description: String, imageResName: String) -> Unit
) {
    var selectedBusinessIndex by remember { mutableStateOf(-1) }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var selectedImageStyle by remember { mutableStateOf("pizza") } // "pizza", "tacos", "cafe", "herramientas", "dientes", "gimnasio", "clases", "general"

    var isMenuExpanded by remember { mutableStateOf(false) }

    val imageStyles = listOf(
        Pair("pizza", "🍕 Pizza"),
        Pair("tacos", "🌮 Tacos"),
        Pair("cafe", "☕ Café"),
        Pair("herramientas", "🛠️ Extras"),
        Pair("dientes", "🦷 Dental"),
        Pair("gimnasio", "🏋️ Deporte"),
        Pair("clases", "🎓 Clases"),
        Pair("general", "🛍️ Tienda")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("add_fair_product_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Publicar en Feria 🎪",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Vincula tu producto a un comercio para auto-generar marca, dirección y contacto garantizados.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Business Spinner Category Selection (Merchants choose their shop)
                Text(
                    text = "Selecciona tu Comercio Oficial",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = isMenuExpanded,
                        onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (selectedBusinessIndex >= 0 && selectedBusinessIndex < businesses.size) {
                                businesses[selectedBusinessIndex].name
                            } else {
                                "Seleccionar mi negocio..."
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Medium)
                        )
                        ExposedDropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            businesses.forEachIndexed { idx, biz ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(biz.name, fontWeight = FontWeight.Bold)
                                            Text(biz.address, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                    },
                                    onClick = {
                                        selectedBusinessIndex = idx
                                        isMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Automatic credentials show (colocando automáticamente la marca, ubicación y contacto)
                if (selectedBusinessIndex >= 0 && selectedBusinessIndex < businesses.size) {
                    val biz = businesses[selectedBusinessIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Marca Local Confirmada", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(text = "• Marca: ${biz.name}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "• Ubicación: ${biz.address}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "• Contacto: ${biz.phone}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nombre del Producto") },
                    placeholder = { Text("ej. Pizza Peperoni Extra") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fair_prod_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = { Text("Precio") },
                    placeholder = { Text("ej. $12.50") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fair_prod_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("ej. Con masa crujiente y bastante queso...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .testTag("fair_prod_desc_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // Image style selection (Simulated live upload style)
                Text(
                    text = "Foto del Producto",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(imageStyles) { (styleKey, styleLabel) ->
                        val isSelected = selectedImageStyle == styleKey
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { selectedImageStyle = styleKey }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = styleLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedBusinessIndex >= 0 && selectedBusinessIndex < businesses.size &&
                                productName.isNotBlank() && productPrice.isNotBlank()
                            ) {
                                onPublish(
                                    businesses[selectedBusinessIndex],
                                    productName,
                                    productPrice,
                                    productDescription,
                                    selectedImageStyle
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = selectedBusinessIndex >= 0 && productName.isNotBlank() && productPrice.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Publicar en Feria")
                    }
                }
            }
        }
    }
}

@Composable
fun GlobeNotificationIcon(selected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlobeNotification")
    
    // Pulse animation for the notification badge scale
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeScale"
    )

    // Halo pulse alpha for the notification glow
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HaloAlpha"
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HaloScale"
    )

    // Periodic iOS Shake / Wobble effect triggered every 5 seconds to get user's attention
    var shouldShake by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            shouldShake = true
            delay(800) // Shake duration
            shouldShake = false
        }
    }

    // Interactive bounce when the user selects or clicks this tab
    val clickScale by animateFloatAsState(
        targetValue = if (selected) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ClickBounce"
    )

    // iOS notification shake rotations (a quick wobble back & forth)
    val wobbleAngle by infiniteTransition.animateFloat(
        initialValue = if (shouldShake) -8f else 0f,
        targetValue = if (shouldShake) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WobbleAngle"
    )

    // iOS notification shake vertical bounce
    val bounceY by infiniteTransition.animateFloat(
        initialValue = if (shouldShake) -3f else 0f,
        targetValue = if (shouldShake) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WobbleBounceY"
    )

    Box(
        modifier = Modifier
            .size(30.dp)
            .graphicsLayer {
                scaleX = clickScale
                scaleY = clickScale
                rotationZ = if (shouldShake) wobbleAngle else 0f
                translationY = if (shouldShake) bounceY.dp.toPx() else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        // Globe Map icon (El Mundo / Mapa Mundi)
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = "Inicio",
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )

        // Halo Glowing background effect like advanced iOS badge glow
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 1.dp, y = (-1).dp)
                .size(10.dp)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                    alpha = haloAlpha
                }
                .background(Color(0xFFFF3B30), CircleShape)
        )

        // Main iOS Red Notification Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 1.dp, y = (-1).dp)
                .graphicsLayer {
                    scaleX = badgeScale
                    scaleY = badgeScale
                }
                .background(Color(0xFFFF3B30), CircleShape) // iOS Red Color
                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .padding(horizontal = 3.dp, vertical = 0.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "3", // Active live events / new products / offers inside directory
                color = Color.White,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun AppleLoginScreen(viewModel: DirectoryViewModel) {
    var emailInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isSendingCode by viewModel.isSendingCode.collectAsStateWithLifecycle()
    val approvedEmails by viewModel.firebaseApprovedEmails.collectAsStateWithLifecycle()
    var showFirebaseConsole by remember { mutableStateOf(false) } // default false
    var devClickCount by remember { mutableStateOf(0) }
    val isDevMode = devClickCount >= 5
    var newApprovedEmailInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Apple iOS Style Lock Silhouette
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Seguridad",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (!isCodeSent) "Verificación de Identidad" else "Confirmar Código",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (!isCodeSent)
                    "Ingresa tu correo electrónico para recibir un código de acceso único de 6 dígitos."
                else
                    "Por tu seguridad, ingresa el código que acabamos de enviar a:\n$emailInput",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (!isCodeSent) {
                        Text(
                            text = "CORREO ELECTRÓNICO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            placeholder = { Text("ejemplo@correo.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                viewModel.sendVerificationCode(
                                    email = emailInput,
                                    onSent = {
                                        isCodeSent = true
                                    },
                                    onError = { err ->
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = emailInput.isNotBlank() && !isSendingCode,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("auth_send_code_btn")
                        ) {
                            if (isSendingCode) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Enviar Código",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "CÓDIGO DE ACCESO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = {
                                codeInput = it
                                errorMessage = null
                            },
                            placeholder = { Text("Ej. 123456") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Código",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_code_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.verifyCode(
                                    codeEntered = codeInput,
                                    onSuccess = {
                                        // Login successful, automatic flow transition
                                    },
                                    onError = { err ->
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = codeInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("auth_verify_btn")
                        ) {
                            Text(
                                "Confirmar e Ingresar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                isCodeSent = false
                                codeInput = ""
                                errorMessage = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "Modificar correo electrónico",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF3B30),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (isDevMode) {
                Spacer(modifier = Modifier.height(20.dp))

                // Simulated Firebase Cloud Console Box (Aceptación de usuarios)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFirebaseConsole = !showFirebaseConsole },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Firebase",
                                    tint = Color(0xFFFFCA28), // Firebase Yellow color
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Consola Firebase: Control de Acceso",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                             )
                        }

                        if (showFirebaseConsole) {
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = "En producción, la aceptación-registro de correos se administra en la Base de Datos Firebase. Agrega o elimina correos autorizados para probar la seguridad:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick inline add email input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newApprovedEmailInput,
                                    onValueChange = { newApprovedEmailInput = it },
                                    placeholder = { Text("nuevo@correo.com", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFFCA28),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (viewModel.addApprovedEmail(newApprovedEmailInput)) {
                                            newApprovedEmailInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCA28)),
                                    modifier = Modifier.height(38.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Aprobar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "CORREOS APROBADOS EN BD:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 110.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                approvedEmails.forEach { email ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Aceptado",
                                                tint = Color(0xFF34C759),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = email,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (email != "alenarexport@gmail.com") {
                                            IconButton(
                                                onClick = { viewModel.removeApprovedEmail(email) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    tint = Color(0xFFFF3B30),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Seguridad de Identidad Protegida • LOPII",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    devClickCount++
                    if (devClickCount == 5) {
                        showFirebaseConsole = true
                    }
                }
            )
        }
    }
}



