package cn.lightink.reader.ui.reader.theme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import cn.lightink.reader.MIPMAP_PATH
import cn.lightink.reader.R
import cn.lightink.reader.controller.ThemeController
import cn.lightink.reader.databinding.ActivityThemeEditorBinding
import cn.lightink.reader.databinding.ItemThemeActionBinding
import cn.lightink.reader.ktx.change
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.ktx.px
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.module.*
import cn.lightink.reader.ui.base.LifecycleActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.navigationBarHeight
import java.io.File

class ThemeEditorActivity : LifecycleActivity() {

    private val inputMethodManager by lazy { applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager  }
    private val controller by lazy { ViewModelProvider(this)[ThemeController::class.java] }
    private val actions = listOf(R.string.theme_background, R.string.theme_foreground, R.string.theme_content, R.string.theme_secondary, R.string.theme_control, R.string.theme_horizontal, R.string.theme_top, R.string.theme_bottom)
    private val binding by lazy { ActivityThemeEditorBinding.inflate(layoutInflater) }
    private val adapter = ListAdapter<Int>(R.layout.item_theme_action) { item, title -> onBindView(item, title) }
    private val behavior by lazy { BottomSheetBehavior.from(binding.mThemeMenuLayout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        controller.setupTheme(intent.getLongExtra(INTENT_THEME, -1), UIModule.isNightMode(this))
        binding.mThemeMenuRecycler.layoutManager = RVGridLayoutManager(this, 4)
        binding.mThemeMenuRecycler.adapter = adapter.apply { submitList(actions) }
        binding.mThemeEditorLayout.setPadding(px(controller.theme.horizontal), px(controller.theme.top), px(controller.theme.horizontal), px(controller.theme.bottom))
        binding.mThemeEditorContent.text = getString(R.string.theme_editor_content)
        binding.mThemeEditorContent.textSize = px(17).toFloat()
        binding.mThemeEditorContent.lineSpacing = 1.3F
        binding.mThemeMenuLayout.parentView.setOnClickListener { showOrHideMenu() }
        binding.mThemeEditorPicker.setOnClickListener { pickPicture() }
        binding.mThemeMenuLayout.post { binding.mThemeMenuLayout.setPadding(0, 0, 0, getRealNavigationBarHeight()) }
        binding.mThemeEditorSubmit.setOnClickListener { submit() }
        //主题名
        binding.mThemeEditorNameInput.change { text -> controller.theme.name = text.trim() }
        binding.mThemeEditorNameInput.setText(controller.theme.name)
        binding.mThemeEditorNameInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) v.clearFocus()
            return@setOnEditorActionListener false
        }
        //更新主题配色
        updateViewTheme()
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_BAR)
            statusBarDarkFont(true)
            navigationBarDarkIcon(true)
        }
        behavior.state = STATE_HIDDEN
        showOrHideMenu()
    }

    private fun updateViewTheme() {
        if (controller.theme.mipmap.isBlank()) {
            binding.mThemeEditorLayout.setBackgroundColor(controller.theme.background)
        } else {
            binding.mThemeEditorLayout.background = UIModule.getMipmapByTheme(controller.theme)
        }
        binding.mThemeEditorPicker.setTextColor(controller.theme.content)
        binding.mThemeEditorPicker.compoundDrawableTintList = ColorStateList.valueOf(controller.theme.content)
        binding.mThemeMenuLayout.backgroundTintList = ColorStateList.valueOf(controller.theme.foreground)
        binding.mThemeEditorSubmit.setTextColor(ColorStateList(arrayOf(arrayOf(android.R.attr.state_enabled).toIntArray(), IntArray(0)), arrayOf(controller.theme.control, controller.theme.secondary).toIntArray()))
        binding.mThemeEditorTitle.setTextColor(controller.theme.content)
        binding.mThemeEditorChapter.setTextColor(controller.theme.secondary)
        binding.mThemeEditorChapterTime.setTextColor(controller.theme.secondary)
        binding.mThemeEditorChapterSchedule.setTextColor(controller.theme.secondary)
        binding.mThemeEditorContent.textColor = controller.theme.content
        binding.mThemeEditorContent.invalidate()
        binding.mThemeEditorNameInput.setTextColor(controller.theme.content)
        binding.mThemeEditorNameInput.setHintTextColor(controller.theme.secondary)
        binding.mThemeMenuTopLine.setBackgroundColor(controller.theme.content)
        binding.mThemeMenuBottomLine.setBackgroundColor(controller.theme.content)
        immersionBar { navigationBarColorInt(controller.theme.foreground) }
        adapter.notifyItemRangeChanged(0, actions.size)
    }

    private fun showOrHideMenu() {
        if (behavior.state == STATE_HIDDEN) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            immersionBar {
                hideBar(BarHide.FLAG_SHOW_BAR)
                transparentNavigationBar()
                navigationBarColorInt(controller.theme.foreground)
            }
        } else {
            currentFocus?.run { inputMethodManager.hideSoftInputFromWindow(windowToken, 0) }
            behavior.state = STATE_HIDDEN
            immersionBar { hideBar(BarHide.FLAG_HIDE_BAR) }
        }
    }

    /**
     * 保存主题
     */
    private fun submit() {
        if (binding.mThemeEditorNameInput.text.isNullOrBlank()) return toast("未填写主题名")
        val result = controller.saveTheme()
        if (result.isNotBlank()) {
            toast(result)
        } else {
            onBackPressed()
        }
    }

    private fun onBindView(item: VH, titleResId: Int) {
        val itemBinding = ItemThemeActionBinding.bind(item.itemView)
        itemBinding.mThemeActionValue.parentView.backgroundTintList = ColorStateList.valueOf(controller.theme.background)
        itemBinding.mThemeActionValue.backgroundTintList = ColorStateList.valueOf(controller.theme.getColorByName(titleResId))
        itemBinding.mThemeActionValue.text = controller.theme.getValueByName(titleResId).let { if (it > 0) it.toString() else EMPTY }
        itemBinding.mThemeActionValue.setTextColor(controller.theme.content)
        itemBinding.mThemeActionTitle.setText(titleResId)
        itemBinding.mThemeActionTitle.setTextColor(controller.theme.content)
        itemBinding.mThemeActionValue.parentView.setOnClickListener { if (controller.theme.getValueByName(titleResId) == 0) showColorPicker(titleResId) else showDistanceSeek(titleResId) }
    }

    private fun showDistanceSeek(titleResId: Int) {
        showOrHideMenu()
        ThemeDistancePopup(this, controller.theme.getValueByName(titleResId)) { value ->
            controller.theme.setValueByName(titleResId, value)
            binding.mThemeEditorLayout.setPadding(px(controller.theme.horizontal), px(controller.theme.top), px(controller.theme.horizontal), px(controller.theme.bottom))
            adapter.notifyItemRangeChanged(0, actions.size)
        }.apply {
            setOnDismissListener { showOrHideMenu() }
        }.showAtLocation(window.decorView, Gravity.BOTTOM, 0, 0)
    }

    private fun showColorPicker(titleResId: Int) {
        showOrHideMenu()
        ThemeColorPickerPopup(this, controller.theme.getColorByName(titleResId)) { color ->
            controller.theme.setValueByName(titleResId, color)
            updateViewTheme()
        }.apply {
            setOnDismissListener { showOrHideMenu() }
        }.showAtLocation(window.decorView, Gravity.BOTTOM, 0, 0)
    }

    /**
     * 选择图片
     */
    private fun pickPicture() {
        try {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Activity.RESULT_FIRST_USER)
        } catch (e: Exception) {
            //抛出异常
        }
    }

    private fun getRealNavigationBarHeight() = if (Preferences.get(Preferences.Key.HAS_NAVIGATION, false)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.rootWindowInsets.systemWindowInsetBottom
        } else {
            navigationBarHeight
        }
    } else 0

    /**
     * 处理图片
     */
    private fun onPickPicture(uri: Uri) {
        val document = DocumentFile.fromSingleUri(this, uri) ?: return
        val bytes = contentResolver.openInputStream(document.uri).use { it?.readBytes() } ?: return
        val cache = File(MIPMAP_PATH, "mipmap").apply { writeBytes(bytes) }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Palette.from(bitmap).generate { palette ->
            palette?.run {
                controller.updateThemeByPalette(cache, this)
                updateViewTheme()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Activity.RESULT_FIRST_USER && resultCode == Activity.RESULT_OK && data?.data != null) {
            onPickPicture(data.data!!)
        }
    }

    override fun onBackPressed() {
        if (controller.theme.id == UIModule.getConfiguredTheme(this).id) {
            //修改正在使用的主题
            setResult(READER_RESULT_RESTART)
        }
        super.onBackPressed()
    }
}