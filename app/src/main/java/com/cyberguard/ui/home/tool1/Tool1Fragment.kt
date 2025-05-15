package com.cyberguard.ui.home.tool1

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.cyberguard.R
import com.cyberguard.databinding.FragmentTool1Binding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okio.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class Tool1Fragment : Fragment() {

    private var _binding: FragmentTool1Binding? = null
    private val binding get() = _binding!!
    private var selectedFileUri: Uri? = null
    private var savedFileUri: Uri? = null
    private var savedPrediction: String? = null

    private var uploadedFileName: String? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openFilePicker() // show file picker only if permission granted
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }



    // API for APK Scan
    interface ApiService {
        @Multipart
        @POST("apk_scan")
        fun uploadApk(@Part file: MultipartBody.Part): Call<ResponseData>
    }

    // API  for storing result in database
    interface DatabaseService {
        @POST("store_apk_result")
        fun storeResult(@retrofit2.http.Body request: StoreResultRequest): Call<StoreResultResponse>
    }

    data class ResponseData(val result: String, val sha256: String, val cached: Boolean)
    data class StoreResultRequest(
        val userId: Int,
        val apk_name: String,
        val hash: String,
        val result: String
    )
    data class StoreResultResponse(val success: Boolean, val message: String)

    // OkHttpClient with timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(5000, TimeUnit.SECONDS)
        .readTimeout(5000, TimeUnit.SECONDS)
        .writeTimeout(5000, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://5430-37-202-73-43.ngrok-free.app/") //NGROCK backend URL (CODE)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    private val dbRetrofit = Retrofit.Builder()
        .baseUrl("https://2909-92-253-1-148.ngrok-free.app/") //NGROCK backend URL (DATABASE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val databaseService = dbRetrofit.create(DatabaseService::class.java)

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                validateAndUploadFile(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTool1Binding.inflate(inflater, container, false)
        val root: View = binding.root

        val uploadButton: Button = binding.uploadButton

        if (savedInstanceState != null) {
            val fileUriString = savedInstanceState.getString("fileUri")
            savedPrediction = savedInstanceState.getString("prediction")
            val isResultCardVisible = savedInstanceState.getBoolean("isResultCardVisible", false)
            val fileName = savedInstanceState.getString("fileName", "")
            val fileSize = savedInstanceState.getString("fileSize", "")
            val apkIcon = getApkIcon(Uri.parse(fileUriString))

            fileUriString?.let { savedFileUri = Uri.parse(it) }

            if (isResultCardVisible) {
                binding.resultCard.visibility = View.VISIBLE
                binding.fileNameTextView.text = fileName
                binding.fileSizeTextView.text = fileSize
                binding.fileIcon.setImageDrawable(apkIcon)
                savedPrediction?.let { updateResultUI(it) }
            }
        }

        uploadButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // process view
        binding.uploadProgressTextView.visibility = View.GONE
        binding.uploadProgressBar.visibility = View.GONE

        binding.scanningProgressBar.visibility = View.GONE
        binding.scanningTextView.visibility = View.GONE

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        savedFileUri?.let { outState.putString("fileUri", it.toString()) }
        outState.putString("prediction", savedPrediction)
        outState.putBoolean("isResultCardVisible", binding.resultCard.visibility == View.VISIBLE)
        outState.putString("fileName", binding.fileNameTextView.text.toString())
        outState.putString("fileSize", binding.fileSizeTextView.text.toString())
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/vnd.android.package-archive"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun validateAndUploadFile(uri: Uri) {
        savedFileUri = uri
        val fileSize = getFileSize(uri)
        if (fileSize > 200 * 1024 * 1024) {
            Toast.makeText(requireContext(), "File size exceeds 200MB limit.", Toast.LENGTH_SHORT).show()
            return
        }


        val fileName = getFileName(uri)
        if (!fileName.endsWith(".apk")) {
            Toast.makeText(requireContext(), "Please select a valid APK file.", Toast.LENGTH_SHORT).show()
            return
        }
        uploadedFileName = fileName

        binding.resultCard.visibility = View.VISIBLE
        binding.fileInfoLayout.visibility = View.VISIBLE
        binding.fileNameTextView.text = fileName
        binding.fileSizeTextView.text = formatFileSize(fileSize)
        binding.fileIcon.visibility = View.VISIBLE

        val apkIcon = getApkIcon(uri)
        if (apkIcon != null) {
            binding.fileIcon.setImageDrawable(apkIcon)
        } else {
            binding.fileIcon.setImageResource(R.drawable.cyberguard)
        }

        uploadFile(uri)
    }

    private fun getApkIcon(uri: Uri): Drawable? {
        val packageManager = requireContext().packageManager
        val tempFile = File(requireContext().cacheDir, "temp_apk_file.apk")
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
            }

            val packageInfo = packageManager.getPackageArchiveInfo(tempFile.absolutePath, 0)
            packageInfo?.applicationInfo?.apply {
                sourceDir = tempFile.absolutePath
                publicSourceDir = tempFile.absolutePath
                return packageManager.getApplicationIcon(this)
            }
        } catch (e: Exception) {
            Log.e("Tool1Fragment", "Error extracting APK icon", e)
        } finally {
            tempFile.delete()
        }

        return null
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024
        val mb = kb / 1024
        return if (mb > 0) "$mb MB" else "$kb KB"
    }

    private fun getFileSize(uri: Uri): Long {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                return it.getLong(sizeIndex)
            }
        }
        return 0
    }

    private fun getFileName(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                return it.getString(nameIndex)
            }
        }
        return ""
    }

    private val SCANNING_DELAY = 1000L
    private fun uploadFile(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".apk", requireContext().cacheDir)

            inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }

            val requestFile = tempFile.asRequestBody("application/vnd.android.package-archive".toMediaTypeOrNull())

            val progressRequestBody = ProgressRequestBody(requestFile) { bytesWritten, totalBytes ->
                val progress = (100 * bytesWritten / totalBytes).toInt()
                activity?.runOnUiThread {
                    binding.uploadProgressTextView.text = "$progress%"
                    binding.uploadProgressBar.progress = progress
                }
            }

            val body = MultipartBody.Part.createFormData("apk_file", tempFile.name, progressRequestBody)

            binding.uploadButton.isEnabled = false
            binding.loadingProgressBar.visibility = View.VISIBLE
            binding.uploadProgressTextView.visibility = View.VISIBLE
            binding.uploadProgressBar.visibility = View.VISIBLE
            binding.uploadProgressTextView.text = "0%"
            binding.uploadProgressBar.progress = 0

            apiService.uploadApk(body).enqueue(object : Callback<ResponseData> {
                override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                    // hide upload
                    binding.uploadButton.isEnabled = true
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.uploadProgressTextView.visibility = View.GONE
                    binding.uploadProgressBar.visibility = View.GONE

                    // show scanning animation
                    binding.scanningProgressBar.visibility = View.VISIBLE
                    binding.scanningTextView.visibility = View.VISIBLE

                    // delay to show scanning animation for at least 1 second
                    binding.root.postDelayed({
                        if (response.isSuccessful) {
                            val result = response.body()?.result ?: "Unknown result"
                            val fileHash = response.body()?.sha256 ?: "Unknown hash"

                            // hide scanning animation
                            binding.scanningProgressBar.visibility = View.GONE
                            binding.scanningTextView.visibility = View.GONE

                            updateResultUI(result)
                            storeResultInDatabase(fileHash, result)
                        } else {
                            // hide scanning animation
                            binding.scanningProgressBar.visibility = View.GONE
                            binding.scanningTextView.visibility = View.GONE

                            updateResultUI("Error: ${response.code()} - ${response.errorBody()?.string()}")
                        }

                        tempFile.delete()
                    }, SCANNING_DELAY)
                }

                override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                    binding.uploadButton.isEnabled = true
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.uploadProgressTextView.visibility = View.GONE
                    binding.uploadProgressBar.visibility = View.GONE
                    binding.scanningProgressBar.visibility = View.GONE
                    binding.scanningTextView.visibility = View.GONE
                    updateResultUI("Upload failed: ${t.message}")
                    tempFile.delete()
                }
            })
        } catch (e: Exception) {
            updateResultUI("Error preparing file: ${e.message}")
            Log.e("Tool1Fragment", "File upload error", e)
        }
    }

    private fun storeResultInDatabase(fileHash: String, result: String) {
        val request = StoreResultRequest(
            userId = 1,  // currently logged in user ID
            apk_name = uploadedFileName ?: "Unknown file",  // use the stored file name
            hash = fileHash,
            result = result
        )

        databaseService.storeResult(request).enqueue(object : Callback<StoreResultResponse> {
            override fun onResponse(call: Call<StoreResultResponse>, response: Response<StoreResultResponse>) {
                if (response.isSuccessful) {
                    Log.d("Tool1Fragment", "Result stored in DB: ${response.body()?.message}")
                } else {
                    Log.e("Tool1Fragment", "Failed to store result: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<StoreResultResponse>, t: Throwable) {
                Log.e("Tool1Fragment", "DB store request failed: ${t.message}")
            }
        })
    }


    private fun updateResultUI(result: String) {
        savedPrediction = result

        binding.resultCard.visibility = View.VISIBLE
        binding.resultTextView.text = result
        binding.resultAnimation.visibility = View.VISIBLE

        binding.resultAnimation2.visibility = View.GONE
        binding.resultAnimation2.cancelAnimation()

        when (result.lowercase()) {
            "benign" -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_green, null))
                binding.resultTextView.text = "✅ Safe: This APK file is secure."
                binding.resultTextView2.text = "You can trust this file!"
                binding.resultAnimation.setAnimation(R.raw.safeall)
            }
            "malware" -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_red, null))
                binding.resultTextView.text = "⚠️ Warning: This APK file may be malicious!"
                binding.resultTextView2.text = "Be careful! This file might be harmful."
                binding.resultAnimation.setAnimation(R.raw.spam)
            }
            else -> {
                binding.resultCard.setCardBackgroundColor(resources.getColor(R.color.light_blue, null))
                binding.resultTextView.text = "❓ Unknown result."
                binding.resultTextView2.text = "\"$result\""
                binding.resultAnimation.setAnimation(R.raw.unknown2)
            }
        }

        binding.resultAnimation.repeatCount = 0 // just we gonna play this for once
        binding.resultAnimation.repeatMode = LottieDrawable.RESTART
        binding.resultAnimation.playAnimation()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ProgressRequestBody(
        private val requestBody: RequestBody,
        private val progressListener: (Long, Long) -> Unit
    ) : RequestBody() {
        override fun contentType(): MediaType? = requestBody.contentType()

        override fun contentLength(): Long = requestBody.contentLength()

        @Throws(IOException::class)
        override fun writeTo(sink: BufferedSink) {
            val countingSink = CountingSink(sink).buffer()
            requestBody.writeTo(countingSink)
            countingSink.flush()
        }

        private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
            private var bytesWritten: Long = 0

            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                bytesWritten += byteCount
                progressListener(bytesWritten, contentLength())
            }
        }
    }
}