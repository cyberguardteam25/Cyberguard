package com.cyberguard.ui.settings.device

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cyberguard.databinding.FragmentDeviceBinding
import java.io.File


class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateDeviceInfo()
        updateRunningSystemInfo()
        updateMemoryUsage()
        updateBatteryPercentage()
        updateStorageUsage()

        return root
    }

    private fun updateStorageUsage() {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes: Long
            val freeBytes: Long

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalBytes = stat.blockSizeLong * stat.blockCountLong
                freeBytes = stat.blockSizeLong * stat.availableBlocksLong
            } else {
                @Suppress("DEPRECATION")
                totalBytes = stat.blockSize.toLong() * stat.blockCount.toLong()
                @Suppress("DEPRECATION")
                freeBytes = stat.blockSize.toLong() * stat.availableBlocks.toLong()
            }

            val usedBytes = totalBytes - freeBytes
            val totalGB = totalBytes.toDouble() / 1e9
            val usedGB = usedBytes.toDouble() / 1e9

            val percentage = (usedBytes.toDouble() / totalBytes.toDouble() * 100).toInt()

            binding.storageCircleView.setStoragePercentage(percentage)
            binding.card3Description.text = String.format("%.1f GB / %.1f GB", usedGB, totalGB)
        } catch (e: Exception) {
            binding.card3Description.text = "Storage info unavailable"
            binding.storageCircleView.setStoragePercentage(0)
        }
    }

    private fun updateDeviceInfo() {
        // to get device name (user defined name ex:duaa)
        val deviceName = Settings.Global.getString(
            requireContext().contentResolver,
            Settings.Global.DEVICE_NAME
        ) ?: Build.MODEL

        // to get device model info
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

        // update views on top of card
        binding.cyberGuardTitle.text = deviceName
        binding.cyberGuardSubtitle.text = deviceModel
    }

    private fun updateRunningSystemInfo() {
        val androidVersion = "Android ${Build.VERSION.RELEASE}"
        binding.userNameTextView.text = androidVersion
        binding.deviceInfoTextView.text = "API Level ${Build.VERSION.SDK_INT}"
    }

    private fun updateMemoryUsage() {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemoryGB = (memoryInfo.totalMem / 1e9).toFloat()
        val usedMemoryGB = ((memoryInfo.totalMem - memoryInfo.availMem) / 1e9).toFloat()

        binding.card4CircleView.setMemoryPercentage((usedMemoryGB / totalMemoryGB * 100).toInt())
        binding.card4Title.text = String.format("%.1f GB / %.1f GB", usedMemoryGB, totalMemoryGB)
    }

    private fun updateBatteryPercentage() {
        val batteryPercentage = getBatteryPercentage()
        binding.batteryCircleView.setBatteryPercentage(batteryPercentage)
    }

    private fun getBatteryPercentage(): Int {
        val batteryManager = requireActivity().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}