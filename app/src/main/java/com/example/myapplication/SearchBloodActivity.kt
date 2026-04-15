package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.User
import kotlinx.coroutines.launch

class SearchBloodActivity : AppCompatActivity() {

    private lateinit var spinnerBloodGroup: AutoCompleteTextView
    private lateinit var rvDonorResults: RecyclerView
    private lateinit var adapter: DonorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_blood)

        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup)
        rvDonorResults = findViewById(R.id.rvDonorResults)

        val bloodGroups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodGroups)
        spinnerBloodGroup.setAdapter(arrayAdapter)

        adapter = DonorAdapter(emptyList()) { user ->
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${user.phoneNumber}")
            startActivity(intent)
        }

        rvDonorResults.layoutManager = LinearLayoutManager(this)
        rvDonorResults.adapter = adapter

        spinnerBloodGroup.setOnItemClickListener { parent, _, position, _ ->
            val selectedGroup = parent.getItemAtPosition(position).toString()
            searchDonors(selectedGroup)
        }
    }

    private fun searchDonors(bloodGroup: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.searchBlood(bloodGroup)
                if (response.isSuccessful) {
                    val donors = response.body() ?: emptyList()
                    adapter.updateDonors(donors)
                    if (donors.isEmpty()) {
                        Toast.makeText(this@SearchBloodActivity, "No donors found for $bloodGroup", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SearchBloodActivity, "Failed to fetch donors", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchBloodActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class DonorAdapter(
        private var donors: List<User>,
        private val onClick: (User) -> Unit
    ) : RecyclerView.Adapter<DonorAdapter.ViewHolder>() {

        fun updateDonors(newDonors: List<User>) {
            donors = newDonors
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donor, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val donor = donors[position]
            holder.tvName.text = donor.fullName
            holder.tvPhone.text = donor.phoneNumber
            holder.itemView.setOnClickListener { onClick(donor) }
        }

        override fun getItemCount(): Int = donors.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvDonorName)
            val tvPhone: TextView = view.findViewById(R.id.tvDonorPhone)
        }
    }
}
