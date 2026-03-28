package cn.lightink.reader.ui.booksource

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.webkit.URLUtil
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.BookSourceController
import cn.lightink.reader.databinding.ActivityBooksourceVerifyBinding
import cn.lightink.reader.ktx.change
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.module.TOAST_TYPE_SUCCESS
import cn.lightink.reader.ui.base.LifecycleActivity

class BookSourceVerifyActivity : LifecycleActivity() {

    private val clipboard by lazy { applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val controller by lazy { ViewModelProvider(this).get(BookSourceController::class.java) }
    private lateinit var binding: ActivityBooksourceVerifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBooksourceVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mBookSourceVerifyTextField.change { text ->
            binding.mBookSourceVerifyTextFieldLayout.error = null
            binding.mBookSourceVerifyButton.isEnabled = URLUtil.isNetworkUrl(text)
        }
        binding.mBookSourceVerifyButton.setOnClickListener { verify(binding.mBookSourceVerifyTextField.text.toString()) }
    }

    private fun verify(url: String) {
        binding.mBookSourceVerifyTextFieldLayout.isEnabled = false
        binding.mBookSourceVerifyTextFieldLayout.error = null
        binding.mBookSourceVerifyLoading.isVisible = true
        binding.mBookSourceVerifyButton.isVisible = false
        controller.verifyRepository(url).observe(this, Observer { message ->
            binding.mBookSourceVerifyTextFieldLayout.isEnabled = true
            binding.mBookSourceVerifyLoading.isVisible = false
            binding.mBookSourceVerifyButton.isVisible = true
            if (message.isNotBlank()) {
                binding.mBookSourceVerifyTextFieldLayout.error = message
            } else {
                binding.mBookSourceVerifyTextField.text?.clear()
                binding.mBookSourceVerifyTextField.requestFocus()
                toast(R.string.booksource_verify_success, TOAST_TYPE_SUCCESS)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if(clipboard.hasPrimaryClip() && URLUtil.isNetworkUrl(clipboard.primaryClip?.getItemAt(0)?.text?.toString())) {
            binding.mBookSourceVerifyTextField.setText(clipboard.primaryClip?.getItemAt(0)?.text?.toString())
            binding.mBookSourceVerifyTextField.setSelection(binding.mBookSourceVerifyTextField.text?.length ?: 0)
        }
    }
}