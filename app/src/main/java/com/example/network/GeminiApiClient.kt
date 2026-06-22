package com.example.network

import com.example.BuildConfig
import com.example.data.Business
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(GeminiApiService::class.java)

    /**
     * Conducts an AI Search across pre-populated data and queries Gemini for recommendations.
     */
    suspend fun searchBusinessAI(query: String, localBusinesses: List<Business>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "El buscador de Inteligencia Artificial requiere configurar 'GEMINI_API_KEY' en el panel de Secrets de AI Studio. Sin embargo, puedes usar el filtro manual por categorías arriba de manera local."
        }

        val businessListString = localBusinesses.joinToString("\n") { b ->
            "- ID: ${b.id}, Nombre: ${b.name}, Categoría: ${b.category}, Calificación: ${b.rating} estrellas, Dirección: ${b.address}, Descripción: ${b.description}"
        }

        val systemPrompt = """
            Eres un asistente de Inteligencia Artificial experto en comunidades locales y directorios comerciales.
            Tu misión es recomendar los mejores negocios locales basados en las necesidades del usuario.
            Cuentas con la lista de negocios registrados localmente en el directorio de la aplicación:
            
            $businessListString
            
            Reglas de respuesta:
            1. Analiza cuidadosamente la consulta del usuario.
            2. Recomienda de 1 a 3 negocios que MEJOR se adapten a lo solicitado.
            3. Si mencionas un negocio de la lista, indícalo claramente detallando su Nombre y por qué es una grandiosa opción.
            4. Utiliza un formato limpio, amigable y estructurado con viñetas en ESPAÑOL.
            5. Si no hay ningún negocio directo para suplir la necesidad en la lista local, propón alternativas creativas educadamente y ofrece sugerencias basadas en cómo el usuario puede usar nuestro formulario de 'Agregar Negocio' para registrarlos.
            6. Sé conciso pero sumamente profesional.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Un usuario busca: '$query'. ¿Cuáles son tus mejores recomendaciones en base a la lista de negocios de nuestro directorio?")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.6f)
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No se recibieron recomendaciones de AI."
        } catch (e: Exception) {
            "Ocurrió un error conectando al buscador de AI: ${e.localizedMessage ?: e.message}. Por favor, verifica tu conexión a internet o la API Key."
        }
    }
}
