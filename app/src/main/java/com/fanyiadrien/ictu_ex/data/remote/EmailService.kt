package com.fanyiadrien.ictu_ex.data.remote

import android.util.Log
import com.fanyiadrien.ictu_ex.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

interface ResendApi {
    @POST("emails")
    suspend fun sendEmail(
        @Header("Authorization") apiKey: String,
        @Body request: EmailRequest
    )
}

data class EmailRequest(
    val from: String,
    val to: List<String>,
    val subject: String,
    val html: String
)

@Singleton
class EmailService @Inject constructor() {
    private val api: ResendApi
    private val developerEmails = listOf(
        "fanyicharllson@gmail.com",
        "adriennathan.tellokombou@ictuniversity.edu.cm"
    )

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.resend.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        api = retrofit.create(ResendApi::class.java)
    }

    private suspend fun sendRawEmail(subject: String, html: String, to: List<String>) {
        val fromEmail = "ICTU-Exchange <onboarding@teamnest.me>"
        val apiKey = "Bearer ${BuildConfig.RESEND_API_KEY}"
        val request = EmailRequest(from = fromEmail, to = to, subject = subject, html = html)
        try {
            api.sendEmail(apiKey, request)
        } catch (e: Exception) {
            Log.e("EmailService", "Failed to send email: ${e.message}")
        }
    }

    suspend fun sendOrderConfirmation(
        sellerEmail: String,
        sellerName: String,
        buyerName: String,
        listingTitle: String,
        orderId: String
    ) {
        val html = """
            <div style="font-family: sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #6200EE;">You have a new order! 🎉</h2>
                <p>Hi <strong>${sellerName}</strong>,</p>
                <p>Great news! <strong>${buyerName}</strong> has just placed an order for your item: <strong>${listingTitle}</strong>.</p>
                <p>Order ID: <strong>#${orderId.take(8).uppercase()}</strong></p>
                <hr/>
                <p>Please log in to the app to manage your listing.</p>
                <p>— The ICTU-Exchange Team</p>
            </div>
        """.trimIndent()
        sendRawEmail("New Order Received! 🚀 - ICTU-Exchange", html, listOf(sellerEmail))
    }

    suspend fun sendErrorReport(error: String, stackTrace: String, userEmail: String?) {
        val html = """
            <div style="font-family: monospace; padding: 20px; background: #f8f8f8; border-left: 4px solid #f44336;">
                <h2 style="color: #f44336;">Critical App Error Reported ⚠️</h2>
                <p><strong>User:</strong> ${userEmail ?: "Anonymous"}</p>
                <p><strong>Error:</strong> $error</p>
                <hr/>
                <p><strong>Stack Trace:</strong></p>
                <pre style="white-space: pre-wrap;">$stackTrace</pre>
            </div>
        """.trimIndent()
        sendRawEmail("Production Error Alert ⚠️ - ICTU-Exchange", html, developerEmails)
    }

    suspend fun sendMessageAlert(recipientEmail: String, senderName: String, messageSnippet: String) {
        val html = """
            <div style="font-family: sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #6200EE;">New Message from $senderName 💬</h2>
                <p>You have received a new message on ICTU-Exchange:</p>
                <blockquote style="background: #f9f9f9; padding: 10px; border-left: 4px solid #6200EE;">
                    "$messageSnippet"
                </blockquote>
                <p>Log in now to reply!</p>
                <p>— The ICTU-Exchange Team</p>
            </div>
        """.trimIndent()
        sendRawEmail("You've got a new message! 💬", html, listOf(recipientEmail))
    }
}
