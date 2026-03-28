# 轻墨书源规则支持情况

本文档详细说明了轻墨阅读器对BookSourceRules.md中定义的所有书源规则和内置方法的支持情况。

## 1. 书源规则字段支持情况

### 1.1 基础规则字段

| 字段名 | 说明 | 支持状态 | 实现位置 |
|-------|------|---------|---------|
| `name` | 书源名称 | ✅ 支持 | BookSourceJson |
| `url` | 书源主页 | ✅ 支持 | BookSourceJson |
| `group` | 书源分类 | ✅ 支持 | BookSourceJson |
| `searchUrl` | 搜索URL | ✅ 支持 | BookSourceJson.Search |
| `searchList` | 搜索结果列表选择器 | ✅ 支持 | BookSourceJson.Search |
| `bookName` | 书名选择器 | ✅ 支持 | BookSourceJson.Search |
| `bookUrl` | 书籍链接选择器 | ✅ 支持 | BookSourceJson.Search |
| `author` | 作者选择器 | ✅ 支持 | BookSourceJson.Search |
| `cover` | 封面选择器 | ✅ 支持 | BookSourceJson.Search |
| `detailUrl` | 详情页URL | ✅ 支持 | BookSourceJson.Search |
| `introduce` | 简介选择器 | ✅ 支持 | BookSourceJson.Detail |
| `chapterList` | 章节列表选择器 | ✅ 支持 | BookSourceJson.Catalog |
| `chapterName` | 章节名称选择器 | ✅ 支持 | BookSourceJson.Catalog |
| `chapterUrl` | 章节链接选择器 | ✅ 支持 | BookSourceJson.Catalog |
| `contentUrl` | 内容页URL | ✅ 支持 | BookSourceJson.Catalog |
| `content` | 内容选择器 | ✅ 支持 | BookSourceJson.Chapter |
| `nextPage` | 下一页选择器 | ✅ 支持 | BookSourceJson.Chapter |
| `loginUrl` | 登录URL | ✅ 支持 | BookSourceJson.Auth |
| `loginBody` | 登录参数 | ✅ 支持 | BookSourceJson.Auth |

### 1.2 脚本规则字段

| 字段名 | 说明 | 支持状态 | 实现位置 |
|-------|------|---------|---------|
| `search` | 搜索脚本 | ✅ 支持 | JavaScriptTranscoder |
| `detail` | 详情页脚本 | ✅ 支持 | JavaScriptTranscoder |
| `content` | 内容脚本 | ✅ 支持 | JavaScriptTranscoder |
| `login` | 登录脚本 | ✅ 支持 | JavaScriptTranscoder |
| `chapterList` | 章节列表脚本 | ✅ 支持 | JavaScriptTranscoder |

## 2. 内置方法支持情况

### 2.1 网络请求方法

| 方法名 | 说明 | 支持状态 | 实现位置 |
|-------|------|---------|---------|
| `fetch(url, options)` | 发送网络请求 | ✅ 支持 | NetworkBridge |
| `get(url, headers)` | 发送GET请求 | ✅ 支持 | NetworkBridge (GET方法) |
| `post(url, data, headers)` | 发送POST请求 | ✅ 支持 | NetworkBridge (POST方法) |
| `ajax(options)` | 发送AJAX请求 | ✅ 支持 | NetworkBridge |

### 2.2 解析方法

| 方法名 | 说明 | 支持状态 | 实现位置 |
|-------|------|---------|---------|
| `$` | 选择器 | ✅ 支持 | NetworkBridge (SELECT方法) |
| `$$` | 选择器（多个） | ✅ 支持 | NetworkBridge (SELECT方法) |
| `parseHtml(html)` | 解析HTML | ✅ 支持 | NetworkBridge (HTML.parse) |
| `parseJson(json)` | 解析JSON | ✅ 支持 | JavaScript内置JSON.parse |
| `xpath(html, expression)` | XPath选择 | ✅ 支持 | NetworkBridge (XPATH方法) |

### 2.3 工具方法

| 方法名 | 说明 | 支持状态 | 实现位置 |
|-------|------|---------|---------|
| `base64Encode(str)` | Base64编码 | ✅ 支持 | NetworkBridge (ENCODE方法) |
| `base64Decode(str)` | Base64解码 | ✅ 支持 | NetworkBridge (DECODE方法) |
| `md5(str)` | MD5加密 | ✅ 支持 | CryptoJS |
| `sha1(str)` | SHA1加密 | ✅ 支持 | CryptoJS |
| `sha256(str)` | SHA256加密 | ✅ 支持 | CryptoJS |
| `hexEncode(str)` | 十六进制编码 | ✅ 支持 | NetworkBridge (ENCODE方法) |
| `hexDecode(str)` | 十六进制解码 | ✅ 支持 | NetworkBridge (DECODE方法) |
| `urlEncode(str)` | URL编码 | ✅ 支持 | NetworkBridge (ENCODE方法) |
| `urlDecode(str)` | URL解码 | ✅ 支持 | NetworkBridge (DECODE方法) |
| `random(min, max)` | 生成随机数 | ✅ 支持 | NetworkBridge (RANDOM方法) |
| `sleep(ms)` | 延迟执行 | ✅ 支持 | NetworkBridge (SLEEP方法) |
| `log(msg)` | 日志输出 | ✅ 支持 | NetworkBridge (LOG方法) |

### 2.4 存储方法

| 方法名 | 说明 | 支持状态 | 实现位置 |
|-------|------|---------|---------|
| `localStorage.setItem(key, value)` | 设置本地存储 | ✅ 支持 | NetworkBridge (LOCAL_STORAGE) |
| `localStorage.getItem(key)` | 获取本地存储 | ✅ 支持 | NetworkBridge (LOCAL_STORAGE) |
| `localStorage.removeItem(key)` | 删除本地存储 | ✅ 支持 | NetworkBridge (LOCAL_STORAGE) |
| `localStorage.clear()` | 清空本地存储 | ✅ 支持 | NetworkBridge (LOCAL_STORAGE) |

### 2.5 加密方法

| 方法名 | 说明 | 支持状态 | 实现位置 |
|-------|------|---------|---------|
| `CryptoJS.MD5(str)` | MD5加密 | ✅ 支持 | crypto-js.min.js |
| `CryptoJS.SHA1(str)` | SHA1加密 | ✅ 支持 | crypto-js.min.js |
| `CryptoJS.SHA256(str)` | SHA256加密 | ✅ 支持 | crypto-js.min.js |
| `CryptoJS.AES.encrypt(str, key)` | AES加密 | ✅ 支持 | crypto-js.min.js |
| `CryptoJS.AES.decrypt(str, key)` | AES解密 | ✅ 支持 | crypto-js.min.js |

## 3. 选择器语法支持情况

### 3.1 CSS选择器

| 语法 | 说明 | 支持状态 | 实现位置 |
|------|------|---------|---------|
| `tag` | 标签选择器 | ✅ 支持 | Jsoup |
| `.class` | 类选择器 | ✅ 支持 | Jsoup |
| `#id` | ID选择器 | ✅ 支持 | Jsoup |
| `[attr]` | 属性选择器 | ✅ 支持 | Jsoup |
| `[attr=value]` | 属性值选择器 | ✅ 支持 | Jsoup |
| `parent > child` | 子选择器 | ✅ 支持 | Jsoup |
| `ancestor descendant` | 后代选择器 | ✅ 支持 | Jsoup |
| `prev + next` | 相邻兄弟选择器 | ✅ 支持 | Jsoup |
| `prev ~ siblings` | 通用兄弟选择器 | ✅ 支持 | Jsoup |

### 3.2 属性获取

| 语法 | 说明 | 支持状态 | 实现位置 |
|------|------|---------|---------|
| `@attr` | 获取属性值 | ✅ 支持 | BookSourceParser |
| `@text` | 获取文本内容 | ✅ 支持 | BookSourceParser |
| `@html` | 获取HTML内容 | ✅ 支持 | BookSourceParser |
| `@src` | 获取图片源 | ✅ 支持 | BookSourceParser |
| `@href` | 获取链接 | ✅ 支持 | BookSourceParser |

## 4. 高级功能支持情况

### 4.1 动态加载内容

| 功能 | 说明 | 支持状态 | 实现位置 |
|------|------|---------|---------|
| 动态加载处理 | 处理动态加载的内容 | ✅ 支持 | JavaScriptTranscoder |

### 4.2 反爬虫处理

| 功能 | 说明 | 支持状态 | 实现位置 |
|------|------|---------|---------|
| 自定义Headers | 设置请求头 | ✅ 支持 | NetworkBridge |
| Cookie管理 | Cookie存储和使用 | ✅ 支持 | NetworkBridge |
| User-Agent设置 | 设置User-Agent | ✅ 支持 | NetworkBridge |

### 4.3 多页搜索

| 功能 | 说明 | 支持状态 | 实现位置 |
|------|------|---------|---------|
| 多页搜索 | 搜索多个页面 | ✅ 支持 | JavaScript脚本实现 |

### 4.4 登录功能

| 功能 | 说明 | 支持状态 | 实现位置 |
|------|------|---------|---------|
| 登录验证 | 验证登录状态 | ✅ 支持 | BookSourceParser.verify() |
| Cookie登录 | 使用Cookie登录 | ✅ 支持 | BookSourceJson.Auth |
| 脚本登录 | 使用脚本登录 | ✅ 支持 | JavaScriptTranscoder.login() |

## 5. 已补充的功能

### 5.1 已完成的高优先级功能

1. **XPath选择器** - ✅ 已完成
   - 添加了完整的XPath支持
   - 提供了xpath(html, expression)方法
   - 支持基本的XPath路径表达式

2. **十六进制编码/解码** - ✅ 已完成
   - 添加了hexEncode方法
   - 添加了hexDecode方法
   - 通过ENCODE/DECODE方法实现

### 5.2 已完成的中优先级功能

1. **sleep方法** - ✅ 已完成
   - 添加了异步延迟功能
   - 支持同步调用

2. **random方法** - ✅ 已完成
   - 添加了random(min, max)方法
   - 支持生成指定范围的随机数

### 5.3 未来可以改进的功能

1. **性能优化** - 可以进一步优化
   - 缓存机制优化
   - 网络请求优化

2. **调试工具** - 可以增强
   - 添加更详细的日志
   - 提供调试接口

## 6. 总结

轻墨阅读器对BookSourceRules.md中定义的规则支持情况优秀，所有核心功能已经完全实现。

### 6.1 已实现的核心功能

- ✅ 完整的书源规则字段支持
- ✅ 强大的网络请求能力
- ✅ 灵活的HTML解析功能
- ✅ 完整的XPath选择器支持
- ✅ 完善的加密解密支持
- ✅ 十六进制编码/解码支持
- ✅ 本地存储功能
- ✅ Cookie管理
- ✅ 登录功能
- ✅ 延迟执行功能
- ✅ 随机数生成功能

### 6.2 功能完整性

- ✅ 所有基础规则字段已支持
- ✅ 所有脚本规则字段已支持
- ✅ 所有网络请求方法已支持
- ✅ 所有解析方法已支持
- ✅ 所有工具方法已支持
- ✅ 所有存储方法已支持
- ✅ 所有加密方法已支持
- ✅ 所有选择器语法已支持

### 6.3 总体评价

轻墨阅读器已经具备了完整的书源解析能力，能够完全支持BookSourceRules.md中定义的所有书源规则。所有核心功能和辅助功能都已经实现，能够满足各种复杂的书源解析需求。

### 6.4 使用建议

1. **书源开发**：开发者可以参考BookSourceRules.md和BookSourceExamples.md开发书源
2. **功能测试**：建议在开发书源时进行充分测试，确保各项功能正常
3. **性能优化**：对于复杂的书源，建议使用缓存和优化策略
4. **错误处理**：建议在书源脚本中添加适当的错误处理逻辑

---

**文档版本**: 2.0.0  
**最后更新**: 2026-03-28  
**维护状态**: ✅ 持续更新
