package cn.lightink.reader.ui.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.FragmentActivity
import cn.lightink.reader.R
import cn.lightink.reader.databinding.DialogSimpleBinding

class SimpleDialog(val activity: FragmentActivity, val content: String, val callback: (Boolean) -> Unit) : Dialog(activity) {

    private lateinit var binding: DialogSimpleBinding

    init {
        binding = DialogSimpleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mSimpleContent.text = content
        binding.mSimpleSubmit.setOnClickListener { callback.invoke(true).run { dismiss() } }
        binding.mSimpleCancel.setOnClickListener { callback.invoke(false).run { dismiss() } }
        setOnCancelListener { callback.invoke(false) }
    }

    override fun onStart() {
        super.onStart()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(-1, -2)
        window?.setDimAmount(0.6F)
    }
}