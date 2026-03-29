package cn.lightink.reader.ui.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookSummaryController
import cn.lightink.reader.databinding.FragmentBookSummaryCacheBinding
import cn.lightink.reader.databinding.ItemChapterCheckableBinding
import cn.lightink.reader.model.CacheChapter
import cn.lightink.reader.model.StateChapter
import cn.lightink.reader.module.BookCacheModule
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.ui.base.LifecycleFragment

class BookSummaryCacheFragment : LifecycleFragment() {

    private var _binding: FragmentBookSummaryCacheBinding? = null
    private val binding get() = _binding!!
    private val controller by lazy { ViewModelProvider(activity!!)[BookSummaryController::class.java] }
    private val adapter by lazy { buildAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBookSummaryCacheBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (controller.book == null) return
        binding.mBookSummaryRecycler.adapter = adapter
        binding.mBookSummaryMenuLayout.isVisible = controller.book?.hasBookSource() == true
        binding.mBookSummaryClear.setOnClickListener { clear() }
        BookCacheModule.attachCacheStatusLive().observe(viewLifecycleOwner, Observer { onCacheStatusChanged() })
        BookCacheModule.attachCacheLive().observe(viewLifecycleOwner, Observer { onChapterCached(it) })
        controller.loadChapters().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it) {
                binding.mBookSummaryRecycler.requestFocus()
                binding.mBookSummaryRecycler.scrollToPosition(controller.book?.chapter ?: 0)
            }
        })
        onCacheStatusChanged()
    }

    /**
     * 缓存状态通知
     */
    private fun onCacheStatusChanged() {
        val caching = BookCacheModule.isCaching(controller.book)
        if (caching) {
            binding.mBookSummaryDownload.text = getString(R.string.pause)
            binding.mBookSummaryDownload.setTextColor(binding.mBookSummaryDownload.context.getColor(R.color.colorRed))
            binding.mBookSummaryDownload.setOnClickListener { BookCacheModule.pause(activity!!, controller.book) }
        } else {
            binding.mBookSummaryDownload.text = getString(R.string.download)
            binding.mBookSummaryDownload.setTextColor(binding.mBookSummaryDownload.context.getColor(R.color.colorAccent))
            binding.mBookSummaryDownload.setOnClickListener { cache() }
        }
    }

    /**
     * 清理缓存
     */
    private fun clear() {
        controller.cleanChapters()
        adapter.currentList.filter { it.isCached }.forEach { it.isCached = false }
        adapter.notifyItemRangeChanged(0, adapter.itemCount, adapter.currentList)
    }

    /**
     * 缓存
     */
    private fun cache() {
        if (controller.book == null) return
        BookCacheModule.cache(controller.book!!, adapter.currentList.filter { it.isChecked && !it.isCached })
    }

    /**
     * 章节缓存通知
     */
    private fun onChapterCached(chapter: CacheChapter) {
        if (controller.book?.objectId != chapter.book) return
        val indexOf = adapter.currentList.indexOfFirst { it.index == chapter.index }
        if (indexOf > -1) {
            adapter.currentList[indexOf].isCached = true
            adapter.notifyItemChanged(indexOf, adapter.currentList[indexOf])
        }
    }

    /**
     * 构建数据适配器
     */
    private fun buildAdapter() = ListAdapter<StateChapter>(R.layout.item_chapter_checkable) { item, chapter ->
        val itemBinding = ItemChapterCheckableBinding.bind(item.view)
        item.view.setOnClickListener { itemBinding.mCheckableChapter.toggle() }
        itemBinding.mCheckableChapter.setOnCheckedChangeListener(null)
        itemBinding.mCheckableChapter.isChecked = chapter.isChecked
        itemBinding.mCheckableChapter.isEnabled = chapter.isCached.not()
        itemBinding.mCheckableChapter.setButtonDrawable(R.drawable.ic_check_download)
        itemBinding.mCheckableChapter.setOnCheckedChangeListener { _, isChecked -> chapter.isChecked = isChecked }
        itemBinding.mCheckableChapter.text = chapter.title
        itemBinding.mCheckableChapter.setTextColor(item.view.context.getColor(if (chapter.index < controller.book?.chapter ?: 0) R.color.colorContent else R.color.colorTitle))
    }
}