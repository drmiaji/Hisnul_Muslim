package com.drmiaji.hisnulmuslim.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.drmiaji.hisnulmuslim.R
import com.drmiaji.hisnulmuslim.activity.About
import com.drmiaji.hisnulmuslim.activity.BaseActivity
import com.drmiaji.hisnulmuslim.activity.SettingsActivity
import com.drmiaji.hisnulmuslim.data.database.HisnulMuslimDatabase
import com.drmiaji.hisnulmuslim.data.entities.DuaDetail
import com.drmiaji.hisnulmuslim.data.repository.HisnulMuslimRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class WebViewActivity : BaseActivity() {
    private lateinit var repository: HisnulMuslimRepository
    private lateinit var webView: WebView
    private var duaGlobalId: String? = null

    override fun getLayoutResource() = R.layout.activity_webview

    override fun onActivityReady(savedInstanceState: Bundle?) {
        setupToolbar()
        setupDatabase()
        setupWebView()
        loadDuaContent()
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

    private fun setupWebView() {
        webView = findViewById(R.id.webview)
        webView.settings.apply {
            javaScriptEnabled = false
            allowFileAccess = false
            allowContentAccess = false
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    private fun loadDuaContent() {
        val chapId = intent.getIntExtra("chap_id", -1)
        val chapterName = intent.getStringExtra("chapter_name") ?: ""

        if (chapId != -1) {
            lifecycleScope.launch {
                try {
                    repository.getDuaDetailsByGlobalId(chapId).collect { duaDetails ->
                        if (duaDetails.isNotEmpty()) {
                            val htmlContent = generateHtmlContent(duaDetails, chapterName)
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        } else {
                            showErrorMessage("No content found for this dua.")
                        }
                    }
                } catch (e: Exception) {
                    showErrorMessage("Error loading dua content: ${e.message}")
                }
            }
        } else {
            showErrorMessage("Invalid dua id")
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
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        margin: 16px;
                        line-height: 1.6;
                        background-color: #f5f5f5;
                    }
                    .dua-container {
                        background: white;
                        border-radius: 8px;
                        padding: 16px;
                        margin-bottom: 16px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .chapter-title {
                        color: #2196F3;
                        font-size: 18px;
                        font-weight: bold;
                        margin-bottom: 16px;
                        text-align: center;
                    }
                    .top-text, .bottom-text {
                        color: #666;
                        font-size: 14px;
                        margin: 8px 0;
                        font-style: italic;
                    }
                    .arabic {
                        font-size: 24px;
                        text-align: right;
                        direction: rtl;
                        margin: 16px 0;
                        line-height: 2;
                        color: #333;
                    }
                    .transliteration {
                        font-size: 16px;
                        color: #444;
                        margin: 12px 0;
                        font-style: italic;
                    }
                    .translation {
                        font-size: 16px;
                        color: #333;
                        margin: 12px 0;
                    }
                    .reference {
                        font-size: 12px;
                        color: #888;
                        margin-top: 12px;
                        border-top: 1px solid #eee;
                        padding-top: 8px;
                    }
                    .segment {
                        margin-bottom: 24px;
                        border-bottom: 1px solid #eee;
                        padding-bottom: 16px;
                    }
                    .segment:last-child {
                        border-bottom: none;
                    }
                </style>
            </head>
            <body>
        """)

        if (chapterName.isNotEmpty()) {
            htmlBuilder.append("<div class='chapter-title'>$chapterName</div>")
        }

        duaDetails.sortedBy { it.id }.forEach { detail ->
            htmlBuilder.append("<div class='dua-container'>")
            htmlBuilder.append("<div class='segment'>")

            // Top text
            detail.top?.takeIf { it.isNotBlank() }?.let {
                htmlBuilder.append("<div class='top-text'>$it</div>")
            }

            // Arabic text
            detail.arabic?.takeIf { it.isNotBlank() }?.let {
                htmlBuilder.append("<div class='arabic'>$it</div>")
            }

            // Transliteration
            detail.transliteration?.takeIf { it.isNotBlank() }?.let {
                htmlBuilder.append("<div class='transliteration'>$it</div>")
            }

            // Translation
            detail.translations?.takeIf { it.isNotBlank() }?.let {
                htmlBuilder.append("<div class='translation'>$it</div>")
            }

            // Bottom text
            detail.bottom?.takeIf { it.isNotBlank() }?.let {
                htmlBuilder.append("<div class='bottom-text'>$it</div>")
            }

            // Reference
            detail.reference?.takeIf { it.isNotBlank() }?.let {
                htmlBuilder.append("<div class='reference'>Reference: $it</div>")
            }

            htmlBuilder.append("</div>")
            htmlBuilder.append("</div>")
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
                <style>
                    body { 
                        font-family: Arial; 
                        padding: 20px; 
                        text-align: center; 
                        color: #666; 
                    }
                </style>
            </head>
            <body>
                <h3>Error</h3>
                <p>$message</p>
            </body>
            </html>
        """
        webView.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null)
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