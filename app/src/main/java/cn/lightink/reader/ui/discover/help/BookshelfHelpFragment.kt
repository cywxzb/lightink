package cn.lightink.reader.ui.discover.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.lightink.reader.R
import cn.lightink.reader.databinding.FragmentHelpBookshelfBinding
import cn.lightink.reader.ktx.startActivity
import cn.lightink.reader.ui.base.LifecycleFragment
import cn.lightink.reader.ui.booksource.BookSourceActivity

class BookshelfHelpFragment : LifecycleFragment() {

    private lateinit var binding: FragmentHelpBookshelfBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHelpBookshelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mHelpBookSource.setOnClickListener { startActivity(BookSourceActivity::class) }
    }
}