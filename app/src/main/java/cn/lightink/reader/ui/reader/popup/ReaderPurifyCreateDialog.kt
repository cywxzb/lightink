package cn.lightink.reader.ui.reader.popup

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.ReaderController
import cn.lightink.reader.databinding.DialogPurifyCreateBinding
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.Theme
import cn.lightink.reader.module.EMPTY

class ReaderPurifyCreateDialog(context: FragmentActivity, val content: String = EMPTY) : Dialog(context) {

    private val controller by lazy { ViewModelProvider(context)[ReaderController::class.java] }
    private lateinit var binding: DialogPurifyCreateBinding

    init {
        setContentView(R.layout.dialog_purify_create)
        val contentView = findViewById<ViewGroup>(android.R.id.content)?.rootView
        if (contentView != null) {
            binding = DialogPurifyCreateBinding.bind(contentView)
            if (content.isNotBlank()) {
                binding.mPurifyCreateKey.text = content
            } else {
                binding.mPurifyCreateRegexInput.isVisible = true
            }
            binding.mPurifyCreateSubmit.setOnClickListener { purify() }
            setupViewTheme(controller.theme, controller.paint)
        }
    }

    private fun setupViewTheme(theme: Theme, paint: TextPaint) {
        binding.mPurifyCreateTitle.parentView.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        binding.mPurifyCreateTitle.typeface = paint.typeface
        binding.mPurifyCreateTitle.setTextColor(theme.content)
        binding.mPurifyCreateKey.typeface = paint.typeface
        binding.mPurifyCreateKey.setTextColor(theme.content)
        binding.mPurifyCreateRegexInput.typeface = paint.typeface
        binding.mPurifyCreateRegexInput.backgroundTintList = ColorStateList.valueOf(theme.secondary)
        binding.mPurifyCreateRegexInput.setTextColor(theme.content)
        binding.mPurifyCreateRegexInput.setHintTextColor(theme.secondary)
        binding.mPurifyCreateInput.typeface = paint.typeface
        binding.mPurifyCreateInput.backgroundTintList = ColorStateList.valueOf(theme.secondary)
        binding.mPurifyCreateInput.setTextColor(theme.content)
        binding.mPurifyCreateInput.setHintTextColor(theme.secondary)
        binding.mPurifyCreateSubmit.typeface = paint.typeface
        binding.mPurifyCreateSubmit.backgroundTintList = ColorStateList.valueOf(theme.background)
        binding.mPurifyCreateSubmit.setTextColor(theme.control)
    }

    private fun purify() {
        if (content.isNotBlank()) {
            controller.purify(content, binding.mPurifyCreateInput.text.toString().trim(), false)
        } else {
            val regex = binding.mPurifyCreateRegexInput.text.toString()
            try {
                Regex(regex)
                if (regex.isNotEmpty()) {
                    controller.purify(regex, binding.mPurifyCreateInput.text.toString().trim(), true)
                }
            } catch (e: Exception) {
                return context.toast("正则表达式有误")
            }
        }
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(-1, -2)
        window?.setDimAmount(0.4F)
    }

}