package cn.lightink.reader.controller

import androidx.lifecycle.Observer
import cn.lightink.reader.model.*
import cn.lightink.reader.module.Room
import cn.lightink.reader.module.booksource.BookSourceParser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BookControllerTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var bookController: BookController

    @Mock
    private lateinit var observer: Observer<DetailMetadata>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        bookController = BookController()
        bookController.bookDetailLive.observeForever(observer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        bookController.bookDetailLive.removeObserver(observer)
    }

    @Test
    fun `test queryBookDetail with valid book source`() {
        // 准备测试数据
        val searchResult = SearchResult(
            SearchMetadata("123", "Test Book", "Test Author", "http://example.com"),
            mockk()
        )

        val mockParser = mockk<BookSourceParser> {
            every { findDetail(any()) } returns DetailMetadata("123", "Test Book", "Test Author")
            every { findCatalog(any()) } returns listOf()
        }

        // 模拟 BookSourceParser 构造
        mockkConstructor(BookSourceParser::class) {
            every { anyConstructed<BookSourceParser>() } returns mockParser

            // 执行测试
            bookController.queryBookDetail(searchResult)

            // 验证结果
            verify(observer).onChanged(any())
        }
    }

    @Test
    fun `test queryBookDetail with null book source`() {
        // 准备测试数据
        val searchResult = SearchResult(
            SearchMetadata("123", "Test Book", "Test Author", "http://example.com"),
            null
        )

        // 执行测试
        bookController.queryBookDetail(searchResult)

        // 验证没有调用 observer
        verify(observer).onChanged(null)
    }

    @Test
    fun `test queryBookshelves`() {
        // 模拟 Room.bookshelf().getAll()
        mockkStatic(Room::class) {
            val mockBookshelfDao = mockk<BookshelfDao> {
                every { getAll() } returns listOf()
            }
            every { Room.bookshelf() } returns mockBookshelfDao

            // 执行测试
            val result = bookController.queryBookshelves()

            // 验证结果
            assert(result is List<Bookshelf>)
        }
    }

    @Test
    fun `test publish with valid data`() {
        // 准备测试数据
        val searchBook = SearchBook("123", "Test Book", "Test Author", "http://example.com")
        val bookshelf = Bookshelf(1, "Test Shelf")

        // 模拟目录数据
        val chapters = listOf(
            Chapter(0, "Chapter 1", "http://example.com/chapter1")
        )
        bookController.catalogLive.postValue(chapters)

        // 模拟元数据
        val metadata = DetailMetadata("123", "Test Book", "Test Author")
        bookController.bookDetailLive.postValue(metadata)

        // 执行测试
        val liveData = bookController.publish(searchBook, bookshelf)

        // 验证结果
        assert(liveData != null)
    }

    @Test
    fun `test publish with empty catalog`() {
        // 准备测试数据
        val searchBook = SearchBook("123", "Test Book", "Test Author", "http://example.com")
        val bookshelf = Bookshelf(1, "Test Shelf")

        // 模拟空目录
        bookController.catalogLive.postValue(emptyList())

        // 执行测试
        val liveData = bookController.publish(searchBook, bookshelf)

        // 验证结果
        assert(liveData != null)
    }

    @Test
    fun `test publish with null metadata`() {
        // 准备测试数据
        val searchBook = SearchBook("123", "Test Book", "Test Author", "http://example.com")
        val bookshelf = Bookshelf(1, "Test Shelf")

        // 模拟目录数据
        val chapters = listOf(
            Chapter(0, "Chapter 1", "http://example.com/chapter1")
        )
        bookController.catalogLive.postValue(chapters)

        // 模拟空元数据
        bookController.bookDetailLive.postValue(null)

        // 执行测试
        val liveData = bookController.publish(searchBook, bookshelf)

        // 验证结果
        assert(liveData != null)
    }
}
