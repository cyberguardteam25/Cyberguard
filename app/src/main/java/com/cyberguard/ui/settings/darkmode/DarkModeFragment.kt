package com.cyberguard.ui.settings.darkmode

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.cyberguard.R
import com.cyberguard.databinding.FragmentDarkModeBinding

class DarkModeFragment : Fragment() {

    private var _binding: FragmentDarkModeBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDarkModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // here we use shared preferences to save the theme
        val sharedPref = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val savedTheme = sharedPref.getInt("theme_preference", AppCompatDelegate.MODE_NIGHT_NO)

        // an initial state for switch and current mode text
        val switchMode: Switch = binding.root.findViewById(R.id.switchMode)
        val tvCurrentMode: TextView = binding.root.findViewById(R.id.tvCurrentMode)
        imageView = binding.root.findViewById(R.id.image1)

        // initial mode (light mode as default)
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            switchMode.isChecked = false // Light Mode
            tvCurrentMode.text = "Light Mode"
            imageView.setImageResource(R.drawable.light)
        } else {
            switchMode.isChecked = true // second mode (dark mode)
            tvCurrentMode.text = "Dark Mode"
            imageView.setImageResource(R.drawable.dark)
        }

        // a toggle between the two modes
        switchMode.setOnCheckedChangeListener { _, isChecked ->
            val themeMode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES // dark
            } else {
                AppCompatDelegate.MODE_NIGHT_NO // light
            }

            // to save  theme preference
            sharedPref.edit().putInt("theme_preference", themeMode).apply()

            // to apply theme
            AppCompatDelegate.setDefaultNightMode(themeMode)

            // update text according to current mode
            if (isChecked) {
                tvCurrentMode.text = "Dark Mode"
            } else {
                tvCurrentMode.text = "Light Mode"
            }

            // here we used delay for sync
            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    imageView.setImageResource(R.drawable.dark) //dark image
                } else {
                    imageView.setImageResource(R.drawable.light) // light image
                }
            }, 500) // 500 milliseconds delay
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
