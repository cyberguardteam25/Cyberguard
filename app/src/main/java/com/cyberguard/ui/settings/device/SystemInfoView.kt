package com.cyberguard.ui.settings.device

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

//class SystemInfoView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {
class SystemInfoView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    init {
        // Set system information text
        text = getSystemInfo()
    }

    private fun getSystemInfo(): String {
        return """
            Device Model: ${Build.MODEL}
            OS Version: ${Build.VERSION.RELEASE}
            SDK Version: ${Build.VERSION.SDK_INT}
            Manufacturer: ${Build.MANUFACTURER}
            Brand: ${Build.BRAND}
        """.trimIndent()
    }
}