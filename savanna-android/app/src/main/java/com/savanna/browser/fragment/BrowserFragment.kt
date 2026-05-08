package com.savanna.browser.fragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.savanna.browser.MainActivity
import com.savanna.browser.NewTabBridge
import com.savanna.browser.R
import com.savanna.browser.util.UrlUtils

class BrowserFragment : Fragment() {

    private var _webView: WebView? = null
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var btnForward: ImageView
    private lateinit var btnReload: ImageView
    private lateinit var btnShare: ImageView
    private lateinit var btnBookmark: ImageView
    private lateinit var btnHistory: ImageView
    private lateinit var btnTabs: ImageView
    private lateinit var btnDownloads: ImageView
    private lateinit var btnPrivacy: ImageView
    private lateinit var btnSettings: ImageView
    private lateinit var tabCountBadge: TextView

    // URL action strip
    private lateinit var urlActionsStrip: HorizontalScrollView
    private lateinit var chipClear: TextView
    private lateinit var chipCopy: TextView
    private lateinit var chipPaste: TextView
    private lateinit var chipPasteGo: TextView
    private lateinit var chipShareLink: TextView

    private var tabId: String = ""
    private val webView get() = _webView!!
    private var isNewTabPage = false

    // Chrome for Android UA — prevents Google suspicious-activity
    private val CHROME_UA =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/124.0.0.0 Mobile Safari/537.36"

    companion object {
        private const val ARG_TAB_ID = "tab_id"
        private const val ARG_URL    = "url"
        const val NEW_TAB_URL        = "file:///android_asset/new_tab.html"

        fun newInstance(tabId: String, url: String = "") = BrowserFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TAB_ID, tabId)
                putString(ARG_URL, url)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_browser, container, false)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabId = arguments?.getString(ARG_TAB_ID) ?: ""
        val initialUrl = arguments?.getString(ARG_URL) ?: ""

        _webView         = view.findViewById(R.id.web_view)
        urlEditText      = view.findViewById(R.id.url_edit_text)
        progressBar      = view.findViewById(R.id.progress_bar)
        btnBack          = view.findViewById(R.id.btn_back)
        btnForward       = view.findViewById(R.id.btn_forward)
        btnReload        = view.findViewById(R.id.btn_reload)
        btnShare         = view.findViewById(R.id.btn_share)
        btnBookmark      = view.findViewById(R.id.btn_bookmark)
        btnHistory       = view.findViewById(R.id.btn_history)
        btnTabs          = view.findViewById(R.id.btn_tabs)
        btnDownloads     = view.findViewById(R.id.btn_downloads)
        btnPrivacy       = view.findViewById(R.id.btn_privacy)
        btnSettings      = view.findViewById(R.id.btn_settings)
        tabCountBadge    = view.findViewById(R.id.tab_count_badge)
        urlActionsStrip  = view.findViewById(R.id.url_actions_strip)
        chipClear        = view.findViewById(R.id.chip_clear)
        chipCopy         = view.findViewById(R.id.chip_copy)
        chipPaste        = view.findViewById(R.id.chip_paste)
        chipPasteGo      = view.findViewById(R.id.chip_paste_go)
        chipShareLink    = view.findViewById(R.id.chip_share_link)

        setupWebView()
        setupUrlBar()
        setupUrlActions()
        setupNavigation()
        setupBottomBar()
        refreshTabCount()

        val urlToLoad = if (initialUrl.isBlank() || initialUrl == "about:blank") NEW_TAB_URL
                        else initialUrl
        loadUrl(urlToLoad)
    }

    // ── WebView setup ─────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val activity = requireActivity() as MainActivity

        webView.settings.apply {
            javaScriptEnabled         = true
            domStorageEnabled         = true
            loadWithOverviewMode      = true
            useWideViewPort           = true
            builtInZoomControls       = true
            displayZoomControls       = false
            setSupportZoom(true)
            allowFileAccess           = true
            allowContentAccess        = true
            setSupportMultipleWindows(false)
            cacheMode                 = WebSettings.LOAD_DEFAULT
            mixedContentMode          = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mediaPlaybackRequiresUserGesture = true
            databaseEnabled           = true
            userAgentString           = CHROME_UA
        }

        webView.addJavascriptInterface(
            NewTabBridge(
                historyManager = activity.historyManager,
                onNavigate     = { url -> requireActivity().runOnUiThread { loadUrl(url) } },
                onFocusUrlBar  = { requireActivity().runOnUiThread { focusUrlBar() } }
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
                    if (!urlEditText.isFocused && !isNewTabPage)
                        urlEditText.setText(UrlUtils.formatUrl(it))
                    else if (isNewTabPage) {
                        urlEditText.setText("")
                        urlEditText.hint = "Search or enter address"
                    }
                    activity.tabManager.updateTab(tabId, url = it, isLoading = true)
                }
                progressBar.visibility = View.VISIBLE
                btnReload.setImageResource(R.drawable.ic_stop)
                updateNavState()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                btnReload.setImageResource(R.drawable.ic_reload)
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
                    } else {
                        activity.tabManager.updateTab(tabId, title = "New Tab", isLoading = false, url = "")
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
    }

    // ── URL bar ───────────────────────────────────────────────────────────────

    private fun setupUrlBar() {
        urlEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                processInput(urlEditText.text.toString())
                hideKeyboardAndStrip(); true
            } else false
        }
        urlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                urlEditText.setBackgroundResource(R.drawable.url_bar_focused)
                val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId)
                if (!isNewTabPage) tab?.let { urlEditText.setText(it.url) }
                urlEditText.selectAll()
                showActionsStrip()
            } else {
                urlEditText.setBackgroundResource(R.drawable.url_bar_background)
                if (isNewTabPage) { urlEditText.setText(""); return@setOnFocusChangeListener }
                val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId)
                tab?.let { urlEditText.setText(UrlUtils.formatUrl(it.url)) }
                hideActionsStrip()
            }
        }
    }

    // ── URL action strip ──────────────────────────────────────────────────────

    private fun setupUrlActions() {
        chipClear.setOnClickListener {
            urlEditText.setText("")
            urlEditText.requestFocus()
        }

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
    }

    private fun showActionsStrip() {
        if (urlActionsStrip.visibility == View.VISIBLE) return
        urlActionsStrip.visibility = View.VISIBLE
        urlActionsStrip.startAnimation(
            android.view.animation.TranslateAnimation(0f, 0f, -urlActionsStrip.height.toFloat().coerceAtLeast(40f), 0f).apply {
                duration = 180
            }
        )
    }

    private fun hideActionsStrip() {
        urlActionsStrip.visibility = View.GONE
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
        loadUrl(if (isUrl) processed
                else (requireActivity() as MainActivity).settingsManager.getSearchUrl(processed))
    }

    fun loadUrl(url: String) { _webView?.loadUrl(url) }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            if (!isNewTabPage && webView.canGoBack()) webView.goBack()
        }
        btnForward.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }
        btnReload.setOnClickListener {
            if (isNewTabPage) return@setOnClickListener
            if (webView.progress in 1..99) webView.stopLoading() else webView.reload()
        }
    }

    // ── Bottom bar ────────────────────────────────────────────────────────────

    private fun setupBottomBar() {
        btnBookmark.setOnClickListener     { toggleBookmark() }
        btnBookmark.setOnLongClickListener { (requireActivity() as MainActivity).showBookmarks(); true }
        btnHistory.setOnClickListener      { (requireActivity() as MainActivity).showHistory() }
        btnTabs.setOnClickListener         { (requireActivity() as MainActivity).showTabSwitcher() }
        btnDownloads.setOnClickListener    { (requireActivity() as MainActivity).showDownloads() }
        btnPrivacy.setOnClickListener      { (requireActivity() as MainActivity).showPrivacyReport() }
        btnSettings.setOnClickListener     { (requireActivity() as MainActivity).showSettings() }
        btnShare.setOnClickListener        { shareCurrentPage() }
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
        val canBack    = !isNewTabPage && (_webView?.canGoBack()    ?: false)
        val canForward = !isNewTabPage && (_webView?.canGoForward() ?: false)
        btnBack.alpha    = if (canBack)    1.0f else 0.28f
        btnForward.alpha = if (canForward) 1.0f else 0.28f
        btnBack.isEnabled    = canBack
        btnForward.isEnabled = canForward
        btnReload.alpha      = if (isNewTabPage) 0.28f else 1.0f
        btnReload.isEnabled  = !isNewTabPage
    }

    fun refreshTabCount() {
        val count = (requireActivity() as? MainActivity)?.tabManager?.tabCount ?: 1
        tabCountBadge.text = count.toString()
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
    }

    fun canGoBack(): Boolean = !isNewTabPage && (_webView?.canGoBack() ?: false)
    fun goBack() { _webView?.goBack() }

    override fun onResume() {
        super.onResume()
        refreshTabCount()
        updateNavState()
        updateBookmarkIcon()
    }

    override fun onDestroyView() {
        _webView?.stopLoading()
        _webView?.destroy()
        _webView = null
        super.onDestroyView()
    }
}
