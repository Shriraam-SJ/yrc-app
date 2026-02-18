package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.navView)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigation(menuItem)
            true
        }

        // Set default fragment
        replaceFragment(ProfileInfoFragment())

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(ProfileInfoFragment())
                    1 -> replaceFragment(ScheduledEventsFragment())
                    2 -> replaceFragment(MessagingFragment())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Handle back press to show exit confirmation dialog or close drawer
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    showExitDialog()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (handleNavigation(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun handleNavigation(item: MenuItem): Boolean {
        val intent = when (item.itemId) {
            R.id.nav_manage_members -> Intent(this, ManageMembersActivity::class.java)
            R.id.nav_manage_events -> Intent(this, ManageEventsActivity::class.java)
            R.id.nav_gallery -> Intent(this, GalleryActivity::class.java)
            R.id.nav_about_us -> Intent(this, AboutUsActivity::class.java)
            R.id.nav_team_members -> Intent(this, TeamMembersActivity::class.java)
            R.id.nav_project_desc -> Intent(this, ProjectDescriptionActivity::class.java)
            else -> null
        }
        
        return if (intent != null) {
            startActivity(intent)
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            true
        } else {
            false
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit Application")
            .setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
