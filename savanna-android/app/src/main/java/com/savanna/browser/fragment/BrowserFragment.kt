package com.savanna.browser.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.util.UrlUtils

class BrowserFragment : Fragment() {

    private var _webView: WebView? = null
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var btnForward: ImageView
    private lateinit var btnReload: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var btnBookmark: ImageView
    private lateinit var btnHistory: ImageView
    private lateinit var btnTabs: ImageView
    private lateinit var btnPrivacy: ImageView
    private lateinit var btnSettings: ImageView
    private lateinit var tabCountBadge: TextView
    private var tabId: String = ""

    private val webView get() = _webView!!

    companion object {
        private const val ARG_TAB_ID = "tab_id"
        private const val ARG_URL = "url"
        fun newInstance(tabId: String, url: String = "") = BrowserFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TAB_ID, tabId)
                putString(ARG_URL, url)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_browser, container, false)

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabId = arguments?.getString(ARG_TAB_ID) ?: ""
        val initialUrl = arguments?.getString(ARG_URL) ?: ""

        _webView        = view.findViewById(R.id.web_view)
        urlEditText     = view.findViewById(R.id.url_edit_text)
        progressBar     = view.findViewById(R.id.progress_bar)
        btnBack         = view.findViewById(R.id.btn_back)
        btnForward      = view.findViewById(R.id.btn_forward)
        btnReload       = view.findViewById(R.id.btn_reload)
        btnMenu         = view.findViewById(R.id.btn_menu)
        btnBookmark     = view.findViewById(R.id.btn_bookmark)
        btnHistory      = view.findViewById(R.id.btn_history)
        btnTabs         = view.findViewById(R.id.btn_tabs)
        btnPrivacy      = view.findViewById(R.id.btn_privacy)
        btnSettings     = view.findViewById(R.id.btn_settings)
        tabCountBadge   = view.findViewById(R.id.tab_count_badge)

        setupWebView()
        setupUrlBar()
        setupNavigation()
        setupBottomBar()
        refreshTabCount()

        if (initialUrl.isNotBlank()) loadUrl(initialUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val activity = requireActivity() as MainActivity
        val settings = activity.settingsManager

        // Safari on iPhone user-agent — sites won't detect this as Chrome/Android
        val safariUA = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                "Version/17.4 Mobile/15E148 Safari/604.1"

        webView.settings.apply {
            javaScriptEnabled        = settings.javascriptEnabled
            domStorageEnabled        = true
            loadWithOverviewMode     = true
            useWideViewPort          = true
            builtInZoomControls      = true
            displayZoomControls      = false
            setSupportZoom(true)
            allowFileAccess          = false
            allowContentAccess       = false
            setSupportMultipleWindows(false)
            cacheMode                = WebSettings.LOAD_DEFAULT
            mixedContentMode         = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString          = safariUA
            mediaPlaybackRequiresUserGesture = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    if (!urlEditText.isFocused) urlEditText.setText(UrlUtils.formatUrl(it))
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
                    val title = view?.title?.takeIf { t -> t.isNotBlank() } ?: it
                    if (!urlEditText.isFocused) urlEditText.setText(UrlUtils.formatUrl(it))
                    activity.tabManager.updateTab(tabId, url = it, title = title,
                        isLoading = false,
                        canGoBack = webView.canGoBack(),
                        canGoForward = webView.canGoForward())
                    activity.historyManager.addEntry(it, title)
                    updateBookmarkIcon()
                }
                updateNavState()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://")) return false
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                } catch (_: Exception) { false }
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                return if (settings.blockTrackers && activity.trackerBlocker.shouldBlockUrl(url))
                    WebResourceResponse("text/plain", "UTF-8", null)
                else null
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress >= 100) progressBar.visibility = View.GONE
                else progressBar.visibility = View.VISIBLE
                activity.tabManager.updateTab(tabId, progress = newProgress)
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                title?.let { activity.tabManager.updateTab(tabId, title = it) }
            }
        }
    }

    private fun setupUrlBar() {
        urlEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                processInput(urlEditText.text.toString())
                hideKeyboard()
                true
            } else false
        }

        urlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                urlEditText.setBackgroundResource(R.drawable.url_bar_focused)
                val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId)
                tab?.let { urlEditText.setText(it.url) }
                urlEditText.selectAll()
            } else {
                urlEditText.setBackgroundResource(R.drawable.url_bar_background)
                val tab = (requireActivity() as MainActivity).tabManager.getTabById(tabId)
                tab?.let { urlEditText.setText(UrlUtils.formatUrl(it.url)) }
            }
        }
    }

    private fun processInput(input: String) {
        val (isUrl, processed) = UrlUtils.smartUrlProcess(input.trim())
        loadUrl(if (isUrl) processed
        else (requireActivity() as MainActivity).settingsManager.getSearchUrl(processed))
    }

    fun loadUrl(url: String) { _webView?.loadUrl(url) }

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            if (webView.canGoBack()) webView.goBack() else updateNavState()
        }
        btnForward.setOnClickListener {
            if (webView.canGoForward()) webView.goForward() else updateNavState()
        }
        btnReload.setOnClickListener {
            if (webView.progress in 1..99) webView.stopLoading()
            else webView.reload()
        }
        btnMenu.setOnClickListener { shareCurrentPage() }
    }

    private fun setupBottomBar() {
        // Short tap → toggle bookmark; long tap → open bookmarks list
        btnBookmark.setOnClickListener { toggleBookmark() }
        btnBookmark.setOnLongClickListener {
            (requireActivity() as MainActivity).showBookmarks()
            true
        }

        btnHistory.setOnClickListener  { (requireActivity() as MainActivity).showHistory() }
        btnTabs.setOnClickListener     { (requireActivity() as MainActivity).showTabSwitcher() }
        btnPrivacy.setOnClickListener  { (requireActivity() as MainActivity).showPrivacyReport() }
        btnSettings.setOnClickListener { (requireActivity() as MainActivity).showSettings() }
    }

    private fun toggleBookmark() {
        val activity = requireActivity() as MainActivity
        val tab = activity.tabManager.getTabById(tabId) ?: return
        if (tab.url.isBlank() || tab.url == "about:blank") return
        if (activity.bookmarkManager.isBookmarked(tab.url)) activity.bookmarkManager.removeByUrl(tab.url)
        else activity.bookmarkManager.addBookmark(tab.url, tab.title)
        updateBookmarkIcon()
    }

    private fun updateBookmarkIcon() {
        val activity = requireActivity() as? MainActivity ?: return
        val tab = activity.tabManager.getTabById(tabId)
        val bookmarked = tab?.let { activity.bookmarkManager.isBookmarked(it.url) } ?: false
        btnBookmark.setImageResource(if (bookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark)
    }

    private fun updateNavState() {
        val canBack    = _webView?.canGoBack()    ?: false
        val canForward = _webView?.canGoForward() ?: false
        btnBack.alpha    = if (canBack)    1.0f else 0.3f
        btnForward.alpha = if (canForward) 1.0f else 0.3f
        btnBack.isEnabled    = canBack
        btnForward.isEnabled = canForward
    }

    fun refreshTabCount() {
        val count = (requireActivity() as? MainActivity)?.tabManager?.tabCount ?: 1
        tabCountBadge.text = count.toString()
    }

    private fun shareCurrentPage() {
        val activity = requireActivity() as MainActivity
        val tab = activity.tabManager.getTabById(tabId)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, tab?.url ?: "")
            putExtra(Intent.EXTRA_SUBJECT, tab?.title ?: "")
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(urlEditText.windowToken, 0)
        urlEditText.clearFocus()
    }

    fun canGoBack(): Boolean = _webView?.canGoBack() ?: false
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
