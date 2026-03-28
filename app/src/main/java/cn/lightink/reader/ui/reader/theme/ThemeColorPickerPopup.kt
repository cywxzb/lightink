package cn.lightink.reader.ui.reader.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.ThemeController
import cn.lightink.reader.databinding.PopupThemeColorPickerBinding
import okhttp3.internal.toHexString
import kotlin.math.roundToInt

@SuppressLint("InflateParams")
class ThemeColorPickerPopup(val context: FragmentActivity, val color: Int, val callback: (Int) -> Unit) : PopupWindow(LayoutInflater.from(context).inflate(R.layout.popup_theme_color_picker, null), -1, -2, true) {

    private val controller by lazy { ViewModelProvider(context)[ThemeController::class.java] }
    private val inputMethodManager by lazy { context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private val binding by lazy { PopupThemeColorPickerBinding.bind(contentView) }
    private val xAndProgress = PointF()

    init {
        isOutsideTouchable = true
        isTouchable = true
        enterTransition = TransitionInflater.from(context).inflateTransition(R.transition.slide_show_bottom)
        exitTransition = TransitionInflater.from(context).inflateTransition(R.transition.slide_hide_bottom)
        setBackgroundDrawable(ColorDrawable())
        setupViewData(color)
        setupViewTheme()
        binding.mColorValueInput.setOnFocusChangeListener { _, hasFocus -> binding.mColorValueText.isVisible =  !hasFocus }
        binding.mColorValueInput.doOnTextChanged { hex, _, _, _ ->
            when {
                hex != null && hex.startsWith("#") && hex.length == 7 -> parseColor(hex.toString())
                hex != null && !hex.startsWith("#") && hex.length == 6 -> parseColor("#$hex")
                else -> {}
            }
        }
        binding.mColorValueReset.setOnClickListener { setupViewData(color) }
    }

    private fun setupViewData(color: Int) {
        //RED
        binding.mColorRForeground.setOnTouchListener { v, event -> passTouchEvent(v, binding.mColorRSeekBar, event) }
        binding.mColorRSeekBar.progress = Color.red(color)
        onProgressChanged(binding.mColorRSeekBar)
        //GREEN
        binding.mColorGForeground.setOnTouchListener { v, event -> passTouchEvent(v, binding.mColorGSeekBar, event) }
        binding.mColorGSeekBar.progress = Color.green(color)
        onProgressChanged(binding.mColorGSeekBar)
        //BLUE
        binding.mColorBForeground.setOnTouchListener { v, event -> passTouchEvent(v, binding.mColorBSeekBar, event) }
        binding.mColorBSeekBar.progress = Color.blue(color)
        onProgressChanged(binding.mColorBSeekBar)
        //Reset
        binding.mColorValueInput.text?.clear()
        binding.mColorValueInput.clearFocus()
        inputMethodManager.hideSoftInputFromWindow(binding.mColorValueInput.windowToken, 0)
    }

    private fun setupViewTheme() {
        binding.mColorValueForeground.backgroundTintList = ColorStateList.valueOf(controller.theme.foreground)
        binding.mColorRForeground.backgroundTintList = ColorStateList.valueOf(controller.theme.foreground)
        binding.mColorRBackground.backgroundTintList = ColorStateList.valueOf(controller.theme.background)
        binding.mColorGForeground.backgroundTintList = ColorStateList.valueOf(controller.theme.foreground)
        binding.mColorGBackground.backgroundTintList = ColorStateList.valueOf(controller.theme.background)
        binding.mColorBForeground.backgroundTintList = ColorStateList.valueOf(controller.theme.foreground)
        binding.mColorBBackground.backgroundTintList = ColorStateList.valueOf(controller.theme.background)
    }

    /**
     * 解析颜色
     */
    private fun parseColor(colorString: String) = try {
        setupViewData(Color.parseColor(colorString))
    } catch (e: Exception) {
        binding.mColorValueInput.text?.clear()
    }

    private fun passTouchEvent(formView: View, toView: SeekBar, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> xAndProgress.set(event.x, toView.progress.toFloat())
            MotionEvent.ACTION_MOVE -> {
                toView.progress = (xAndProgress.y + (event.x - xAndProgress.x) / (formView.width / toView.max * 0.8)).roundToInt()
                onProgressChanged(toView)
            }
        }
        return false
    }

    private fun onProgressChanged(seekBar: SeekBar) {
        when (seekBar) {
            //RED
            binding.mColorRSeekBar -> {
                binding.mColorRText.text = context.getString(R.string.theme_color_red, seekBar.progress)
            }
            //GREEN
            binding.mColorGSeekBar -> {
                binding.mColorGText.text = context.getString(R.string.theme_color_green, seekBar.progress)
            }
            //BLUE
            binding.mColorBSeekBar -> {
                binding.mColorBText.text = context.getString(R.string.theme_color_blue, seekBar.progress)
            }
        }
        //VALUE
        val color = Color.rgb(binding.mColorRSeekBar.progress, binding.mColorGSeekBar.progress, binding.mColorBSeekBar.progress)
        binding.mColorValueText.text = color.toHexString().let { if (it.length > 6) it.substring(it.length - 6, it.length) else it }
        binding.mColorValueBackground.backgroundTintList = ColorStateList.valueOf(color)
        callback.invoke(color)
        setupViewTheme()
    }
}