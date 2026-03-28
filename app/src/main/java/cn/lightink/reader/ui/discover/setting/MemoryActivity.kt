package cn.lightink.reader.ui.discover.setting

import android.content.Context
import android.os.Bundle
import cn.lightink.reader.R
import cn.lightink.reader.databinding.ActivityMemoryBinding
import cn.lightink.reader.ktx.parentView
import cn.lightink.reader.ktx.total
import cn.lightink.reader.ui.base.LifecycleActivity

class MemoryActivity : LifecycleActivity() {

    private lateinit var binding: ActivityMemoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //数据库
        binding.mMemoryDatabaseData.text = format(getDatabasePath("canary").parentFile?.total())
        //动态链接库
        binding.mMemoryLibsData.text = format(getDir("libs", Context.MODE_PRIVATE).total())
        //图书数据
        binding.mMemoryBookData.text = format(getDir("book", Context.MODE_PRIVATE).total())
        //图片缓存
        binding.mMemoryCacheData.text = format(cacheDir.total())
        binding.mMemoryCacheData.parentView.setOnClickListener {
            cacheDir.listFiles()?.forEach { file -> file.deleteRecursively() }
            binding.mMemoryCacheData.text = format(cacheDir.total())
        }
        //系统字体
        binding.mMemorySystemFontData.text = format(getDir("font", Context.MODE_PRIVATE).total())
        binding.mMemorySystemFontData.parentView.setOnClickListener {
            getDir("font", Context.MODE_PRIVATE).listFiles()?.forEach { file -> file.deleteRecursively() }
            binding.mMemorySystemFontData.text = format(getDir("font", Context.MODE_PRIVATE).total())
        }
        //字体
        binding.mMemoryFontData.text = format(getExternalFilesDir("fonts").total())
        //图书
        binding.mMemoryFileData.text = format(getExternalFilesDir("books").total())
    }

    /**
     * 格式化文件体积
     */
    private fun format(total: Long?) = when {
        total == null -> "0B"
        //Byte
        total < 1000 -> "${total}B"
        //KB
        total < 1000 * 1024 -> "${String.format("%.1f", total / 1000F)}KB"
        //MB
        else -> "${String.format("%.1f", total / 1024000F)}MB"
    }

}