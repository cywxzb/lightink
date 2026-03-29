package cn.lightink.reader.ui.feed

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.FeedController
import cn.lightink.reader.ktx.setDrawableStart
import cn.lightink.reader.model.Feed
import cn.lightink.reader.model.Flow
import cn.lightink.reader.module.*
import cn.lightink.reader.databinding.FragmentFeedBinding
import cn.lightink.reader.databinding.ItemFlowCompatBinding
import cn.lightink.reader.ui.base.BottomSelectorDialog
import cn.lightink.reader.ui.base.LifecycleFragment

class FeedFragment : LifecycleFragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val controller by lazy { ViewModelProvider(this)[FeedController::class.java] }
    private var adapter = buildAdapter()
    private var groupId = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mFeedRecycler.layoutManager = RVLinearLayoutManager(activity)
        binding.mFeedRecycler.adapter = adapter
        groupId = arguments?.getLong(INTENT_FEED_GROUP, 0) ?: 0
        if (groupId != -1L) {
            controller.queryFeedsByGroupId(groupId).observe(viewLifecycleOwner, Observer { queryFlows(it) })
        } else {
            controller.queryLoves().observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun queryFlows(feeds: List<Feed>) {
        controller.queryFlows(feeds).observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
    }

    /**
     * 构建数据适配器
     */
    private fun buildAdapter() = ListAdapter<Flow>(R.layout.item_flow_compat, equalContent = { old, new -> old.same(new) }, equalItem = { old, new -> old.link == new.link }) { item, flow ->
        val itemBinding = ItemFlowCompatBinding.bind(item.view)
        itemBinding.mFlowTitle.setTextColor(resources.getColor(if (flow.read && groupId > -1) R.color.colorContent else R.color.colorTitle, item.view.context.theme))
        itemBinding.mFlowTitle.text = flow.title.trim()
        itemBinding.mFlowSummary.text = String.format("%s\u2000%s", TextUtils.ellipsize(flow.feedName, itemBinding.mFlowSummary.paint, 300F, TextUtils.TruncateAt.END), TimeFormat.format(flow.date))
        itemBinding.mFlowSummary.setDrawableStart(if (flow.love) R.drawable.ic_flow_loved else 0)
        itemBinding.mFlowCover.isVisible = flow.cover.isNullOrBlank().not()
        if (itemBinding.mFlowCover.isVisible) {
            itemBinding.mFlowCover.radius(1F).failure { Room.flow().update(flow.apply { cover = null }) }.load(flow.cover)
        }
        item.view.setOnClickListener { openFlow(flow) }
        item.view.setOnLongClickListener { showFlowPopup(flow) }
    }

    /**
     * 查看文章
     */
    private fun openFlow(flow: Flow) {
        val intent = Intent(activity, FlowActivity::class.java)
        intent.putExtra(INTENT_FEED_GROUP, arguments?.getLong(INTENT_FEED_GROUP, 0))
        intent.putExtra(INTENT_FEED_FLOW, flow.link)
        startActivity(intent)
    }

    /**
     * 长按菜单
     */
    private fun showFlowPopup(flow: Flow): Boolean {
        BottomSelectorDialog(requireContext(), flow.title, listOf(if (flow.love) R.string.flow_collected else R.string.flow_collect, R.string.delete)) { getString(it) }.callback { item ->
            when (item) {
                R.string.flow_collect, R.string.flow_collected -> controller.collect(flow.link).observe(viewLifecycleOwner, Observer { })
                R.string.delete -> controller.deleteFlow(flow)
            }
        }.show()
        return false
    }

    companion object {
        fun newInstance(groupId: Long) = FeedFragment().apply {
            arguments = Bundle().apply { putLong(INTENT_FEED_GROUP, groupId) }
        }
    }

}