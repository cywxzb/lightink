package cn.lightink.reader.ui.reader.popup

import android.content.res.ColorStateList
import android.text.TextPaint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.ReaderController
import cn.lightink.reader.databinding.DialogReaderBookSourceBinding
import cn.lightink.reader.databinding.ItemReaderChangeBookSourceBinding
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.model.Theme
import cn.lightink.reader.module.EMPTY
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.VH
import cn.lightink.reader.module.booksource.BookSourceSearchResponse
import cn.lightink.reader.ui.reader.ReaderActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class ReaderBookSourceDialog(val context: FragmentActivity) : BottomSheetDialog(context, R.style.AppTheme_BottomSheet) {

    private val controller by lazy { ViewModelProvider(context)[ReaderController::class.java] }
    private var current = EMPTY
    private lateinit var binding: DialogReaderBookSourceBinding
    private val adapter = ListAdapter<BookSourceSearchResponse>(R.layout.item_reader_change_book_source) { item, result -> onBindView(item, result) }

    init {
        setContentView(R.layout.dialog_reader_book_source)
        val contentView = findViewById<ViewGroup>(android.R.id.content)?.rootView
        if (contentView != null) {
            binding = DialogReaderBookSourceBinding.bind(contentView)
            setupViewTheme(controller.theme, controller.paint)
            binding.mTopbar.setNavigationOnClickListener { dismiss() }
            binding.mBookSourceRecycler.adapter = adapter
            binding.mBookSourceRecycler.post { binding.mBookSourceRecycler.minimumHeight = context.resources.displayMetrics.heightPixels / 2 - binding.mBookSourceRecycler.top }
            controller.searchAll().observe(context, Observer { result -> onSearchResult(result) })
        }
    }

    private fun onSearchResult(response: BookSourceSearchResponse?) {
        if (response != null) {
            adapter.submitList(adapter.currentList.plus(response))
        } else {
            binding.mBookSourceLoading.isVisible = false
        }
    }

    private fun setupViewTheme(theme: Theme, paint: TextPaint) {
        binding.mTopbar.parentView.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        binding.mTopIndicator.backgroundTintList = ColorStateList.valueOf(theme.secondary)
        binding.mTopbar.setTint(theme.content)
        binding.mTopbar.setTypeface(paint.typeface)
        binding.mBookSourceLoading.indeterminateTintList = ColorStateList.valueOf(theme.control)
    }

    /**
     * 换源
     */
    private fun changeBookSource(item: VH, result: BookSourceSearchResponse) {
        if (current.isNotBlank()) return
        current = result.source.url
        adapter.notifyItemChanged(item.adapterPosition)
        controller.changeBookSource(result).observe(context, Observer { (context as ReaderActivity).recreate().run { dismiss() } })
    }

    private fun onBindView(item: VH, result: BookSourceSearchResponse) {
        val itemBinding = ItemReaderChangeBookSourceBinding.bind(item.itemView)
        itemBinding.mBookSourceName.typeface = controller.paint.typeface
        itemBinding.mBookSourceName.text = result.source.name
        itemBinding.mBookSourceName.setTextColor(controller.theme.content)
        itemBinding.mBookSourceChapter.typeface = controller.paint.typeface
        itemBinding.mBookSourceChapter.text = result.book.lastChapter
        itemBinding.mBookSourceChapter.setTextColor(controller.theme.secondary)
        itemBinding.mBookSourceLoading.indeterminateTintList = ColorStateList.valueOf(controller.theme.control)
        itemBinding.mBookSourceLoading.isVisible = result.source.url == current
        itemBinding.mBookSourceUsed.typeface = controller.paint.typeface
        itemBinding.mBookSourceUsed.isEnabled = result.source.name != controller.getBookSourceName()
        itemBinding.mBookSourceUsed.setText(if (!itemBinding.mBookSourceUsed.isEnabled) R.string.used else R.string.use)
        itemBinding.mBookSourceUsed.setTextColor(if (!itemBinding.mBookSourceUsed.isEnabled) controller.theme.secondary else controller.theme.control)
        itemBinding.mBookSourceUsed.backgroundTintList = ColorStateList.valueOf(controller.theme.background)
        itemBinding.mBookSourceUsed.visibility = if (result.source.url == current) View.INVISIBLE else View.VISIBLE
        itemBinding.mBookSourceUsed.setOnClickListener { changeBookSource(item, result) }
    }

    override fun onStart() {
        super.onStart()
        window?.navigationBarColor = controller.theme.foreground
        window?.setLayout(-1, (context.resources.displayMetrics.heightPixels * 0.8F).toInt())
        window?.setDimAmount(0.4F)
        window?.setGravity(Gravity.BOTTOM)
    }
}