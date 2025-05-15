package com.cyberguard.ui.settings.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.cyberguard.databinding.FragmentAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val userId = 123 // dummy user id just for now

    private lateinit var userService: UserService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://2909-92-253-1-148.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        userService = retrofit.create(UserService::class.java)

        // updating button click
        binding.btnUpdate.setOnClickListener {
            val newUsername = binding.changeUsernameText.text.toString().trim()
            val newEmail = binding.changeEmailText.text.toString().trim()
            val newPassword = binding.changePasswordText.text.toString().trim()

            if (newUsername.isEmpty() && newEmail.isEmpty() && newPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter at least one value to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateUser(newUsername, newEmail, newPassword)
        }
    }

    private fun updateUser(username: String?, email: String?, password: String?) {
        val call = userService.updateUser(userId, username, email, password)

        call.enqueue(object : Callback<UpdateResponse> {
            override fun onResponse(call: Call<UpdateResponse>, response: Response<UpdateResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        Toast.makeText(requireContext(), "Update successful: ${result.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Update failed: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Server error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Update failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// to retrofit interface
interface UserService {
    @FormUrlEncoded
    @POST("update_user.php")
    fun updateUser(
        @Field("user_id") userId: Int,
        @Field("username") username: String?,
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Call<UpdateResponse>
}

// response model
data class UpdateResponse(
    val success: Boolean,
    val message: String
)
