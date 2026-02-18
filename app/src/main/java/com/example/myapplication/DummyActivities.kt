package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

open class BaseDummyActivity(private val activityTitle: String) : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy)
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = activityTitle
        
        findViewById<TextView>(R.id.tvTitle).text = activityTitle
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
            // Avoid restarting the same activity
            if (this::class.java != intent.component?.className?.let { Class.forName(it) }) {
                startActivity(intent)
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}

class ManageMembersActivity : BaseDummyActivity("Manage Members")
class ManageEventsActivity : BaseDummyActivity("Manage Events")
class GalleryActivity : BaseDummyActivity("Gallery")
class AboutUsActivity : BaseDummyActivity("About Us")
// TeamMembersActivity and ProjectDescriptionActivity are now separate classes in their own files
