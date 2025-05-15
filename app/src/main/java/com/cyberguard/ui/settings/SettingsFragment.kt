package com.cyberguard.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cyberguard.R
import com.cyberguard.databinding.SettingsFragmentBinding

class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.menu_settings)

        /*binding.cardAccount.setOnClickListener {
            navigateToAccount()
        }*/

        binding.cardDarkMode.setOnClickListener {
            toggleDarkMode()
        }

        binding.cardShare.setOnClickListener {
            shareApp()
        }

        binding.cardAbout.setOnClickListener {
            navigateToAbout()
        }

        binding.cardDevice.setOnClickListener {
            navigateToDevice()
        }

    }

    private fun navigateToAccount() {
       findNavController().navigate(R.id.nav_account)
    }

    private fun toggleDarkMode() {
        findNavController().navigate(R.id.darkModeFragment)
    }

    private fun navigateToAbout() {
        findNavController().navigate(R.id.nav_about)
    }

    private fun navigateToDevice() {
        findNavController().navigate(R.id.nav_device)
    }


    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://limewire.com/d/kPapt#xfKDsTL8Dw") //our CyberGuard apk download link
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}