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

    private lateinit var webView: WebView
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

    companion object {
        private const val ARG_TAB_ID = "tab_id"
        private const val ARG_URL = "url"

        fun newInstance(tabId: String, url: String = ""): BrowserFragment {
            return BrowserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAB_ID, tabId)
                    putString(ARG_URL, url)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabId = arguments?.getString(ARG_TAB_ID) ?: ""
        val initialUrl = arguments?.getString(ARG_URL) ?: ""

        webView = view.findViewById(R.id.web_view)
        urlEditText = view.findViewById(R.id.url_edit_text)
        progressBar = view.findViewById(R.id.progress_bar)
        btnBack = view.findViewById(R.id.btn_back)
        btnForward = view.findViewById(R.id.btn_forward)
        btnReload = view.findViewById(R.id.btn_reload)
        btnMenu = view.findViewById(R.id.btn_menu)
        btnBookmark = view.findViewById(R.id.btn_bookmark)
        btnHistory = view.findViewById(R.id.btn_history)
        btnTabs = view.findViewById(R.id.btn_tabs)
        btnPrivacy = view.findViewById(R.id.btn_privacy)
        btnSettings = view.findViewById(R.id.btn_settings)
        tabCountBadge = view.findViewById(R.id.tab_count_badge)

        setupWebView()
        setupUrlBar()
        setupNavigation()
        setupBottomBar()
        updateTabCount()

        if (initialUrl.isNotBlank()) {
            loadUrl(initialUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val activity = requireActivity() as MainActivity
        val settings = activity.settingsManager

        webView.settings.apply {
            javaScriptEnabled = settings.javascriptEnabled
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            allowFileAccess = false
            allowContentAccess = false
            setSupportMultipleWindows(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = webView.settings.userAgentString.replace("; wv", "")
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    urlEditText.setText(UrlUtils.formatUrl(it))
                    activity.tabManager.updateTab(tabId, url = it, isLoading = true)
                }
                progressBar.visibility = View.VISIBLE
                updateNavButtons()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                url?.let {
                    val title = view?.title ?: it
                    activity.tabManager.updateTab(
                        tabId,
                        url = it,
                        title = title,
                        isLoading = false,
                        canGoBack = webView.canGoBack(),
                        canGoForward = webView.canGoForward()
                    )
                    activity.historyManager.addEntry(it, title)
                    updateBookmarkIcon()
                }
                updateNavButtons()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false
                }
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (_: Exception) { }
                return true
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (settings.blockTrackers && activity.trackerBlocker.shouldBlockUrl(url)) {
                    return WebResourceResponse("text/plain", "UTF-8", null)
                }
                return null
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                activity.tabManager.updateTab(tabId, progress = newProgress)
                if (newProgress >= 100) {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                title?.let {
                    activity.tabManager.updateTab(tabId, title = it)
                }
            }
        }
    }

    private fun setupUrlBar() {
        urlEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val input = urlEditText.text.toString()
                processInput(input)
                urlEditText.clearFocus()
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
        val (isUrl, processed) = UrlUtils.smartUrlProcess(input)
        if (isUrl) {
            loadUrl(processed)
        } else {
            val searchUrl = (requireActivity() as MainActivity).settingsManager.getSearchUrl(processed)
            loadUrl(searchUrl)
        }
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        btnForward.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }

        btnReload.setOnClickListener {
            webView.reload()
        }

        btnMenu.setOnClickListener {
            showMenuOptions()
        }
    }

    private fun setupBottomBar() {
        btnBookmark.setOnClickListener {
            toggleBookmark()
        }

        btnHistory.setOnClickListener {
            (requireActivity() as MainActivity).showHistory()
        }

        btnTabs.setOnClickListener {
            (requireActivity() as MainActivity).showTabSwitcher()
        }

        btnPrivacy.setOnClickListener {
            (requireActivity() as MainActivity).showPrivacyReport()
        }

        btnSettings.setOnClickListener {
            (requireActivity() as MainActivity).showSettings()
        }
    }

    private fun toggleBookmark() {
        val activity = requireActivity() as MainActivity
        val tab = activity.tabManager.getTabById(tabId) ?: return
        if (tab.url.isBlank()) return

        if (activity.bookmarkManager.isBookmarked(tab.url)) {
            activity.bookmarkManager.removeByUrl(tab.url)
        } else {
            activity.bookmarkManager.addBookmark(tab.url, tab.title)
        }
        updateBookmarkIcon()
    }

    private fun updateBookmarkIcon() {
        val activity = requireActivity() as? MainActivity ?: return
        val tab = activity.tabManager.getTabById(tabId)
        val isBookmarked = tab?.let { activity.bookmarkManager.isBookmarked(it.url) } ?: false
        btnBookmark.setImageResource(
            if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
        )
    }

    private fun updateNavButtons() {
        btnBack.alpha = if (webView.canGoBack()) 1.0f else 0.4f
        btnForward.alpha = if (webView.canGoForward()) 1.0f else 0.4f
    }

    fun updateTabCount() {
        val count = (requireActivity() as? MainActivity)?.tabManager?.tabCount ?: 1
        tabCountBadge.text = count.toString()
    }

    private fun showMenuOptions() {
        val activity = requireActivity() as MainActivity
        val tab = activity.tabManager.getTabById(tabId)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, tab?.url ?: "")
            putExtra(Intent.EXTRA_SUBJECT, tab?.title ?: "")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun canGoBack(): Boolean = webView.canGoBack()

    fun goBack() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    override fun onDestroyView() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroyView()
    }
}
