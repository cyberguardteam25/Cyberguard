package com.cyberguard.ui.home.tool2

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.cyberguard.databinding.FragmentTool2Binding
import com.cyberguard.R
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// backend APIs
interface ApiService {
    @POST("predict_spam")
    fun sendInputData(@Body requestData: RequestData): Call<ResponseData>
}

interface StoreApiService {
    @POST("store_text_result")
    fun storeTextResult(@Body requestData: StoreTextRequest): Call<ResponseData>
}

// data models
data class RequestData(val email: String)
data class ResponseData(val prediction: String)
data class StoreTextRequest(val userId: Int, val text: String, val result: String)

class Tool2Fragment : Fragment() {

    private var _binding: FragmentTool2Binding? = null
    private val binding get() = _binding!!
    private var savedInputText: String? = null
    private var savedPrediction: String? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.SECONDS)
        .readTimeout(500, TimeUnit.SECONDS)
        .writeTimeout(500, TimeUnit.SECONDS)
        .build()

    private val retrofitPredictSpam = Retrofit.Builder()
        .baseUrl("https://32ab-92-253-1-148.ngrok-free.app/") // port 1234
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofitPredictSpam.create(ApiService::class.java)

    private val retrofitStore = Retrofit.Builder()
        .baseUrl("https://32ab-92-253-1-148.ngrok-free.app/") // port 5000
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val storeApiService = retrofitStore.create(StoreApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTool2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val scanButton: Button = binding.scanButton
        val inputTextField: EditText = binding.inputTextField
        val resultCard: CardView = binding.resultCard
        val loadingProgressBar: ProgressBar = binding.loadingProgressBar

        if (savedInstanceState != null) {
            savedInputText = savedInstanceState.getString("inputText")
            savedPrediction = savedInstanceState.getString("prediction")

            savedInputText?.let {
                binding.inputTextField.setText(it)
            }

            savedPrediction?.let {
                updateResultUI(it)
            }
        }

        scanButton.setOnClickListener {
            val inputText = inputTextField.text.toString().trim()

            if (inputText.isNotEmpty()) {
                resultCard.visibility = View.GONE
                scanButton.isEnabled = false
                loadingProgressBar.visibility = View.VISIBLE

                val requestData = RequestData(inputText)
                apiService.sendInputData(requestData).enqueue(object : Callback<ResponseData> {
                    override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                        scanButton.isEnabled = true
                        loadingProgressBar.visibility = View.GONE

                        if (response.isSuccessful) {
                            val prediction = response.body()?.prediction ?: "Unknown"
                            updateResultUI(prediction)

                            // store result in the database
                            val storeRequest = StoreTextRequest(
                                userId = 1, // current logged in user id
                                text = inputText,
                                result = prediction
                            )
                            storeApiService.storeTextResult(storeRequest).enqueue(object : Callback<ResponseData> {
                                override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                                    if (response.isSuccessful) {
                                        Log.d("StoreResult", "Result stored successfully.")
                                    } else {
                                        Log.e("StoreResult", "Failed to store result: ${response.code()}")
                                    }
                                }

                                override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                                    Log.e("StoreResult", "Error storing result: ${t.message}")
                                }
                            })

                        } else {
                            val errorMessage = extractMessage(response.errorBody()?.string())
                            updateResultUI("Error: ${response.code()} - $errorMessage")
                        }
                    }

                    override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                        scanButton.isEnabled = true
                        loadingProgressBar.visibility = View.GONE
                        updateResultUI("${t.message}")
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Please enter text to scan.", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("inputText", binding.inputTextField.text.toString())
        outState.putString("prediction", savedPrediction)
    }


    private fun updateResultUI(prediction: String) {
        savedPrediction = prediction
        binding.resultCard.visibility = View.VISIBLE
        binding.resultAnimation.visibility = View.VISIBLE
        binding.resultAnimation2.visibility = View.GONE
        binding.resultAnimation2.cancelAnimation()

        when (prediction.lowercase()) {
            "ham" -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_green, null))
                binding.resultTextView.text = "✅ Safe: This text does not appear to be spam."
                binding.resultTextView2.text = "You can trust this text!"
                binding.resultAnimation.setAnimation(R.raw.safeall)
            }
            "spam" -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_red, null))
                binding.resultTextView.text = "⚠️ Warning: This text may be spam!"
                binding.resultTextView2.text = "Be careful! This might be a spam message."
                binding.resultAnimation.setAnimation(R.raw.spam)
            }
            else -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
                binding.resultTextView.text = "❓ Unknown result."
                binding.resultTextView2.text = "\"$prediction\""
                binding.resultAnimation.setAnimation(R.raw.unknown2)
            }
        }

        binding.resultAnimation.repeatCount = 0 // just play once
        binding.resultAnimation.repeatMode = LottieDrawable.RESTART
        binding.resultAnimation.playAnimation()
    }

    private fun extractMessage(json: String?): String {
        return try {
            json?.let {
                val jsonObject = JSONObject(it)
                jsonObject.getString("message")
            } ?: "Login failed"
        } catch (e: Exception) {
            "Login failed"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}