# 轻墨阅读器本地书籍支持情况

本文档详细说明了轻墨阅读器对本地书籍文件的支持情况。

## 1. 支持的文件格式

### 1.1 声明支持的格式

根据 [AndroidManifest.xml](file:///c:/Users/cywxz/Desktop/qingyue/lightink-1.3.1.0515/app/src/main/AndroidManifest.xml) 的配置，轻墨阅读器声明支持以下本地书籍格式：

| 文件格式 | 文件扩展名 | MIME类型 | 支持状态 |
|---------|-----------|---------|---------|
| TXT文本 | `.txt` | `text/*` | ⚠️ 声明支持，但未找到实现代码 |
| EPUB电子书 | `.epub` | `application/epub+zip` | ⚠️ 声明支持，但未找到实现代码 |
| MOBI电子书 | `.mobi` | - | ⚠️ 声明支持，但未找到实现代码 |
| AZW电子书 | `.azw` | - | ⚠️ 声明支持，但未找到实现代码 |
| AZW3电子书 | `.azw3` | - | ⚠️ 声明支持，但未找到实现代码 |

### 1.2 Intent Filter配置

```xml
<intent-filter tools:ignore="AppLinkUrlError">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:mimeType="text/*" />
    <data android:mimeType="application/epub+zip" />
    <data android:mimeType="application/x-expandedbook" />
</intent-filter>
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="file" />
    <data android:host="*" />
    <data android:pathPattern=".*\.epub" />
    <data android:pathPattern=".*\.mobi" />
    <data android:pathPattern=".*\.azw3" />
    <data android:pathPattern=".*\.azw" />
    <data android:pathPattern=".*\.txt" />
</intent-filter>
```

## 2. 实际实现情况

### 2.1 文件导入功能

| 功能 | 支持状态 | 说明 |
|------|---------|------|
| 文件选择器 | ❌ 未实现 | 未找到文件选择器相关代码 |
| Intent导入 | ⚠️ 部分实现 | 声明了Intent Filter，但未找到处理代码 |
| 文件权限请求 | ❌ 未实现 | 未请求存储权限 |
| 文件路径处理 | ⚠️ 部分实现 | 有DocumentFile相关代码，但未用于本地书籍 |

### 2.2 文件解析功能

| 功能 | 支持状态 | 说明 |
|------|---------|------|
| TXT解析 | ❌ 未实现 | 未找到TXT文件解析代码 |
| EPUB解析 | ❌ 未实现 | 未找到EPUB文件解析代码 |
| MOBI解析 | ❌ 未实现 | 未找到MOBI文件解析代码 |
| AZW解析 | ❌ 未实现 | 未找到AZW文件解析代码 |
| AZW3解析 | ❌ 未实现 | 未找到AZW3文件解析代码 |

### 2.3 相关代码分析

#### 2.3.1 TXTConvertModel.kt

[TXTConvertModel.kt](file:///c:/Users/cywxz/Desktop/qingyue/lightink-1.3.1.0515/app/src/main/java/cn/lightink/reader/model/TXTConvertModel.kt) 文件存在，但仅定义了数据模型：

```kotlin
data class StateChapter(val index: Int, val title: String, var isChecked: Boolean = true, var href: String = EMPTY, var isCached: Boolean = false) {
    val encodeHref: String
        get() = href.encode().let { if (it.length < 0xFF) it else it.md5() }
}

data class CacheChapter(val index: Int, val book: String)
```

**问题**：只定义了数据模型，没有实际的TXT解析逻辑。

#### 2.3.2 MainActivity.kt

[MainActivity.kt](file:///c:/Users/cywxz/Desktop/qingyue/lightink-1.3.1.0515/app/src/main/java/cn/lightink/reader/ui/main/MainActivity.kt) 中的 `onNewIntent` 方法为空：

```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
}
```

**问题**：没有处理从外部打开文件的Intent。

#### 2.3.3 ContentParser.kt

[ContentParser.kt](file:///c:/Users/cywxz/Desktop/qingyue/lightink-1.3.1.0515/app/src/main/java/cn/lightink/reader/transcode/ContentParser.kt) 提供了HTML内容解析功能：

```kotlin
fun read(url: String, content: String, output: File?): String {
    var markdown = if (content.contains("""<[^>]+>""".toRegex())) {
        val html = content.replace("\n", "<br>")
        val node = Jsoup.parseBodyFragment(html, url)
        parseHtml(node, output)
    } else content
    markdown = markdown.replace("""\n+(\s|\u3000)+""".toRegex(), "\n")
    markdown = markdown.replace("""\n+""".toRegex(), "\n")
    return markdown
}
```

**说明**：这个解析器可以处理HTML内容，但没有专门处理本地书籍文件的逻辑。

## 3. 存储权限

### 3.1 权限声明

AndroidManifest.xml 中**未声明**存储相关权限：

```xml
<!-- 未声明的权限 -->
<!-- <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /> -->
<!-- <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> -->
<!-- <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" /> -->
```

**问题**：缺少存储权限，无法访问外部存储中的文件。

## 4. 本地书籍管理

### 4.1 数据模型

[BookModel.kt](file:///c:/Users/cywxz/Desktop/qingyue/lightink-1.3.1.0515/app/src/main/java/cn/lightink/reader/model/BookModel.kt) 中定义了本地书籍的数据模型：

```kotlin
@Entity(tableName = "file", indices = [Index("book")], foreignKeys = [ForeignKey(entity = Book::class, parentColumns = ["objectId"], childColumns = ["book"], onDelete = ForeignKey.CASCADE)])
data class BookFileLink(@PrimaryKey val path: String, var book: String)
```

**说明**：存在本地文件关联的数据模型，但没有实际使用。

### 4.2 书籍存储结构

本地书籍存储在 `BOOK_PATH` 目录下，结构如下：

```
BOOK_PATH/
└── {bookId}/
    ├── images/           # 图片文件夹
    │   └── cover         # 封面图片
    ├── texts/            # 文本文件夹
    ├── catalog.md        # 目录文件
    ├── metadata.json     # 元数据文件
    ├── source.json       # 书源文件
    └── purify.json       # 净化规则文件
```

## 5. 功能缺失总结

### 5.1 高优先级缺失

1. **文件导入功能** - ❌ 完全缺失
   - 没有文件选择器
   - 没有处理Intent导入的代码
   - 没有请求存储权限

2. **文件解析功能** - ❌ 完全缺失
   - 没有TXT文件解析器
   - 没有EPUB文件解析器
   - 没有MOBI/AZW/AZW3文件解析器

### 5.2 中优先级缺失

1. **书籍管理功能** - ⚠️ 部分实现
   - 有数据模型，但没有实际使用
   - 没有本地书籍识别逻辑

2. **用户体验** - ❌ 缺失
   - 没有导入进度提示
   - 没有导入失败处理
   - 没有书籍格式转换

### 5.3 低优先级缺失

1. **高级功能** - ❌ 缺失
   - 没有批量导入
   - 没有自动识别书籍编码
   - 没有章节智能分割

## 6. 建议改进方案

### 6.1 短期改进（1-2周）

1. **添加存储权限**
   ```xml
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   ```

2. **实现TXT文件导入**
   - 添加文件选择器
   - 实现TXT文件解析
   - 实现章节智能分割

3. **处理Intent导入**
   - 在MainActivity中处理文件打开Intent
   - 提取文件路径并解析

### 6.2 中期改进（1-2个月）

1. **实现EPUB文件导入**
   - 使用EPUB解析库（如epublib）
   - 提取封面、目录、内容
   - 转换为应用内部格式

2. **优化用户体验**
   - 添加导入进度提示
   - 添加导入失败处理
   - 支持批量导入

### 6.3 长期改进（3-6个月）

1. **支持更多格式**
   - MOBI格式支持
   - AZW/AZW3格式支持
   - PDF格式支持（可选）

2. **高级功能**
   - 自动识别书籍编码
   - 智能章节分割
   - 书籍格式转换

## 7. 总结

轻墨阅读器在AndroidManifest.xml中声明支持TXT、EPUB、MOBI、AZW、AZW3等本地书籍格式，但**实际代码中并没有实现相关的导入和解析功能**。这是一个**功能声明与实现不匹配**的问题。

### 7.1 当前状态

- ✅ 声明了文件格式支持
- ❌ 没有实际的文件导入功能
- ❌ 没有实际的文件解析功能
- ❌ 没有请求必要的存储权限

### 7.2 优先级建议

1. **立即修复**：移除AndroidManifest.xml中不支持的文件格式声明，避免误导用户
2. **短期实现**：添加TXT文件支持（最简单，用户需求最大）
3. **中期实现**：添加EPUB文件支持（标准格式，用户需求较大）
4. **长期考虑**：添加MOBI/AZW/AZW3支持（Kindle格式，用户需求较小）

---

**文档版本**: 1.0.0  
**最后更新**: 2026-03-28  
**维护状态**: ✅ 持续更新
