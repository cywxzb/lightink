package cn.lightink.reader.ui.booksource

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookSourceController
import cn.lightink.reader.databinding.ActivityBookSourceBinding
import cn.lightink.reader.databinding.ItemBooksourceBinding
import cn.lightink.reader.ktx.startActivity
import cn.lightink.reader.model.BookSource
import cn.lightink.reader.module.INTENT_BOOK_SOURCE
import cn.lightink.reader.module.PageListAdapter
import cn.lightink.reader.module.RVLinearLayoutManager
import cn.lightink.reader.ui.base.BottomSelectorDialog
import cn.lightink.reader.ui.base.LifecycleActivity
import cn.lightink.reader.ui.base.PopupMenu
import cn.lightink.reader.widget.VerticalDividerItemDecoration

class BookSourceActivity : LifecycleActivity() {

    private lateinit var binding: ActivityBookSourceBinding
    private val controller by lazy { ViewModelProvider(this).get(BookSourceController::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSourceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mTopbar.setOnMenuClickListener { showPopup() }
        binding.mBookSourceRecycler.addItemDecoration(VerticalDividerItemDecoration(this, R.dimen.margin_horizontal))
        binding.mBookSourceRecycler.layoutManager = RVLinearLayoutManager(this)
        binding.mBookSourceRecycler.adapter = this@BookSourceActivity.adapter
        controller.bookSources.observe(this, Observer { list ->
            binding.mTopbar.setProgressVisible(false)
            binding.mBookSourceNone.isVisible = list.isEmpty()
            adapter.submitList(list)
        })
    }

    private fun showPopup() {
        PopupMenu(this).gravity(Gravity.END).items(R.string.booksource_import).callback { item ->
            when (item) {
                R.string.booksource_import -> startActivity(BookSourceVerifyActivity::class)
            }
        }.show(binding.mTopbar)
    }

    /**
     * 长按菜单
     */
    private fun showPopupMenu(position: Int, bookSource: BookSource) {
        val items = mutableListOf(R.string.uninstall)
        if (bookSource.account) items.add(0, R.string.login)
        BottomSelectorDialog(this, bookSource.name, items) { getString(it) }.callback { item ->
            when (item) {
                R.string.login -> startActivityForResult(Intent(this, BookSourceAuthActivity::class.java).putExtra(INTENT_BOOK_SOURCE, bookSource.url), Activity.RESULT_FIRST_USER)
                R.string.uninstall -> controller.uninstall(bookSource)
            }
        }.show()
    }

    /**
     * 构建数据适配器
     */
    private val adapter = PageListAdapter<BookSource>(R.layout.item_booksource, equalItem = { old, new -> old.url == new.url }) { item, bookSource ->
        if (bookSource == null) return@PageListAdapter
        val itemBinding = ItemBooksourceBinding.bind(item.view)
        itemBinding.mBookSourceAuth.isVisible = false
        itemBinding.mBookSourceRank.isVisible = bookSource.rank
        itemBinding.mBookSourceName.text = bookSource.name
        itemBinding.mBookSourceOwner.text = bookSource.author
        if (bookSource.account) {
            controller.verify(bookSource).observe(this, Observer { verify ->
                itemBinding.mBookSourceAuth.isVisible = true
                itemBinding.mBookSourceAuth.setText(if (verify) R.string.booksource_auth_success else R.string.booksource_auth_failure)
                itemBinding.mBookSourceAuth.setTextColor(getColor(if (verify) R.color.colorAccent else R.color.colorGrapefruit))
                itemBinding.mBookSourceAuth.backgroundTintList = ColorStateList.valueOf(getColor(if (verify) R.color.colorAccent else R.color.colorGrapefruit))
            })
        }
        itemBinding.mBookSourceInstall.paint.isFakeBoldText = true
        itemBinding.mBookSourceInstall.typeface = Typeface.DEFAULT_BOLD
        itemBinding.mBookSourceInstall.setText(R.string.more)
        itemBinding.mBookSourceInstallLayout.setOnClickListener { showPopupMenu(item.adapterPosition, bookSource) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == Activity.RESULT_FIRST_USER && resultCode == Activity.RESULT_OK -> adapter.currentList?.indexOfFirst { it.url == data?.getStringExtra(INTENT_BOOK_SOURCE).orEmpty() }?.run { adapter.notifyItemChanged(this) }
        }
    }
}