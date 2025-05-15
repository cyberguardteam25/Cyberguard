package com.cyberguard.ui.home.tool4

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.cyberguard.PortraitCaptureActivity
import com.cyberguard.R
import com.cyberguard.databinding.FragmentTool4Binding
import com.google.zxing.integration.android.IntentIntegrator
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class RequestData4(val url: String)
data class RequestDataResult4(val userId: String, val url: String, val result: String)
data class ResponseData4(val status: String?, val error: String?)

interface ApiServiceScan4 {
    @POST("check_url")
    fun sendInputData(@Body requestData: RequestData4): Call<ResponseData4>
}

interface ApiServiceResult4 {
    @POST("store_url_result")
    fun sendScanResult(@Body requestData: RequestDataResult4): Call<ResponseData4>
}

class Tool4Fragment : Fragment() {

    private var _binding: FragmentTool4Binding? = null
    private val binding get() = _binding!!

    private var savedResult: String? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.SECONDS)
        .readTimeout(500, TimeUnit.SECONDS)
        .writeTimeout(500, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://32ab-92-253-1-148.ngrok-free.app/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiServiceScan = retrofit.create(ApiServiceScan4::class.java)
    private val apiServiceResult = retrofit.create(ApiServiceResult4::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTool4Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val scanQRButton: Button = binding.scanQRButton

        scanQRButton.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setOrientationLocked(false)
                .setCaptureActivity(PortraitCaptureActivity::class.java)
                .setPrompt("Scan a QR Code")
                .setBeepEnabled(true)
                .initiateScan()
        }

        savedInstanceState?.getString("result")?.let {
            savedResult = it
            updateResultUI(it)
        }

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedResult?.let { outState.putString("result", it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val scannedUrl = result.contents
            if (scannedUrl != null && isValidUrlOrDomain(scannedUrl)) {
                submitUrl(scannedUrl)
            } else {
                Toast.makeText(requireContext(), "Invalid or empty QR code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidUrlOrDomain(input: String): Boolean {
        val urlRegex = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[a-zA-Z0-9\\-._~:/?#[\\\\]@!\$&'()*+,;=]*)?\$".toRegex()
        return input.matches(urlRegex)
    }



    private fun submitUrl(url: String) {
        val resultCard = binding.resultCard
        val loadingProgressBar = binding.loadingProgressBar

        resultCard.visibility = View.GONE
        binding.URLTextView.text = url // here we see scanned results
        binding.scanQRButton.isEnabled = false
        loadingProgressBar.visibility = View.VISIBLE

        val requestData = RequestData4(url)

        apiServiceScan.sendInputData(requestData).enqueue(object : Callback<ResponseData4> {
            override fun onResponse(call: Call<ResponseData4>, response: Response<ResponseData4>) {
                binding.scanQRButton.isEnabled = true
                loadingProgressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val result = responseBody.status ?: responseBody.error ?: "Unknown"
                        updateResultUI(result)
                        storeScanResult(getUserId(), url, result)
                    }
                } else {
                    handleApiError(response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<ResponseData4>, t: Throwable) {
                binding.scanQRButton.isEnabled = true
                binding.loadingProgressBar.visibility = View.GONE

                val errorMessage = "❌ Error: Unable to reach the server.\n${t.localizedMessage}"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()

                binding.resultCard.visibility = View.VISIBLE
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
                binding.resultTextView.text = "🚫 Server Unreachable"
                binding.resultTextView2.text = t.localizedMessage ?: "Unknown error"
                binding.resultAnimation.setAnimation(R.raw.unknown2)
                binding.resultAnimation.visibility = View.VISIBLE
                binding.resultAnimation.playAnimation()
            }
        })
    }

    private fun updateResultUI(result: String) {
        savedResult = result
        binding.resultCard.visibility = View.VISIBLE
        binding.resultAnimation.visibility = View.VISIBLE
        binding.resultAnimation2.visibility = View.GONE
        binding.resultAnimation2.cancelAnimation()

        when (result.lowercase()) {
            "safe" -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_green, null))
                binding.resultTextView.text = "✅ Safe: This URL does not appear to be malicious."
                binding.resultTextView2.text = "You can trust this URL!"
                binding.resultAnimation.setAnimation(R.raw.safeall)
            }
            "spam" -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_red, null))
                binding.resultTextView.text = "⚠️ Warning: This URL may be malicious!"
                binding.resultTextView2.text = "Be careful! This might be a malicious URL."
                binding.resultAnimation.setAnimation(R.raw.spam)
            }
            else -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
                binding.resultTextView.text = "❓ Unknown result."
                binding.resultTextView2.text = "\"$result\""
                binding.resultAnimation.setAnimation(R.raw.unknown2)
            }
        }

        binding.resultAnimation.repeatCount = 0 // play only once
        binding.resultAnimation.repeatMode = LottieDrawable.RESTART
        binding.resultAnimation.playAnimation()
    }

    private fun storeScanResult(userId: Int, url: String, result: String) {
        val requestData = RequestDataResult4(userId.toString(), url, result)
        apiServiceResult.sendScanResult(requestData).enqueue(object : Callback<ResponseData4> {
            override fun onResponse(call: Call<ResponseData4>, response: Response<ResponseData4>) {
                if (response.isSuccessful) {
                    Log.d("ScanResult", "Result stored successfully")
                } else {
                    Log.e("ScanResultError", "Error storing result: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseData4>, t: Throwable) {
                Log.e("ScanResultError", "Request failed: ${t.message}")
            }
        })
    }

    private fun getUserId(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }


    private fun handleApiError(code: Int, message: String) {
        binding.resultCard.visibility = View.VISIBLE
        binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
        binding.resultTextView.text = "Server Unreachable: $code"
        binding.resultTextView2.text = message
        binding.resultAnimation.setAnimation(R.raw.unknown2)
        binding.resultAnimation.visibility = View.VISIBLE
        binding.resultAnimation.playAnimation()

        Toast.makeText(requireContext(), "API Error $code: $message", Toast.LENGTH_LONG).show()
        Log.e("API Error", "Code: $code, Message: $message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
