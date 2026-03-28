package cn.lightink.reader.ui.book

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookController
import cn.lightink.reader.databinding.ActivityBookCoverBinding
import cn.lightink.reader.databinding.ItemBookCoverBinding
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.Book
import cn.lightink.reader.module.INTENT_BOOK
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.RVGridLayoutManager
import cn.lightink.reader.module.TOAST_TYPE_SUCCESS
import cn.lightink.reader.ui.base.LifecycleActivity
import kotlin.math.max

class BookCoverActivity : LifecycleActivity() {

    private val controller by lazy { ViewModelProvider(this)[BookController::class.java] }
    private val book by lazy { intent.getParcelableExtra<Book>(INTENT_BOOK) }
    private val adapter by lazy { buildAdapter() }
    private lateinit var binding: ActivityBookCoverBinding
    //封面尺寸
    private val size by lazy { resources.getDimensionPixelSize(R.dimen.dimenBookshelfCoverSize) }
    //封面边距
    private val edge by lazy { getRecyclerItemEdge() }
    //网格数量
    private var span = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookCoverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mTopbar.setOnMenuClickListener { openSelector() }
        binding.mBookCoverRecycler.layoutManager = RVGridLayoutManager(this, span)
        binding.mBookCoverRecycler.setPadding(edge)
        binding.mBookCoverRecycler.adapter = adapter
        controller.queryCover(book?.name.orEmpty()).observe(this, Observer {
            binding.mTopbar.setProgressVisible(false)
            adapter.submitList(adapter.currentList.plus(it))
        })
    }

    /**
     * 打开选择器
     */
    private fun openSelector() {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Activity.RESULT_FIRST_USER)
    }

    /**
     * 下载封面
     */
    private fun downloadCover(uri: String) {
        controller.downloadCover(book!!, uri).observe(this, Observer { onSetupCoverResult(it) })
    }

    /**
     * 封面设置结果
     */
    private fun onSetupCoverResult(result: Boolean?) {
        if (result == true) {
            toast("下载封面成功", TOAST_TYPE_SUCCESS).run { onBackPressed() }
        } else {
            toast("下载封面失败")
        }
    }

    /**
     * 计算边缘
     */
    private fun getRecyclerItemEdge(): Int {
        val width = resources.displayMetrics.widthPixels
        span = max(3, width / (size + resources.getDimensionPixelSize(R.dimen.dimen4x)))
        return (width - size * span) / (span * 2 + 2)
    }

    /**
     * 构建数据适配器
     */
    private fun buildAdapter() = ListAdapter<String>(R.layout.item_book_cover) { item, uri ->
        val binding = ItemBookCoverBinding.bind(item.view)
        item.view.setPadding(edge)
        binding.mBookCover.layoutParams.width = size
        binding.mBookCover.layoutParams.height = (size * 1.4F).toInt()
        binding.mBookCover.hint(book?.name.orEmpty()).load(uri)
        item.view.setOnClickListener { downloadCover(uri) }
    }

    /**
     * 相册结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Activity.RESULT_FIRST_USER && resultCode == Activity.RESULT_OK && data?.data != null) {
            controller.copyCover(this, data.data!!, book!!).observe(this, Observer { onSetupCoverResult(it) })
        }
    }

}