package cn.lightink.reader.ui.feed

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.FeedController
import cn.lightink.reader.databinding.ActivityFeedManagementBinding
import cn.lightink.reader.databinding.ItemFeedGroupManagementBinding
import cn.lightink.reader.databinding.ItemFeedManagementBinding
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.Feed
import cn.lightink.reader.model.FeedGroup
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.RVLinearLayoutManager
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.TOAST_TYPE_SUCCESS
import cn.lightink.reader.ui.base.BottomSelectorDialog
import cn.lightink.reader.ui.base.LifecycleActivity

class FeedManagementActivity : LifecycleActivity() {

    private val controller by lazy { ViewModelProvider(this)[FeedController::class.java] }
    private val adapter by lazy { buildAdapter() }
    private lateinit var binding: ActivityFeedManagementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mTopbar.setOnMenuClickListener { createFeedGroup() }
        binding.mFeedManageRecycler.layoutManager = RVLinearLayoutManager(this)
        binding.mFeedManageRecycler.adapter = adapter
        controller.groupLiveData.observe(this, Observer {
            adapter.submitList(it)
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        })
    }

    /**
     * 新建频道分组
     */
    private fun createFeedGroup() {
        FeedGroupCreateDialog(this).show()
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildAdapter() = ListAdapter<FeedGroup>(R.layout.item_feed_group_management, equalContent = { o, n -> o.name == n.name }, equalItem = { o, n -> o.id == n.id }) { item, group ->
        val binding = ItemFeedGroupManagementBinding.bind(item.view)
        binding.mFeedGroupName.text = group.name
        binding.mFeedGroupArrow.setOnClickListener { arrowView ->
            binding.mFeedGroupRecycler.isVisible = !binding.mFeedGroupRecycler.isVisible
            arrowView.rotation = if (binding.mFeedGroupRecycler.isVisible) 0F else -90F
        }
        var adapter = binding.mFeedGroupRecycler.adapter as? ListAdapter<Feed>
        if (adapter == null) {
            adapter = buildChildAdapter()
            binding.mFeedGroupRecycler.adapter = adapter
        }
        controller.queryFeedsByGroupId(group.id).observe(this, Observer { feeds ->
            binding.mFeedGroupRecycler.isVisible = feeds.isNotEmpty()
            binding.mFeedGroupArrow.isVisible = feeds.isNotEmpty()
            binding.mFeedGroupArrow.rotation = if (binding.mFeedGroupRecycler.isVisible) 0F else -90F
            adapter.submitList(feeds)
        })
        item.view.setOnClickListener { showFeedGroupPopup(group) }
    }

    private fun buildChildAdapter() = ListAdapter<Feed>(R.layout.item_feed_management) { item, feed ->
        val binding = ItemFeedManagementBinding.bind(item.view)
        binding.mFeedName.text = feed.name
        item.view.setOnClickListener { showFeedPopup(feed) }
    }

    /**
     * 频道分组菜单
     */
    private fun showFeedGroupPopup(group: FeedGroup) {
        BottomSelectorDialog(this, group.name, listOf(if (controller.hasPushpin(group)) R.string.feed_pushpin_cancel else R.string.feed_pushpin, R.string.edit, R.string.delete)) { resId -> getString(resId) }.callback { item ->
            when (item) {
                R.string.edit -> FeedGroupCreateDialog(this, group).show()
                R.string.delete -> controller.deleteFeedGroup(group).observe(this, Observer { message -> if (message.isNotBlank()) toast(message) })
                R.string.feed_pushpin -> showSelectBookshelfPopup(group)
                R.string.feed_pushpin_cancel -> controller.unPushpin(group).observe(this, Observer {
                    toast(R.string.feed_pushpin_cancel, TOAST_TYPE_SUCCESS)
                })
            }
        }.show()
    }

    /**
     * 选择书架
     */
    private fun showSelectBookshelfPopup(group: FeedGroup) {
        BottomSelectorDialog(this, "选择书架", Room.bookshelf().getAllImmediately()) { it.name }.callback { bookshelf ->
            controller.pushpin(group, bookshelf).observe(this, Observer { result ->
                toast(if (result) R.string.feed_pushpin_success else R.string.feed_pushpin_failure)
            })
        }.show()
    }

    /**
     * 频道菜单
     */
    private fun showFeedPopup(feed: Feed) {
        BottomSelectorDialog(this, feed.name, listOf(R.string.move, R.string.unsubscribe_public_account)) { resId -> getString(resId) }.callback { item ->
            when (item) {
                R.string.move -> FeedSelectGroupDialog(this, feed) { groupId -> controller.moveFeedToGroup(feed, groupId) }.show()
                R.string.unsubscribe_public_account -> controller.unsubscribeFeed(feed.link)
            }
        }.show()
    }

}