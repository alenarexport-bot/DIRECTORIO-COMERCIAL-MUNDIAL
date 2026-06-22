package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Business
import com.example.data.BusinessRepository
import com.example.data.Review
import com.example.data.Offer
import com.example.data.LiveStream
import com.example.data.FairProduct
import com.example.network.GeminiApiClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DirectoryViewModel(private val repository: BusinessRepository) : ViewModel() {

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInEmail = MutableStateFlow("")
    val loggedInEmail: StateFlow<String> = _loggedInEmail.asStateFlow()

    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()

    private val _emailSentTo = MutableStateFlow("")
    val emailSentTo: StateFlow<String> = _emailSentTo.asStateFlow()

    private val _isSendingCode = MutableStateFlow(false)
    val isSendingCode: StateFlow<Boolean> = _isSendingCode.asStateFlow()

    // Firebase Simulated Authorized/Accepted User Emails Representation
    private val _firebaseApprovedEmails = MutableStateFlow<List<String>>(listOf(
        "alenarexport@gmail.com",
        "admin@lopii.com",
        "prueba@lopii.com",
        "usuario@lopii.com"
    ))
    val firebaseApprovedEmails: StateFlow<List<String>> = _firebaseApprovedEmails.asStateFlow()

    // Shared UI notifications (e.g. system incoming email simulation)
    private val _simulatedIncomingNotification = MutableStateFlow<String?>(null)
    val simulatedIncomingNotification: StateFlow<String?> = _simulatedIncomingNotification.asStateFlow()

    fun addApprovedEmail(email: String): Boolean {
        val trimmed = email.trim().lowercase()
        if (trimmed.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) return false
        if (_firebaseApprovedEmails.value.contains(trimmed)) return true
        _firebaseApprovedEmails.value = _firebaseApprovedEmails.value + trimmed
        return true
    }

    fun removeApprovedEmail(email: String) {
        _firebaseApprovedEmails.value = _firebaseApprovedEmails.value.filter { it != email }
    }

    fun sendVerificationCode(email: String, onSent: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _simulatedIncomingNotification.value = "⚠️ Error: Por favor introduce un correo electrónico válido."
            viewModelScope.launch {
                delay(3000)
                _simulatedIncomingNotification.value = null
            }
            onError("Por favor introduce un correo electrónico válido.")
            return
        }

        val normalized = email.trim().lowercase()
        val isApproved = _firebaseApprovedEmails.value.any { it.trim().lowercase() == normalized }
        if (!isApproved) {
            _simulatedIncomingNotification.value = "⚠️ Acceso Denegado: No está aceptado en Firebase"
            viewModelScope.launch {
                delay(4000)
                _simulatedIncomingNotification.value = null
            }
            onError("Acceso Denegado: Tu dirección de correo no ha sido aprobada ni registrada en la consola de Firebase. Por favor agrégala arriba en el simulador de Firebase.")
            return
        }

        viewModelScope.launch {
            _isSendingCode.value = true
            delay(1200) // Realistic simulation delay
            val code = (1000..9999).random().toString() // Modern 4-digit or 6-digit pin. Let's use 4 digit (e.g. 5824) or 6 digit
            val sixDigitCode = (100000..999999).random().toString()
            _verificationCode.value = sixDigitCode
            _emailSentTo.value = email
            _isSendingCode.value = false
            onSent()
            
            // Pop up a beautiful iOS notification to mock the email delivery
            delay(300)
            _simulatedIncomingNotification.value = "✉️ Mail: Tu código de verificación es $sixDigitCode"
        }
    }

    fun verifyCode(codeEntered: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (codeEntered.trim() == _verificationCode.value && _verificationCode.value.isNotEmpty()) {
            _isLoggedIn.value = true
            _loggedInEmail.value = _emailSentTo.value
            _simulatedIncomingNotification.value = null
            onSuccess()
        } else {
            onError("Código incorrecto, por favor verifica el código recibido.")
        }
    }

    fun dismissIncomingNotification() {
        _simulatedIncomingNotification.value = null
    }

    fun logout() {
        _isLoggedIn.value = false
        _loggedInEmail.value = ""
        _verificationCode.value = ""
        _emailSentTo.value = ""
    }

    // Selected category for manual list filtering
    private val _currentCategory = MutableStateFlow("Todos")
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    // Local manual search query text
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Currently selected business for detailed view
    private val _selectedBusiness = MutableStateFlow<Business?>(null)
    val selectedBusiness: StateFlow<Business?> = _selectedBusiness.asStateFlow()

    // Reviews list of the currently selected business
    val selectedBusinessReviews: StateFlow<List<Review>> = _selectedBusiness
        .flatMapLatest { business ->
            if (business != null) {
                repository.getReviewsForBusiness(business.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of business IDs currently mimicking and performing a background Firestore Cloud Sync of LOPII
    private val _syncingStates = MutableStateFlow<Set<Int>>(emptySet())
    val syncingStates: StateFlow<Set<Int>> = _syncingStates.asStateFlow()

    // All active businesses from local database Room, dynamically filtered
    val filteredBusinesses: StateFlow<List<Business>> = combine(
        repository.allBusinesses,
        _currentCategory,
        _searchQuery
    ) { all, category, query ->
        var list = all
        if (category != "Todos") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorite businesses list
    val favoriteBusinesses: StateFlow<List<Business>> = repository.favoriteBusinesses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active promotional offers from database
    val allOffers: StateFlow<List<Offer>> = repository.allOffers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active video live streams from database
    val activeLiveStreams: StateFlow<List<LiveStream>> = repository.activeLiveStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active products in the local community virtual Fair (Feria)
    val allFairProducts: StateFlow<List<FairProduct>> = repository.allFairProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini Smart Search state
    private val _aiSearchQuery = MutableStateFlow("")
    val aiSearchQuery: StateFlow<String> = _aiSearchQuery.asStateFlow()

    private val _aiSearchResult = MutableStateFlow<String?>(null)
    val aiSearchResult: StateFlow<String?> = _aiSearchResult.asStateFlow()

    private val _isAiSearching = MutableStateFlow(false)
    val isAiSearching: StateFlow<Boolean> = _isAiSearching.asStateFlow()

    init {
        viewModelScope.launch {
            // Guarantee that standard high value businesses populate Room on launch!
            repository.prepopulateIfEmpty()
        }
    }

    fun selectCategory(category: String) {
        _currentCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateAiSearchQuery(query: String) {
        _aiSearchQuery.value = query
    }

    fun clearAiSearch() {
        _aiSearchQuery.value = ""
        _aiSearchResult.value = null
        _isAiSearching.value = false
    }

    fun selectBusiness(business: Business?) {
        _selectedBusiness.value = business
    }

    fun toggleFavorite(business: Business) {
        viewModelScope.launch {
            repository.toggleFavorite(business.id, business.isFavorite)
            // If the selected business is active, update our reference
            val active = _selectedBusiness.value
            if (active != null && active.id == business.id) {
                _selectedBusiness.value = active.copy(isFavorite = !active.isFavorite)
            }
        }
    }

    /**
     * Executes advanced Gemini powered business lookup grounding search
     */
    fun performAiSearch() {
        val query = _aiSearchQuery.value
        if (query.isBlank()) return

        viewModelScope.launch {
            _isAiSearching.value = true
            _aiSearchResult.value = null
            // Feed local directory data to Gemini to obtain context-aware recommendations
            val currentList = repository.allBusinesses.first()
            val response = GeminiApiClient.searchBusinessAI(query, currentList)
            _aiSearchResult.value = response
            _isAiSearching.value = false
        }
    }

    /**
     * Submit a new business into database Room, then trigger Firebase cloud synchronization simulation
     */
    fun addNewBusiness(
        name: String,
        category: String,
        address: String,
        description: String,
        phone: String,
        hours: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val imgRes = when (category) {
                "Restaurantes" -> "restaurant"
                "Servicios" -> "construction"
                "Tiendas" -> "shopping"
                "Salud" -> "health"
                "Educación" -> "school"
                else -> "general"
            }

            val newBusiness = Business(
                name = name,
                category = category,
                address = address,
                description = description,
                phone = phone,
                hours = hours,
                rating = 5.0f, // Initial default rating
                imageResName = imgRes,
                isSynced = false,
                isCustom = true
            )

            val insertedId = repository.insertBusiness(newBusiness).toInt()
            onSuccess()

            // Run realistic LOPII syncing process!
            mimicLopiiCloudSync(insertedId)
        }
    }

    /**
     * Simulates Firebase "LOPII" cloud synchronization with visual reactive state
     */
    private fun mimicLopiiCloudSync(businessId: Int) {
        viewModelScope.launch {
            _syncingStates.value = _syncingStates.value + businessId
            // Simulate networking delay to cloud server
            delay(2500)
            repository.updateSyncStatus(businessId, isSynced = true)
            _syncingStates.value = _syncingStates.value - businessId

            // If selected business is the synced one, reflect it
            val active = _selectedBusiness.value
            if (active != null && active.id == businessId) {
                _selectedBusiness.value = active.copy(isSynced = true)
            }
        }
    }

    /**
     * Publish a new promotional offer into Room database
     */
    fun publishOffer(
        title: String,
        businessName: String,
        description: String,
        city: String,
        address: String,
        discountPercent: Int,
        imageCategory: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val imgResName = when (imageCategory) {
                "Gastronomía" -> "img_offer_food"
                "Moda" -> "img_offer_shopping"
                "Servicios" -> "img_offer_services"
                else -> "img_offer_food"
            }

            val newOffer = Offer(
                title = title,
                businessName = businessName,
                description = description,
                city = city,
                address = address,
                discountPercent = discountPercent,
                imageResName = imgResName
            )

            repository.insertOffer(newOffer)
            onSuccess()
        }
    }

    /**
     * Publish a new live stream broadcast linked to social media
     */
    fun publishLiveStream(
        businessName: String,
        title: String,
        socialPlatform: String,
        socialHandle: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val randomViewerCount = (45..450).random()
            val newLive = LiveStream(
                businessName = businessName,
                title = title,
                socialPlatform = socialPlatform,
                socialHandle = socialHandle,
                viewerCount = randomViewerCount,
                description = description
            )
            repository.insertLiveStream(newLive)
            onSuccess()
        }
    }

    /**
     * Publish a product to the community Virtual Fair (Feria).
     * The business brand (name), location (address), and contact (phone) are
     * automatically inherited from the selected Business model.
     */
    fun publishFairProduct(
        business: Business,
        productName: String,
        productPrice: String,
        productDescription: String,
        productImageResName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val newProd = FairProduct(
                businessId = business.id,
                businessName = business.name,
                businessAddress = business.address,
                businessPhone = business.phone,
                productName = productName,
                productPrice = productPrice,
                productDescription = productDescription,
                productImageResName = productImageResName,
                likesCount = 0
            )
            repository.insertFairProduct(newProd)
            onSuccess()
        }
    }

    /**
     * Increments the spectator interest likes count for a specific Fair product
     */
    fun likeFairProduct(productId: Int) {
        viewModelScope.launch {
            repository.likeFairProduct(productId)
        }
    }

    /**
     * Adds an authentic review to a specific business
     */
    fun submitReview(author: String, rating: Float, comment: String) {
        val active = _selectedBusiness.value ?: return
        viewModelScope.launch {
            val review = Review(
                businessId = active.id,
                author = author.ifBlank { "Anónimo" },
                rating = rating,
                comment = comment
            )
            repository.insertReview(review)

            // Refresh selected business so it shows updated rating instantly
            val updated = repository.allBusinesses.first().firstOrNull { it.id == active.id }
            if (updated != null) {
                _selectedBusiness.value = updated
            }
        }
    }
}

class DirectoryViewModelFactory(private val repository: BusinessRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DirectoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DirectoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
