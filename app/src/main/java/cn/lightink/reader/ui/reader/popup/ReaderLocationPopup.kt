package cn.lightink.reader.ui.reader.popup

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.ReaderController
import cn.lightink.reader.databinding.PopupReaderLocationBinding
import cn.lightink.reader.model.Theme
import kotlin.math.max
import kotlin.math.roundToInt

@SuppressLint("InflateParams")
class ReaderLocationPopup(val context: FragmentActivity) : PopupWindow(LayoutInflater.from(context).inflate(R.layout.popup_reader_location, null), -1, -2, true) {

    private val controller by lazy { ViewModelProvider(context)[ReaderController::class.java] }
    private val xAndProgress = PointF()
    private val binding by lazy { PopupReaderLocationBinding.bind(contentView) }

    init {
        isOutsideTouchable = true
        isTouchable = true
        enterTransition = TransitionInflater.from(context).inflateTransition(R.transition.slide_show_bottom)
        exitTransition = TransitionInflater.from(context).inflateTransition(R.transition.slide_hide_bottom)
        setBackgroundDrawable(ColorDrawable())
        setupViewData()
        setupViewTheme(controller.theme)
    }

    private fun setupViewData() {
        //行间距
        binding.mLocationForeground.setOnTouchListener { v, event -> passTouchEvent(v, binding.mLocationSeekBar, event) }
        controller.findRangeByPage().run {
            binding.mLocationSeekBar.progress = start
            binding.mLocationSeekBar.max = last
        }
        onProgressChanged(binding.mLocationSeekBar)
    }

    private fun passTouchEvent(formView: View, toView: SeekBar, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> xAndProgress.set(event.x, toView.progress.toFloat())
            MotionEvent.ACTION_MOVE -> {
                toView.progress = max(1, (xAndProgress.y + (event.x - xAndProgress.x) / (formView.width / toView.max * 0.8)).roundToInt())
                onProgressChanged(toView)
            }
        }
        return false
    }


    private fun setupViewTheme(theme: Theme) {
        //行间距
        binding.mLocationForeground.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        binding.mLocationBackground.backgroundTintList = ColorStateList.valueOf(theme.background)
        binding.mLocationText.backgroundTintList = ColorStateList.valueOf(theme.control)
        binding.mLocationText.setTextColor(theme.foreground)
        binding.mLocationSeekBar.progressTintList = ColorStateList.valueOf(theme.control)
    }


    private fun onProgressChanged(seekBar: SeekBar) {
        binding.mLocationText.text = context.getString(R.string.reader_setting_location_value, seekBar.progress, seekBar.max)
        controller.pageSeekCallback?.invoke(controller.findChapterStartIndex() + seekBar.progress - 1)
    }

}