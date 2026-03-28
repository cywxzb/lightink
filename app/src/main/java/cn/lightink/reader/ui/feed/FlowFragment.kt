package cn.lightink.reader.ui.feed

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.GlideApp
import cn.lightink.reader.R
import cn.lightink.reader.controller.FeedController
import cn.lightink.reader.databinding.FragmentFlowBinding
import cn.lightink.reader.ktx.autoUrl
import cn.lightink.reader.ktx.isHtml
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.module.EMPTY
import cn.lightink.reader.module.INTENT_FEED_FLOW
import cn.lightink.reader.ui.base.BottomSelectorDialog
import cn.lightink.reader.ui.base.LifecycleFragment

class FlowFragment : LifecycleFragment() {

    private val controller by lazy { ViewModelProvider(activity!!)[FeedController::class.java] }
    private var title = EMPTY
    private var isLoadByHtml = false
    private lateinit var binding: FragmentFlowBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFlowBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mTopbar.setTint(controller.theme.content)
        binding.mLoadingBar.indeterminateTintList = ColorStateList.valueOf(controller.theme.control)
        binding.mWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        binding.mWebView.settings.mediaPlaybackRequiresUserGesture = false
        binding.mWebView.settings.javaScriptCanOpenWindowsAutomatically = false
        binding.mWebView.settings.javaScriptEnabled = true
        binding.mWebView.settings.loadWithOverviewMode = true
        binding.mWebView.settings.domStorageEnabled = true
        binding.mWebView.webViewClient = buildViewClient()
        binding.mWebView.webChromeClient = buildChromeClient()
        try {
            binding.mWebView.setBackgroundColor(0)
            binding.mWebView.background.alpha = 0
        } catch (e: Exception) {
        }
        binding.mFlowLoved.imageTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(view.context.getColor(R.color.colorFlower), controller.theme.content))
        binding.mFlowLoved.setOnClickListener { collect() }
        binding.mTopbar.setOnMenuClickListener { showBottomSelector() }
        controller.queryFlow(requireContext(), arguments?.getString(INTENT_FEED_FLOW)).observe(viewLifecycleOwner, Observer { flow ->
            isLoadByHtml = flow?.summary?.isHtml() == true
            binding.mFlowLoved.isChecked = flow?.love == true
            title = flow?.title.orEmpty()
            if (isLoadByHtml) {
                binding.mWebView.loadDataWithBaseURL("file:///android_asset/feed.css/", flow?.summary!!, "text/html", "utf-8", null)
            } else {
                binding.mWebView.loadUrl(arguments?.getString(INTENT_FEED_FLOW)!!)
            }
        })
    }

    /**
     * 选项菜单
     */
    private fun showBottomSelector() {
        BottomSelectorDialog(requireContext(), title, listOf(R.string.flow_menu_open_url, R.string.flow_menu_open_url_by_browser)) { getString(it) }.callback { item ->
            when (item) {
                R.string.flow_menu_open_url -> {
                    isLoadByHtml = false
                    binding.mWebView.loadDataWithBaseURL(EMPTY, EMPTY, "text/html", "utf-8", null)
                    binding.mWebView.loadUrl(arguments?.getString(INTENT_FEED_FLOW)!!)
                }
                R.string.flow_menu_open_url_by_browser -> openOriginWeb(arguments?.getString(INTENT_FEED_FLOW).orEmpty())
            }
        }.show()
    }

    /**
     * 访问原地址
     */
    private fun openOriginWeb(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            requireActivity().toast("未安装浏览器！")
        }
    }

    /**
     * 收藏
     */
    private fun collect() {
        controller.collect(arguments?.getString(INTENT_FEED_FLOW)).observe(viewLifecycleOwner, Observer { isChecked ->
            isChecked?.run { binding.mFlowLoved.isChecked = this }
        })
    }

    private fun buildViewClient() = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return !URLUtil.isNetworkUrl(request?.url?.toString())
        }


        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            if (isLoadByHtml && request?.url != null && request.requestHeaders["Accept"]?.contains("image/") == true) {
                //拦截网络图片
                return try {
                    val url = if (URLUtil.isFileUrl(request.url.toString())) request.url.toString().removePrefix("file://").autoUrl(arguments?.getString(INTENT_FEED_FLOW).orEmpty()) else request.url.toString()
                    WebResourceResponse("image/*", "UTF-8", GlideApp.with(requireActivity()).downloadOnly().load(url).submit().get().inputStream())
                } catch (e: Exception) {
                    super.shouldInterceptRequest(view, request)
                }
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            binding.mLoadingBar.isVisible = false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            binding.mLoadingBar.isVisible = true
        }
    }

    private fun buildChromeClient() = object : WebChromeClient() {}

    override fun onResume() {
        super.onResume()
        //因为ViewPager加载原因需先排除首个且不是选中的子项
        if (!controller.isStarted) {
            controller.isStarted = arguments?.getString(INTENT_FEED_FLOW) == controller.startedFlowLink
            if (controller.isStarted) {
                controller.isAlreadyRead(arguments?.getString(INTENT_FEED_FLOW))
            }
        } else {
            controller.isAlreadyRead(arguments?.getString(INTENT_FEED_FLOW))
        }
    }

    override fun onPause() {
        binding.mWebView.reload()
        super.onPause()
    }

    companion object {
        fun newInstance(flowLink: String) = FlowFragment().apply {
            arguments = Bundle().apply { putString(INTENT_FEED_FLOW, flowLink) }
        }
    }

}