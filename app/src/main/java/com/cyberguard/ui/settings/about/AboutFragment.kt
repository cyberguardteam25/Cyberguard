package com.cyberguard.ui.settings.about

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cyberguard.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // to retrieve app version dynamically
        val versionTextView: TextView = view.findViewById(R.id.versionTextView)
        val version = getAppVersion()
        versionTextView.text = "Version: $version"

        view.findViewById<View>(R.id.text1).setOnClickListener {
            sendEmail("cyberguard.team.25@gmail.com") // our team contact email
        }

        view.findViewById<View>(R.id.text2).setOnClickListener {
        }

        view.findViewById<View>(R.id.text3).setOnClickListener {
            openWebsite("https://www.just.edu.jo/Pages/Default.aspx") // our uni JUST website
        }
    }

    private fun getAppVersion(): String {
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            return packageInfo.versionName ?: "Unknown Version" // access version name
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return "Unknown Version" // in case version not found
        }
    }

    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$email")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Regarding ${resources.getString(R.string.app_name)}")
        startActivity(Intent.createChooser(intent, "Send email via"))
    }
}
