package com.fanyiadrien.ictu_ex.data.remote.spring

import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpringMessagingRepository @Inject constructor(
    private val api: SpringMessagingApi
) {

    suspend fun getMyConversations(): AppResult<List<ConversationView>> = safeCall {
        api.getMyConversations()
    }

    suspend fun startConversation(
        otherUserId: String,
        listingId: String? = null
    ): AppResult<ConversationView> = safeCall {
        api.startConversation(StartConversationRequest(otherUserId, listingId))
    }

    suspend fun getMessages(conversationId: String): AppResult<List<MessageView>> = safeCall {
        api.getMessages(conversationId)
    }

    suspend fun sendMessage(
        conversationId: String,
        content: String
    ): AppResult<MessageView> = safeCall {
        api.sendMessage(conversationId, SendMessageRequest(content))
    }

    // ── Error handling ────────────────────────────────────────────────────────

    private suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: IOException) {
            AppResult.Error(AppError.NETWORK_ERROR, e)
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                401 -> "Unauthorized. Please sign in again."
                403 -> "You are not part of this conversation."
                404 -> "Conversation not found."
                400 -> "Invalid request parameters."
                500, 502, 503 -> "Server error. Please try again later."
                else -> AppError.FETCH_FAILED
            }
            AppResult.Error(msg, e)
        } catch (e: Exception) {
            AppResult.Error(AppError.FETCH_FAILED, e)
        }
    }
}
