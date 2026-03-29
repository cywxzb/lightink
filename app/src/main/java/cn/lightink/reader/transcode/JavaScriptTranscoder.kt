package cn.lightink.reader.transcode

import android.util.Log
import cn.lightink.reader.transcode.NetworkBridge.castString
import cn.lightink.reader.transcode.entity.*
import cn.lightink.reader.transcode.ktx.decodeJson
import com.github.seven332.quickjs.android.*
import java.io.File

/**
 * 转码器
 * @property host 书源地址
 * @property bookSource 书源内容
 */
class JavaScriptTranscoder(private val host: String, private val bookSource: String) {

    private val filename = "run.js"
    private val quickJS = QuickJS.Builder().build()

    /**
     * 书源信息
     */
    fun bookSource(): BookSourceInfo? {
        return javaScript { context ->
            try {
                val json = context.getGlobalObject().getProperty("bookSource").cast(JSString::class.java).string
                return@javaScript json.decodeJson<BookSourceInfo>()
            } catch (e: Exception) {
                Log.e("JavaScriptTranscoder", "", e)
                null
            }
        }
    }

    /**
     * 搜索
     */
    fun search(key: String): List<SearchResult>? {
        return javaScript { context ->
            try {
                val response = context.evaluate("search('$key');", filename, String::class.java)
                val results = response.decodeJson<List<SearchResult>>()
                return@javaScript results.filter {
                    it.author.isNotBlank() && (it.name.contains(key) || it.author.contains(key))
                }
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "search error, key: $key, bookSource: $host", e)
                return@javaScript null
            }
        }
    }

    /**
     * 详情
     */
    fun detail(url: String): BookDetail? {
        return javaScript { context ->
            try {
                val response = context.evaluate("detail('$url');", filename, String::class.java)
                return@javaScript response.decodeJson<BookDetail>()
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "detail error, url: $url, bookSource: $host", e)
                return@javaScript null
            }
        }
    }

    /**
     * 目录
     */
    fun catalog(url: String): List<Chapter>? {
        return javaScript { context ->
            try {
                val response = context.evaluate("catalog('$url');", filename, String::class.java)
                return@javaScript response.decodeJson<List<Chapter>>()
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "catalog error, url: $url, bookSource: $host", e)
                return@javaScript null
            }
        }
    }

    /**
     * 章节
     */
    fun chapter(chapter: Chapter, output: File? = null): String {
        return javaScript { context ->
            try {
                if (chapter.url.isBlank()) return@javaScript ""
                val response = context.evaluate("chapter('${chapter.url}');", filename, JSValue::class.java)
                    .castString()
                val markdown = ContentParser.read(chapter.url, response, output)
                if (markdown.isNotBlank()) markdown else ""
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "chapter content error, chapter: $chapter, bookSource: $host", e)
                return@javaScript e.message ?: ""
            }
        }
    }

    /**
     * 登录
     */
    fun login(args: Array<String>): String {
        return javaScript { context ->
            val array = StringBuilder()
            args.forEachIndexed { index, arg ->
                if (index > 0) array.append(",")
                array.append("'$arg'")
            }
            try {
                context.evaluate("login(${array});", filename, String::class.java)
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "login error, args: ${args.contentToString()}, bookSource: $host", e)
                return@javaScript ""
            }
        }
    }

    /**
     * 检查登录
     */
    fun checkLogin(): Boolean {
        return javaScript { context ->
            try {
                context.evaluate("checkLogin();", filename, Boolean::class.java)
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "checkLogin error, bookSource: $host", e)
                return@javaScript false
            }
        }
    }

    /**
     * 个人中心
     */
    fun user(): String {
        return javaScript { context ->
            try {
                context.evaluate("user();", filename, String::class.java)
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "user error, bookSource: $host", e)
                return@javaScript ""
            }
        }
    }

    /**
     * 排行榜
     */
    fun rank(): String {
        return javaScript { context ->
            try {
                context.evaluate("rank();", filename, String::class.java)
            } catch (e: Exception) {
                Log.w("JavaScriptTranscoder", "rank error, bookSource: $host", e)
                return@javaScript ""
            }
        }
    }

    /**
     * 执行 JavaScript
     */
    private fun <T> javaScript(block: (JSContext) -> T): T {
        val runtime = quickJS.createJSRuntime()
        runtime.use {
            val context = runtime.createJSContext()
            context.use {
                NetworkBridge.inject(context, host, bookSource)
                return block(context)
            }
        }
    }
}
