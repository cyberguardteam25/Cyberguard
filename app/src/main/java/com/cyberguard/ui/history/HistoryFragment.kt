package com.cyberguard.ui.history

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.cyberguard.databinding.FragmentHistoryBinding

// backend APIs
interface HistoryApiService {
    @POST("history")
    fun getHistory(@Body request: HistoryRequest): Call<HistoryResponse>

    @POST("delete_history")
    fun deleteHistory(@Body request: DeleteHistoryRequest): Call<BasicResponse>
}

// data models
data class HistoryRequest(val userId: Int)
data class DeleteHistoryRequest(val userId: Int, val tool: String, val input: String)
data class HistoryItem(val tool: String, val input: String, val result: String)
data class HistoryResponse(val success: Boolean, val history: List<HistoryItem>)
data class BasicResponse(val success: Boolean, val message: String)

// retrofit instance
private val retrofit = Retrofit.Builder()
    .baseUrl("https://32ab-92-253-1-148.ngrok-free.app/") // backend NGROCK URL
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val historyApi = retrofit.create(HistoryApiService::class.java)

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    private val filteredList = mutableListOf<HistoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        historyAdapter = HistoryAdapter(filteredList) { item -> deleteHistory(item) }
        binding.recyclerView.adapter = historyAdapter

        fetchHistory()
        setupSearch()

        return binding.root
    }

    private fun fetchHistory() {
        val userId = getUserId()
        if (userId == -1) return

        historyApi.getHistory(HistoryRequest(userId)).enqueue(object : Callback<HistoryResponse> {
            override fun onResponse(call: Call<HistoryResponse>, response: Response<HistoryResponse>) {
                if (response.isSuccessful) {
                    historyList.clear()
                    historyList.addAll(response.body()?.history ?: emptyList())

                    filteredList.clear()
                    filteredList.addAll(historyList)
                    historyAdapter.notifyDataSetChanged()

                    toggleViews()
                } else {
                    Toast.makeText(requireContext(), "Failed to load history", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteHistory(item: HistoryItem) {
        val userId = getUserId()
        historyApi.deleteHistory(DeleteHistoryRequest(userId, item.tool, item.input)).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful) {
                    fetchHistory()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete item", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Delete failed: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterHistory(newText)
                return true
            }
        })
    }

    private fun filterHistory(query: String?) {
        filteredList.clear()
        if (query.isNullOrEmpty()) {
            filteredList.addAll(historyList)
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredList.addAll(historyList.filter {
                it.tool.lowercase().contains(lowerCaseQuery) ||
                        it.input.lowercase().contains(lowerCaseQuery) ||
                        it.result.lowercase().contains(lowerCaseQuery)
            })
        }
        historyAdapter.notifyDataSetChanged()
        toggleViews()
    }

    private fun toggleViews() {
        if (historyList.isEmpty()) {
            // no history at all (even before search)!
            binding.recyclerView.visibility = View.GONE
            binding.searchView.visibility = View.GONE
            binding.tableHeader.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            // there is history, even if search is empty
            binding.recyclerView.visibility = View.VISIBLE
            binding.searchView.visibility = View.VISIBLE
            binding.tableHeader.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }


    private fun getUserId(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }
}
