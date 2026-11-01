package com.example.lifesync

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

enum class MedicalSearchType { CONDITIONS, MEDICATIONS }

object MedicalApiHelper {
    private val client = OkHttpClient()

    /**
     * Uses the 100% Free US National Institutes of Health (NIH) Clinical Tables API.
     * No API Key required. Highly accurate standard medical terms.
     */
    suspend fun search(query: String, type: MedicalSearchType): List<String> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()

        val url = when (type) {
            MedicalSearchType.CONDITIONS -> "https://clinicaltables.nlm.nih.gov/api/conditions/v3/search?terms=$query&sf=primary_name&df=primary_name&maxList=10"
            MedicalSearchType.MEDICATIONS -> "https://clinicaltables.nlm.nih.gov/api/rxterms/v3/search?terms=$query&sf=DISPLAY_NAME&df=DISPLAY_NAME&maxList=10"
        }

        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyString = response.body?.string() ?: return@withContext emptyList()

                // NIH API returns an array like: [ totalCount, ["code1", "code2"], null, [["term1"], ["term2"]] ]
                // For CONDITIONS, the actual names are in the FOURTH element (index 3) because index 1 contains codes.
                // For MEDICATIONS, the actual names are in the SECOND element (index 1), but can also be extracted from the fourth element.
                val rootArray = JSONArray(bodyString)
                val termsArray = rootArray.getJSONArray(3)

                val results = mutableListOf<String>()
                for (i in 0 until termsArray.length()) {
                    val innerArray = termsArray.getJSONArray(i)
                    if (innerArray.length() > 0) {
                        results.add(innerArray.getString(0))
                    }
                }

                return@withContext results.distinct() // Remove duplicates
            }
        } catch (e: IOException) {
            Log.e("MedicalApi", "Network error", e)
            return@withContext emptyList()
        } catch (e: Exception) {
            Log.e("MedicalApi", "Parsing error", e)
            return@withContext emptyList()
        }
    }
}
