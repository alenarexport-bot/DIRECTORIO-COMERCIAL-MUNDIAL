package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class BusinessRepository(private val database: AppDatabase) {
    private val businessDao = database.businessDao()
    private val reviewDao = database.reviewDao()
    private val offerDao = database.offerDao()
    private val liveStreamDao = database.liveStreamDao()
    private val fairProductDao = database.fairProductDao()

    val allBusinesses: Flow<List<Business>> = businessDao.getAllBusinesses()
    val favoriteBusinesses: Flow<List<Business>> = businessDao.getFavoriteBusinesses()
    val allOffers: Flow<List<Offer>> = offerDao.getAllOffers()
    val activeLiveStreams: Flow<List<LiveStream>> = liveStreamDao.getActiveLiveStreams()
    val allFairProducts: Flow<List<FairProduct>> = fairProductDao.getAllFairProducts()

    fun getBusinessesByCategory(category: String): Flow<List<Business>> {
        return if (category == "Todos") {
            businessDao.getAllBusinesses()
        } else {
            businessDao.getBusinessesByCategory(category)
        }
    }

    fun getReviewsForBusiness(businessId: Int): Flow<List<Review>> {
        return reviewDao.getReviewsForBusiness(businessId)
    }

    suspend fun insertBusiness(business: Business): Long {
        return businessDao.insertBusiness(business)
    }

    suspend fun insertReview(review: Review) {
        reviewDao.insertReview(review)
        // Recalculate average rating of business!
        recalculateBusinessRating(review.businessId)
    }

    suspend fun deleteBusiness(business: Business) {
        businessDao.deleteBusiness(business)
    }

    suspend fun insertOffer(offer: Offer): Long {
        return offerDao.insertOffer(offer)
    }

    suspend fun deleteOffer(offer: Offer) {
        offerDao.deleteOffer(offer)
    }

    suspend fun insertLiveStream(liveStream: LiveStream): Long {
        return liveStreamDao.insertLiveStream(liveStream)
    }

    suspend fun updateLiveStream(liveStream: LiveStream) {
        liveStreamDao.updateLiveStream(liveStream)
    }

    suspend fun deleteLiveStream(liveStream: LiveStream) {
        liveStreamDao.deleteLiveStream(liveStream)
    }

    suspend fun insertFairProduct(product: FairProduct): Long {
        return fairProductDao.insertFairProduct(product)
    }

    suspend fun likeFairProduct(productId: Int) {
        fairProductDao.likeProduct(productId)
    }

    suspend fun deleteFairProduct(product: FairProduct) {
        fairProductDao.deleteFairProduct(product)
    }

    suspend fun toggleFavorite(id: Int, isCurrentlyFavorite: Boolean) {
        businessDao.updateFavoriteStatus(id, !isCurrentlyFavorite)
    }

    suspend fun updateSyncStatus(id: Int, isSynced: Boolean) {
        businessDao.updateSyncStatus(id, isSynced)
    }

    private suspend fun recalculateBusinessRating(businessId: Int) {
        val reviews = reviewDao.getReviewsForBusiness(businessId).first()
        if (reviews.isNotEmpty()) {
            val avg = reviews.map { it.rating }.average().toFloat()
            val business = businessDao.getBusinessById(businessId)
            if (business != null) {
                businessDao.insertBusiness(business.copy(rating = Math.round(avg * 10f) / 10f))
            }
        }
    }

    suspend fun prepopulateIfEmpty() {
        // Run check to see if database has items
        val current = businessDao.getAllBusinesses().first()
        if (current.isEmpty()) {
            val initialList = listOf(
                Business(
                    name = "Tacos El Carboncito",
                    category = "Restaurantes",
                    rating = 4.7f,
                    address = "Av. de la Reforma #482, Zona de Comidas",
                    description = "Los tacos al pastor más famosos de la ciudad, asados en trompo tradicional de carbón de encino. Contamos con deliciosas salsas caseras de aguacate y habanero.",
                    phone = "+52 55 1234 5678",
                    hours = "11:00 AM - 11:00 PM",
                    imageResName = "restaurant",
                    isSynced = true
                ),
                Business(
                    name = "La Trattoria del Sol",
                    category = "Restaurantes",
                    rating = 4.8f,
                    address = "Calle 10 #14-25, Sector Centro Histórico",
                    description = "Genuina comida italiana italiana para saborear con amigos o familia. Pastas artesanales preparadas diariamente desde cero y pizzas a la leña.",
                    phone = "+34 91 555 4321",
                    hours = "1:00 PM - 11:00 PM",
                    imageResName = "restaurant",
                    isSynced = true
                ),
                Business(
                    name = "Ferretería El Tornillo",
                    category = "Servicios",
                    rating = 4.5f,
                    address = "Calle 45 #8-16, Zona Comercial Sur",
                    description = "Tu ferretería de confianza con más de 20 años en la zona. Herramientas de grado profesional, plomería, tornillos especiales y asesoría personalizada.",
                    phone = "+54 11 9876 5432",
                    hours = "8:00 AM - 6:00 PM",
                    imageResName = "construction",
                    isSynced = true
                ),
                Business(
                    name = "Dentistas Clínicos DentalCare",
                    category = "Salud",
                    rating = 4.9f,
                    address = "Av. de las Palmas #102, Cons. 3A",
                    description = "Centro especializado en cuidado oral y diseño de sonrisas. Brindamos servicios integrales preventivos y de urgencias las 24 horas del día.",
                    phone = "+56 2 2456 7890",
                    hours = "9:00 AM - 7:00 PM",
                    imageResName = "health",
                    isSynced = true
                ),
                Business(
                    name = "Café & Librería El Ateneo",
                    category = "Tiendas",
                    rating = 4.7f,
                    address = "Paseo de la República #780",
                    description = "Un oasis para amantes de la lectura. Ofrecemos libros clásicos y contemporáneos, acompañados de deliciosos expresos, croissants y repostería artesanal.",
                    phone = "+51 1 444 8888",
                    hours = "10:00 AM - 8:00 PM",
                    imageResName = "shopping",
                    isSynced = true
                ),
                Business(
                    name = "Asistencia Tecnomóvil Express",
                    category = "Servicios",
                    rating = 4.6f,
                    address = "Av. de la Tecnología #12, Pasillo B",
                    description = "Reparación express para celulares de todas las marcas en menos de 1 hora. Reemplazos de pantalla, baterías y rescate por daño con líquidos.",
                    phone = "+57 601 345 6789",
                    hours = "9:00 AM - 6:30 PM",
                    imageResName = "construction",
                    isSynced = true
                ),
                Business(
                    name = "Gimnasio FitLife Sport",
                    category = "Salud",
                    rating = 4.4f,
                    address = "Calle Deportiva #22",
                    description = "Gimnasio de última tecnología equipado con pesas de alta gama, pistas inteligentes de spinning, clases guiadas y nutricionistas certificados de planta.",
                    phone = "+54 9 11 3456 1234",
                    hours = "6:00 AM - 9:00 PM",
                    imageResName = "health",
                    isSynced = true
                ),
                Business(
                    name = "Clases de Apoyo Albert Einstein",
                    category = "Educación",
                    rating = 4.3f,
                    address = "Paseo del Saber #55",
                    description = "Clases académicas, asesoramiento personalizado en matemáticas, física e inglés. Ideal para preparación universitaria y perfeccionamiento académico.",
                    phone = "+52 55 9876 5432",
                    hours = "8:00 AM - 5:00 PM",
                    imageResName = "school",
                    isSynced = true
                )
            )

            // Insert initial businesses
            businessDao.insertBusinesses(initialList)

            // Let's add a few great initial reviews!
            val actual = businessDao.getAllBusinesses().first()
            val initialReviews = mutableListOf<Review>()

            for (b in actual) {
                // Generate two positive reviews for each
                when (b.category) {
                    "Restaurantes" -> {
                        initialReviews.add(Review(businessId = b.id, author = "Carlos Gómez", rating = 5f, comment = "Excelente servicio y la comida estuvo espectacular. ¡Totalmente recomendado!"))
                        initialReviews.add(Review(businessId = b.id, author = "Mariana S.", rating = 4f, comment = "El ambiente es fabuloso y los platos son abundantes. El servicio podría ser un poquito más rápido."))
                    }
                    "Servicios" -> {
                        initialReviews.add(Review(businessId = b.id, author = "Juan Pérez", rating = 5f, comment = "Me atendieron rápido, encontré el repuesto que buscaba y el trato del personal fue sumamente amable."))
                        initialReviews.add(Review(businessId = b.id, author = "Lucía Ramírez", rating = 4f, comment = "Muy serviciales. Tienen un inventario enorme, te lo explican todo detenidamente."))
                    }
                    "Salud" -> {
                        initialReviews.add(Review(businessId = b.id, author = "Elena Martínez", rating = 5f, comment = "Altamente profesionales. Me atendieron de emergencia a media noche y calmaron todo mi dolor."))
                        initialReviews.add(Review(businessId = b.id, author = "Roberto Díaz", rating = 5f, comment = "Súper limpia la clínica y la atención del equipo dental es de primera clase. Mi lugar preferido."))
                    }
                    "Tiendas" -> {
                        initialReviews.add(Review(businessId = b.id, author = "Sofía Vega", rating = 5f, comment = "La combinación perfecta entre café de alta calidad y una selección de libros fenomenal. Me encanta pasar el rato aquí."))
                        initialReviews.add(Review(businessId = b.id, author = "Diego Alarcón", rating = 4.5f, comment = "Un lugar muy acogedor y con un olor a café riquísimo. Tienen novedades literarias de inmediato."))
                    }
                    else -> {
                        initialReviews.add(Review(businessId = b.id, author = "Alberto Ruiz", rating = 4f, comment = "Muy conforme con el servicio recibido. Cumplió cabalmente con mis expectativas."))
                    }
                }
            }

            reviewDao.insertReviews(initialReviews)

            // Prepopulate initial promotional offers
            val initialOffers = listOf(
                Offer(
                    title = "¡50% de Descuento en todas las Pizzas!",
                    businessName = "La Trattoria del Sol",
                    description = "Aprovecha la mejor combinación de masa artesanal italiana y quesos premium al horno de leña a mitad de precio.",
                    city = "Madrid",
                    address = "Calle 10 #14-25, Sector Centro Histórico",
                    discountPercent = 50,
                    imageResName = "img_offer_food"
                ),
                Offer(
                    title = "Tacos al Pastor 2x1 de Lunes a Miércoles",
                    businessName = "Tacos El Carboncito",
                    description = "Disfruta de nuestros tradicionales tacos al pastor al carbón de encino al doble de sabor para iniciar la semana.",
                    city = "Sinaloa",
                    address = "Av. de la Reforma #482, Zona de Comidas",
                    discountPercent = 30,
                    imageResName = "img_offer_food"
                ),
                Offer(
                    title = "Renovación de Temporada: Hasta 45% de Descuento",
                    businessName = "Modas París & Tendencias",
                    description = "Vestidos, sacos, calzado y accesorios seleccionados de última línea con descuentos espectaculares.",
                    city = "Puebla",
                    address = "Paseo de la República #780",
                    discountPercent = 45,
                    imageResName = "img_offer_shopping"
                ),
                Offer(
                    title = "Masaje Hidratante y Piedras Calientes -25%",
                    businessName = "Spa DentalCare & Wellness",
                    description = "Relajación muscular profunda e hidratante facial premium en un ambiente ambientado y tranquilo.",
                    city = "Santiago",
                    address = "Calle Deportiva #22",
                    discountPercent = 25,
                    imageResName = "img_offer_services"
                )
            )
            offerDao.insertOffers(initialOffers)

            // Prepopulate initial active live streams
            val initialLiveStreams = listOf(
                LiveStream(
                    businessName = "La Trattoria del Sol",
                    title = "¡Cocinando pizza margarita artesanal en vivo!",
                    socialPlatform = "TikTok",
                    socialHandle = "@trattoriasol_live",
                    viewerCount = 345,
                    description = "Acompáñanos a nuestro chef Giuseppe mientras prepara la tradicional masa italiana y comparte sus secretos culinarios en vivo."
                ),
                LiveStream(
                    businessName = "Tacos El Carboncito",
                    title = "Viernes de Tacos al Pastor 2x1 en vivo",
                    socialPlatform = "Facebook",
                    socialHandle = "fb.com/tacoscarboncito",
                    viewerCount = 189,
                    description = "Transmitiendo en directo desde nuestra parrilla al carbón de encino. ¡Ven por tu promo!"
                ),
                LiveStream(
                    businessName = "Modas París & Tendencias",
                    title = "Pasarela de Lanzamiento: Colección Verano 🏖️",
                    socialPlatform = "Instagram",
                    socialHandle = "@modas_paris_boutique",
                    viewerCount = 512,
                    description = "Conoce de primera mano nuestros nuevos vestidos, blusas y calzado premium para la temporada veraniega."
                ),
                LiveStream(
                    businessName = "Spa DentalCare & Wellness",
                    title = "Charla de Bienestar: Cuidado de la piel y relajación",
                    socialPlatform = "Instagram",
                    socialHandle = "@spadentalcare_wellness",
                    viewerCount = 92,
                    description = "Consejos rápidos de hidratación facial profunda y aromaterapia en casa con nuestros especialistas."
                )
            )
            liveStreamDao.insertLiveStreams(initialLiveStreams)

            // Prepopulate initial virtual fair products (productos de la feria)
            val initialFairProducts = listOf(
                FairProduct(
                    businessName = "La Trattoria del Sol",
                    businessAddress = "Calle 10 #14-25, Sector Centro Histórico",
                    businessPhone = "+34 91 555 4321",
                    productName = "Pizza Margarita al Horno de Leña",
                    productPrice = "$14.50",
                    productDescription = "Masa de fermentación lenta de 48 horas, salsa de tomate San Marzano DOP, mozzarella de búfala, aceite de oliva virgen extra y albahaca fresca.",
                    productImageResName = "pizza",
                    likesCount = 42
                ),
                FairProduct(
                    businessName = "Tacos El Carboncito",
                    businessAddress = "Av. de la Reforma #482, Zona de Comidas",
                    businessPhone = "+52 55 1234 5678",
                    productName = "Combo Taco-Pastor Tradicional (12 piezas)",
                    productPrice = "$18.00",
                    productDescription = "Tacos tradicionales de trompo asados al pastor con piña caramelizada, cebollita y cilantro. Incluye salsas picantes artesanales de la casa.",
                    productImageResName = "tacos",
                    likesCount = 89
                ),
                FairProduct(
                    businessName = "Café & Librería El Ateneo",
                    businessAddress = "Paseo de la República #780",
                    businessPhone = "+51 1 444 8888",
                    productName = "Capuchino de Altura + Tarta Selva Negra",
                    productPrice = "$7.20",
                    productDescription = "Café premium doble arábica con cremosa emulsión de leche y canela, acompañado de una porción de nuestra clásica tarta artesanal.",
                    productImageResName = "cafe",
                    likesCount = 37
                ),
                FairProduct(
                    businessName = "Ferretería El Tornillo",
                    businessAddress = "Calle 45 #8-16, Zona Comercial Sur",
                    businessPhone = "+54 11 9876 5432",
                    productName = "Maletín de Herramientas Mecánicas (120 pcs)",
                    productPrice = "$59.90",
                    productDescription = "Kit completo con llaves combinadas, destornilladores intercambiables, puntas de precisión y matraca ergonómica resistente al impacto.",
                    productImageResName = "herramientas",
                    likesCount = 15
                )
            )
            fairProductDao.insertFairProducts(initialFairProducts)
        }
    }
}
