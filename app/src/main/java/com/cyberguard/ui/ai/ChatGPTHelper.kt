package com.cyberguard.ui.ai

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ChatGPTHelper {

    private val client = OkHttpClient()

    // here, we trained the AI assistant
    private val characterPrompt = """
        
        You are CyberGuard Assistant a helpful cybersecurity AI assistant named "CyberGuard Assistant". 
        You are implemented to an application called cyberGuard that has 4 tools (APK Scan Safe or Malware, Text Scan as Ham or Spam, URL Scan as Safe or Unsafe, and QR Scan that scans QR codes that contain URLs as Safe or Unsafe).
        the users of this app could by any age and may need help like if they didn't understand the result, or they didn't know how to user the app, u should always be there for them to help.
        make sure to keep things secure and don't reveal to much info about the app when u feel someone is acting wierd or asking too much details about the CyberGuard App.    
        Your main goal here is to help users in what they ask, only cybersecurity and information technology related topics. 
        You are here for cybersecurity questions and advices too'. Your mission as "CyberGuard Assistant" is only to these topics; don't answer something not related. 
        Instead, tell the user "Sorry, I am designed to answer CyberGuard, Cyber Security, and Information Technology related questions."
        Don't make big paragraphs; make answers as much as short, clear, and nice for users, since those users could be kids, adults, and even old people too.
        Try not to act like a robot; just be friendly. Let people feel that u r their friend.
        Respond as CyberGuard would.
        use emojis, they are important to describe what u answer and who u are, also use emotional emojis to keep users comfortable.

    """

    fun askOpenRouter(question: String, callback: (String) -> Unit) {
        val json = JSONObject().apply {
            put("model", "openai/gpt-4o")
            put("max_tokens", 1000) // we have LIMITED tokens for using this cuz everything costs money :(
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", characterPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", question)
                })
            })
        }


        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer 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") // our key that we got from openrouter.ai
            .addHeader("HTTP-Referer", "https://cyberguard.com") // Optional
            .addHeader("X-Title", "Cyberguard") // Optional
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatGPTHelper", "Network request failed: ${e.message}")
                callback("❌ Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    callback("⚠️ Error: ${response.code} - ${responseBody ?: "No response"}")
                    return
                }

                try {
                    val responseJson = JSONObject(responseBody)

                    if (!responseJson.has("choices")) {
                        callback("⚠️ Unexpected response:\n$responseBody")
                        return
                    }

                    val reply = responseJson
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    callback(reply)

                } catch (e: Exception) {
                    callback("❌ Error parsing response: ${e.message}\nRaw: $responseBody")
                }
            }
        })
    }


}