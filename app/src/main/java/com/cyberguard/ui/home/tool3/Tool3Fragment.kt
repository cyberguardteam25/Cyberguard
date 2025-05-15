package com.cyberguard.ui.home.tool3

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
import com.cyberguard.R
import com.cyberguard.databinding.FragmentTool3Binding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// backend APIs
interface ApiServiceScan {
    @POST("check_url")
    fun sendInputData(@Body requestData: RequestData3): Call<ResponseData3>
}

interface ApiServiceResult {
    @POST("store_url_result")
    fun sendScanResult(@Body requestData: RequestDataResult): Call<ResponseData3>
}

// data models
data class RequestData3(val url: String)
data class RequestDataResult(val userId: String, val url: String, val result: String)
data class ResponseData3(val status: String?, val error: String?)

class Tool3Fragment : Fragment() {

    private var _binding: FragmentTool3Binding? = null
    private val binding get() = _binding!!

    private var savedInputText: String? = null
    private var savedResult: String? = null

    // OkHttpClient with timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.SECONDS)
        .readTimeout(500, TimeUnit.SECONDS)
        .writeTimeout(500, TimeUnit.SECONDS)
        .build()

    private val retrofitScan = Retrofit.Builder()
        .baseUrl("https://32ab-92-253-1-148.ngrok-free.app/") //1234
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private val retrofitResult = Retrofit.Builder()
        .baseUrl("https://32ab-92-253-1-148.ngrok-free.app/") //5000
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiServiceScan = retrofitScan.create(ApiServiceScan::class.java)
    private val apiServiceResult = retrofitResult.create(ApiServiceResult::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTool3Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val scanButton: Button = binding.scanButton3
        val inputTextField: EditText = binding.inputTextField3
        val resultTextView: TextView = binding.resultTextView3
        val resultCard: CardView = binding.resultCard3
        val loadingProgressBar: ProgressBar = binding.loadingProgressBar3
        val resultAnimation: LottieAnimationView = binding.resultAnimation3
        val resultAnimation2: LottieAnimationView = binding.resultAnimation32

        if (savedInstanceState != null) {
            savedInputText = savedInstanceState.getString("inputText")
            savedResult = savedInstanceState.getString("result")

            savedInputText?.let {
                binding.inputTextField3.setText(it)
            }

            savedResult?.let {
                updateResultUI(it)
            }
        }

        scanButton.setOnClickListener {
            val inputText = inputTextField.text.toString().trim()

            if (inputText.length > 300) {
                Toast.makeText(requireContext(), "Input exceeds 300 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidUrlOrDomain(inputText)) {
                Toast.makeText(requireContext(), "Invalid URL or domain format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resultCard.visibility = View.GONE
            scanButton.isEnabled = false
            loadingProgressBar.visibility = View.VISIBLE

            val requestData = RequestData3(inputText)

            apiServiceScan.sendInputData(requestData).enqueue(object : Callback<ResponseData3> {
                override fun onResponse(call: Call<ResponseData3>, response: Response<ResponseData3>) {
                    scanButton.isEnabled = true
                    loadingProgressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            val result = when {
                                responseBody.status != null -> responseBody.status
                                responseBody.error != null -> responseBody.error
                                else -> "Unknown result"
                            }
                            updateResultUI(result)

                            // get user id from SharedPreferences
                            val userId = getUserId()

                            // store scan result with user id
                            storeScanResult(userId, inputText, result)
                        }
                    } else {
                        handleApiError(response.code(), response.message())
                    }
                }

                override fun onFailure(call: Call<ResponseData3>, t: Throwable) {
                    binding.scanButton3.isEnabled = true
                    binding.loadingProgressBar3.visibility = View.GONE

                    val errorMessage = "❌ Error: Unable to reach the server.\n${t.localizedMessage}"

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()

                    binding.resultCard3.visibility = View.VISIBLE
                    binding.resultCard3.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
                    binding.resultTextView3.text = "🚫 Server Unreachable"
                    binding.resultTextView32.text = t.localizedMessage ?: "Unknown error"
                    binding.resultAnimation3.setAnimation(R.raw.unknown2)
                    binding.resultAnimation3.visibility = View.VISIBLE
                    binding.resultAnimation3.playAnimation()
                }

            })
        }

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("inputText", binding.inputTextField3.text.toString())
        outState.putString("result", savedResult)
    }

    private fun updateResultUI(result: String) {
        savedResult = result
        binding.resultCard3.visibility = View.VISIBLE
        binding.resultAnimation3.visibility = View.VISIBLE
        binding.resultAnimation32.visibility = View.GONE
        binding.resultAnimation32.cancelAnimation()

        when (result.lowercase()) {
            "safe" -> {
                binding.resultCard3.setCardBackgroundColor(resources.getColor(R.color.light_green, null))
                binding.resultTextView3.text = "✅ Safe: This URL does not appear to be malicious."
                binding.resultTextView32.text = "You can trust this URL!"

                binding.resultAnimation3.setAnimation(R.raw.safeall)
            }
            "spam" -> {
                binding.resultCard3.setCardBackgroundColor(resources.getColor(R.color.light_red, null))
                binding.resultTextView3.text = "⚠️ Warning: This URL may be malicious!"
                binding.resultTextView32.text = "Be careful! This might be a malicious URL."

                binding.resultAnimation3.setAnimation(R.raw.spam)
            }
            else -> {
                binding.resultCard3.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
                binding.resultTextView3.text = "❓ Unknown result."
                binding.resultTextView32.text = "\"$result\""

                binding.resultAnimation3.setAnimation(R.raw.unknown2)
            }
        }

        binding.resultAnimation3.repeatCount = 0 // Play once
        binding.resultAnimation3.repeatMode = LottieDrawable.RESTART
        binding.resultAnimation3.playAnimation()
    }

    private fun isValidUrlOrDomain(input: String): Boolean {
        val urlRegex = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[a-zA-Z0-9\\-._~:/?#[\\\\]@!\$&'()*+,;=]*)?\$".toRegex()
        return input.matches(urlRegex)
    }

    private fun storeScanResult(userId: Int, url: String, result: String) {
        val requestData = RequestDataResult(userId.toString(), url, result)

        apiServiceResult.sendScanResult(requestData).enqueue(object : Callback<ResponseData3> {
            override fun onResponse(call: Call<ResponseData3>, response: Response<ResponseData3>) {
                if (response.isSuccessful) {
                    Log.d("ScanResult", "Scan result saved successfully")
                } else {
                    Log.e("ScanResultError", "Error saving scan result: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseData3>, t: Throwable) {
                Log.e("ScanResultError", "Request failed: ${t.message}")
            }
        })
    }

    private fun getUserId(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }

    private fun handleApiError(code: Int, message: String) {
        binding.resultCard3.visibility = View.VISIBLE
        binding.resultCard3.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
        binding.resultTextView3.text = "Server Unreachable: $code"
        binding.resultTextView32.text = message
        binding.resultAnimation3.setAnimation(R.raw.unknown2)
        binding.resultAnimation3.visibility = View.VISIBLE
        binding.resultAnimation3.playAnimation()

        Toast.makeText(requireContext(), "API Error $code: $message", Toast.LENGTH_LONG).show()
        Log.e("API Error", "Code: $code, Message: $message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}