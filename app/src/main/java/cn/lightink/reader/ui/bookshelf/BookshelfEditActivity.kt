package cn.lightink.reader.ui.bookshelf

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.core.view.get
import cn.lightink.reader.R
import cn.lightink.reader.databinding.ActivityBookshelfEditBinding
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.Bookshelf
import cn.lightink.reader.module.*
import cn.lightink.reader.ui.base.LifecycleActivity

class BookshelfEditActivity : LifecycleActivity() {

    private var bookshelf: Bookshelf? = null
    private lateinit var binding: ActivityBookshelfEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookshelfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookshelf = intent.getParcelableExtra(INTENT_BOOKSHELF)
        //名字
        binding.mBookshelfNameInput.paint.isFakeBoldText = true
        if (bookshelf == null) {
            //新建书架时自动弹起软键盘
            binding.mBookshelfNameInput.requestFocus()
            binding.mBookshelfNameInput.postDelayed({
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.mBookshelfNameInput, InputMethodManager.SHOW_IMPLICIT)
            }, 200L)
        } else {
            //编辑书架
            binding.mBookshelfNameInput.setText(bookshelf?.name)
            binding.mBookshelfNameInput.setSelection(bookshelf?.name.orEmpty().length)
        }
        //首选
        binding.mBookshelfPreferredGroup.check(binding.mBookshelfPreferredGroup[if (bookshelf?.preferred == true) 1 else 0].id)
        //布局
        binding.mBookshelfLayoutGroup.check(binding.mBookshelfLayoutGroup[if (bookshelf?.layout == 1) 1 else 0].id)
        //信息
        binding.mBookshelfInfoGroup.check(binding.mBookshelfInfoGroup[if (bookshelf?.info == 1) 1 else 0].id)
        //排序
        binding.mBookshelfSortGroup.check(binding.mBookshelfSortGroup[if (bookshelf?.sort == 1) 1 else 0].id)
        //保存
        binding.mBookshelfSave.setOnClickListener { onSave() }
    }

    /**
     * 保存
     */
    private fun onSave() {
        val name = binding.mBookshelfNameInput.text.toString().trim()
        if (name.isBlank()) return toast("未设置书架名字")
        if (bookshelf?.name != name && Room.bookshelf().has(name)) return toast("已存在同名书架")
        if (bookshelf == null) bookshelf = Bookshelf(name = name)
        bookshelf!!.name = name
        bookshelf!!.preferred = binding.mBookshelfPreferredGroup.indexOfChild(binding.mBookshelfPreferredGroup.findViewById(binding.mBookshelfPreferredGroup.checkedButtonId)) == 1
        bookshelf!!.layout = binding.mBookshelfLayoutGroup.indexOfChild(binding.mBookshelfLayoutGroup.findViewById(binding.mBookshelfLayoutGroup.checkedButtonId))
        bookshelf!!.info = binding.mBookshelfInfoGroup.indexOfChild(binding.mBookshelfInfoGroup.findViewById(binding.mBookshelfInfoGroup.checkedButtonId))
        bookshelf!!.sort = binding.mBookshelfSortGroup.indexOfChild(binding.mBookshelfSortGroup.findViewById(binding.mBookshelfSortGroup.checkedButtonId))
        //覆盖已存在的首选书架
        Room.bookshelf().insert(bookshelf!!)
        if (Preferences.get(Preferences.Key.BOOKSHELF, 1L) == bookshelf?.id) {
            //修改的是当前正在使用的书架
            Notify.post(Notify.BookshelfChangedEvent())
        }
        toast("已保存", TOAST_TYPE_SUCCESS)
        onBackPressed()
    }
}