package com.cyberguard.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cyberguard.R
import com.cyberguard.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // here to access tool's title, description, and icon TextViews inside CardView (the xml layout)
        val toolTitle1: TextView = binding.toolTitle1
        val toolIcon1: ImageView = binding.toolIcon1

        // set the title, description, and icon for Tool 1 (apk scan)
        toolTitle1.text = "APK Scan"
        toolIcon1.setImageResource(R.drawable.android2)

        // navigate (open) to Tool 1 on click
        binding.toolCard1.setOnClickListener {
            findNavController().navigate(R.id.tool1Fragment)
        }

        val toolTitle2: TextView = binding.toolTitle2
        val toolIcon2: ImageView = binding.toolIcon2

        toolTitle2.text = "Text Scan"
        toolIcon2.setImageResource(R.drawable.paragraph1)

        binding.toolCard2.setOnClickListener {
            findNavController().navigate(R.id.tool2Fragment)
        }


        val toolTitle3: TextView = binding.toolTitle3
        val toolIcon3: ImageView = binding.toolIcon3

        toolTitle3.text = "URL Scan"
        toolIcon3.setImageResource(R.drawable.link1)

        binding.toolCard3.setOnClickListener {
            findNavController().navigate(R.id.tool3Fragment)
        }

        val toolTitle4: TextView = binding.toolTitle4
        val toolIcon4: ImageView = binding.toolIcon4

        toolTitle4.text = "QR Scan"
        toolIcon3.setImageResource(R.drawable.link1)

        binding.toolCard4.setOnClickListener {
            findNavController().navigate(R.id.tool4Fragment)
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}