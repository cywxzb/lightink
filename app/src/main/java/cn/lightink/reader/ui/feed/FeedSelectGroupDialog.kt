package cn.lightink.reader.ui.feed

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import cn.lightink.reader.R
import cn.lightink.reader.databinding.DialogFeedSelectGroupBinding
import cn.lightink.reader.databinding.ItemFeedGroupBinding
import cn.lightink.reader.model.Feed
import cn.lightink.reader.model.FeedGroup
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.Room
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedSelectGroupDialog(context: FragmentActivity, feed: Feed, tag: String? = null, val callback: (Long) -> Unit) : BottomSheetDialog(context, R.style.AppTheme_BottomSheet) {

    private lateinit var binding: DialogFeedSelectGroupBinding

    init {
        binding = DialogFeedSelectGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mFeedGroupCreateByTag.isVisible = tag.isNullOrBlank().not()
        binding.mFeedGroupCreateByTag.text = context.getString(R.string.feed_group_create_by_tag, tag)
        binding.mFeedGroupCreateByTag.setOnClickListener { createGroupBySelf(tag!!) }
        binding.mFeedGroupCreateBySelf.text = context.getString(R.string.feed_group_create_by_self, feed.name)
        binding.mFeedGroupCreateBySelf.setOnClickListener { createGroupBySelf(feed.name) }
        binding.mFeedGroupCreate.setOnClickListener { showCreateDialog() }
        binding.mFeedGroupCancel.setOnClickListener { dismiss() }
        binding.mFeedGroupRecycler.adapter = buildAdapter().apply {
            Room.feedGroup().getAll().observe(context, Observer { submitList(it.filterNot { group -> group.name == "收藏" }) })
        }
    }

    /**
     * 查找是否已存在同名的分组
     * 存在：返回分组ID
     * 不存在：创建后返回新分组ID
     */
    private fun createGroupBySelf(name: String) {
        callback.invoke(Room.feedGroup().getByName(name)?.id ?: Room.feedGroup().insert(FeedGroup(name)))
        dismiss()
    }

    private fun showCreateDialog() {
        FeedGroupCreateDialog(context).show()
    }

    private fun buildAdapter() = ListAdapter<FeedGroup>(R.layout.item_feed_group) { item, group ->
        val binding = ItemFeedGroupBinding.bind(item.view)
        binding.mFeedGroupName.text = group.name
        item.view.setOnClickListener { callback.invoke(group.id).run { dismiss() } }
    }

    override fun onStart() {
        super.onStart()
        window?.setDimAmount(0.6F)
    }

}