package cn.lightink.reader.ui.discover.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.lightink.reader.R
import cn.lightink.reader.databinding.FragmentHelpFeedBinding
import cn.lightink.reader.ktx.startActivity
import cn.lightink.reader.ui.base.LifecycleFragment
import cn.lightink.reader.ui.feed.FeedVerifyActivity

class FeedHelpFragment : LifecycleFragment() {

    private lateinit var binding: FragmentHelpFeedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHelpFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mHelpFeedVerify.setOnClickListener { startActivity(FeedVerifyActivity::class) }
    }
}