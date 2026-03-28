package cn.lightink.reader.ui.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import cn.lightink.reader.R
import cn.lightink.reader.databinding.DialogWarningMessageBinding

class WarningMessageDialog(context: Context, message: String) : Dialog(context) {

    private lateinit var binding: DialogWarningMessageBinding

    init {
        binding = DialogWarningMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mWarningMessageContent.text = message
        binding.mWarningMessageCancel.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(-1, -2)
        window?.setDimAmount(0.6F)
    }

}