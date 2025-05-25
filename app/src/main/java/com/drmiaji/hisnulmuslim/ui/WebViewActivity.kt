package com.drmiaji.hisnulmuslim.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import com.drmiaji.hisnulmuslim.R
import com.drmiaji.hisnulmuslim.activity.About
import com.drmiaji.hisnulmuslim.activity.BaseActivity
import com.drmiaji.hisnulmuslim.activity.SettingsActivity
import com.drmiaji.hisnulmuslim.data.Hadith
import com.drmiaji.hisnulmuslim.utils.ThemeUtils
import com.drmiaji.hisnulmuslim.utils.loadHadiths
import kotlin.math.abs

class WebViewActivity : BaseActivity() {

    override fun getLayoutResource() = R.layout.activity_webview
    private lateinit var webView: WebView
    private var currentIndex = 0
    private lateinit var chapters: List<Hadith>
    private var currentThemeMode = ""

    private val gestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (abs(diffX) > abs(diffY) && abs(diffX) > 100 && abs(velocityX) > 100) {
                    if (diffX > 0) loadPreviousChapter()
                    else loadNextChapter()
                    return true
                }
                return false
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onActivityReady(savedInstanceState: Bundle?) {
        currentThemeMode = ThemeUtils.getCurrentThemeMode(this)
        setupToolbar()
        setupWebView()
        loadInitialContent()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra("title") ?: "Reading"
        }

        setCustomFontToTitle(toolbar)
        val iconColor = ContextCompat.getColor(this, R.color.toolbar_icon_color)
        toolbar.navigationIcon?.let { drawable ->
            val wrapped = DrawableCompat.wrap(drawable).mutate()
            DrawableCompat.setTint(wrapped, iconColor)
            toolbar.navigationIcon = wrapped
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = findViewById(R.id.webview)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar?.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar?.visibility = View.GONE
            }
        }

        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            textZoom = 110
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        webView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    private fun loadInitialContent() {
        val externalUrl = intent.getStringExtra("url")
        val hadithId = intent.getIntExtra("hadith_id", -1)

        when {
            externalUrl != null -> webView.loadUrl(externalUrl)
            hadithId != -1 -> loadHadithContent(hadithId)
            else -> loadHtmlContent()
        }
    }

    private fun loadHadithContent(hadithId: Int) {
        chapters = loadHadiths(this)
        currentIndex = chapters.indexOfFirst { it.id == hadithId }.takeIf { it >= 0 } ?: 0

        if (chapters.isNotEmpty() && currentIndex in chapters.indices) {
            loadCurrentChapter()
        } else {
            webView.loadData("<h2>Hadith not found</h2>", "text/html", "utf-8")
        }
    }

    private fun loadHtmlContent() {
        val fileName = intent.getStringExtra("fileName") ?: "chapter1.html"
        val themeClass = getCurrentThemeClass()

        val baseHtml = assets.open("contents/base.html").bufferedReader().use { it.readText() }
        val contentHtml = assets.open("contents/topics/$fileName").bufferedReader().use { it.readText() }
        val fullHtml = baseHtml
            .replace("{{CONTENT}}", contentHtml)
            .replace("{{THEME}}", themeClass)
            .replace("{{STYLE}}", "")

        webView.loadDataWithBaseURL(
            "file:///android_asset/contents/",
            fullHtml,
            "text/html",
            "utf-8",
            null
        )
    }

    private fun loadCurrentChapter() {
        if (chapters.isNotEmpty() && currentIndex in chapters.indices) {
            val hadith = chapters[currentIndex]
            val themeClass = getCurrentThemeClass()

            val baseHtml = assets.open("contents/base.html").bufferedReader().use { it.readText() }
            val contentHtml = """
                <h2>${hadith.title}</h2>
                <div class="arabic">${hadith.arabic}</div>
                <div class="transliteration">${hadith.transliteration}</div>
                <div class="translation">${hadith.translation}</div>
                <div class="reference">${hadith.reference}</div>
            """.trimIndent()

            val fullHtml = baseHtml
                .replace("{{CONTENT}}", contentHtml)
                .replace("{{THEME}}", themeClass)
                .replace("{{STYLE}}", "")

            webView.loadDataWithBaseURL(
                "file:///android_asset/contents/",
                fullHtml,
                "text/html",
                "utf-8",
                null
            )
        }
    }

    private fun getCurrentThemeClass(): String {
        return when (ThemeUtils.getCurrentThemeMode(this)) {
            ThemeUtils.THEME_DARK -> "dark"
            ThemeUtils.THEME_LIGHT -> "light"
            else -> {
                val nightModeFlags = resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                    "dark" else "light"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val newThemeMode = ThemeUtils.getCurrentThemeMode(this)
        if (newThemeMode != currentThemeMode) {
            currentThemeMode = newThemeMode
            recreate()
        }
    }

    // Rest of the methods remain unchanged...
    private fun loadPreviousChapter() {
        if (currentIndex > 0) {
            currentIndex--
            loadCurrentChapter()
        }
    }

    private fun loadNextChapter() {
        if (chapters.isNotEmpty() && currentIndex < chapters.size - 1) {
            currentIndex++
            loadCurrentChapter()
        }
    }

    private fun setCustomFontToTitle(toolbar: Toolbar) {
        for (i in 0 until toolbar.childCount) {
            val view = toolbar.getChildAt(i)
            if (view is TextView && view.text == toolbar.title) {
                val typeface = Typeface.createFromAsset(assets, "fonts/solaimanlipi.ttf")
                view.typeface = typeface
                break
            }
        }
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