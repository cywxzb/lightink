package cn.lightink.reader.transcode

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import cn.lightink.reader.transcode.ktx.encodeJson
import com.github.seven332.quickjs.android.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.mozilla.universalchardet.UniversalDetector
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.net.ssl.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object NetworkBridge {

    private val client by lazy { buildHttpClient() }

    /**
     * 执行异步请求同步返回结果
     * @param request 请求
     * @return response
     */
    suspend fun execute(request: Request) = suspendCoroutine<Response> {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                it.resume(response)
            }
        })
    }

    /**
     * 读取字节数组
     */
    fun get(url: String) = try {
        val headers = Headers.Builder()
        CookieManager.getInstance().getCookie(url)?.let { cookie -> headers.add("Cookie", cookie) }
        client.newCall(Request.Builder().url(url).headers(headers.build()).build())
            .execute().body?.bytes()
    } catch (e: Exception) {
        null
    }

    /**
     * 构建请求
     */
    fun request(
        method: Connection.Method,
        url: String,
        data: String?,
        headers: List<String>,
        unzipFilename: String?
    ): String {
        //构建Headers
        val headersBuilder = Headers.Builder()
        CookieManager.getInstance().getCookie(url)?.let { cookie ->
            headersBuilder.add("Cookie", cookie)
        }
        headers.forEach { header -> headersBuilder.add(header) }
        //构建请求参数
        var requestBody: RequestBody? = null
        if (data?.isNotBlank() == true) {
            requestBody = try {
                JSONObject(data)
                data.toRequestBody("application/json;charset=utf-8".toMediaType())
            } catch (e: JSONException) {
                data.toRequestBody("application/x-www-form-urlencoded;charset=utf-8".toMediaType())
            }
        }
        //构建请求
        val request = Request.Builder().url(url)
            .headers(headersBuilder.build())
            .method(method.name, requestBody)
            .build()
        val response = client.newCall(request).execute()
        printHeaders(response)
        return string(url, response, unzipFilename)
    }

    /**
     * 解析为文本
     */
    private fun string(url: String, response: Response, unzipFilename: String?): String {
        val body = response.body?.bytes() ?: return ""
        //解压缩
        val unzipBody = if (unzipFilename?.isNotBlank() == true) unzip(body, unzipFilename) else body
        //自动识别编码
        val charset = charset(response.header("Content-Type"), unzipBody) ?: Charset.defaultCharset()
        //存储Cookie
        response.headers("Set-Cookie").forEach { cookie ->
            CookieManager.getInstance().setCookie(url, cookie)
        }
        return String(unzipBody, charset)
    }

    /**
     * 解压缩
     */
    private fun unzip(bytes: ByteArray, filename: String): ByteArray {
        return try {
            ZipInputStream(bytes.inputStream()).use { zipInput ->
                var entry: ZipEntry?
                while (zipInput.nextEntry.also { entry = it } != null) {
                    if (entry?.name == filename) {
                        return zipInput.readBytes()
                    }
                }
            }
            bytes
        } catch (e: Exception) {
            bytes
        }
    }

    /**
     * 打印请求头
     */
    private fun printHeaders(response: Response) {
        Log.d("NetworkBridge", "Request: ${response.request.url}")
        Log.d("NetworkBridge", "Response: ${response.code}")
        response.headers.forEach { (name, value) ->
            Log.d("NetworkBridge", "$name: $value")
        }
    }

    /**
     * 字符集识别
     */
    private fun charset(contentType: String?, bytes: ByteArray): Charset? {
        //从Content-Type中识别
        contentType?.let {
            val charset = it.replace(".*charset=".toRegex(), "")
            if (charset != it) return Charset.forName(charset)
        }
        //从字节中识别
        return UniversalDetector(null).apply {
            handleData(bytes, 0, bytes.size)
            dataEnd()
        }.detectedCharset?.let { Charset.forName(it) }
    }

    /**
     * 构建OkHttp客户端
     */
    private fun buildHttpClient(): OkHttpClient {
        return buildTrustManager().let { trustManagers ->
            OkHttpClient.Builder()
                .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .hostnameVerifier { _, _ -> true }
                .sslSocketFactory(
                    buildSSLSocketFactory(trustManagers),
                    trustManagers[0] as X509TrustManager
                )
                .build()
        }
    }

    /**
     * 构建SSL工厂
     */
    private fun buildSSLSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory {
        val context = SSLContext.getInstance("SSL")
        context.init(null, trustManagers, SecureRandom())
        return context.socketFactory
    }

    /**
     * 构建X509信任管理者
     */
    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun buildTrustManager(): Array<TrustManager> {
        val x509TrustManager = @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        return arrayOf(x509TrustManager)
    }

    /**
     * 构建JavaScript环境
     */
    fun inject(context: JSContext, host: String, bookSource: String) {
        // 注入网络请求函数
        val requestFunction = context.createJSFunction { _, args ->
            val method = args[0].cast(JSString::class.java).string
            val url = args[1].cast(JSString::class.java).string
            val data = if (args.size > 2) args[2].cast(JSString::class.java).string else null
            val headers = if (args.size > 3) {
                args[3].cast(JSArray::class.java).map { it.cast(JSString::class.java).string }
            } else emptyList()
            val unzipFilename = if (args.size > 4) args[4].cast(JSString::class.java).string else null
            
            val result = request(
                Connection.Method.valueOf(method.uppercase()),
                url,
                data,
                headers,
                unzipFilename
            )
            context.createJSString(result)
        }
        context.getGlobalObject().setProperty("request", requestFunction)

        // 注入GET请求函数
        val getFunction = context.createJSFunction { _, args ->
            val url = args[0].cast(JSString::class.java).string
            val bytes = get(url)
            if (bytes != null) {
                context.createJSString(Base64.encodeToString(bytes, Base64.DEFAULT))
            } else {
                context.createJSNull()
            }
        }
        context.getGlobalObject().setProperty("get", getFunction)

        // 注入URL编码/解码函数
        val encodeFunction = context.createJSFunction { _, args ->
            val text = args[0].cast(JSString::class.java).string
            context.createJSString(URLEncoder.encode(text, "UTF-8"))
        }
        context.getGlobalObject().setProperty("encode", encodeFunction)

        val decodeFunction = context.createJSFunction { _, args ->
            val text = args[0].cast(JSString::class.java).string
            context.createJSString(URLDecoder.decode(text, "UTF-8"))
        }
        context.getGlobalObject().setProperty("decode", decodeFunction)

        // 注入书源内容
        context.evaluate(bookSource, "bookSource.js")
    }

    /**
     * 类型转换
     */
    fun castString(value: JSValue?): String? {
        return value?.cast(JSString::class.java)?.string
    }
}
