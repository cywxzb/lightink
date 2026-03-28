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
import cn.lightink.reader.databinding.PopupReaderLineBinding
import cn.lightink.reader.model.Theme
import cn.lightink.reader.module.Preferences
import kotlin.math.roundToInt

@SuppressLint("InflateParams")
class ReaderLinePopup(val context: FragmentActivity) : PopupWindow(LayoutInflater.from(context).inflate(R.layout.popup_reader_line, null), -1, -2, true) {

    private val controller by lazy { ViewModelProvider(context)[ReaderController::class.java] }
    private val xAndProgress = PointF()
    private val binding by lazy { PopupReaderLineBinding.bind(contentView) }

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
        binding.mLineForeground.setOnTouchListener { v, event -> passTouchEvent(v, binding.mLineSeekBar, event) }
        binding.mLineSeekBar.progress = ((Preferences.get(Preferences.Key.LINE_SPACING, 1.3F) - 1) * 10).toInt()
        onProgressChanged(binding.mLineSeekBar)
        //段间距
        binding.mParagraphForeground.setOnTouchListener { v, event -> passTouchEvent(v, binding.mParagraphSeekBar, event) }
        binding.mParagraphSeekBar.progress = Preferences.get(Preferences.Key.PARAGRAPH_DISTANCE, 0)
        onProgressChanged(binding.mParagraphSeekBar)
    }

    private fun passTouchEvent(formView: View, toView: SeekBar, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> xAndProgress.set(event.x, toView.progress.toFloat())
            MotionEvent.ACTION_MOVE -> {
                toView.progress = (xAndProgress.y + (event.x - xAndProgress.x) / (formView.width / toView.max * 0.8)).roundToInt()
                onProgressChanged(toView)
            }
            MotionEvent.ACTION_UP -> if (xAndProgress.y.toInt() != toView.progress) onStopTrackingTouch(toView)
        }
        return false
    }


    private fun setupViewTheme(theme: Theme) {
        //行间距
        binding.mLineForeground.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        binding.mLineBackground.backgroundTintList = ColorStateList.valueOf(theme.background)
        binding.mLineText.backgroundTintList = ColorStateList.valueOf(theme.control)
        binding.mLineText.setTextColor(theme.foreground)
        binding.mLineSeekBar.progressTintList = ColorStateList.valueOf(theme.control)
        //段间距
        binding.mParagraphForeground.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        binding.mParagraphBackground.backgroundTintList = ColorStateList.valueOf(theme.background)
        binding.mParagraphText.backgroundTintList = ColorStateList.valueOf(theme.control)
        binding.mParagraphText.setTextColor(theme.foreground)
        binding.mParagraphSeekBar.progressTintList = ColorStateList.valueOf(theme.control)
    }


    private fun onProgressChanged(seekBar: SeekBar) {
        when (seekBar) {
            //字号
            binding.mLineSeekBar -> binding.mLineText.text = context.getString(R.string.reader_setting_line_spacing, seekBar.progress * 0.1F + 1F)
            //字间距
            binding.mParagraphSeekBar -> binding.mParagraphText.text = context.getString(R.string.reader_setting_paragraph_spacing, seekBar.progress * 0.1F)
        }
    }

    private fun onStopTrackingTouch(seekBar: SeekBar) {
        when (seekBar) {
            //行间距
            binding.mLineSeekBar -> {
                val newValue = seekBar.progress * 0.1F + 1F
                if (newValue != Preferences.get(Preferences.Key.LINE_SPACING, 1.3F)) {
                    Preferences.put(Preferences.Key.LINE_SPACING, newValue)
                    controller.setupDisplay(context).jump()
                }
            }
            //段间距
            binding.mParagraphSeekBar -> if (seekBar.progress != Preferences.get(Preferences.Key.PARAGRAPH_DISTANCE, 0)) {
                Preferences.put(Preferences.Key.PARAGRAPH_DISTANCE, seekBar.progress).run { controller.setupDisplay(context).jump() }
            }
        }
    }

}