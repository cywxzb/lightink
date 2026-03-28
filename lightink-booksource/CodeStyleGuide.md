# 轻墨阅读器代码风格指南

本文档详细说明了轻墨阅读器项目的代码风格规范和最佳实践。

## 1. 代码风格配置

### 1.1 Detekt配置

项目使用 [Detekt](https://detekt.dev/) 作为静态代码分析工具，配置文件位于：
- [config/detekt/detekt.yml](file:///c:/Users/cywxz/Desktop/qingyue/lightink-1.3.1.0515/config/detekt/detekt.yml)

### 1.2 主要配置项

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 最大行长度 | 200 | 超过此长度会警告 |
| 最大方法长度 | 60 | 超过此长度会警告 |
| 最大类长度 | 600 | 超过此长度会警告 |
| 最大参数数量 | 6（函数）/ 7（构造函数） | 超过此数量会警告 |
| 最大嵌套深度 | 4 | 超过此深度会警告 |
| 最大返回语句数 | 2 | 超过此数量会警告 |

## 2. 命名规范

### 2.1 类命名

```kotlin
// ✅ 正确：使用大驼峰命名法
class BookController : ViewModel()
class BookSourceParser(val bookSource: BookSource)
data class Book(val objectId: String, var name: String)

// ❌ 错误：使用小驼峰命名法
class bookController : ViewModel()
class bookSourceParser(val bookSource: BookSource)
```

**规则**：
- 类名使用大驼峰命名法（PascalCase）
- 类名应该是一个名词或名词短语
- 避免使用缩写，除非是通用的（如URL、HTTP）

### 2.2 函数命名

```kotlin
// ✅ 正确：使用小驼峰命名法，动词开头
fun queryBookDetail(result: SearchResult): LiveData<DetailMetadata>
fun search(key: String): List<SearchMetadata>
fun getCache(key: String): T?

// ❌ 错误：使用大驼峰命名法
fun QueryBookDetail(result: SearchResult): LiveData<DetailMetadata>
fun Search(key: String): List<SearchMetadata>
```

**规则**：
- 函数名使用小驼峰命名法（camelCase）
- 函数名应该是一个动词或动词短语
- 避免使用缩写，除非是通用的

### 2.3 变量命名

```kotlin
// ✅ 正确：使用小驼峰命名法
private val bookDetailLive = MutableLiveData<DetailMetadata>()
private var bookSource: BookSource? = null
private val adapter = buildGridAdapter(books)

// ✅ 正确：私有变量可以以下划线开头
private var _books = MutableLiveData<List<Book>>()
val books: LiveData<List<Book>> = _books

// ❌ 错误：使用大驼峰命名法
private val BookDetailLive = MutableLiveData<DetailMetadata>()
private var BookSource: BookSource? = null
```

**规则**：
- 变量名使用小驼峰命名法（camelCase）
- 私有变量可以以下划线开头（但不强制）
- 避免使用缩写，除非是通用的

### 2.4 常量命名

```kotlin
// ✅ 正确：使用全大写，下划线分隔
const val EMPTY = ""
const val LIMIT = 20
const val INTENT_BOOK = "book"
const val BOOK_STATE_END = -1
const val MP_FOLDER_IMAGES = "images"

// ❌ 错误：使用小驼峰命名法
const val empty = ""
const val limit = 20
const val intentBook = "book"
```

**规则**：
- 常量名使用全大写，下划线分隔（SCREAMING_SNAKE_CASE）
- 常量应该使用 `const val` 声明
- 常量应该放在顶层或 companion object 中

### 2.5 包命名

```kotlin
// ✅ 正确：全小写，点分隔
package cn.lightink.reader.controller
package cn.lightink.reader.module.booksource

// ❌ 错误：使用大驼峰命名法
package cn.lightink.reader.Controller
package cn.lightink.reader.Module.BookSource
```

**规则**：
- 包名使用全小写，点分隔
- 避免使用下划线
- 包名应该反映代码的组织结构

## 3. 代码格式化

### 3.1 缩进和空格

```kotlin
// ✅ 正确：使用4个空格缩进
class BookController : ViewModel() {
    fun queryBookDetail(result: SearchResult): LiveData<DetailMetadata> {
        viewModelScope.launch(Dispatchers.IO) {
            bookSource = result.source
            queryBookDetail(result.metadata)
        }
        return bookDetailLive
    }
}

// ❌ 错误：使用Tab缩进
class BookController : ViewModel() {
	fun queryBookDetail(result: SearchResult): LiveData<DetailMetadata> {
		viewModelScope.launch(Dispatchers.IO) {
			bookSource = result.source
			queryBookDetail(result.metadata)
		}
		return bookDetailLive
	}
}
```

**规则**：
- 使用4个空格缩进（不使用Tab）
- 不在行尾添加空格
- 文件末尾应该有一个空行

### 3.2 行长度

```kotlin
// ✅ 正确：行长度不超过200字符
data class Book(
    val objectId: String, 
    var name: String, 
    var author: String, 
    var link: String
)

// ✅ 正确：长行可以换行
@Parcelize
@Entity(primaryKeys = ["objectId"], indices = [Index("objectId", unique = true)])
data class Book(
    val objectId: String, 
    var name: String, 
    var author: String, 
    var link: String, 
    var publishId: String, 
    var bookshelf: Long
) : Parcelable

// ❌ 错误：行长度超过200字符
data class Book(val objectId: String, var name: String, var author: String, var link: String, var publishId: String, var bookshelf: Long, var lastChapter: String = EMPTY, var state: Int = BOOK_STATE_IDLE, var catalog: Int = 0, var chapter: Int = 0, var chapterProgress: Int = 0, var chapterName: String = EMPTY, var time: Int = 0, var word: Int = 0, var speed: Float = 0F, var updatedAt: Long = System.currentTimeMillis(), var finishedAt: Long = 0L, var configuration: Long = 0, var createdAt: Long = System.currentTimeMillis(), var version: Int = -1) : Parcelable
```

**规则**：
- 最大行长度为200字符
- 长行应该合理换行
- 换行后应该保持良好的可读性

### 3.3 空行

```kotlin
// ✅ 正确：在逻辑块之间添加空行
class BookController : ViewModel() {

    private val bookDetailLive = MutableLiveData<DetailMetadata>()
    private var bookSource: BookSource? = null

    val catalogLive = MutableLiveData<List<Chapter>>()

    /**
     * 查询书架列表
     */
    fun queryBookshelves() = Room.bookshelf().getAll()

    /**
     * 查询图书详情
     */
    fun queryBookDetail(result: SearchResult): LiveData<DetailMetadata> {
        viewModelScope.launch(Dispatchers.IO) {
            bookSource = result.source
            queryBookDetail(result.metadata)
        }
        return bookDetailLive
    }
}

// ❌ 错误：没有空行
class BookController : ViewModel() {
    private val bookDetailLive = MutableLiveData<DetailMetadata>()
    private var bookSource: BookSource? = null
    val catalogLive = MutableLiveData<List<Chapter>>()
    fun queryBookshelves() = Room.bookshelf().getAll()
    fun queryBookDetail(result: SearchResult): LiveData<DetailMetadata> {
        viewModelScope.launch(Dispatchers.IO) {
            bookSource = result.source
            queryBookDetail(result.metadata)
        }
        return bookDetailLive
    }
}
```

**规则**：
- 在类成员（属性、方法）之间添加空行
- 在逻辑块之间添加空行
- 不要有多余的空行

### 3.4 大括号

```kotlin
// ✅ 正确：大括号在同一行
if (book != null) {
    startActivity(Intent(activity, ReaderActivity::class.java).putExtra(INTENT_BOOK, book))
}

// ✅ 正确：when表达式
when (item) {
    R.string.menu_summary -> startActivity(Intent(activity, BookSummaryActivity::class.java).putExtra(INTENT_BOOK, book))
    R.string.menu_move_bookshelf -> showBookshelfSelector()
    R.string.menu_delete_book -> BookDeleteDialog(requireActivity()) { withResource ->
        controller.deleteBooks(listOf(book), withResource)
    }.show()
}

// ❌ 错误：大括号在下一行（C风格）
if (book != null)
{
    startActivity(Intent(activity, ReaderActivity::class.java).putExtra(INTENT_BOOK, book))
}
```

**规则**：
- 大括号在同一行（Kotlin风格）
- 即使是单行语句，也使用大括号
- when表达式的大括号也在同一行

## 4. 注释风格

### 4.1 类注释

```kotlin
// ✅ 正确：使用KDoc格式
/**
 * 图书控制器
 *
 * @see cn.lightink.reader.ui.book.BookDetailActivity
 * @since 1.0.0
 */
class BookController : ViewModel() {
    // ...
}

// ✅ 正确：使用多行注释
/********************************************************************************************************************************
 * 存储类  书
 ********************************************************************************************************************************
 *  @property objectId          唯一ID
 *  @property name              名字
 *  @property author            作者
 *  @property link              链接
 *  @property publishId         出版ID
 *  @property bookshelf         书架
 *  @property lastChapter       最新章节
 *  @property state             连载状态
 *  @property catalog           目录章节数
 *  @property chapter           上次阅读章节索引
 *  @property chapterProgress   上次阅读章节内索引
 *  @property chapterName       上次阅读章节名
 *  @property time              累计阅读时长
 *  @property word              累计阅读字数
 *  @property speed             平均阅读速度
 *  @property updatedAt         上次阅读时间
 *  @property finishedAt        结束阅读时间
 *  @property createdAt         加入书架时间
 *  @property configuration     配置
 *  @property version           同步版本号
 */
@Parcelize
@Entity(primaryKeys = ["objectId"], indices = [Index("objectId", unique = true)])
data class Book(val objectId: String, var name: String, var author: String) : Parcelable {
    // ...
}
```

**规则**：
- 类注释使用KDoc格式（/** ... */）
- 说明类的用途和职责
- 使用 @property 标注属性说明
- 使用 @see 标注相关类
- 使用 @since 标注版本信息

### 4.2 函数注释

```kotlin
// ✅ 正确：使用KDoc格式
/**
 * 查询图书详情
 * 先返回上个页面传递来的元数据
 * 再通过书源规则联网读取元数据
 */
fun queryBookDetail(result: SearchResult): LiveData<DetailMetadata> {
    viewModelScope.launch(Dispatchers.IO) {
        bookSource = result.source
        queryBookDetail(result.metadata)
    }
    return bookDetailLive
}

// ✅ 正确：简单注释
/**
 * 查询书架列表
 */
fun queryBookshelves() = Room.bookshelf().getAll()
```

**规则**：
- 函数注释使用KDoc格式
- 说明函数的功能、参数、返回值
- 对于简单函数，可以使用单行注释
- 使用 @param 标注参数说明
- 使用 @return 标注返回值说明

### 4.3 属性注释

```kotlin
// ✅ 正确：使用KDoc格式
/**
 * 解析结果缓存
 */
private val parseCache = mutableMapOf<String, Pair<Long, Any>>()

/**
 * 缓存过期时间（毫秒）
 */
private val CACHE_EXPIRY_TIME = 60 * 60 * 1000L // 1小时

/**
 * 最大缓存数量
 */
private val MAX_CACHE_SIZE = 50
```

**规则**：
- 属性注释使用KDoc格式
- 说明属性的用途和单位
- 对于常量，说明其含义

### 4.4 代码块注释

```kotlin
// ✅ 正确：使用单行注释
//检查缓存
val cacheKey = generateCacheKey("search", key)
getCache<List<SearchMetadata>>(cacheKey)?.let { return it }

// ✅ 正确：使用TODO注释
// TODO: 添加更多错误处理
fun parseBookSource(json: String): BookSource? {
    // ...
}

// ❌ 错误：使用FIXME注释（Detekt会警告）
// FIXME: 这里有问题
fun parseBookSource(json: String): BookSource? {
    // ...
}
```

**规则**：
- 代码块注释使用单行注释（//）
- 说明代码块的功能或目的
- 使用 TODO 标注待办事项
- 避免使用 FIXME、STOPSHIP 等注释（Detekt会警告）

## 5. 代码组织

### 5.1 文件结构

```kotlin
// ✅ 正确：文件结构清晰
package cn.lightink.reader.controller

// 1. 导入语句
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 2. 类声明
/**
 * 图书控制器
 *
 * @see cn.lightink.reader.ui.book.BookDetailActivity
 * @since 1.0.0
 */
class BookController : ViewModel() {
    
    // 3. 伴生对象
    companion object {
        private const val TAG = "BookController"
    }
    
    // 4. 属性
    private val bookDetailLive = MutableLiveData<DetailMetadata>()
    private var bookSource: BookSource? = null
    
    // 5. 初始化块
    init {
        // 初始化代码
    }
    
    // 6. 公共方法
    fun queryBookshelves() = Room.bookshelf().getAll()
    
    fun queryBookDetail(result: SearchResult): LiveData<DetailMetadata> {
        // ...
    }
    
    // 7. 私有方法
    private fun queryBookDetail(metadata: SearchMetadata) {
        // ...
    }
    
    // 8. 内部类
    inner class BookSourceHandler {
        // ...
    }
}
```

**规则**：
- 文件结构应该清晰有序
- 导入语句按字母顺序排列
- 类成员按以下顺序组织：
  1. 伴生对象
  2. 属性
  3. 初始化块
  4. 公共方法
  5. 私有方法
  6. 内部类

### 5.2 导入语句

```kotlin
// ✅ 正确：导入语句按字母顺序排列
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.lightink.reader.model.Book
import cn.lightink.reader.module.Room
import java.io.File

// ❌ 错误：使用通配符导入
import android.content.*
import androidx.lifecycle.*
import cn.lightink.reader.model.*
import java.io.*
```

**规则**：
- 导入语句按字母顺序排列
- 避免使用通配符导入（`import package.*`）
- 移除未使用的导入语句
- 导入语句应该放在文件顶部

## 6. 最佳实践

### 6.1 空安全

```kotlin
// ✅ 正确：使用安全调用操作符
book?.let { 
    startActivity(Intent(activity, ReaderActivity::class.java).putExtra(INTENT_BOOK, it))
}

// ✅ 正确：使用Elvis操作符
val name = book?.name ?: "未知"

// ✅ 正确：使用requireNotNull
val bookSource = requireNotNull(bookSource) { "BookSource cannot be null" }

// ❌ 错误：使用非空断言（尽量避免）
val name = book!!.name
```

**规则**：
- 优先使用安全调用操作符（`?.`）
- 使用Elvis操作符（`?:`）提供默认值
- 避免使用非空断言（`!!`）
- 使用 `requireNotNull` 进行参数验证

### 6.2 集合操作

```kotlin
// ✅ 正确：使用函数式操作
books.filter { it.author.isNotBlank() }
     .sortedBy { it.name }
     .map { it.name }

// ✅ 正确：使用扩展函数
books.isNotEmpty()
books.isNullOrEmpty()
books.orEmpty()

// ❌ 错误：使用传统循环
val filteredBooks = mutableListOf<Book>()
for (book in books) {
    if (book.author.isNotBlank()) {
        filteredBooks.add(book)
    }
}
```

**规则**：
- 优先使用函数式操作（filter、map、sortedBy等）
- 使用扩展函数（isNotEmpty、isNullOrEmpty、orEmpty等）
- 避免使用传统循环

### 6.3 协程使用

```kotlin
// ✅ 正确：在ViewModel中使用viewModelScope
viewModelScope.launch(Dispatchers.IO) {
    bookSource = result.source
    queryBookDetail(result.metadata)
}

// ✅ 正确：使用withContext切换线程
withContext(Dispatchers.IO) {
    val response = NetworkBridge.execute(buildRequest(url))
    parseResponse(response)
}

// ❌ 错误：使用GlobalScope（Detekt会警告）
GlobalScope.launch {
    // ...
}
```

**规则**：
- 在ViewModel中使用 `viewModelScope`
- 在Activity/Fragment中使用 `lifecycleScope`
- 使用 `withContext` 切换线程
- 避免使用 `GlobalScope`
- 指定合适的Dispatcher（IO、Default、Main）

### 6.4 数据类

```kotlin
// ✅ 正确：使用data class
data class Book(
    val objectId: String,
    var name: String,
    var author: String
)

// ✅ 正确：使用Parcelize注解
@Parcelize
data class Book(
    val objectId: String,
    var name: String,
    var author: String
) : Parcelable

// ❌ 错误：使用普通class
class Book(
    val objectId: String,
    var name: String,
    var author: String
)
```

**规则**：
- 对于纯数据类，使用 `data class`
- 需要序列化时，使用 `@Parcelize` 注解
- 避免在数据类中添加复杂逻辑

## 7. 代码质量工具

### 7.1 Detekt

项目使用Detekt进行静态代码分析，配置文件：
- [config/detekt/detekt.yml](file:///c:/Users/cywxz/Desktop/qingyue/lightink-1.3.1.0515/config/detekt/detekt.yml)

运行Detekt：
```bash
./gradlew detekt
```

### 7.2 常见问题

| 问题 | 说明 | 解决方法 |
|------|------|---------|
| MaxLineLength | 行长度超过200 | 合理换行 |
| LongMethod | 方法长度超过60 | 拆分方法 |
| LongParameterList | 参数数量超过6 | 使用数据类 |
| NestedBlockDepth | 嵌套深度超过4 | 提取方法 |
| MagicNumber | 魔法数字 | 使用常量 |
| ForbiddenComment | 禁止的注释 | 移除TODO/FIXME |

## 8. 代码审查清单

### 8.1 命名

- [ ] 类名使用大驼峰命名法
- [ ] 函数名使用小驼峰命名法，动词开头
- [ ] 变量名使用小驼峰命名法
- [ ] 常量名使用全大写，下划线分隔
- [ ] 包名使用全小写，点分隔

### 8.2 格式化

- [ ] 使用4个空格缩进
- [ ] 行长度不超过200字符
- [ ] 在逻辑块之间添加空行
- [ ] 大括号在同一行
- [ ] 文件末尾有一个空行

### 8.3 注释

- [ ] 类有KDoc注释
- [ ] 公共方法有KDoc注释
- [ ] 复杂逻辑有注释说明
- [ ] 没有FIXME、STOPSHIP注释

### 8.4 代码质量

- [ ] 没有魔法数字
- [ ] 没有深层嵌套
- [ ] 方法长度合理
- [ ] 参数数量合理
- [ ] 没有重复代码

### 8.5 最佳实践

- [ ] 使用安全调用操作符
- [ ] 使用函数式操作
- [ ] 正确使用协程
- [ ] 使用data class
- [ ] 移除未使用的导入

## 9. 总结

轻墨阅读器项目遵循Kotlin官方代码风格指南，使用Detekt进行静态代码分析。主要特点包括：

### 9.1 优点

- ✅ 有完整的Detekt配置
- ✅ 命名规范统一
- ✅ 注释风格一致
- ✅ 代码组织清晰
- ✅ 使用现代Kotlin特性

### 9.2 改进建议

- ⚠️ 添加.editorconfig文件统一编辑器配置
- ⚠️ 增加单元测试覆盖率
- ⚠️ 添加更多文档注释
- ⚠️ 统一错误处理方式

---

**文档版本**: 1.0.0  
**最后更新**: 2026-03-28  
**维护状态**: ✅ 持续更新
