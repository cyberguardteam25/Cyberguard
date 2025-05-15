package com.cyberguard.ui.awareness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cyberguard.databinding.FragmentCyberAwarenessBinding

class AwarenessNavigation : Fragment() {

    private var _binding: FragmentCyberAwarenessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCyberAwarenessBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.passwordsSection.text = "Use strong and long passwords that include uppercase and lowercase letters, numbers, and symbols. Do not use the same password across multiple accounts. Use two-factor authentication (2FA) to protect your accounts. Do not share passwords with others."
        binding.linksSection.text = "Do not click on suspicious links or unknown links contained in emails or text messages, do not upload attached files from unreliable sources, make sure that the links begin with 'https://' to ensure a secure connection."
        binding.socialEngineeringSection.text = "Be cautious of any unexpected requests for personal or financial information. Do not share personal information (such as passwords or credit card numbers) over the phone or internet unless you are certain of the other party's identity."
        binding.financialActivitySection.text = "Regularly check bank accounts and credit careds to detect any suspicious or fraudulent transactions, do not share any financial information with anyone, also subscribe to a banking alerts service that notifies you of any transection on your accounts."
        binding.updateSoftwareSection.text = "constantly update your operating system, applications, and antivirus software to protect against vulnerabilities."

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}