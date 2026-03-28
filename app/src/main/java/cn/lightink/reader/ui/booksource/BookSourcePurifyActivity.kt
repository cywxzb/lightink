package cn.lightink.reader.ui.booksource

import android.os.Bundle
import cn.lightink.reader.R
import cn.lightink.reader.databinding.ActivityBookSourcePurifyBinding
import cn.lightink.reader.module.INTENT_BOOK_SOURCE
import cn.lightink.reader.module.RVLinearLayoutManager
import cn.lightink.reader.ui.base.LifecycleActivity

class BookSourcePurifyActivity : LifecycleActivity() {

    private val source by lazy { intent.getStringExtra(INTENT_BOOK_SOURCE).orEmpty().split("#") }
    private lateinit var binding: ActivityBookSourcePurifyBinding
//    private val adapter by lazy { buildAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSourcePurifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mTopbar.text = source.firstOrNull().orEmpty()
        binding.mPurifyRecycler.layoutManager = RVLinearLayoutManager(this)
//        binding.mPurifyRecycler.adapter = adapter
//        Room.purify().getAllLive(source.lastOrNull().orEmpty()).observe(this, Observer { adapter.submitList(it) })
    }
//
//    private fun buildAdapter() = ListAdapter<Purify>(R.layout.item_book_source_purify) { item, purify ->
//        item.view.mPurifyWord.text = purify.content
//        item.view.setOnClickListener { remove(purify) }
//    }
//
//    private fun remove(purify: Purify) {
//        dialog("移除对「${purify.content}」的净化？") {
//            Room.purify().remove(purify)
//        }
//    }

}