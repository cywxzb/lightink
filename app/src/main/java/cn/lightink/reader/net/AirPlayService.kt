package cn.lightink.reader.net

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import androidx.lifecycle.MutableLiveData
import cn.lightink.reader.ktx.encode
import cn.lightink.reader.ktx.fromJson
import cn.lightink.reader.ktx.md5
import cn.lightink.reader.ktx.toJson
import cn.lightink.reader.model.BookRank
import cn.lightink.reader.model.BookSource
import cn.lightink.reader.model.Chapter
import cn.lightink.reader.module.*
import cn.lightink.reader.module.booksource.BookSourceJson
import cn.lightink.reader.module.booksource.BookSourceParser
import cn.lightink.reader.module.booksource.BookSourceResponse
import cn.lightink.reader.module.booksource.SearchMetadata
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AirPlayService : Service() {

    private val hostLiveData = MutableLiveData<String>()
    private val binder = AirPlayBinder()
    private var bookSourceParser: BookSourceParser? = null
    private var server: ApplicationEngine? = null

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onCreate() {
        super.onCreate()
        startServer()
        startForeground(NotificationHelper.AIR_PLAY, NotificationHelper.airPlay(applicationContext))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }

    private fun startServer() {
        val address = IPAddress.getLocalIPAddress()?.hostAddress ?: "127.0.0.1"
        val port = 8888

        server = embeddedServer(Netty, port = port, host = address) {
            install(ContentNegotiation) {
                json()
            }
            install(CORS) {
                anyHost()
                allowHeaders { true }
                allowMethods(HttpMethod.Get, HttpMethod.Post, HttpMethod.Put, HttpMethod.Delete, HttpMethod.Options)
            }
            install(DefaultHeaders) {
                header("Server", "lightink")
            }
            routing {
                // API endpoints
                get("/api/bookshelves") {
                    val bookshelves = Room.bookshelf().getAllImmediately().map {
                        mapOf(
                            "id" to it.id,
                            "name" to it.name,
                            "current" to (it.id == Preferences.get(Preferences.Key.BOOKSHELF, 1L))
                        )
                    }
                    call.respondText(bookshelves.toJson(), ContentType.Application.Json)
                }
                get("/api/books") {
                    val bookshelf = call.parameters["bookshelf"]?.toLongOrNull() ?: 0
                    val books = Room.book().getAll(bookshelf).map {
                        mapOf(
                            "id" to it.objectId,
                            "name" to it.name,
                            "author" to it.author,
                            "cover" to "/cover/${it.objectId}",
                            "updatedAt" to it.updatedAt
                        )
                    }
                    call.respondText(books.toJson(), ContentType.Application.Json)
                }
                get("/api/book") {
                    val objectId = call.parameters["id"] ?: return@get call.respondText("Missing id parameter", status = HttpStatusCode.BadRequest)
                    val book = Room.book().get(objectId)
                    val bookData = mapOf(
                        "id" to book.objectId,
                        "name" to book.name,
                        "author" to book.author,
                        "cover" to "/cover/${book.objectId}",
                        "bookshelf" to book.bookshelf,
                        "chapter" to book.chapter,
                        "chapterName" to book.chapterName,
                        "chapterTotal" to book.catalog,
                        "speed" to book.speed,
                        "state" to book.state,
                        "time" to book.time,
                        "updatedAt" to book.updatedAt
                    )
                    call.respondText(bookData.toJson(), ContentType.Application.Json)
                }
                get("/api/book/catalog") {
                    val objectId = call.parameters["id"] ?: return@get call.respondText("Missing id parameter", status = HttpStatusCode.BadRequest)
                    val book = Room.book().get(objectId)
                    val catalog = File(book.path, MP_FILENAME_CATALOG).readLines().mapIndexed { i, line -> Chapter(i, line) }
                    call.respondText(catalog.toJson(), ContentType.Application.Json)
                }
                get("/api/book/chapter") {
                    val objectId = call.parameters["id"] ?: return@get call.respondText("Missing id parameter", status = HttpStatusCode.BadRequest)
                    val href = call.parameters["href"] ?: return@get call.respondText("Missing href parameter", status = HttpStatusCode.BadRequest)
                    val book = Room.book().get(objectId)
                    val markdown = File(book.path, "$MP_FOLDER_TEXTS/${href.encode().let { if (it.length < 0xFF) it else it.md5() }}.md")
                    val content = when {
                        markdown.exists() -> markdown.readText()
                        book.hasBookSource() -> when (val bookSource = book.getBookSource()) {
                            is BookSourceParser -> bookSource.findContent(href, "${book.path}/$MP_FOLDER_IMAGES")
                            else -> EMPTY
                        }
                        else -> EMPTY
                    }
                    // Cache network data
                    if (markdown.parentFile?.exists() == true && !markdown.exists() && content.isNotBlank() && content != GET_FAILED_NET_THROWABLE) {
                        markdown.writeText(content)
                    }
                    val cleanedContent = content
                        .replace("""\n+\s+\n+""".toRegex(), "\n\n")
                        .replace("""\n+""".toRegex(), "\n")
                    call.respondText(mapOf("content" to cleanedContent).toJson(), ContentType.Application.Json)
                }
                get("/cover/{id}") {
                    val objectId = call.parameters["id"] ?: return@get call.respondText("Missing id parameter", status = HttpStatusCode.BadRequest)
                    val book = Room.book().get(objectId)
                    val coverFile = File(book.cover)
                    if (coverFile.exists()) {
                        call.respondFile(coverFile)
                    } else {
                        call.respondText("Cover not found", status = HttpStatusCode.NotFound)
                    }
                }
                post("/dev/debug") {
                    val json = call.receiveText()
                    try {
                        val bookSource = json.fromJson<BookSourceJson>()
                        bookSourceParser = BookSourceParser(BookSource(
                            0,
                            bookSource.name,
                            bookSource.url,
                            bookSource.version,
                            !bookSource.rank.isNullOrEmpty(),
                            bookSource.auth != null,
                            bookSource.url,
                            "json",
                            bookSource.toJson()
                        ))
                        call.respondText(mapOf("message" to "调试书源已更新").toJson(true), ContentType.Application.Json)
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to "书源格式错误").toJson(true), ContentType.Application.Json)
                    }
                }
                post("/dev/install") {
                    val json = call.receiveText()
                    try {
                        val bookSource = json.fromJson<BookSourceJson>()
                        bookSourceParser = BookSourceParser(BookSource(
                            0,
                            bookSource.name,
                            bookSource.url,
                            bookSource.version,
                            !bookSource.rank.isNullOrEmpty(),
                            bookSource.auth != null,
                            bookSource.url,
                            "json",
                            bookSource.toJson()
                        ))
                        val result = when {
                            bookSource.name.isBlank() -> mapOf("message" to "书源未命名")
                            Room.bookSource().isInstalled(bookSource.url) -> mapOf("message" to "书源已安装，无法覆盖")
                            else -> {
                                Room.bookSource().install(BookSource(0, bookSource.name, bookSource.url, bookSource.version, !bookSource.rank.isNullOrEmpty(), bookSource.auth != null, EMPTY, "json", json))
                                // Support installing JS book sources
                                if (!bookSource.rank.isNullOrEmpty()) {
                                    Room.bookRank().insert(BookRank(bookSource.url, bookSource.name))
                                }
                                mapOf("message" to "书源已安装")
                            }
                        }
                        call.respondText(result.toJson(true), ContentType.Application.Json)
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to "格式错误：${e.message}").toJson(true), ContentType.Application.Json)
                    }
                }
                get("/dev/debug/auto") {
                    val key = call.parameters["key"] ?: return@get call.respondText("Missing key parameter", status = HttpStatusCode.BadRequest)
                    try {
                        if (bookSourceParser != null) {
                            val search = bookSourceParser?.search(key)
                            if (search.isNullOrEmpty()) {
                                call.respondText(mapOf("search" to "无结果").toJson(true), ContentType.Application.Json)
                            } else {
                                val detail = bookSourceParser!!.findDetail(search.first())?.apply { if (catalog is BookSourceResponse) catalog = search.first().detail }
                                if (detail == null) {
                                    call.respondText(mapOf("search" to search.first(), "detail" to "无结果").toJson(true), ContentType.Application.Json)
                                } else {
                                    val catalog = bookSourceParser!!.findCatalog(detail)
                                    if (catalog.isEmpty()) {
                                        call.respondText(mapOf("detail" to detail, "catalog" to "无结果").toJson(true), ContentType.Application.Json)
                                    } else {
                                        val chapter = catalog[minOf(5, catalog.size - 1)]
                                        val content = bookSourceParser!!.findContent(chapter.name, chapter.url)
                                        call.respondText(mapOf("detail" to detail, "chapter" to chapter, "content" to content).toJson(true), ContentType.Application.Json)
                                    }
                                }
                            }
                        } else {
                            call.respondText(mapOf("message" to "请先调用/dev/debug设置调试书源").toJson(true), ContentType.Application.Json)
                        }
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to e.toString()).toJson(true), ContentType.Application.Json)
                    }
                }
                get("/dev/debug/search") {
                    val key = call.parameters["key"] ?: return@get call.respondText("Missing key parameter", status = HttpStatusCode.BadRequest)
                    try {
                        if (bookSourceParser != null) {
                            val searchResults = bookSourceParser?.search(key)
                            call.respondText(searchResults?.toJson(true) ?: "[]", ContentType.Application.Json)
                        } else {
                            call.respondText(mapOf("message" to "请先调用/dev/debug设置调试书源").toJson(true), ContentType.Application.Json)
                        }
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to e.toString()).toJson(true), ContentType.Application.Json)
                    }
                }
                post("/dev/debug/detail") {
                    val json = call.receiveText()
                    try {
                        if (bookSourceParser != null) {
                            val book = json.fromJson<SearchMetadata>()
                            val detail = bookSourceParser?.findDetail(book)?.apply {
                                if (catalog is BookSourceResponse) catalog = book.detail
                            }
                            call.respondText(detail?.toJson(true) ?: "{}", ContentType.Application.Json)
                        } else {
                            call.respondText(mapOf("message" to "请先调用/dev/debug设置调试书源").toJson(true), ContentType.Application.Json)
                        }
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to e.toString()).toJson(true), ContentType.Application.Json)
                    }
                }
                post("/dev/debug/catalog") {
                    val json = call.receiveText()
                    try {
                        if (bookSourceParser != null) {
                            val catalog = bookSourceParser?.findCatalog(json.fromJson())
                            call.respondText(catalog?.toJson(true) ?: "[]", ContentType.Application.Json)
                        } else {
                            call.respondText(mapOf("message" to "请先调用/dev/debug设置调试书源").toJson(true), ContentType.Application.Json)
                        }
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to e.toString()).toJson(true), ContentType.Application.Json)
                    }
                }
                post("/dev/debug/chapter") {
                    val json = call.receiveText()
                    try {
                        if (bookSourceParser != null) {
                            val chapter = json.fromJson<cn.lightink.reader.module.booksource.Chapter>()
                            val content = bookSourceParser?.findContent("", chapter.url)
                            call.respondText(mapOf("content" to content).toJson(true), ContentType.Application.Json)
                        } else {
                            call.respondText(mapOf("message" to "请先调用/dev/debug设置调试书源").toJson(true), ContentType.Application.Json)
                        }
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to e.toString()).toJson(true), ContentType.Application.Json)
                    }
                }
                get("/dev/debug/rank") {
                    try {
                        if (bookSourceParser != null) {
                            if (bookSourceParser?.bookSource?.json?.rank?.isNotEmpty() == true) {
                                val results = mutableMapOf<String, List<SearchMetadata>>()
                                bookSourceParser!!.bookSource.json.rank.forEach { rank ->
                                    var url = rank.url
                                    if (rank.page > -1) url = url.replace("\${page}", rank.page.toString())
                                    if (rank.categories.isNotEmpty()) url = url.replace("\${key}", rank.categories.first().key)
                                    results[rank.title] = bookSourceParser!!.queryRank(url, rank)
                                }
                                call.respondText(results.toJson(true), ContentType.Application.Json)
                            } else {
                                call.respondText(mapOf("message" to "正在调试的书源没有排行榜").toJson(true), ContentType.Application.Json)
                            }
                        } else {
                            call.respondText(mapOf("message" to "请先调用/dev/debug设置调试书源").toJson(true), ContentType.Application.Json)
                        }
                    } catch (e: Exception) {
                        call.respondText(mapOf("message" to e.toString()).toJson(true), ContentType.Application.Json)
                    }
                }

            }
        }

        server?.start(wait = false)
        hostLiveData.postValue("http://$address:$port")
    }

    private fun stopServer() {
        server?.stop(1000, 5000)
        server = null
    }

    // Start server
    fun start() {
        startServer()
    }

    /***********************************************************************************************
     * BINDER
     ***********************************************************************************************
     * Communication
     */
    inner class AirPlayBinder : Binder() {

        fun hostLiveData() = hostLiveData

        fun stop() {
            stopSelf()
        }
    }

}

object IPAddress {

    private val IPV4_PATTERN = Pattern.compile("^([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

    fun getLocalIPAddress(): InetAddress? {
        val enumeration = NetworkInterface.getNetworkInterfaces() ?: return null
        while (enumeration.hasMoreElements()) {
            val nif = enumeration.nextElement()
            val addresses = nif.inetAddresses ?: continue
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && IPV4_PATTERN.matcher(address.hostAddress).matches()) {
                    return address
                }
            }
        }
        return null
    }
}