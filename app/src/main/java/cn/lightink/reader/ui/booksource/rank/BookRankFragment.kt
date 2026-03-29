package cn.lightink.reader.ui.booksource.rank

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookRankController
import cn.lightink.reader.databinding.FragmentBookRankBinding
import cn.lightink.reader.databinding.ItemBookRankGroupBinding
import cn.lightink.reader.databinding.ItemSimpleBookBinding
import cn.lightink.reader.model.BookRank
import cn.lightink.reader.module.*
import cn.lightink.reader.module.booksource.BookSourceJson
import cn.lightink.reader.module.booksource.BookSourceParser
import cn.lightink.reader.module.booksource.SearchMetadata
import cn.lightink.reader.ui.base.BottomSelectorDialog
import cn.lightink.reader.ui.base.LifecycleFragment
import cn.lightink.reader.ui.book.BookDetailActivity
import cn.lightink.reader.widget.VerticalDividerItemDecoration

class BookRankFragment : LifecycleFragment() {

    private var _binding: FragmentBookRankBinding? = null
    private val binding get() = _binding!!
    private val controller by lazy { ViewModelProvider(this)[BookRankController::class.java] }
    private val bookRank by lazy { arguments?.getParcelable<BookRank>(INTENT_BOOK_RANK)!! }
    private val bookSource by lazy { Room.bookSource().get(bookRank.url)!! }
    private val bookSourceJson by lazy { bookSource.json }
    private val bookSourceParser by lazy { BookSourceParser(bookSource) }
    private var group: BookSourceJson.Rank? = null
    private var category: BookSourceJson.Category? = null
    private var page = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBookRankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        group = bookSourceJson.rank.getOrElse(bookRank.preferred) { bookSourceJson.rank.firstOrNull().apply { bookRank.preferred = 0 } }
        page = group?.page ?: -1
        binding.mBookRankGroupRecycler.addItemDecoration(VerticalDividerItemDecoration(view.context, R.dimen.padding_horizontal_half))
        binding.mBookRankGroupRecycler.isVisible = bookSourceJson.rank.isNotEmpty()
        binding.mBookRankGroupRecycler.adapter = groupAdapter.apply { submitList(bookSourceJson.rank) }
        binding.mBookRankCategory.isVisible = group?.categories?.isNotEmpty() == true
        binding.mBookRankCategory.setOnClickListener { showCategoryDialog() }
        if (group?.categories?.isNotEmpty() == true) {
            category = group!!.categories.firstOrNull { it.key == bookRank.category } ?: group!!.categories.firstOrNull()
            binding.mBookRankCategory.text = category?.value
        }
        binding.mBookRankRecycler.layoutManager = RVLinearLayoutManager(activity)
        binding.mBookRankRecycler.adapter = adapter
        binding.mBookRankRecycler.setOnLoadMoreListener { onLoadMore() }
    }

    private fun showCategoryDialog() {
        BottomSelectorDialog(requireActivity(), getString(R.string.select_category), group?.categories.orEmpty()) { it.value }.callback { selected ->
            if (category != selected) {
                bookRank.apply { category = selected.key }
                category = selected
                onRefresh()
            }
        }.show()
    }

    private fun onRefresh() {
        Room.bookRank().update(bookRank)
        binding.mBookRankCategory.isVisible = category != null
        binding.mBookRankCategory.text = category?.value
        page = group?.page ?: -1
        controller.refresh()
        adapter.submitList(emptyList())
        groupAdapter.notifyItemRangeChanged(0, groupAdapter.itemCount)
        onLoadMore()
    }

    private fun onLoadMore() {
        binding.mBookRankLoading.isVisible = true
        controller.loadMore(bookSourceParser, group!!, page, category?.key).observe(viewLifecycleOwner, Observer { list ->
            binding.mBookRankRecycler.finishLoadMore(page < 0 || list.isNullOrEmpty())
            binding.mBookRankLoading.isVisible = false
            if (list.isNotEmpty()) {
                page += (group?.unit ?: 0)
                adapter.submitList(list)
                if (list.size < group!!.size) onLoadMore()
            }
        })
    }

    private val adapter = ListAdapter<SearchMetadata>(R.layout.item_simple_book) { item, book ->
        val itemBinding = ItemSimpleBookBinding.bind(item.view)
        itemBinding.mSimpleBookCover.hint(book.name).load(book.cover)
        itemBinding.mSimpleBookNo.text = (item.adapterPosition + 1).toString()
        itemBinding.mSimpleBookName.text = book.name
        itemBinding.mSimpleBookAuthor.text = book.author
        item.view.setOnClickListener { openBookDetail(itemBinding.mSimpleBookCover, book) }
    }

    private val groupAdapter = ListAdapter<BookSourceJson.Rank>(R.layout.item_book_rank_group) { item, rank ->
        val itemBinding = ItemBookRankGroupBinding.bind(item.view)
        itemBinding.mBookRankTitle.text = rank.title
        itemBinding.mBookRankTitle.paint.isFakeBoldText = item.adapterPosition == bookRank.preferred
        itemBinding.mBookRankTitle.setTextColor(item.view.context.getColor(if (item.adapterPosition == bookRank.preferred) R.color.colorAccent else R.color.colorContent))
        item.view.setOnClickListener {
            Room.bookRank().update(bookRank.apply { preferred = item.adapterPosition })
            group = rank
            category = rank.categories.firstOrNull()
            onRefresh()
        }
    }

    /**
     * 打开图书详情
     */
    private fun openBookDetail(view: View, metadata: SearchMetadata) {
        controller.search(metadata, bookSource).observe(this, Observer { book ->
            val intent = Intent(activity, BookDetailActivity::class.java)
            intent.putExtra(INTENT_BOOK, book?.objectId())
            val options = ActivityOptions.makeSceneTransitionAnimation(activity, view, getString(R.string.transition))
            startActivity(intent, options.toBundle())
        })
    }

    companion object {
        fun newInstance(rank: BookRank) = BookRankFragment().apply {
            arguments = Bundle().apply { putParcelable(INTENT_BOOK_RANK, rank) }
        }
    }

}