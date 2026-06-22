package com.savanna.browser.fragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import com.savanna.browser.MainActivity
import com.savanna.browser.NewTabBridge
import com.savanna.browser.R
import com.savanna.browser.manager.ThemeManager
import com.savanna.browser.util.UrlUtils
import kotlin.math.abs

class BrowserFragment : Fragment() {

    private var _webView: WebView? = null
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomBar: View
    private lateinit var btnBack: ImageView
    private lateinit var btnForward: ImageView
    private lateinit var btnShare: ImageView
    private lateinit var btnBookmark: ImageView
    private lateinit var btnTabs: TextView
    private lateinit var btnSettings: ImageView
    private lateinit var urlActionsStrip: HorizontalScrollView
    private lateinit var chipClear: TextView
    private lateinit var chipCopy: TextView
    private lateinit var chipPaste: TextView
    private lateinit var chipPasteGo: TextView
    private lateinit var chipShareLink: TextView
    private lateinit var chipBack: TextView
    private lateinit var chipForward: TextView
    private lateinit var chipDate: TextView
    private lateinit var chipTime: TextView
    private lateinit var chipReload: TextView
    private lateinit var chipHistory: TextView
    private lateinit var chipFind: TextView

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var findBar: LinearLayout
    private lateinit var findInput: EditText
    private lateinit var findCount: TextView
    private lateinit var findPrev: ImageView
    private lateinit var findNext: ImageView
    private lateinit var findClose: ImageView
    private lateinit var urlSuggestions: RecyclerView
    private lateinit var urlLock: ImageView
    private lateinit var urlReload: ImageView
    private lateinit var urlReader: ImageView
    private lateinit var urlBarSearch: FrameLayout
    private lateinit var urlScrollPill: FrameLayout
    private lateinit var urlDomainText: TextView
    private lateinit var topSearchBar: FrameLayout
    private lateinit var compactBottomBar: LinearLayout

    private var isCompactMode = false
    private var isScrolledDown = false
    private var lastScrollY = 0
    private var scrollPillAlpha = 0f
    private var isReaderMode = false
    private var isFindVisible = false

    private var tabId: String = ""
    private val webView get() = _webView!!
    private var isNewTabPage = false
    private var currentToolbarTint = Color.TRANSPARENT
    private var themeBgColor = Color.parseColor("#FF1C1C1E")
    private var lastDefaultBg = Color.parseColor("#FF1C1C1E")
    private var lastUrlBarStyle = ThemeManager.STYLE_GLASS
    private lateinit var urlBarContainer: View
    private val density get() = resources.displayMetrics.density

    private val CHROME_UA by lazy { WebSettings.getDefaultUserAgent(requireContext()) }

    companion object {
        private const val ARG_TAB_ID = "tab_id"
        private const val ARG_URL = "url"
        const val NEW_TAB_URL = "file:///android_asset/new_tab.html"

        fun newInstance(tabId: String, url: String = "") = BrowserFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TAB_ID, tabId)
                putString(ARG_URL, url)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_browser, container, false)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabId = arguments?.getString(ARG_TAB_ID) ?: ""
        val initialUrl = arguments?.getString(ARG_URL) ?: ""

        _webView = view.findViewById(R.id.web_view)
        urlEditText = view.findViewById(R.id.url_edit_text)
        progressBar = view.findViewById(R.id.progress_bar)
        bottomBar = view.findViewById(R.id.bottom_bar)
        btnBack = view.findViewById(R.id.btn_back)
        btnForward = view.findViewById(R.id.btn_forward)
        btnShare = view.findViewById(R.id.btn_share)
        btnBookmark = view.findViewById(R.id.btn_bookmark)
        btnTabs = view.findViewById(R.id.btn_tabs)
        btnSettings = view.findViewById(R.id.btn_settings)
        urlBarContainer = view.findViewById(R.id.tabs_mode_c)
        urlActionsStrip = view.findViewById(R.id.url_actions_strip)
        chipClear = view.findViewById(R.id.chip_clear)
        chipCopy = view.findViewById(R.id.chip_copy)
        chipPaste = view.findViewById(R.id.chip_paste)
        chipPasteGo = view.findViewById(R.id.chip_paste_go)
        chipShareLink = view.findViewById(R.id.chip_share_link)
        chipBack = view.findViewById(R.id.chip_back)
        chipForward = view.findViewById(R.id.chip_forward)
        chipDate = view.findViewById(R.id.chip_date)
        chipTime = view.findViewById(R.id.chip_time)
        chipReload = view.findViewById(R.id.chip_reload)
        chipHistory = view.findViewById(R.id.chip_history)
        chipFind = view.findViewById(R.id.chip_find)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        findBar = view.findViewById(R.id.find_bar)
        findInput = view.findViewById(R.id.find_input)
        findCount = view.findViewById(R.id.find_count)
        findPrev = view.findViewById(R.id.find_prev)
        findNext = view.findViewById(R.id.find_next)
        findClose = view.findViewById(R.id.find_close)
        urlSuggestions = view.findViewById(R.id.url_suggestions)
        urlSuggestions.layoutManager = LinearLayoutManager(requireContext())
        urlLock = view.findViewById(R.id.url_lock)
        urlBarSearch = view.findViewById(R.id.url_bar_search)
        urlReload = view.findViewById(R.id.url_reload)
        urlReader = view.findViewById(R.id.url_reader)
        urlScrollPill = view.findViewById(R.id.url_scroll_pill)
        urlDomainText = view.findViewById(R.id.url_domain_text)
        topSearchBar = view.findViewById(R.id.top_search_bar)
        compactBottomBar = view.findViewById(R.id.compact_bottom_bar)

        setupWebView()
        setupUrlBar()
        setupUrlActions()
        setupNavigation()
        setupBottomBar()
        setupSwipeRefresh()
        setupFindInPage()
        refreshTabCount()
        applyThemeColors()
        applyTabMode()

        val urlToLoad = if (initialUrl.isBlank() || initialUrl == "about:blank") NEW_TAB_URL else initialUrl
        loadUrl(urlToLoad)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val activity = requireActivity() as MainActivity

        val s = activity.settingsManager
        webView.settings.apply {
            javaScriptEnabled = s.javascriptEnabled
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            allowFileAccess = true
            allowContentAccess = true
            setSupportMultipleWindows(!s.blockPopups)
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mediaPlaybackRequiresUserGesture = true
            databaseEnabled = true
            userAgentString = CHROME_UA
        }

        webView.addJavascriptInterface(
            NewTabBridge(
                historyManager = activity.historyManager,
                onNavigate = { url -> requireActivity().runOnUiThread { loadUrl(url) } },
                onFocusUrlBar = { requireActivity().runOnUiThread { focusUrlBar() } }
            ),
            "Android"
        )

        webView.setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            try {
                activity.downloadManager.enqueue(url, CHROME_UA, contentDisposition, mimetype)
                Toast.makeText(requireContext(), "Download started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                catch (_: Exception) {
                    Toast.makeText(requireContext(), "Cannot download this file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isNewTabPage = (url == NEW_TAB_URL)
                url?.let {
                    if (!urlEditText.isFocused && !isNewTabPage) urlEditText.setText(UrlUtils.formatUrl(it))
                    else if (isNewTabPage) {
                        urlEditText.setText("")
                        urlEditText.hint = "Search or enter address"
                    }
                    activity.tabManager.updateTab(tabId, url = it, isLoading = true)
                    if (isCompactMode) {
                        view?.findViewById<android.widget.EditText>(R.id.compact_url_text)?.setText(UrlUtils.formatUrl(it))
                    }
                }
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
                chipReload.text = "Stop"
                if (!isNewTabPage) updateUrlBarIcons()
                updateNavState()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                progressBar.progress = 0
                chipReload.text = if (isReaderMode) "Reader" else "Reload"
                url?.let { url ->
                    if (isNewTabPage) {
                        isReaderMode = false
                        updateUrlBarIcons()
                    }
                }.also {
                    val tab = activity.tabManager.getTabById(tabId)
                    if (tab?.readerModeOn == true && !isReaderMode && !isNewTabPage) {
                        isReaderMode = true
                        chipReload.text = "Reader"
                        reapplyReaderMode()
                    }
                }
                url?.let {
                    if (!isNewTabPage) {
                        val title = view?.title?.takeIf { t -> t.isNotBlank() } ?: it
                        if (!urlEditText.isFocused) urlEditText.setText(UrlUtils.formatUrl(it))
                        activity.tabManager.updateTab(
                            tabId, url = it, title = title,
                            isLoading = false, canGoBack = webView.canGoBack(),
                            canGoForward = webView.canGoForward()
                        )
                                activity.historyManager.addEntry(it, title)
                                updateBookmarkIcon()
                                updateBottomBarTint(it)
                                if (isCompactMode) {
                                    view?.findViewById<android.widget.EditText>(R.id.compact_url_text)?.setText(UrlUtils.formatUrl(it))
                                }
                    } else {
                        activity.tabManager.updateTab(tabId, title = "New Tab", isLoading = false, url = "")
                        resetBottomBarTint()
                    }
                }
                updateNavState()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) return false
                return try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true } catch (_: Exception) { false }
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (url.startsWith("file://")) return null
                val act = activity as? MainActivity ?: return null
                return if (act.settingsManager.blockTrackers && act.trackerBlocker.shouldBlockUrl(url))
                    WebResourceResponse("text/plain", "UTF-8", null) else null
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100 && !isNewTabPage) View.VISIBLE else View.GONE
                activity.tabManager.updateTab(tabId, progress = newProgress)
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (!isNewTabPage) title?.let { activity.tabManager.updateTab(tabId, title = it) }
            }
        }

        var touchStartY = 0f
        webView.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> touchStartY = event.y
                android.view.MotionEvent.ACTION_UP -> {
                    if (isScrolledDown && abs(event.y - touchStartY) < 15f) {
                        toggleScrollPill(false)
                    }
                }
            }
            false
        }
        webView.setOnLongClickListener { v ->
            val wv = v as? android.webkit.WebView ?: return@setOnLongClickListener false
            val result = wv.hitTestResult
            val url = currentUrl()
            val items = mutableListOf<Pair<String, () -> Unit>>()
            items.add("Find in Page" to { showFindInPage() })
            if (result.type == android.webkit.WebView.HitTestResult.IMAGE_TYPE ||
                result.type == android.webkit.WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                val imgUrl = result.extra ?: ""
                items.add("Save Image" to { downloadUrl(imgUrl) })
                items.add("Copy Image Link" to {
                    requireContext().let { ctx ->
                        val clip = android.content.ClipData.newPlainText("url", imgUrl)
                        ctx.getSystemService(android.content.ClipboardManager::class.java)
                            ?.setPrimaryClip(clip)
                    }
                })
            }
            if (result.type == android.webkit.WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                result.type == android.webkit.WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                val linkUrl = result.extra ?: ""
                items.add("Copy Link" to {
                    requireContext().let { ctx ->
                        val clip = android.content.ClipData.newPlainText("url", linkUrl)
                        ctx.getSystemService(android.content.ClipboardManager::class.java)
                            ?.setPrimaryClip(clip)
                    }
                })
            }
            items.add("Copy URL" to {
                requireContext().let { ctx ->
                    val clip = android.content.ClipData.newPlainText("url", url)
                    ctx.getSystemService(android.content.ClipboardManager::class.java)
                        ?.setPrimaryClip(clip)
                }
            })
            items.add("Print" to {
                try {
                    val printMgr = requireContext().getSystemService(android.print.PrintManager::class.java)
                    printMgr?.print("Savanna", wv.createPrintDocumentAdapter(), null)
                } catch (_: Exception) {}
            })
            items.add("Add to Home Screen" to {
                addToHomeScreen()
            })
            items.add("Share" to { shareCurrentPage() })
            val labels = items.map { it.first }.toTypedArray()
            android.app.AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setItems(labels) { _, i -> items[i].second() }
                .show()
            true
        }

        webView.setFindListener { activeMatchOrdinal, numberOfMatches, isDone ->
            if (isDone && isFindVisible) {
                findCount.text = if (numberOfMatches > 0)
                    "${activeMatchOrdinal + 1}/$numberOfMatches" else "0/0"
            }
        }

    }

    fun updateWebViewSettings() {
        val wv = _webView ?: return
        val s = (requireActivity() as? MainActivity)?.settingsManager ?: return
        wv.settings.javaScriptEnabled = s.javascriptEnabled
        wv.settings.setSupportMultipleWindows(!s.blockPopups)
    }

    private fun showFavoriteDialog() {
        val input = TextInputEditText(requireContext()).apply {
            hint = "https://"
            setText(currentUrl())
        }
        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Add Favorite Link")
            .setView(input as View)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val url = input.text?.toString()?.trim().orEmpty()
                        if (url.isNotBlank()) {
                            Toast.makeText(requireContext(), "Favorite saved", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }
                }
            }
            .show()
    }

    private fun setupUrlBar() {
        urlEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                processInput(urlEditText.text.toString())
                hideKeyboardAndStrip(); true
            } else false
        }
        urlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId)
                if (!isNewTabPage) urlEditText.setText(tab?.url ?: "")
                urlEditText.selectAll()
                showActionsStrip()
                showUrlSuggestions()
            } else {
                if (isNewTabPage) { urlEditText.setText(""); return@setOnFocusChangeListener }
                val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId)
                tab?.let { urlEditText.setText(UrlUtils.formatUrl(it.url)) }
                hideActionsStrip()
                hideUrlSuggestions()
            }
        }
        urlEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (urlEditText.isFocused) showUrlSuggestions()
            }
        })
        urlReload.setOnClickListener {
            if (isNewTabPage) return@setOnClickListener
            if (webView.progress in 1..99) {
                webView.stopLoading()
            } else {
                webView.reload()
            }
        }
        urlReader.setOnClickListener { if (!isNewTabPage) toggleReaderMode() }
    }

    private fun setupUrlActions() {
        chipClear.setOnClickListener { urlEditText.setText(""); urlEditText.requestFocus() }
        chipCopy.setOnClickListener {
            val url = currentUrl()
            if (url.isNotBlank()) {
                clipboard().setPrimaryClip(ClipData.newPlainText("URL", url))
                Toast.makeText(requireContext(), "Link copied", Toast.LENGTH_SHORT).show()
            }
        }
        chipPaste.setOnClickListener {
            val text = pasteFromClipboard()
            if (text != null) {
                urlEditText.setText(text)
                urlEditText.setSelection(text.length)
            }
        }
        chipPasteGo.setOnClickListener {
            val text = pasteFromClipboard()
            if (text != null) {
                urlEditText.setText(text)
                processInput(text)
                hideKeyboardAndStrip()
            }
        }
        chipShareLink.setOnClickListener {
            val activity = requireActivity() as MainActivity
            val tab = activity.tabManager.getTabById(tabId)
            val url = tab?.url?.takeIf { it.isNotBlank() } ?: return@setOnClickListener
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, url)
                    putExtra(Intent.EXTRA_SUBJECT, tab.title)
                }, "Share Link"
            ))
        }
        chipBack.setOnClickListener {
            if (!isNewTabPage && webView.canGoBack()) { webView.goBack(); hideKeyboardAndStrip() }
        }
        chipForward.setOnClickListener {
            if (webView.canGoForward()) { webView.goForward(); hideKeyboardAndStrip() }
        }
        chipShareLink.setOnLongClickListener { showFavoriteDialog(); true }
        chipDate.setOnClickListener { showDatePicker() }
        chipTime.setOnClickListener { showTimePicker() }
        chipReload.setOnClickListener {
            if (isNewTabPage) return@setOnClickListener
            if (webView.progress in 1..99) {
                webView.stopLoading()
            } else if (isReaderMode) {
                toggleReaderMode() // toggles off
            } else {
                webView.reload()
            }
            hideKeyboardAndStrip()
        }
        chipHistory.setOnClickListener {
            (requireActivity() as MainActivity).showHistory()
            hideKeyboardAndStrip()
        }
        chipFind.setOnClickListener {
            hideKeyboardAndStrip()
            showFindInPage()
        }
    }

    private fun showActionsStrip() {
        if (urlActionsStrip.visibility == View.VISIBLE) return
        urlActionsStrip.visibility = View.VISIBLE
        urlActionsStrip.startAnimation(
            TranslateAnimation(0f, 0f, urlActionsStrip.height.toFloat().coerceAtLeast(40f), 0f).apply { duration = 180 }
        )
    }

    private fun hideActionsStrip() {
        urlActionsStrip.visibility = View.GONE
    }

    private fun reapplyReaderMode() {
        webView.evaluateJavascript("""
            (function(){
                var s = document.getElementById('savanna-reader');
                if (s) return;
                s = document.createElement('style');
                s.id = 'savanna-reader';
                s.textContent = 'body * { background: transparent !important; border-color: transparent !important; box-shadow: none !important; } ' +
                    'body { background: #000 !important; color: #ddd !important; } ' +
                    'nav, header, footer, aside, .sidebar, .ad, .popup, .modal, iframe, .comments, .share, .related { display: none !important; } ' +
                    'article, main, .content, [role="main"], .post, .entry, .article-body { display: block !important; max-width: 680px !important; margin: 0 auto !important; padding: 20px 16px !important; font-size: 17px !important; line-height: 1.7 !important; } ' +
                    'p, li, blockquote { font-size: 17px !important; line-height: 1.7 !important; margin-bottom: 14px !important; } ' +
                    'h1, h2, h3 { font-weight: 600 !important; margin-top: 20px !important; margin-bottom: 8px !important; } ' +
                    'img { max-width: 100% !important; height: auto !important; border-radius: 8px !important; }';
                document.head.appendChild(s);
            })();
        """.trimIndent(), null)
        updateUrlBarIcons()
    }

    private fun toggleReaderMode() {
        isReaderMode = !isReaderMode
        val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId) ?: return
        tab.readerModeOn = isReaderMode

        val js = if (isReaderMode) """
            (function(){
                var s = document.createElement('style');
                s.id = 'savanna-reader';
                s.textContent = 'body * { background: transparent !important; border-color: transparent !important; box-shadow: none !important; } ' +
                    'body { background: #000 !important; color: #ddd !important; } ' +
                    'nav, header, footer, aside, .sidebar, .ad, .popup, .modal, iframe, .comments, .share, .related { display: none !important; } ' +
                    'article, main, .content, [role="main"], .post, .entry, .article-body { display: block !important; max-width: 680px !important; margin: 0 auto !important; padding: 20px 16px !important; font-size: 17px !important; line-height: 1.7 !important; } ' +
                    'p, li, blockquote { font-size: 17px !important; line-height: 1.7 !important; margin-bottom: 14px !important; } ' +
                    'h1, h2, h3 { font-weight: 600 !important; margin-top: 20px !important; margin-bottom: 8px !important; } ' +
                    'img { max-width: 100% !important; height: auto !important; border-radius: 8px !important; }';
                document.head.appendChild(s);
            })();
        """.trimIndent() else """
            (function(){
                var s = document.getElementById('savanna-reader');
                if (s) s.remove();
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
        updateUrlBarIcons()
        chipReload.text = if (isReaderMode) "Reader" else "Reload"
    }

    private fun updateUrlBarIcons() {
        if (isNewTabPage) {
            urlLock.visibility = View.GONE
            urlReload.visibility = View.GONE
            urlReader.visibility = View.GONE
            return
        }
        val url = currentUrl()
        urlLock.visibility = if (url.startsWith("https")) View.VISIBLE else View.GONE
        urlReload.visibility = View.VISIBLE
        urlReload.setImageResource(
            if (webView.progress in 1..99) R.drawable.ic_stop else R.drawable.ic_reload
        )
        urlReader.setImageResource(if (isReaderMode) R.drawable.ic_reader_active else R.drawable.ic_reader)
        urlReader.visibility = View.VISIBLE
    }

    private fun toggleScrollPill(collapsed: Boolean) {
        val url = currentUrl()
        if (url.isBlank()) return
        val domain = try { android.net.Uri.parse(url).host?.removePrefix("www.") ?: url } catch (e: Exception) { url }
        urlDomainText.text = domain
        val bar = urlBarContainer
        if (collapsed) {
            scrollPillAlpha = 1f
            urlScrollPill.visibility = View.VISIBLE
            urlScrollPill.animate().alpha(1f).setDuration(200).start()
            if (!isCompactMode && bar.visibility != View.GONE) {
                bar.animate().alpha(0f).setDuration(150).withEndAction {
                    bar.visibility = View.GONE
                }.start()
            }
            urlScrollPill.setOnClickListener { toggleScrollPill(false) }
        } else {
            scrollPillAlpha = 0f
            urlScrollPill.animate().alpha(0f).setDuration(150).withEndAction {
                urlScrollPill.visibility = View.GONE
            }.start()
            urlScrollPill.setOnClickListener(null)
            if (!isCompactMode && bar.visibility != View.VISIBLE) {
                bar.visibility = View.VISIBLE
                bar.alpha = 0f
                bar.animate().alpha(1f).setDuration(200).start()
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(android.R.color.white)
        swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#FF1C1C1E"))
        swipeRefresh.setOnRefreshListener {
            if (!isNewTabPage) webView.reload()
            swipeRefresh.isRefreshing = false
        }
        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            swipeRefresh.isEnabled = scrollY == 0
            if (isNewTabPage || isCompactMode) return@setOnScrollChangeListener
            val delta = scrollY - oldScrollY
            val url = currentUrl()
            if (abs(delta) > 6) updateBottomBarTint(url)
            if (abs(delta) < 8) return@setOnScrollChangeListener
            if (scrollY > 60 && delta > 0 && !isScrolledDown) {
                isScrolledDown = true
                toggleScrollPill(true)
            } else if (scrollY <= 10 && isScrolledDown) {
                isScrolledDown = false
                toggleScrollPill(false)
            } else if (isScrolledDown && delta < -20 && scrollY < 200) {
                isScrolledDown = false
                toggleScrollPill(false)
            }
        }
    }

    private fun setupFindInPage() {
        findClose.setOnClickListener { hideFindInPage() }
        findInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performFind(findInput.text.toString())
                true
            } else false
        }
        findInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performFind(s?.toString() ?: "")
            }
        })
        findNext.setOnClickListener { webView.findNext(true) }
        findPrev.setOnClickListener { webView.findNext(false) }
    }

    private fun performFind(query: String) {
        if (query.isBlank()) {
            webView.clearMatches()
            findCount.text = ""
            return
        }
        webView.findAllAsync(query)
        findCount.text = "0/0"
    }

    private fun showFindInPage() {
        isFindVisible = true
        findBar.visibility = View.VISIBLE
        findInput.requestFocus()
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(findInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideFindInPage() {
        isFindVisible = false
        findBar.visibility = View.GONE
        findInput.setText("")
        webView.clearMatches()
        findCount.text = ""
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(findInput.windowToken, 0)
    }

    private fun showUrlSuggestions() {
        val activity = requireActivity() as MainActivity
        val query = urlEditText.text.toString().trim()

        val suggestions = mutableListOf<Map<String, String>>()

        // Add search suggestion for the typed text
        if (query.isNotBlank()) {
            suggestions.add(mapOf(
                "label" to "Search for \"$query\"",
                "url" to activity.settingsManager.getSearchUrl(query),
                "type" to "search"
            ))
        }

        // Add matching bookmarks
        if (query.isNotBlank()) {
            activity.bookmarkManager.search(query).take(3).forEach { bm ->
                suggestions.add(mapOf(
                    "label" to bm.title,
                    "url" to bm.url,
                    "type" to "bookmark"
                ))
            }
        }

        // Add matching history
        if (query.isNotBlank()) {
            activity.historyManager.search(query).take(5).forEach { h ->
                suggestions.add(mapOf(
                    "label" to "${h.title} — ${UrlUtils.extractDomain(h.url)}",
                    "url" to h.url,
                    "type" to "history"
                ))
            }
        }

        if (suggestions.isEmpty()) {
            hideUrlSuggestions()
            return
        }

        urlSuggestions.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(16, 12, 16, 12)
                    textSize = 14f
                    setTextColor(Color.WHITE)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                return object : RecyclerView.ViewHolder(tv) {}
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).apply {
                    text = suggestions[position]["label"]
                    setOnClickListener {
                        val item = suggestions[position]
                        loadUrl(item["url"] ?: "")
                        hideKeyboardAndStrip()
                    }
                }
            }
            override fun getItemCount() = suggestions.size
        }
        urlSuggestions.visibility = View.VISIBLE
    }

    private fun hideUrlSuggestions() {
        urlSuggestions.visibility = View.GONE
    }

    private fun currentUrl(): String {
        val activity = requireActivity() as? MainActivity ?: return ""
        return activity.tabManager.getTabById(tabId)?.url ?: ""
    }

    private fun clipboard(): ClipboardManager =
        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private fun pasteFromClipboard(): String? {
        val clip = clipboard().primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0)?.text?.toString()
    }

    fun focusUrlBar() {
        urlEditText.requestFocus()
        urlEditText.selectAll()
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(urlEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun processInput(input: String) {
        val (isUrl, processed) = UrlUtils.smartUrlProcess(input.trim())
        loadUrl(if (isUrl) processed else (requireActivity() as MainActivity).settingsManager.getSearchUrl(processed))
    }

    fun loadUrl(url: String) { _webView?.loadUrl(url) }

    private fun setupNavigation() {
        btnBack.setOnClickListener { if (!isNewTabPage && webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnBack.setOnLongClickListener { showHistoryPopUp(); true }
        btnForward.setOnLongClickListener { showForwardPopUp(); true }
        chipBack.setOnLongClickListener { showHistoryPopUp(); true }
        chipForward.setOnLongClickListener { showForwardPopUp(); true }
    }

    private fun showHistoryPopUp() {
        val activity = requireActivity() as MainActivity
        val backHistory = activity.tabManager.getBackHistory(tabId)
        if (backHistory.isEmpty()) {
            Toast.makeText(requireContext(), "No back history", Toast.LENGTH_SHORT).show()
            return
        }
        val titles = backHistory.map { UrlUtils.extractDomain(it) }.toTypedArray()
        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Back")
            .setItems(titles) { _, which ->
                val url = backHistory[which]
                loadUrl(url)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showForwardPopUp() {
        val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId) ?: return
        val fwd = tab.forwardStack.toList().reversed()
        if (fwd.isEmpty()) {
            Toast.makeText(requireContext(), "No forward history", Toast.LENGTH_SHORT).show()
            return
        }
        val titles = fwd.map { UrlUtils.extractDomain(it) }.toTypedArray()
        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Forward")
            .setItems(titles) { _, which ->
                loadUrl(fwd[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomBar() {
        btnBookmark.setOnClickListener { toggleBookmark() }
        btnBookmark.setOnLongClickListener { (requireActivity() as MainActivity).showBookmarks(); true }
        btnTabs.setOnClickListener { (requireActivity() as MainActivity).showTabSwitcher() }
        btnSettings.setOnClickListener { (requireActivity() as MainActivity).showSettings() }
        btnShare.setOnClickListener { shareCurrentPage() }
        btnShare.setOnLongClickListener { showFavoriteDialog(); true }

        view?.findViewById<android.widget.ImageView>(R.id.compact_btn_back)?.setOnClickListener { webView.goBack() }
        view?.findViewById<android.widget.ImageView>(R.id.compact_btn_forward)?.setOnClickListener { webView.goForward() }
        view?.findViewById<android.widget.ImageView>(R.id.compact_btn_share)?.setOnClickListener { shareCurrentPage() }
        view?.findViewById<android.widget.TextView>(R.id.compact_btn_tabs)?.setOnClickListener { (requireActivity() as MainActivity).showTabSwitcher() }
        view?.findViewById<android.widget.ImageView>(R.id.compact_btn_bookmark)?.setOnClickListener { toggleBookmark() }
        view?.findViewById<android.widget.ImageView>(R.id.compact_btn_settings)?.setOnClickListener { (requireActivity() as MainActivity).showSettings() }
        view?.findViewById<android.widget.EditText>(R.id.compact_url_text)?.setOnClickListener { focusUrlBar() }
    }

    private fun toggleBookmark() {
        val activity = requireActivity() as MainActivity
        val tab = activity.tabManager.getTabById(tabId) ?: return
        if (tab.url.isBlank() || tab.url == "about:blank" || isNewTabPage) return
        if (activity.bookmarkManager.isBookmarked(tab.url)) activity.bookmarkManager.removeByUrl(tab.url)
        else activity.bookmarkManager.addBookmark(tab.url, tab.title)
        updateBookmarkIcon()
    }

    private fun updateBookmarkIcon() {
        val activity = requireActivity() as? MainActivity ?: return
        val tab = activity.tabManager.getTabById(tabId)
        val bookmarked = !isNewTabPage && (tab?.let { activity.bookmarkManager.isBookmarked(it.url) } ?: false)
        btnBookmark.setImageResource(if (bookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark)
    }

    private fun updateNavState() {
        val canBack = !isNewTabPage && (_webView?.canGoBack() ?: false)
        val canForward = !isNewTabPage && (_webView?.canGoForward() ?: false)
        btnBack.alpha = if (canBack) 1.0f else 0.28f
        btnForward.alpha = if (canForward) 1.0f else 0.28f
        btnBack.isEnabled = canBack
        btnForward.isEnabled = canForward
        chipBack.alpha = if (canBack) 1.0f else 0.28f
        chipForward.alpha = if (canForward) 1.0f else 0.28f
    }

    fun refreshTabCount() {
        val count = (requireActivity() as? MainActivity)?.tabManager?.tabCount ?: 1
        btnTabs.text = count.toString()
        view?.findViewById<android.widget.TextView>(R.id.compact_btn_tabs)?.text = count.toString()
    }

    fun applyThemeColors() {
        val activity = requireActivity() as? MainActivity ?: return
        val tm = activity.themeManager
        val newBg = tm.bgColor
        val style = tm.urlBarStyle
        val bgChanged = newBg != themeBgColor
        val styleChanged = style != lastUrlBarStyle
        if (!bgChanged && !styleChanged) return

        themeBgColor = newBg
        lastUrlBarStyle = style
        view?.setBackgroundColor(newBg)

        val isDark = isDarkColor(newBg)
        val borderColor = if (isDark) 0x44FFFFFF.toInt() else 0x44000000.toInt()

        // URL search bar — CSS: rgba(0,0,0,0.04) light / 8% white dark, radius 22dp
        urlBarSearch.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 22f * density
            setColor(if (isDark) 0x14FFFFFF.toInt() else 0x0A000000.toInt())
            setStroke((2 * density).toInt(), borderColor)
        }

        // Compact top bar — 58% frosted glass, radius 22dp
        val frostFill = if (isDark) 0x941C1C1E.toInt() else 0x94FFFFFF.toInt()
        topSearchBar.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 22f * density
            setColor(frostFill)
            setStroke((2 * density).toInt(), borderColor)
        }

        // Compact bottom bar — 58% frosted glass, radius 24dp
        compactBottomBar.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f * density
            setColor(frostFill)
            setStroke((2 * density).toInt(), borderColor)
        }

        // Main address bar container — CSS: white frosted glass, radius 30dp
        when (style) {
            ThemeManager.STYLE_SOLID -> {
                urlBarContainer.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 30f * density
                    setColor(newBg)
                }
            }
            ThemeManager.STYLE_FROSTED -> {
                urlBarContainer.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 30f * density
                    setColor(if (isDark) 0xCC1C1C1E.toInt() else 0xCCFFFFFF.toInt())
                }
            }
            else -> {  // GLASS — 58% frosted, 30dp radius
                urlBarContainer.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 30f * density
                    setColor(frostFill)
                    setStroke((1 * density).toInt(), borderColor)
                }
            }
        }

        // Scroll pill — 58% frosted (keep size 142x46dp from earlier)
        urlScrollPill.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f * density
            setColor(frostFill)
        }
        urlDomainText.setTextColor(if (isDark) Color.WHITE else Color.parseColor("#1B1B1B"))

        _webView?.settings?.textZoom = (100 * tm.textSizeMultiplier).toInt()

        swipeRefresh.setProgressBackgroundColorSchemeColor(newBg)

        if (!tm.isDarkMode) {
            findBar.setBackgroundColor(newBg)
            val fi = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 18f * density
                setColor(newBg)
            }
            findInput.background = fi
        } else {
            findBar.setBackgroundResource(R.drawable.safari_bottom_bar)
            findInput.setBackgroundResource(R.drawable.safari_url_capsule)
        }

        val showingDefault = currentToolbarTint == lastDefaultBg || currentToolbarTint == Color.TRANSPARENT
        if (showingDefault || bgChanged) {
            if (tm.isDarkMode) {
                currentToolbarTint = Color.TRANSPARENT
                bottomBar.setBackgroundResource(R.drawable.safari_bottom_bar)
            } else {
                currentToolbarTint = newBg
                bottomBar.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(newBg)
                }
            }
        }
        lastDefaultBg = newBg
    }

    private fun downloadUrl(url: String) {
        try {
            val activity = requireActivity() as MainActivity
            activity.downloadManager.enqueue(url, CHROME_UA, null, null)
        } catch (_: Exception) {}
    }

    private fun addToHomeScreen() {
        val url = currentUrl()
        if (url.isBlank()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val shortcut = android.content.Intent(android.content.Intent.ACTION_CREATE_SHORTCUT).apply {
            putExtra(android.content.Intent.EXTRA_SHORTCUT_INTENT, intent)
            putExtra(android.content.Intent.EXTRA_SHORTCUT_NAME, webView.title ?: url)
            putExtra(android.content.Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                android.content.Intent.ShortcutIconResource.fromContext(requireContext(), R.drawable.ic_launcher_foreground))
        }
        try { startActivity(shortcut) } catch (_: Exception) {
            Toast.makeText(requireContext(), "Add to Home Screen not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDarkColor(color: Int): Boolean {
        val r = android.graphics.Color.red(color)
        val g = android.graphics.Color.green(color)
        val b = android.graphics.Color.blue(color)
        return (0.299 * r + 0.587 * g + 0.114 * b) < 128
    }

    private fun shareCurrentPage() {
        if (isNewTabPage) return
        val activity = requireActivity() as MainActivity
        val tab = activity.tabManager.getTabById(tabId)
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, tab?.url ?: "")
                putExtra(Intent.EXTRA_SUBJECT, tab?.title ?: "")
            }, "Share"
        ))
    }

    private fun hideKeyboardAndStrip() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(urlEditText.windowToken, 0)
        urlEditText.clearFocus()
        hideActionsStrip()
        hideUrlSuggestions()
        if (isFindVisible) hideFindInPage()
    }

    fun canGoBack(): Boolean = !isNewTabPage && (_webView?.canGoBack() ?: false)
    fun goBack() { _webView?.goBack() }

    override fun onResume() {
        super.onResume()
        refreshTabCount()
        updateNavState()
        updateBookmarkIcon()
        applyThemeColors()
        applyTabMode()
    }

    fun applyTabMode() {
        val s = (requireActivity() as? MainActivity)?.settingsManager ?: return
        val compact = s.tabMode == com.savanna.browser.manager.SettingsManager.TAB_MODE_COMPACT
        isCompactMode = compact
        urlBarContainer.visibility = if (compact) View.GONE else View.VISIBLE
        topSearchBar.visibility = if (compact) View.VISIBLE else View.GONE
        compactBottomBar.visibility = if (compact) View.VISIBLE else View.GONE
        if (compact) {
            val url = currentUrl()
            if (!isNewTabPage) {
                view?.findViewById<android.widget.EditText>(R.id.compact_url_text)?.setText(
                    com.savanna.browser.util.UrlUtils.formatUrl(url)
                )
            }
        }
    }

    override fun onDestroyView() {
        _webView?.stopLoading()
        _webView?.destroy()
        _webView = null
        super.onDestroyView()
    }

    private fun updateBottomBarTint(url: String) {
        val color = websiteTint(url)
        if (color == currentToolbarTint) return
        currentToolbarTint = color
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
        }
        bottomBar.background = drawable
    }

    private fun resetBottomBarTint() {
        val activity = requireActivity() as? MainActivity ?: return
        if (activity.themeManager.isDarkMode) {
            currentToolbarTint = Color.TRANSPARENT
            bottomBar.setBackgroundResource(R.drawable.safari_bottom_bar)
        } else {
            currentToolbarTint = themeBgColor
            val d = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(themeBgColor)
            }
            bottomBar.background = d
        }
    }

    private fun websiteTint(url: String): Int {
        val host = try { Uri.parse(url).host ?: "" } catch (_: Exception) { "" }
        val hash = host.hashCode()
        val r = 24 + (hash shr 16 and 0x3F)
        val g = 16 + (hash shr 8 and 0x3F)
        val b = 16 + (hash and 0x3F)
        return Color.argb(242, r, g, b)
    }

    private fun showDatePicker() {
        IOSDatePickerFragment { y, m, d, h, mi ->
            val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val amPm = if (h < 12) "AM" else "PM"
            val hr = if (h == 0) 12 else if (h > 12) h - 12 else h
            Toast.makeText(requireContext(), "${months[m]} ${d}, ${y} · ${hr}:${"%02d".format(mi)} $amPm", Toast.LENGTH_SHORT).show()
        }.show(parentFragmentManager, "date_picker")
    }

    private fun showTimePicker() {
        IOSDatePickerFragment { y, m, d, h, mi ->
            val amPm = if (h < 12) "AM" else "PM"
            val hr = if (h == 0) 12 else if (h > 12) h - 12 else h
            Toast.makeText(requireContext(), "${hr}:${"%02d".format(mi)} $amPm", Toast.LENGTH_SHORT).show()
        }.show(parentFragmentManager, "time_picker")
    }
}
