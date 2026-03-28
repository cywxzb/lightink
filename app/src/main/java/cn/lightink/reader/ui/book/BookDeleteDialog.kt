package cn.lightink.reader.ui.book

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.FragmentActivity
import cn.lightink.reader.R
import cn.lightink.reader.databinding.DialogBookDeleteBinding

class BookDeleteDialog(val activity: FragmentActivity, val callback: (Boolean) -> Unit) : Dialog(activity) {

    private lateinit var binding: DialogBookDeleteBinding

    init {
        binding = DialogBookDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mBookDeleteSubmit.setOnClickListener {
            callback.invoke(binding.mBookDeleteCheck.isChecked)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(-1, -2)
        window?.setDimAmount(0.6F)
    }
}