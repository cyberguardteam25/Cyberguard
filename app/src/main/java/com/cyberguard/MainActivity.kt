package com.cyberguard

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cyberguard.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {



    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarIcon: ImageView
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        // here we get the stored theme preference and apply it
        val sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val savedTheme = sharedPref.getInt("theme_preference", AppCompatDelegate.MODE_NIGHT_NO) // default is light

        // apply the saved theme mode
        AppCompatDelegate.setDefaultNightMode(savedTheme)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbarIcon = binding.appBarMain.toolbar.findViewById(R.id.toolbar_icon)
        toolbarTitle = binding.appBarMain.toolbar.findViewById(R.id.toolbar_title)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")
        val email = sharedPreferences.getString("email", "example@domain.com")

        val headerView = navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = headerView.findViewById<TextView>(R.id.emailTextView)

        usernameTextView.text = username
        emailTextView.text = email

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home,R.id.nav_ai, R.id.nav_history, R.id.nav_awareness, R.id.nav_settings),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            toolbarTitle.text = destination.label

            val iconRes = when (destination.id) {
                R.id.nav_home -> R.drawable.white_logo6
                R.id.nav_ai -> R.drawable.chat
                R.id.nav_history -> R.drawable.history_nav3
                R.id.tool1Fragment -> R.drawable.android2
                R.id.tool2Fragment -> R.drawable.paragraph1
                R.id.nav_awareness -> R.drawable.advice
                R.id.tool3Fragment -> R.drawable.link1
                R.id.tool4Fragment -> R.drawable.scan
                R.id.nav_about -> R.drawable.about
                R.id.nav_settings -> R.drawable.settings
                R.id.darkModeFragment -> R.drawable.moon
                R.id.nav_account -> R.drawable.account
                R.id.nav_device -> R.drawable.device
                else -> R.drawable.white_logo6
            }

            val resizedBitmap = resizeDrawable(iconRes, 50, 50)
            toolbarIcon.setImageBitmap(resizedBitmap)
        }

        // logout
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    logoutUser()
                    showToast("Logged out")
                    true
                }
                else -> {
                    val destination = navController.graph.findNode(menuItem.itemId)
                    if (destination != null) {
                        navController.navigate(menuItem.itemId)
                    } else {
                        Toast.makeText(this, "Feature under construction!", Toast.LENGTH_SHORT).show()
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun resizeDrawable(drawableRes: Int, width: Int, height: Int): Bitmap {
        val bitmap = BitmapFactory.decodeResource(resources, drawableRes)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
