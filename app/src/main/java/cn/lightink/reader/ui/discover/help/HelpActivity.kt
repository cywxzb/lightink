package cn.lightink.reader.ui.discover.help

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.lightink.reader.databinding.ActivityHelpBinding
import cn.lightink.reader.ui.base.LifecycleActivity
import com.google.android.material.tabs.TabLayout

class HelpActivity : LifecycleActivity(), TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    private lateinit var binding: ActivityHelpBinding
    private val titles = listOf("关于书架", "关于时刻")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mTabLayout.addOnTabSelectedListener(this)
        titles.forEach { title ->
            val itemView = LayoutInflater.from(this).inflate(cn.lightink.reader.R.layout.layout_tab_item, binding.mTabLayout, false)
            itemView.findViewById<TextView>(cn.lightink.reader.R.id.mLabel).text = title
            itemView.findViewById<TextView>(cn.lightink.reader.R.id.mLabel).setTextColor(binding.mTabLayout.tabTextColors)
            binding.mTabLayout.addTab(binding.mTabLayout.newTab().setCustomView(itemView))
        }
        binding.mViewPager.adapter = FragmentAdapter(titles)
        binding.mViewPager.addOnPageChangeListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.findViewById<TextView>(cn.lightink.reader.R.id.mLabel)?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        tab?.customView?.findViewById<TextView>(cn.lightink.reader.R.id.mLabel)?.paint?.isFakeBoldText = false
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.customView?.findViewById<TextView>(cn.lightink.reader.R.id.mLabel)?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        tab?.customView?.findViewById<TextView>(cn.lightink.reader.R.id.mLabel)?.paint?.isFakeBoldText = true
        tab?.position?.run { binding.mViewPager.currentItem = this }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        binding.mTabLayout.selectTab(binding.mTabLayout.getTabAt(position))
    }

    inner class FragmentAdapter(private val titles: List<String>) : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int) = when (position) {
            1 -> FeedHelpFragment()
            else -> BookshelfHelpFragment()
        }

        override fun getCount() = titles.size
    }

}