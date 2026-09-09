package com.mr10.vello.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mr10.vello.data.repository.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MessageRetryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val messageRepo: MessageRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val messageId = inputData.getString("messageId") ?: return@withContext Result.failure()
        
        // In a real implementation, we would fetch the message from a local database (Room)
        // Since Room isn't fully wired for messages yet in this phase, we'll simulate the retry logic.
        Log.d("MessageRetryWorker", "Retrying message: $messageId")
        
        return@withContext try {
            // Placeholder for actual sending logic:
            // val message = localDb.messageDao().getMessage(messageId)
            // messageRepo.sendMessage(message)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("MessageRetryWorker", "Retry failed for $messageId", e)
            if (runAttemptCount < 5) Result.retry() else Result.failure()
        }
    }
}
