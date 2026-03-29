package cn.lightink.reader.ui.book

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookSummaryController
import cn.lightink.reader.databinding.ActivityBookSummaryBinding
import cn.lightink.reader.databinding.LayoutTabItemBinding
import cn.lightink.reader.module.INTENT_BOOK_CACHE
import cn.lightink.reader.ui.base.LifecycleActivity
import com.google.android.material.tabs.TabLayout

class BookSummaryActivity : LifecycleActivity(), TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    private lateinit var binding: ActivityBookSummaryBinding
    private val controller by lazy { ViewModelProvider(this)[BookSummaryController::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controller.attach(intent)
        binding.mViewPager.adapter = FragmentAdapter()
        binding.mViewPager.addOnPageChangeListener(this)
        binding.mTabLayout.addOnTabSelectedListener(this)
        listOf(R.string.book_summary_info, R.string.book_summary_cache).forEach { typeResId ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.layout_tab_item, binding.mTabLayout, false)
            val itemBinding = LayoutTabItemBinding.bind(itemView)
            itemBinding.mLabel.text = getString(typeResId)
            itemBinding.mLabel.minWidth = (resources.getDimension(R.dimen.dimen1) * 64).toInt()
            itemBinding.mLabel.setTextColor(binding.mTabLayout.tabTextColors)
            binding.mTabLayout.addTab(binding.mTabLayout.newTab().setCustomView(itemView))
        }
        if (intent.getBooleanExtra(INTENT_BOOK_CACHE, false)) {
            binding.mViewPager.setCurrentItem(1, false)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.let { LayoutTabItemBinding.bind(it).mLabel }?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        tab?.customView?.let { LayoutTabItemBinding.bind(it).mLabel }?.paint?.isFakeBoldText = false
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.customView?.let { LayoutTabItemBinding.bind(it).mLabel }?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        tab?.customView?.let { LayoutTabItemBinding.bind(it).mLabel }?.paint?.isFakeBoldText = true
        tab?.position?.run { binding.mViewPager.currentItem = this }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        binding.mTabLayout.selectTab(binding.mTabLayout.getTabAt(position))
    }

    inner class FragmentAdapter : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int) = if (position == 0) BookSummaryInfoFragment() else BookSummaryCacheFragment()

        override fun getCount() = 2
    }

}