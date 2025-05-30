package com.drmiaji.hisnulmuslim.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.drmiaji.hisnulmuslim.R
import com.drmiaji.hisnulmuslim.activity.About
import com.drmiaji.hisnulmuslim.activity.BaseActivity
import com.drmiaji.hisnulmuslim.activity.SettingsActivity
import com.drmiaji.hisnulmuslim.adapter.WebViewPagerAdapter
import com.drmiaji.hisnulmuslim.data.database.HisnulMuslimDatabase
import com.drmiaji.hisnulmuslim.data.entities.DuaDetail
import com.drmiaji.hisnulmuslim.data.repository.HisnulMuslimRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class WebViewActivity : BaseActivity() {
    private lateinit var repository: HisnulMuslimRepository
    private lateinit var viewPager: ViewPager2
    private val duaIdToChapterName = mutableMapOf<Int, String>()

    override fun getLayoutResource() = R.layout.activity_webview

    override fun onActivityReady(savedInstanceState: Bundle?) {
        setupToolbar()
        setupDatabase()
        setupViewPager()
        // Load all dua names to populate the chapter name map, then load pages
        lifecycleScope.launch {
            repository.getAllDuaNames().collect { duaNames ->
                duaIdToChapterName.clear()
                duaNames.forEach { duaName ->
                    duaIdToChapterName[duaName.chap_id] = duaName.chapname ?: ""
                }
                loadAllDuaPages()
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val titleTextView = findViewById<TextView>(R.id.toolbar_title)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val title = intent.getStringExtra("title") ?: getString(R.string.app_name)
        titleTextView.text = title

        val typeface = Typeface.createFromAsset(assets, "fonts/solaimanlipi.ttf")
        titleTextView.typeface = typeface

        val iconColor = ContextCompat.getColor(this, R.color.toolbar_icon_color)
        toolbar.navigationIcon?.let { drawable ->
            val wrapped = DrawableCompat.wrap(drawable).mutate()
            DrawableCompat.setTint(wrapped, iconColor)
            toolbar.navigationIcon = wrapped
        }
    }

    private fun setupDatabase() {
        val database = HisnulMuslimDatabase.getDatabase(this)
        repository = HisnulMuslimRepository(
            database.categoryDao(),
            database.duaNameDao(),
            database.duaDetailDao()
        )
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
    }

    private fun loadAllDuaPages() {
        val selectedDuaId = intent.getIntExtra("dua_id", -1)
        lifecycleScope.launch {
            repository.getAllDuaDetailsSorted().collect { allDuaDetails ->
                // Build HTML for each dua detail, using chapter name map
                val htmlPages = allDuaDetails.map { detail ->
                    val chapterName = duaIdToChapterName[detail.dua_global_id] ?: ""
                    generateHtmlContent(listOf(detail), chapterName)
                }
                viewPager.adapter = WebViewPagerAdapter(this@WebViewActivity, htmlPages)

                // Scroll to the selected dua if available
                val startIndex = allDuaDetails.indexOfFirst { it.id == selectedDuaId }
                if (startIndex >= 0) {
                    viewPager.setCurrentItem(startIndex, false)
                }

                // Update toolbar title as user swipes
                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        val duaDetail = allDuaDetails[position]
                        val chapterName = duaIdToChapterName[duaDetail.dua_global_id] ?: getString(R.string.app_name)
                        val titleTextView = findViewById<TextView>(R.id.toolbar_title)
                        titleTextView.text = chapterName
                    }
                })
            }
        }
    }

    private fun generateHtmlContent(duaDetails: List<DuaDetail>, chapterName: String): String {
        val htmlBuilder = StringBuilder()
        htmlBuilder.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" type="text/css" href="style.css">
            </head>
            <body>
        """)
        if (chapterName.isNotEmpty()) {
            htmlBuilder.append("<h3 class='chapter-title'>$chapterName</h3>")
        }
        duaDetails.forEach { detail ->
            htmlBuilder.append("<div class='dua-container'><div class='segment'>")
            detail.top?.takeIf { it.isNotBlank() }?.let { htmlBuilder.append("<div class='top-text'>$it</div>") }
            detail.arabic?.takeIf { it.isNotBlank() }?.let { htmlBuilder.append("<div class='arabic'>$it</div>") }
            detail.transliteration?.takeIf { it.isNotBlank() }?.let { htmlBuilder.append("<div class='transliteration'>$it</div>") }
            detail.translations?.takeIf { it.isNotBlank() }?.let { htmlBuilder.append("<div class='translation'>$it</div>") }
            detail.bottom?.takeIf { it.isNotBlank() }?.let { htmlBuilder.append("<div class='bottom-text'>$it</div>") }
            detail.reference?.takeIf { it.isNotBlank() }?.let { htmlBuilder.append("<div class='reference'>Reference: $it</div>") }
            htmlBuilder.append("</div></div>")
        }
        htmlBuilder.append("""
            </body>
            </html>
        """)
        return htmlBuilder.toString()
    }

    private fun showErrorMessage(message: String) {
        val errorHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" type="text/css" href="style.css">
            </head>
            <body>
                <div class="error-message">
                    <h3>Error</h3>
                    <p>$message</p>
                </div>
            </body>
            </html>
        """
        viewPager.adapter = WebViewPagerAdapter(this, listOf(errorHtml))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        menu.findItem(R.id.action_search)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.share -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                }
                startActivity(Intent.createChooser(shareIntent, "Share using"))
                true
            }
            R.id.more_apps -> {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = "https://play.google.com/store/apps/dev?id=5204491413792621474".toUri()
                })
                true
            }
            R.id.action_about_us -> {
                startActivity(Intent(this, About::class.java))
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}