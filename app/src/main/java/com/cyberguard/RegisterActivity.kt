package com.cyberguard

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
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
import com.cyberguard.R

// APIs
interface RegisterApiService {
    @POST("register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
}

data class RegisterRequest(val username: String, val email: String, val password: String)
data class RegisterResponse(val success: Boolean, val message: String)

class RegisterActivity : AppCompatActivity() {

    private lateinit var inputUsername: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputReEnterPassword: EditText
    private lateinit var checkboxTerms: CheckBox
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val savedTheme = sharedPref.getInt("theme_preference", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedTheme)

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        // views
        checkboxTerms = findViewById(R.id.checkboxTerms)
        val textViewTerms = findViewById<TextView>(R.id.textViewTerms)
        btnRegister = findViewById(R.id.btnRegister)
        inputUsername = findViewById(R.id.inputUsername)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        inputReEnterPassword = findViewById(R.id.inputReEnterPassword)
        val txtLoginLink = findViewById<TextView>(R.id.txtLoginLink)

        val grayColor = resources.getColor(R.color.gray, null)
        val activeColor = resources.getColor(R.color.teal_700, null)

        btnRegister.isEnabled = false
        btnRegister.setBackgroundColor(grayColor)

        fun updateRegisterButtonState() {
            val termsChecked = checkboxTerms.isChecked
            btnRegister.isEnabled = termsChecked
            btnRegister.setBackgroundColor(if (termsChecked) activeColor else grayColor)
        }


        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRegisterButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        inputUsername.addTextChangedListener(textWatcher)
        inputEmail.addTextChangedListener(textWatcher)
        inputPassword.addTextChangedListener(textWatcher)
        inputReEnterPassword.addTextChangedListener(textWatcher)

        checkboxTerms.setOnCheckedChangeListener { _, _ ->
            updateRegisterButtonState()
        }

        textViewTerms.setOnClickListener {
            showPolicyDialog(checkboxTerms, btnRegister)
        }

        btnRegister.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val reEnteredPassword = inputReEnterPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || reEnteredPassword.isEmpty()) {
                showSnackbar("Please fill all fields")
            } else if (!isValidUsername(username)) {
                showSnackbar("Username must be at least 3 characters, can include letters, numbers, underscores (_), and periods (.), but can't start with a number or period or end with a period.")
            } else if (!isValidPassword(password)) {
                showSnackbar("Password must be at least 8 characters long, include uppercase, lowercase, and a symbol.")
            } else if (!isValidEmail(email)) {
                showSnackbar("Please enter a valid email address.")
            } else if (password != reEnteredPassword) {
                showSnackbar("Passwords do not match!")
            } else {
                registerUser(username, email, password)
            }
        }

        txtLoginLink.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    private fun showPolicyDialog(checkbox: CheckBox, registerButton: Button) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Privacy Policy & Terms")
        builder.setMessage(
            """
            By using CyberGuard, you agree to the following:
            
            • We collect only necessary data (scan results so u can check them in History, and your email and hashed password for authentication).
            • Your info is stored securely.
            • You must not use this app for malicious purposes.
            • This app uses Third-party tools (VirusTotal), so accepting our policy mean accepting their policies too.
            
            Do you accept these terms?
            """.trimIndent()
        )

        builder.setPositiveButton("Accept") { dialog, _ ->
            checkbox.isChecked = true
            dialog.dismiss()
        }

        builder.setNegativeButton("Reject") { dialog, _ ->
            checkbox.isChecked = false
            registerButton.isEnabled = false
            dialog.dismiss()
        }

        builder.setCancelable(false)
        builder.show()
    }

    private fun isValidUsername(username: String): Boolean {
        val pattern = Pattern.compile("^(?![0-9.])[A-Za-z0-9._]{3,}(?<!\\.)$")
        return pattern.matcher(username).matches()
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return pattern.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{8,}$")
        return pattern.matcher(password).matches()
    }

    private fun registerUser(username: String, email: String, password: String) {
        val hashedPassword = hashPassword(password)
        val request = RegisterRequest(username, email, hashedPassword)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://32ab-92-253-1-148.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(RegisterApiService::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.registerUser(request)
                val registerResponse = response.body()

                if (response.isSuccessful && registerResponse != null) {
                    showToast(registerResponse.message)
                    if (registerResponse.success) {
                        startActivity(Intent(this@RegisterActivity, SplashActivity::class.java))
                        finish()
                    }
                } else {
                    val errorMessage = extractMessage(response.errorBody()?.string())
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                showToast(e.message ?: "An error occurred")
            }
        }
    }

    private fun extractMessage(json: String?): String {
        return try {
            json?.let {
                val jsonObject = JSONObject(it)
                jsonObject.getString("message")
            } ?: "Registration failed"
        } catch (e: Exception) {
            "Registration failed"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        val rootView = findViewById<android.view.View>(android.R.id.content)
        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 5
        snackbar.show()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        val bigInt = BigInteger(1, hashedBytes)
        return bigInt.toString(16).padStart(64, '0')
    }
}
