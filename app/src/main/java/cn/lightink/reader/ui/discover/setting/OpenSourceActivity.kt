package cn.lightink.reader.ui.discover.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import cn.lightink.reader.R
import cn.lightink.reader.databinding.ActivityOpenSourceBinding
import cn.lightink.reader.databinding.ItemOpenSourceBinding
import cn.lightink.reader.ktx.toast
import cn.lightink.reader.model.OpenSource
import cn.lightink.reader.module.ListAdapter
import cn.lightink.reader.module.RVLinearLayoutManager
import cn.lightink.reader.ui.base.LifecycleActivity
import cn.lightink.reader.widget.VerticalDividerItemDecoration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OpenSourceActivity : LifecycleActivity() {

    private lateinit var binding: ActivityOpenSourceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenSourceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mOpenSourceRecycler.layoutManager = RVLinearLayoutManager(this)
        binding.mOpenSourceRecycler.addItemDecoration(VerticalDividerItemDecoration(this, R.dimen.margin))
        binding.mOpenSourceRecycler.adapter = buildAdapter().apply {
            submitList(Gson().fromJson(String(assets.open("license").readBytes()), object : TypeToken<List<OpenSource>>() {}.type))
        }
    }

    private fun buildAdapter() = ListAdapter<OpenSource>(R.layout.item_open_source) { item, openSource ->
        val binding = ItemOpenSourceBinding.bind(item.view)
        binding.mOpenSourceName.text = openSource.name
        binding.mOpenSourceLicense.text = openSource.license
        item.view.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(openSource.link)))
            } catch (e: Exception) {
                toast("未安装浏览器")
            }
        }
    }

}