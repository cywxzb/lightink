package cn.lightink.reader.ui.feed

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import cn.lightink.reader.R
import cn.lightink.reader.controller.FeedController
import cn.lightink.reader.databinding.ActivityFeedBinding
import cn.lightink.reader.ktx.startActivity
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.FeedGroup
import cn.lightink.reader.module.Preferences
import cn.lightink.reader.ui.base.BottomSelectorDialog
import cn.lightink.reader.ui.base.LifecycleActivity
import cn.lightink.reader.ui.discover.help.FeedHelpFragment
import kotlin.math.max

class FeedActivity : LifecycleActivity(), ViewPager.OnPageChangeListener {

    private val controller by lazy { ViewModelProvider(this)[FeedController::class.java] }
    private lateinit var binding: ActivityFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mTopbar.setOnMenuClickListener { startActivity(FeedVerifyActivity::class) }
        binding.mFeedClean.setOnClickListener { showPopupMenu() }
        binding.mFeedManage.setOnClickListener { startActivity(FeedManagementActivity::class) }
        binding.mFeedTabLayout.setOnPageChangeListener(this)
        controller.groupLiveData.observe(this, Observer { groups ->
            binding.mFeedClean.isVisible = groups.isNotEmpty()
            binding.mFeedManage.isVisible = groups.isNotEmpty()
            binding.mFeedViewPager.adapter = PagerAdapter(supportFragmentManager, groups)
            binding.mFeedViewPager.offscreenPageLimit = 3
            binding.mFeedViewPager.setCurrentItem(max(groups.indexOfFirst { it.id == Preferences.get(Preferences.Key.LAST_FEED, 0L) }, 0), false)
            binding.mFeedTabLayout.setViewPager(binding.mFeedViewPager)
            onPageSelected(binding.mFeedViewPager.currentItem)
            checkHelpView(groups.isEmpty())
        })
    }

    private fun showPopupMenu() {
        BottomSelectorDialog(this, getString(R.string.feed_clear), listOf(R.string.feed_clear_current, R.string.feed_clear_all)) { resId -> getString(resId) }.callback { item ->
            controller.clean(if (item == R.string.feed_clear_current) (binding.mFeedViewPager.adapter as? PagerAdapter)?.getGroupId(binding.mFeedViewPager.currentItem) ?: 0L else -1)
            toast(R.string.feed_clean_completed)
        }.show()
    }

    /**
     * 检查是否显示使用指南
     */
    private fun checkHelpView(isVisible: Boolean) {
        binding.container.isVisible = isVisible
        val transaction = supportFragmentManager.beginTransaction()
        if (isVisible && supportFragmentManager.fragments.isEmpty()) {
            transaction.replace(R.id.container, FeedHelpFragment(), "HELP").commitAllowingStateLoss()
        } else if (!isVisible) {
            supportFragmentManager.findFragmentByTag("HELP")?.run { transaction.remove(this).commitAllowingStateLoss() }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (binding.mFeedViewPager.adapter?.count ?: 0 <= 0) return
        (0 until (binding.mFeedViewPager.adapter?.count ?: 0)).mapNotNull { binding.mFeedTabLayout.getTabAt(it) }.forEachIndexed { index, view ->
            (view as TextView).typeface = if (position == index) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        Preferences.put(Preferences.Key.LAST_FEED, (binding.mFeedViewPager.adapter as? PagerAdapter)?.getGroupId(position) ?: 0L)
    }

    class PagerAdapter(fm: FragmentManager, private val groups: List<FeedGroup>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        fun getGroupId(position: Int) = groups[position].id

        override fun getItem(position: Int) = FeedFragment.newInstance(if (groups[position].date == 0L) -1 else groups[position].id)

        override fun getCount() = groups.size

        override fun getPageTitle(position: Int) = groups[position].name
    }

}