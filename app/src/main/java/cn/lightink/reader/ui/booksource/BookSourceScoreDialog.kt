package cn.lightink.reader.ui.booksource

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import cn.lightink.reader.R
import cn.lightink.reader.databinding.DialogBooksourceScoreBinding
import cn.lightink.reader.model.BookSource

class BookSourceScoreDialog(context: Context, val bookSource: BookSource) : Dialog(context) {

    private var callback: ((Float) -> Unit)? = null
    private lateinit var binding: DialogBooksourceScoreBinding

    init {
        binding = DialogBooksourceScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mBookSourceScoreRating.setOnRatingBarChangeListener { _, rating, _ -> binding.mBookSourceScoreSubmit.isEnabled = rating > 0F }
        binding.mBookSourceScoreSubmit.setOnClickListener {
            callback?.invoke(binding.mBookSourceScoreRating.rating)
            dismiss()
        }
    }

    fun callback(callback: (Float) -> Unit): BookSourceScoreDialog {
        this.callback = callback
        return this
    }

    override fun onStart() {
        super.onStart()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(-1, -2)
        window?.setDimAmount(0.6F)
    }

}