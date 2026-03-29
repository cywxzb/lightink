package cn.lightink.reader.transcode

import android.util.Log
import cn.lightink.reader.transcode.entity.*
import java.io.File

/**
 * 转码器 - 暂时禁用，因为 QuickJS 库缺失
 * @property host 书源地址
 * @property bookSource 书源内容
 */
class JavaScriptTranscoder(private val host: String, private val bookSource: String) {

    /**
     * 书源信息
     */
    fun bookSource(): BookSourceInfo? {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return null
    }

    /**
     * 搜索
     */
    fun search(key: String): List<SearchResult>? {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return null
    }

    /**
     * 详情
     */
    fun detail(url: String): BookDetail? {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return null
    }

    /**
     * 目录
     */
    fun catalog(url: String): List<Chapter>? {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return null
    }

    /**
     * 章节
     */
    fun chapter(chapter: Chapter, output: File? = null): String {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return ""
    }

    /**
     * 登录
     */
    fun login(args: Array<String>): String {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return ""
    }

    /**
     * 检查登录
     */
    fun checkLogin(): Boolean {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return false
    }

    /**
     * 个人中心
     */
    fun user(): String {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return ""
    }

    /**
     * 排行榜
     */
    fun rank(): String {
        Log.w("JavaScriptTranscoder", "JS transcoder is disabled due to missing QuickJS library")
        return ""
    }
}
