package cn.lightink.reader.ui.reader

import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.ReaderSettingController
import cn.lightink.reader.databinding.ActivityReaderFontBinding
import cn.lightink.reader.databinding.ItemFontBinding
import cn.lightink.reader.ktx.notifyItemAllChanged
import cn.lightink.reader.model.Font
import cn.lightink.reader.model.SystemFont
import cn.lightink.reader.module.FontModule
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.RVLinearLayoutManager
import cn.lightink.reader.module.VH
import cn.lightink.reader.ui.base.LifecycleActivity

class ReaderFontActivity : LifecycleActivity() {

    private val controller by lazy { ViewModelProvider(this)[ReaderSettingController::class.java] }
    private val systemAdapter by lazy { buildSystemFontAdapter() }
    private val adapter by lazy { buildFontAdapter() }
    private lateinit var binding: ActivityReaderFontBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderFontBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mSystemFontRecycler.layoutManager = RVLinearLayoutManager(this)
        binding.mSystemFontRecycler.adapter = systemAdapter.apply { submitList(listOf(SystemFont.System)) }
        binding.mReaderFontFolder.text = getString(R.string.reader_setting_font_fixed, getExternalFilesDir("fonts")?.absolutePath?.removePrefix(Environment.getExternalStorageDirectory().absolutePath))
        binding.mFontRecycler.layoutManager = RVLinearLayoutManager(this)
        binding.mFontRecycler.adapter = adapter
        controller.queryFont(getExternalFilesDir("fonts")).observe(this, Observer { list -> adapter.submitList(list) })
    }

    /**
     * 构建内置字体数据适配器
     */
    private fun buildSystemFontAdapter() = ListAdapter<SystemFont>(R.layout.item_font) { item, font ->
        val itemBinding = ItemFontBinding.bind(item.itemView)
        itemBinding.mFontDisplay.text = font.display
        itemBinding.mFontDisplay.typeface = if (font.demo.isBlank()) Typeface.DEFAULT else Typeface.createFromAsset(assets, font.demo)
        itemBinding.mFontUsing.isVisible = font.display == FontModule.mCurrentFont.display
        itemBinding.mFontInstall.isVisible = !itemBinding.mFontUsing.isVisible
        if (FontModule.isInstalled(font)) {
            itemBinding.mFontInstall.setText(R.string.use)
            itemBinding.mFontInstall.setOnClickListener { useFont(font) }
        } else {
            itemBinding.mFontInstall.setText(R.string.download)
            itemBinding.mFontInstall.setOnClickListener { download(item, font) }
        }
    }

    /**
     * 使用字体
     */
    private fun useFont(font: Any) {
        controller.useFont(font)
        binding.mFontRecycler.notifyItemAllChanged()
        binding.mSystemFontRecycler.notifyItemAllChanged()
    }

    /**
     * 下载内置字体
     */
    private fun download(item: VH, font: SystemFont) {
        controller.downloadFont(font).observe(this, Observer { result ->
            val itemBinding = ItemFontBinding.bind(item.itemView)
            itemBinding.mFontInstallLoading.isVisible = result
            itemBinding.mFontInstall.isVisible = !result
            if (!result) systemAdapter.notifyItemChanged(item.adapterPosition)
        })
    }

    /**
     * 构建外置字体数据适配器
     */
    private fun buildFontAdapter() = ListAdapter<Font>(R.layout.item_font) { item, font ->
        val itemBinding = ItemFontBinding.bind(item.itemView)
        itemBinding.mFontDisplay.text = font.display
        itemBinding.mFontDisplay.typeface = font.typeface
        itemBinding.mFontUsing.isVisible = font == FontModule.mCurrentFont
        itemBinding.mFontInstall.isVisible = font != FontModule.mCurrentFont
        itemBinding.mFontInstall.setOnClickListener { useFont(font) }
    }
}