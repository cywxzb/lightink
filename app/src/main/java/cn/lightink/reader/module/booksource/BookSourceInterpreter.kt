package cn.lightink.reader.module.booksource

import android.net.Uri
import android.webkit.CookieManager
import cn.lightink.reader.ktx.charset
import cn.lightink.reader.ktx.isJson
import cn.lightink.reader.ktx.md5
import cn.lightink.reader.module.EMPTY
import cn.lightink.reader.net.Http
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap

object BookSourceInterpreter {

    //Header
    private const val HEADER = "@header->"
    //Post
    private const val POST = "@post->"
    //缓存过期时间（毫秒）
    private const val CACHE_EXPIRY_TIME = 30 * 60 * 1000L // 30分钟
    
    //内存缓存
    private val memoryCache = ConcurrentHashMap<String, Pair<Long, BookSourceResponse>>()

    /**
     * 请求
     */
    fun execute(url: String, auth: BookSourceJson.Auth?): BookSourceResponse? {
        //生成缓存键
        val cacheKey = generateCacheKey(url, auth)
        
        //检查内存缓存
        val cachedResponse = getFromCache(cacheKey)
        if (cachedResponse != null) {
            return cachedResponse
        }
        
        //最多重试3次
        val maxRetries = 3
        var lastException: Exception? = null
        
        for (attempt in 1..maxRetries) {
            try {
                val request = buildRequest(url, auth) ?: return null
                val response = Http.client.newCall(request).execute()
                val bookSourceResponse = if (response.isSuccessful && response.body != null) {
                    onResponse(response.url, response.body!!.bytes(), response.header("Content-Type") ?: "text/plain")
                } else null
                
                //保存到缓存
                if (bookSourceResponse != null) {
                    saveToCache(cacheKey, bookSourceResponse)
                }
                
                return bookSourceResponse
            } catch (e: Exception) {
                lastException = e
                //重试前短暂延迟
                if (attempt < maxRetries) {
                    Thread.sleep(1000L * attempt) // 指数退避
                }
            }
        }
        
        //所有重试都失败
        return null
    }
    
    /**
     * 生成缓存键
     */
    private fun generateCacheKey(url: String, auth: BookSourceJson.Auth?): String {
        return (url + (auth?.cookie ?: "") + (auth?.params ?: "")).md5()
    }
    
    /**
     * 从缓存获取
     */
    private fun getFromCache(key: String): BookSourceResponse? {
        val (timestamp, response) = memoryCache[key] ?: return null
        if (System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME) {
            memoryCache.remove(key)
            return null
        }
        return response
    }
    
    /**
     * 保存到缓存
     */
    private fun saveToCache(key: String, response: BookSourceResponse) {
        //限制缓存大小
        if (memoryCache.size > 100) {
            //移除最旧的缓存
            val oldestKey = memoryCache.entries.minByOrNull { it.value.first }?.key
            oldestKey?.let { memoryCache.remove(it) }
        }
        memoryCache[key] = Pair(System.currentTimeMillis(), response)
    }

    /**
     * 响应
     */
    private fun onResponse(url: String, body: ByteArray, contentType: String): BookSourceResponse {
        var document = String(body, body.charset() ?: charset("UTF-8"))
        return when {
            contentType.contains("tar") -> BookSourceResponse(url, document.replace(Regex("""(info\.txt|\u0000).+\u0000"""), EMPTY).trim())
            contentType.contains(Regex("html|octet-stream|xml")) && !document.isJson() -> {
                try {
                    val head = Jsoup.parse(document).head()
                    var attr = head.selectFirst("meta[charset]")?.attr("charset")
                    if (attr.isNullOrBlank()) {
                        attr = Regex("(?<=charset=).+").find(head.selectFirst("meta[content*=charset]")!!.attr("content"))?.value
                    }
                    if (attr?.isNotBlank() == true) {
                        document = String(body, Charset.forName(attr))
                    }
                } catch (e: Exception) {
                    //忽略异常，不会有任何影响
                }
                BookSourceResponse(url, Jsoup.parse(document))
            }
            else -> BookSourceResponse(url, document)
        }
    }

    /**
     * 构建请求
     */
    private fun buildRequest(url: String, auth: BookSourceJson.Auth?): Request? {
        val headers = Headers.Builder()
        val cookies = parseCookies(auth)
        
        // 处理认证头
        if (!auth?.header.isNullOrBlank()) {
            val headersParams = processAuthHeaders(auth?.header.orEmpty(), cookies)
            if (headersParams == null) return null
            headersParams.split(HEADER).filter { it.isNotBlank() }.forEach { header -> headers.add(header) }
        }
        
        // 处理URL操作符
        val (host, requestBody) = processUrlOperators(url, headers)
        
        // 处理认证参数
        val authParams = processAuthParams(auth?.params.orEmpty(), cookies)
        
        // 构建请求
        return Request.Builder()
            .headers(headers.build())
            .url(buildUrl(host, authParams, requestBody.isNotBlank()))
            .method(getRequestMethod(requestBody), buildRequestBody(requestBody, authParams))
            .build()
    }
    
    /**
     * 解析Cookies
     */
    private fun parseCookies(auth: BookSourceJson.Auth?): Map<String, String> {
        val cookies = hashMapOf<String, String>()
        if (auth?.cookie != null) {
            CookieManager.getInstance().getCookie(auth.cookie)?.apply {
                split(";").filter { it.contains("=") }.forEach { cookie ->
                    val index = cookie.indexOf("=")
                    cookies[cookie.substring(0, index).trim()] = cookie.substring(index + 1).trim()
                }
            }
        }
        return cookies
    }
    
    /**
     * 处理认证头
     */
    private fun processAuthHeaders(headerParams: String, cookies: Map<String, String>): String? {
        var processedParams = headerParams
        var hasHeaders = true
        
        Regex("""(?<=\$\{).+?(?=\})""").findAll(headerParams).map { it.value }.toSet().forEach { variable ->
            hasHeaders = hasHeaders && cookies.contains(variable)
            processedParams = processedParams.replace("\${$variable}", cookies.getOrElse(variable) { EMPTY })
        }
        
        return if (hasHeaders) processedParams else null
    }
    
    /**
     * 处理URL操作符
     */
    private fun processUrlOperators(url: String, headers: Headers.Builder): Pair<String, StringBuilder> {
        val operators = Regex("@.+?->").findAll(url).toList()
        val host = if (operators.isNotEmpty()) url.substring(0, operators.first().range.first) else url
        val requestBody = StringBuilder()
        
        if (operators.isNotEmpty()) {
            operators.forEachIndexed { index, operator ->
                val endIndex = if (index < operators.lastIndex) operators[index + 1].range.first else url.length
                val params = url.substring(operator.range.last + 1, endIndex)
                when (operator.value) {
                    POST -> requestBody.append(params)
                    HEADER -> headers.add(params)
                }
            }
        }
        
        return Pair(host, requestBody)
    }
    
    /**
     * 处理认证参数
     */
    private fun processAuthParams(authParams: String, cookies: Map<String, String>): String {
        var processedParams = authParams
        
        Regex("""(?<=\$\{).+?(?=\})""").findAll(authParams).map { it.value }.toSet().forEach { variable ->
            processedParams = processedParams.replace("\${$variable}", cookies.getOrElse(variable) { EMPTY })
        }
        
        return processedParams
    }
    
    /**
     * 构建URL
     */
    private fun buildUrl(host: String, authParams: String, hasRequestBody: Boolean): String {
        if (!hasRequestBody && authParams.isNotBlank()) {
            return "$host${if (Uri.parse(host).query.isNullOrBlank()) "?" else "&"}$authParams"
        }
        return host
    }
    
    /**
     * 获取请求方法
     */
    private fun getRequestMethod(requestBody: StringBuilder): String {
        return if (requestBody.isNotBlank()) "POST" else "GET"
    }
    
    /**
     * 构建请求体
     */
    private fun buildRequestBody(requestBody: StringBuilder, authParams: String): okhttp3.RequestBody? {
        if (requestBody.isBlank()) return null
        
        val mediaType = if (requestBody.toString().isJson()) "application/json;charset=utf-8" else "application/x-www-form-urlencoded;charset=utf-8"
        if (mediaType != "application/json;charset=utf-8" && authParams.isNotBlank()) {
            requestBody.append(if (requestBody.isBlank()) authParams else "&$authParams")
        }
        
        return requestBody.toString().toRequestBody(mediaType.toMediaType())
    }

}