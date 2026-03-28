package cn.lightink.reader.ui.reader

import android.os.Bundle
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.ReaderController
import cn.lightink.reader.databinding.FragmentReaderSummaryBinding
import cn.lightink.reader.databinding.ItemBookmarkBinding
import cn.lightink.reader.model.Bookmark
import cn.lightink.reader.model.Theme
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.RVLinearLayoutManager
import cn.lightink.reader.module.Room
import cn.lightink.reader.ui.base.LifecycleFragment
import cn.lightink.reader.ui.base.PopupMenu
import kotlin.math.min

class ReaderSummaryFragment : LifecycleFragment() {

    private val controller by lazy { ViewModelProvider(activity!!)[ReaderController::class.java] }
    private val adapter by lazy { buildBookmarkAdapter() }
    private lateinit var binding: FragmentReaderSummaryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReaderSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mReaderBookmarkRecycler.updateLayoutParams<LinearLayout.LayoutParams> { setMargins(0, 0, 0, controller.defaultMenuPaddingBottom) }
        binding.mReaderBookmarkRecycler.layoutManager = RVLinearLayoutManager(activity)
        binding.mReaderBookmarkRecycler.adapter = adapter
        controller.displayStateLiveData.observe(viewLifecycleOwner, Observer { setupViewTheme(binding, controller.theme, controller.paint) })
        controller.bookmarks.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
        onUpdateSummary()
    }

    /**
     * 设置主题
     */
    private fun setupViewTheme(binding: FragmentReaderSummaryBinding, theme: Theme, paint: TextPaint) {
        binding.mReaderProgressTitle.setTextColor(theme.secondary)
        binding.mReaderProgressTitle.typeface = paint.typeface
        binding.mReaderProgress.setTextColor(theme.content)
        binding.mReaderProgress.typeface = paint.typeface
        binding.mReaderStatisticsTitle.setTextColor(theme.secondary)
        binding.mReaderStatisticsTitle.typeface = paint.typeface
        binding.mReaderStatistics.setTextColor(theme.content)
        binding.mReaderStatistics.typeface = paint.typeface
        binding.mReaderBookmarkTitle.setTextColor(theme.secondary)
        binding.mReaderBookmarkTitle.typeface = paint.typeface
    }

    /**
     * 构建书签数据适配器
     */
    private fun buildBookmarkAdapter() = ListAdapter<Bookmark>(R.layout.item_bookmark) { item, bookmark ->
        val itemBinding = ItemBookmarkBinding.bind(item.itemView)
        itemBinding.mBookmarkTitle.setTextColor(controller.theme.content)
        itemBinding.mBookmarkTitle.typeface = controller.paint.typeface
        itemBinding.mBookmarkTitle.text = bookmark.title
        itemBinding.mBookmarkSummary.setTextColor(controller.theme.content)
        itemBinding.mBookmarkSummary.typeface = controller.paint.typeface
        itemBinding.mBookmarkSummary.text = bookmark.summary
        itemBinding.root.setOnClickListener {
            controller.book.chapter = bookmark.chapter
            controller.book.chapterProgress = bookmark.progress
            (activity as? ReaderActivity)?.recreate()
        }
        itemBinding.root.setOnLongClickListener {
            PopupMenu(requireActivity()).items(R.string.delete).callback { Room.bookmark().delete(bookmark) }.show(itemBinding.mBookmarkTitle)
            return@setOnLongClickListener true
        }
    }

    override fun onResume() {
        super.onResume()
        onUpdateSummary()
    }

    private fun onUpdateSummary() {
        binding.mReaderProgress.text = getString(R.string.reader_summary_progress, min(Room.bookRecord().count(controller.book.objectId), controller.catalog.size), controller.catalog.size)
        binding.mReaderStatistics.text = getString(R.string.reader_summary_statistics, controller.book.time / 60, controller.book.speed.toInt())
    }

}