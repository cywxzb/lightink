package cn.lightink.reader.ui.reader.popup

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.TextPaint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.lightink.reader.R
import cn.lightink.reader.controller.ReaderController
import cn.lightink.reader.databinding.DialogReaderThemeBinding
import cn.lightink.reader.databinding.ItemThemeBinding
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.ktx.px
import cn.lightink.reader.ktx.startActivity
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.Theme
import cn.lightink.reader.module.*
import cn.lightink.reader.ui.base.PopupMenu
import cn.lightink.reader.ui.reader.ReaderActivity
import cn.lightink.reader.ui.reader.theme.ThemeEditorActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class ReaderThemeDialog(val context: FragmentActivity) : BottomSheetDialog(context, R.style.AppTheme_BottomSheet) {

    private val controller by lazy { ViewModelProvider(context)[ReaderController::class.java] }
    private lateinit var binding: DialogReaderThemeBinding
    private val size by lazy { (context.resources.displayMetrics.widthPixels - context.px(48)) / 2 }
    private val adapter = ListAdapter<Theme>(R.layout.item_theme) { item, theme -> onBindView(item, theme) }

    init {
        setContentView(R.layout.dialog_reader_theme)
        val contentView = findViewById<ViewGroup>(android.R.id.content)?.rootView
        if (contentView != null) {
            binding = DialogReaderThemeBinding.bind(contentView)
            setupViewTheme(controller.theme, controller.paint)
            binding.mTopbar.text = context.getString(if (UIModule.isNightMode(context)) R.string.theme_night else R.string.theme_light)
            binding.mTopbar.setNavigationOnClickListener { dismiss() }
            binding.mTopbar.setOnMenuClickListener { showPopup() }
            binding.mThemeRecycler.layoutManager = RVGridLayoutManager(context, 2)
            binding.mThemeRecycler.adapter = adapter
            binding.mThemeRecycler.post { binding.mThemeRecycler.minimumHeight = context.resources.displayMetrics.heightPixels / 2 - binding.mThemeRecycler.top }
            controller.queryThemes(!UIModule.isNightMode(context)).observe(context, Observer { adapter.submitList(it) })
        }
    }

    private fun showPopup() {
        PopupMenu(context).items( R.string.theme_new).gravity(Gravity.END).callback { item ->
            context.startActivity(ThemeEditorActivity::class)
        }.show(binding.mTopbar)
    }

    private fun setupViewTheme(theme: Theme, paint: TextPaint) {
        binding.mTopbar.parentView.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        binding.mTopIndicator.backgroundTintList = ColorStateList.valueOf(theme.secondary)
        binding.mTopbar.setTint(theme.content)
        binding.mTopbar.setTypeface(paint.typeface)
    }

    private fun onBindView(item: VH, theme: Theme) {
        val itemBinding = ItemThemeBinding.bind(item.itemView)
        itemBinding.mThemeCardView.setCardBackgroundColor(theme.background)
        itemBinding.mThemeBackground.layoutParams.height = (size * 1.3F).toInt()
        if (theme.mipmap.isNotBlank()) {
            itemBinding.mThemeBackground.background = UIModule.getMipmapByTheme(theme)
        } else {
            itemBinding.mThemeBackground.setBackgroundColor(theme.background)
        }
        itemBinding.mThemeForegroundDark.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        itemBinding.mThemeForegroundDark.updateLayoutParams<RelativeLayout.LayoutParams> {
            width = size
            height = size
            setMargins(0, 0, -size / 2, -size / 2)
        }
        itemBinding.mThemeForegroundLight.backgroundTintList = ColorStateList.valueOf(theme.foreground)
        itemBinding.mThemeForegroundLight.updateLayoutParams<RelativeLayout.LayoutParams> {
            width = (size * 1.5F).toInt()
            height = (size * 1.5F).toInt()
            setMargins(0, 0, (-size * 1.5F / 2).toInt(), (-size * 1.5F / 2).toInt())
        }
        itemBinding.mThemeName.setTextColor(theme.content)
        itemBinding.mThemeName.text = theme.name
        itemBinding.mThemeAuthor.setTextColor(theme.secondary)
        itemBinding.mThemeAuthor.text = if (theme.owner) "我" else theme.author
        itemBinding.mThemeTime.setTextColor(theme.secondary)
        itemBinding.mThemeTime.text = String.format("累计阅读%d分钟", theme.time / 60)
        itemBinding.mThemeCheckStatus.imageTintList = ColorStateList.valueOf(theme.background)
        itemBinding.mThemeCheckStatus.backgroundTintList = ColorStateList.valueOf(theme.control)
        itemBinding.mThemeCheckStatus.setImageResource(if (theme.id == controller.theme.id) R.drawable.ic_check_line else 0)
        itemBinding.mThemeMore.imageTintList = ColorStateList.valueOf(theme.secondary)
        itemBinding.mThemeMore.setOnClickListener { showPopupMenu(it, theme) }
        itemBinding.root.setOnClickListener {
            if (controller.theme.id != theme.id) {
                Preferences.put(if (UIModule.isNightMode(context)) Preferences.Key.THEME_NIGHT_ID else Preferences.Key.THEME_LIGHT_ID, theme.id)
                (context as ReaderActivity).recreate()
            }
        }
    }

    /**
     * 展示菜单
     */
    private fun showPopupMenu(view: View, theme: Theme) {
        if (theme.id < 2) {
            return context.toast("不允许删除默认主题")
        }
        PopupMenu(context).items(R.string.edit, R.string.delete).theme(controller.theme, Typeface.DEFAULT).callback { item ->
            if (item == R.string.edit) context.startActivityForResult(Intent(context, ThemeEditorActivity::class.java).putExtra(INTENT_THEME, theme.id), Activity.RESULT_FIRST_USER)
            else controller.removeTheme(theme)
        }.show(view)
    }

    override fun onStart() {
        super.onStart()
        window?.navigationBarColor = controller.theme.foreground
        window?.setLayout(-1, (context.resources.displayMetrics.heightPixels * 0.88).toInt())
        window?.setDimAmount(0.4F)
        window?.setGravity(Gravity.BOTTOM)
    }
}