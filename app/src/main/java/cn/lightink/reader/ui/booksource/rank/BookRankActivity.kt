package cn.lightink.reader.ui.booksource.rank

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookRankController
import cn.lightink.reader.databinding.ActivityBookRankBinding
import cn.lightink.reader.databinding.LayoutTabItemBinding
import cn.lightink.reader.ktx.startActivityForResult
import cn.lightink.reader.model.BookRank
import cn.lightink.reader.ui.base.LifecycleActivity
import com.google.android.material.tabs.TabLayout

class BookRankActivity : LifecycleActivity(), TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    private lateinit var binding: ActivityBookRankBinding
    private val controller by lazy { ViewModelProvider(this)[BookRankController::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookRankBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mTopbar.setOnMenuClickListener { startActivityForResult(BookRankSettingsActivity::class) }
        binding.mTabLayout.addOnTabSelectedListener(this)
        setupRanks(controller.getVisibleBookRanks())
    }

    /**
     * 设置排行榜
     */
    private fun setupRanks(ranks: List<BookRank>) {
        binding.mBookRankNone.isVisible = ranks.isEmpty()
        binding.mTabLayout.isVisible = ranks.isNotEmpty()
        binding.mTabLayout.removeAllTabs()
        ranks.forEach { rank ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.layout_tab_item2, binding.mTabLayout, false)
            val itemBinding = LayoutTabItemBinding.bind(itemView)
            itemBinding.mLabel.text = rank.name
            itemBinding.mLabel.setTextColor(binding.mTabLayout.tabTextColors)
            binding.mTabLayout.addTab(binding.mTabLayout.newTab().setCustomView(itemView))
        }
        binding.mViewPager.adapter = FragmentAdapter(ranks)
        binding.mViewPager.addOnPageChangeListener(this)
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

    inner class FragmentAdapter(private val ranks: List<BookRank>) : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int) = BookRankFragment.newInstance(ranks[position])

        override fun getCount() = ranks.size
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Activity.RESULT_FIRST_USER && resultCode == Activity.RESULT_OK) {
            setupRanks(controller.getVisibleBookRanks())
        }
    }
}