package cn.lightink.reader.ui.book

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookSummaryController
import cn.lightink.reader.databinding.FragmentBookSummaryInfoBinding
import cn.lightink.reader.ktx.change
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.module.*
import cn.lightink.reader.ui.base.LifecycleFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BookSummaryInfoFragment : LifecycleFragment() {

    private var _binding: FragmentBookSummaryInfoBinding? = null
    private val binding get() = _binding!!
    private val controller by lazy { ViewModelProvider(activity!!)[BookSummaryController::class.java] }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBookSummaryInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (controller.book == null) return
        //修改封面
        binding.mBookSummaryCover.parentView.isEnabled = true
        binding.mBookSummaryCover.parentView.setOnClickListener { startActivity(Intent(activity, BookCoverActivity::class.java).putExtra(INTENT_BOOK, controller.book)) }
        binding.mBookSummaryCover.load(controller.book!!.cover)
        //修改书名
        binding.mBookSummaryName.isEnabled = true
        binding.mBookSummaryName.setText(controller.book!!.name)
        binding.mBookSummaryName.setSelection(controller.book!!.name.length)
        binding.mBookSummaryName.setOnFocusChangeListener { _, hasFocus ->
            binding.mBookSummaryNameLine.setBackgroundColor(getColor(view.context, if (hasFocus) R.color.colorAccent else R.color.colorStroke))
        }
        binding.mBookSummaryName.change { text -> onTextChanged(text, binding.mBookSummaryName, binding.mBookSummaryNameLine) }
        //修改作者
        binding.mBookSummaryAuthor.isEnabled = true
        binding.mBookSummaryAuthor.setText(controller.book!!.author)
        binding.mBookSummaryAuthor.setSelection(controller.book!!.author.length)
        binding.mBookSummaryAuthor.setOnFocusChangeListener { _, hasFocus ->
            binding.mBookSummaryAuthorLine.setBackgroundColor(getColor(view.context, if (hasFocus) R.color.colorAccent else R.color.colorStroke))
        }
        binding.mBookSummaryAuthor.change { text -> onTextChanged(text, binding.mBookSummaryAuthor, binding.mBookSummaryAuthorLine) }
        //状态
        binding.mBookSummaryStatus.isEnabled = true
        binding.mBookSummaryStatus.setCheckedImmediatelyNoEvent(controller.book!!.state == BOOK_STATE_END)
        binding.mBookSummaryStatus.setOnCheckedChangeListener { _, isChecked ->
            controller.book?.run { Room.book().update(this.apply { state = if (isChecked) BOOK_STATE_END else BOOK_STATE_IDLE }) }
        }
        //阅读进度
        controller.synchronize().observe(viewLifecycleOwner, Observer { progress ->
            binding.mBookSummaryChapter.text = if (progress.title.isNotBlank()) progress.title else getString(R.string.book_summary_chapter_none)
            binding.mBookSummaryStatistics.text = getString(R.string.book_summary_statistics_format, progress.total.toInt(), progress.speed)
        })
    }

    /**
     * 文本发生变化
     */
    private fun onTextChanged(text: String, input: AppCompatEditText, line: View) {
        line.setBackgroundColor(getColor(line.context, if (text.isNotBlank()) R.color.colorAccent else R.color.colorFlower))
        if (text.isNotBlank()) {
            if (input == mBookSummaryName) controller.book?.name = text else controller.book?.author = text
            controller.book?.run { Room.book().update(this) }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Notify.Event) {
        if (event is Notify.BookCoverChangedEvent) binding.mBookSummaryCover.force().load(controller.book!!.cover)
    }

}