package org.kasumi321.ushio.phitracker.data.song

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongDataUpdater @Inject constructor(
    private val httpClient: HttpClient,
    @ApplicationContext private val context: Context,
    private val songDataProvider: SongDataProvider
) {

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com/Catrong/phi-plugin/refs/heads/main/resources/info/"
        private val FILES = listOf(
            "difficulty.csv",
            "info.csv",
            "infolist.json",
            "notesInfo.json"
        )
    }

    suspend fun updateSongData(onProgress: (Int, Int) -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val targetDir = File(context.filesDir, "song_data")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            var currentIndex = 0
            for (filename in FILES) {
                onProgress(currentIndex, FILES.size)
                val response = httpClient.get("$BASE_URL$filename")
                val channel = response.bodyAsChannel()
                
                val targetFile = File(targetDir, filename)
                val tempFile = File(targetDir, "$filename.tmp")
                
                tempFile.outputStream().use { output ->
                    channel.copyTo(output)
                }
                
                // Rename atomic
                tempFile.renameTo(targetFile)
                currentIndex++
            }
            onProgress(FILES.size, FILES.size)
            
            // Invalidate cache
            songDataProvider.invalidateCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
