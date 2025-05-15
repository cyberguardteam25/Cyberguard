package com.cyberguard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigInteger
import java.security.MessageDigest
import java.util.regex.Pattern

// APIs
interface ApiService {
    @POST("login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
}

// data models
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String, val id: Int?, val username: String?)

object RetrofitClient {
    private const val BASE_URL = "https://32ab-92-253-1-148.ngrok-free.app/" //5000

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // here we get stored theme preference and apply it
        val sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val savedTheme = sharedPref.getInt("theme_preference", AppCompatDelegate.MODE_NIGHT_NO) // Default to Light mode if not set

        // just apply the saved theme mode
        AppCompatDelegate.setDefaultNightMode(savedTheme)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash)

        if (isUserLoggedIn()) {
            navigateToMain()  // navigate directly if logged in (log in once!)
            return
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        //val proceedButton = findViewById<Button>(R.id.proceedButton)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerPrompt = findViewById<TextView>(R.id.registerPrompt)

        """proceedButton.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please fill all fields")
            } else {
                navigateToMain()
            }
        }"""

        loginButton.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please fill all fields")
            } else if (!isValidEmail(email)) {
                showToast("Please enter a valid email address")
            } else {
                loginUser(email, password)
            }
        }

        registerPrompt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }


    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("is_logged_in", false)
    }



    private fun loginUser(email: String, password: String) {
        val hashedPassword = hashPassword(password)
        val loginRequest = LoginRequest(email, hashedPassword)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.instance.loginUser(loginRequest)
                val loginResponse = response.body()

                if (response.isSuccessful && loginResponse != null) {
                    showToast(loginResponse.message)

                    if (loginResponse.success && loginResponse.id != null) {
                        saveUserData(loginResponse.id, loginResponse.username ?: "User", email)
                        navigateToMain()
                    }
                } else {
                    val errorMessage = extractMessage(response.errorBody()?.string())
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                showToast("Login failed: ${e.message}")
            }
        }
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

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        val bigInt = BigInteger(1, hashedBytes)
        return bigInt.toString(16).padStart(64, '0')
    }

    // save user data in SharedPreferences
    private fun saveUserData(userId: Int, username: String, email: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("user_id", userId)
            .putString("username", username)
            .putString("email", email)
            .putBoolean("is_logged_in", true)
            .apply()
    }



    // may be needed: to retrieve user id
    private fun getUserId(): Int {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }
}
