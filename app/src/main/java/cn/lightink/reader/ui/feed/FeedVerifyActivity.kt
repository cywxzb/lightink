package cn.lightink.reader.ui.feed

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.FeedController
import cn.lightink.reader.databinding.ActivityFeedVerifyBinding
import cn.lightink.reader.databinding.ItemFlowCompatBinding
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.Feed
import cn.lightink.reader.model.Flow
import cn.lightink.reader.model.StoreFeed
import cn.lightink.reader.module.INTENT_FEED
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.TOAST_TYPE_SUCCESS
import cn.lightink.reader.module.TimeFormat
import cn.lightink.reader.ui.base.LifecycleActivity

class FeedVerifyActivity : LifecycleActivity() {

    private val controller by lazy { ViewModelProvider(this)[FeedController::class.java] }
    private val adapter by lazy { buildAdapter() }
    private lateinit var binding: ActivityFeedVerifyBinding

    private val storeFeed by lazy { intent.getParcelableExtra<StoreFeed?>(INTENT_FEED) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (storeFeed != null) {
            binding.mFeedVerifyInput.parentView.isVisible = false
            binding.mFeedVerifySubmit.isVisible = false
            binding.mFeedVerifyCancel.isVisible = false
            verify("http://${storeFeed!!.rss}", false)
        } else {
            binding.mFeedVerifyInput.doOnTextChanged { text, _, _, _ ->
                binding.mFeedVerifySubmit.isVisible = URLUtil.isNetworkUrl(text.toString())
                binding.mFeedVerifyClear.isVisible = text.isNullOrBlank().not()
            }
            binding.mFeedVerifyInput.postDelayed({
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.mFeedVerifyInput, InputMethodManager.SHOW_IMPLICIT)
            }, 200)
            binding.mFeedVerifyInput.setOnEditorActionListener { _, actionId, _ -> if (actionId == EditorInfo.IME_ACTION_GO) binding.mFeedVerifySubmit.callOnClick() else false }
            binding.mFeedVerifyClear.setOnClickListener { cancel() }
            binding.mFeedVerifySubmit.setOnClickListener { verify(binding.mFeedVerifyInput.text.toString().trim(), true) }
            binding.mFeedVerifyCancel.setOnClickListener { cancel() }
        }
        binding.mFeedVerifyRecycler.adapter = adapter
    }

    private fun updateFeedInfoView(feed: Feed, flows: List<Flow>) {
        binding.mFeedInfoLayout.isVisible = true
        binding.mFeedName.text = storeFeed?.name ?: feed.name
        binding.mFeedSummary.text = storeFeed?.summary ?: feed.summary
        adapter.submitList(flows)
        if (controller.hasFeed(feed)) {
            binding.mFeedVerifySubscribe.setText(R.string.feed_unsubscribe)
            binding.mFeedVerifySubscribe.backgroundTintList = ColorStateList.valueOf(getColor(R.color.colorRed))
            binding.mFeedVerifySubscribe.setOnClickListener { unsubscribe() }
        } else {
            binding.mFeedVerifySubscribe.setText(R.string.feed_subscribe)
            binding.mFeedVerifySubscribe.backgroundTintList = ColorStateList.valueOf(getColor(R.color.colorAccent))
            binding.mFeedVerifySubscribe.setOnClickListener { subscribe() }
        }
    }

    /**
     * 验证频道
     */
    private fun verify(link: String, upload: Boolean) {
        binding.mFeedVerifyInput.isEnabled = false
        binding.mFeedVerifyClear.isVisible = false
        binding.mFeedVerifySubmit.isVisible = false
        binding.mFeedVerifyProgressBar.isVisible = true
        controller.verify(link.replace("https://", "http://"), upload).observe(this, Observer { result ->
            binding.mFeedVerifyProgressBar.isVisible = false
            binding.mFeedVerifyInput.isEnabled = true
            binding.mFeedVerifyClear.isVisible = true
            //验证失败
            if (result.message.isNotBlank() || result.feed == null || result.flows == null) {
                return@Observer toast(result.message)
            }
            updateFeedInfoView(result.feed, result.flows)
        })
    }

    /**
     * 取消验证
     */
    private fun cancel() {
        binding.mFeedInfoLayout.isVisible = false
        binding.mFeedVerifyInput.isEnabled = true
        binding.mFeedVerifyInput.text?.clear()
    }

    /**
     * 订阅
     */
    private fun subscribe() {
        controller.verifyResultLiveData.value?.feed?.run {
            //空内容时订阅不会成功
            if (controller.verifyResultLiveData.value?.flows.isNullOrEmpty()) return toast(R.string.feed_subscribe_failure)
            FeedSelectGroupDialog(this@FeedVerifyActivity, this, storeFeed?.tag) { groupId ->
                controller.subscribeFeed(groupId, this, controller.verifyResultLiveData.value?.flows.orEmpty())
                toast(R.string.feed_subscribe_success, TOAST_TYPE_SUCCESS)
                updateFeedInfoView(this, controller.verifyResultLiveData.value?.flows.orEmpty())
            }.show()
        }
    }

    /**
     * 取消订阅
     */
    private fun unsubscribe() {
        controller.verifyResultLiveData.value?.feed?.run {
            controller.unsubscribeFeed(link)
            updateFeedInfoView(this, controller.verifyResultLiveData.value?.flows.orEmpty())
        }
    }

    /**
     * 构建数据适配器
     */
    private fun buildAdapter() = ListAdapter<Flow>(R.layout.item_flow_compat, equalContent = { old, new -> old.same(new) }, equalItem = { old, new -> old.link == new.link }) { item, flow ->
        val itemBinding = ItemFlowCompatBinding.bind(item.itemView)
        itemBinding.mFlowTitle.setTextColor(resources.getColor(if (flow.read) R.color.colorContent else R.color.colorTitle, itemBinding.mFlowTitle.context.theme))
        itemBinding.mFlowTitle.text = flow.title.trim()
        itemBinding.mFlowSummary.text = TimeFormat.format(flow.date)
        itemBinding.mFlowCover.isVisible = flow.cover.isNullOrBlank().not()
        if (itemBinding.mFlowCover.isVisible) {
            itemBinding.mFlowCover.radius(1F).load(flow.cover)
        }
    }

}