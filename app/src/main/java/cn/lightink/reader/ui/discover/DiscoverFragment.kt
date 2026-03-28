package cn.lightink.reader.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.lightink.reader.App
import cn.lightink.reader.R
import cn.lightink.reader.databinding.FragmentDiscoverBinding
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.ktx.startActivity
import cn.lightink.reader.module.Preferences
import cn.lightink.reader.module.UIModule
import cn.lightink.reader.ui.base.LifecycleFragment
import cn.lightink.reader.ui.booksource.BookSourceActivity
import cn.lightink.reader.ui.booksource.rank.BookRankActivity
import cn.lightink.reader.ui.discover.setting.SettingActivity
import cn.lightink.reader.ui.feed.FeedActivity

class DiscoverFragment : LifecycleFragment() {

    private lateinit var binding: FragmentDiscoverBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupView()
    }

    /**
     * 设置控件
     */
    private fun setupView() {
        binding.mDiscoverNight.post {
            binding.mDiscoverNight.onCheckedChangeListener = null
            binding.mDiscoverNight.isChecked = !UIModule.isNightMode(requireContext())
            binding.mDiscoverNight.setOnCheckedChangeListener { _, isChecked ->
                Preferences.put(Preferences.Key.LIGHT, isChecked)
                (activity?.application as? App)?.setupTheme()
            }
        }
        binding.mDiscoverNight.parentView.setOnClickListener { binding.mDiscoverNight.toggle() }
        binding.mDiscoverBookSource.setOnClickListener { startActivity(BookSourceActivity::class) }
        binding.mDiscoverRank.setOnClickListener { startActivity(BookRankActivity::class) }
        binding.mDiscoverFeed.setOnClickListener { startActivity(FeedActivity::class) }
        binding.mDiscoverSetting.setOnClickListener { startActivity(SettingActivity::class) }
    }

}