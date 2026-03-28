# 轻墨 JS 书源规则

## 1. 书源开发模板

### 1.1 基础模板

```javascript
{
  "name": "书源名称",
  "url": "https://example.com",
  "group": "分类",
  "searchUrl": "https://example.com/search?q={{key}}",
  "searchList": "div.book-item",
  "bookName": "h3.title",
  "bookUrl": "a@href",
  "author": ".author",
  "cover": ".cover img@src",
  "detailUrl": "https://example.com{{bookUrl}}",
  "introduce": ".intro",
  "chapterList": "ul.chapter-list li",
  "chapterName": "a",
  "chapterUrl": "a@href",
  "contentUrl": "https://example.com{{chapterUrl}}",
  "content": ".chapter-content",
  "nextPage": ".next-page@href",
  "loginUrl": "https://example.com/login",
  "loginBody": "username={{username}}&password={{password}}"
}
```

### 1.2 高级模板（支持脚本）

```javascript
{
  "name": "书源名称",
  "url": "https://example.com",
  "group": "分类",
  "search": "function(key) { return fetch('https://example.com/search?q=' + key).then(r => r.text()); }",
  "searchList": "div.book-item",
  "bookName": "h3.title",
  "bookUrl": "a@href",
  "author": ".author",
  "cover": ".cover img@src",
  "detail": "function(url) { return fetch(url).then(r => r.text()); }",
  "introduce": ".intro",
  "chapterList": "ul.chapter-list li",
  "chapterName": "a",
  "chapterUrl": "a@href",
  "content": "function(url) { return fetch(url).then(r => r.text()).then(html => html.match(/<div class=\"content\">([\s\S]*?)<\/div>/)[1]); }",
  "login": "function(username, password) { return fetch('https://example.com/login', { method: 'POST', body: 'username=' + username + '&password=' + password }).then(r => r.text()); }"
}
```

## 2. 内置方法列表

### 2.1 网络请求方法

| 方法名 | 说明 | 参数 | 返回值 |
|-------|------|------|-------|
| `fetch(url, options)` | 发送网络请求 | url: 字符串, options: 对象 | Promise<string> |
| `get(url, headers)` | 发送GET请求 | url: 字符串, headers: 对象 | Promise<string> |
| `post(url, data, headers)` | 发送POST请求 | url: 字符串, data: 对象/字符串, headers: 对象 | Promise<string> |
| `ajax(options)` | 发送AJAX请求 | options: 对象 | Promise<string> |

### 2.2 解析方法

| 方法名 | 说明 | 参数 | 返回值 |
|-------|------|------|-------|
| `$` | 选择器 | selector: 字符串 | 元素对象 |
| `$$` | 选择器（多个） | selector: 字符串 | 元素数组 |
| `parseHtml(html)` | 解析HTML | html: 字符串 | DOM对象 |
| `parseJson(json)` | 解析JSON | json: 字符串 | 对象/数组 |
| `xpath(html, expression)` | XPath选择 | html: 字符串, expression: 字符串 | 元素数组 |

### 2.3 工具方法

| 方法名 | 说明 | 参数 | 返回值 |
|-------|------|------|-------|
| `base64Encode(str)` | Base64编码 | str: 字符串 | 字符串 |
| `base64Decode(str)` | Base64解码 | str: 字符串 | 字符串 |
| `md5(str)` | MD5加密 | str: 字符串 | 字符串 |
| `sha1(str)` | SHA1加密 | str: 字符串 | 字符串 |
| `sha256(str)` | SHA256加密 | str: 字符串 | 字符串 |
| `hexEncode(str)` | 十六进制编码 | str: 字符串 | 字符串 |
| `hexDecode(str)` | 十六进制解码 | str: 字符串 | 字符串 |
| `urlEncode(str)` | URL编码 | str: 字符串 | 字符串 |
| `urlDecode(str)` | URL解码 | str: 字符串 | 字符串 |
| `random(min, max)` | 生成随机数 | min: 数字, max: 数字 | 数字 |
| `sleep(ms)` | 延迟执行 | ms: 数字 | Promise<void> |
| `log(msg)` | 日志输出 | msg: 任意 | void |

### 2.4 存储方法

| 方法名 | 说明 | 参数 | 返回值 |
|-------|------|------|-------|
| `localStorage.setItem(key, value)` | 设置本地存储 | key: 字符串, value: 任意 | void |
| `localStorage.getItem(key)` | 获取本地存储 | key: 字符串 | 字符串 |
| `localStorage.removeItem(key)` | 删除本地存储 | key: 字符串 | void |
| `localStorage.clear()` | 清空本地存储 | 无 | void |

### 2.5 加密方法

| 方法名 | 说明 | 参数 | 返回值 |
|-------|------|------|-------|
| `CryptoJS.MD5(str)` | MD5加密 | str: 字符串 | 字符串 |
| `CryptoJS.SHA1(str)` | SHA1加密 | str: 字符串 | 字符串 |
| `CryptoJS.SHA256(str)` | SHA256加密 | str: 字符串 | 字符串 |
| `CryptoJS.AES.encrypt(str, key)` | AES加密 | str: 字符串, key: 字符串 | 字符串 |
| `CryptoJS.AES.decrypt(str, key)` | AES解密 | str: 字符串, key: 字符串 | 字符串 |

## 3. 书源规则说明

### 3.1 基础规则

| 字段名 | 说明 | 示例 |
|-------|------|------|
| `name` | 书源名称 | "示例书源" |
| `url` | 书源主页 | "https://example.com" |
| `group` | 书源分类 | "小说" |
| `searchUrl` | 搜索URL | "https://example.com/search?q={{key}}" |
| `searchList` | 搜索结果列表选择器 | "div.book-item" |
| `bookName` | 书名选择器 | "h3.title" |
| `bookUrl` | 书籍链接选择器 | "a@href" |
| `author` | 作者选择器 | ".author" |
| `cover` | 封面选择器 | ".cover img@src" |
| `detailUrl` | 详情页URL | "https://example.com{{bookUrl}}" |
| `introduce` | 简介选择器 | ".intro" |
| `chapterList` | 章节列表选择器 | "ul.chapter-list li" |
| `chapterName` | 章节名称选择器 | "a" |
| `chapterUrl` | 章节链接选择器 | "a@href" |
| `contentUrl` | 内容页URL | "https://example.com{{chapterUrl}}" |
| `content` | 内容选择器 | ".chapter-content" |
| `nextPage` | 下一页选择器 | ".next-page@href" |
| `loginUrl` | 登录URL | "https://example.com/login" |
| `loginBody` | 登录参数 | "username={{username}}&password={{password}}" |

### 3.2 脚本规则

| 字段名 | 说明 | 示例 |
|-------|------|------|
| `search` | 搜索脚本 | "function(key) { return fetch('https://example.com/search?q=' + key).then(r => r.text()); }" |
| `detail` | 详情页脚本 | "function(url) { return fetch(url).then(r => r.text()); }" |
| `content` | 内容脚本 | "function(url) { return fetch(url).then(r => r.text()).then(html => html.match(/<div class=\"content\">([\s\S]*?)<\/div>/)[1]); }" |
| `login` | 登录脚本 | "function(username, password) { return fetch('https://example.com/login', { method: 'POST', body: 'username=' + username + '&password=' + password }).then(r => r.text()); }" |
| `chapterList` | 章节列表脚本 | "function(html) { return html.match(/<ul class=\"chapter-list\">([\s\S]*?)<\/ul>/)[1]; }" |

## 4. 选择器语法

### 4.1 CSS选择器

| 语法 | 说明 | 示例 |
|------|------|------|
| `tag` | 标签选择器 | "div" |
| `.class` | 类选择器 | ".book-item" |
| `#id` | ID选择器 | "#content" |
| `[attr]` | 属性选择器 | "[href]" |
| `[attr=value]` | 属性值选择器 | "[class=book-item]" |
| `parent > child` | 子选择器 | "div > ul" |
| `ancestor descendant` | 后代选择器 | "div li" |
| `prev + next` | 相邻兄弟选择器 | "div + p" |
| `prev ~ siblings` | 通用兄弟选择器 | "div ~ p" |

### 4.2 属性获取

| 语法 | 说明 | 示例 |
|------|------|------|
| `@attr` | 获取属性值 | "a@href" |
| `@text` | 获取文本内容 | ".title@text" |
| `@html` | 获取HTML内容 | ".content@html" |
| `@src` | 获取图片源 | "img@src" |
| `@href` | 获取链接 | "a@href" |

## 5. 书源测试

### 5.1 测试步骤

1. **导入书源**：在书源管理界面导入书源文件
2. **启用书源**：在书源列表中启用书源
3. **搜索测试**：在搜索界面搜索书籍，验证搜索结果
4. **详情测试**：点击搜索结果，查看书籍详情
5. **目录测试**：查看书籍目录，验证目录加载
6. **内容测试**：点击章节，查看章节内容
7. **登录测试**：如果书源需要登录，测试登录功能

### 5.2 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 搜索无结果 | 搜索URL错误或选择器错误 | 检查搜索URL和选择器 |
| 详情页加载失败 | 详情页URL错误或选择器错误 | 检查详情页URL和选择器 |
| 目录加载失败 | 目录选择器错误 | 检查目录选择器 |
| 内容加载失败 | 内容选择器错误或需要登录 | 检查内容选择器，测试登录功能 |
| 登录失败 | 登录参数错误或登录URL错误 | 检查登录参数和登录URL |

## 6. 书源优化

### 6.1 性能优化

1. **使用缓存**：对于频繁访问的页面，可以使用localStorage缓存
2. **减少请求**：合并请求，减少网络请求次数
3. **优化选择器**：使用更精确的选择器，减少DOM遍历
4. **异步处理**：使用Promise和async/await处理异步操作

### 6.2 稳定性优化

1. **错误处理**：添加try-catch处理异常
2. **重试机制**：对于网络请求失败，添加重试机制
3. **用户代理**：设置合理的User-Agent，避免被网站屏蔽
4. **编码处理**：处理URL编码和HTML编码

## 7. 注意事项

1. **遵守网站规则**：不要过度请求，避免给网站服务器造成负担
2. **尊重版权**：只用于个人学习和研究，不要用于商业用途
3. **安全第一**：不要在书源中包含恶意代码
4. **定期更新**：网站结构变化时，及时更新书源规则
5. **分享交流**：可以在社区分享优质书源，共同完善书源生态

## 8. 高级技巧

### 8.1 动态加载内容

```javascript
{
  "name": "动态加载书源",
  "url": "https://example.com",
  "group": "小说",
  "search": "function(key) {
    return fetch('https://example.com/search?q=' + key)
    .then(r => r.text())
    .then(html => {
      // 处理动态加载的内容
      // 例如，执行JavaScript生成内容
      return html;
    });
  }",
  // 其他规则...
}
```

### 8.2 反爬虫处理

```javascript
{
  "name": "反爬虫书源",
  "url": "https://example.com",
  "group": "小说",
  "search": "function(key) {
    // 添加反爬虫头
    const headers = {
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
      'Referer': 'https://example.com',
      'Cookie': 'sessionid=123456'
    };
    return fetch('https://example.com/search?q=' + key, { headers })
    .then(r => r.text());
  }",
  // 其他规则...
}
```

### 8.3 多页搜索

```javascript
{
  "name": "多页搜索书源",
  "url": "https://example.com",
  "group": "小说",
  "search": "async function(key) {
    let results = '';
    // 搜索多页
    for (let page = 1; page <= 3; page++) {
      const html = await fetch('https://example.com/search?q=' + key + '&page=' + page).then(r => r.text());
      results += html;
      // 检查是否有下一页
      if (!html.includes('下一页')) break;
    }
    return results;
  }",
  // 其他规则...
}
```

## 9. 工具推荐

1. **浏览器开发者工具**：用于分析网站结构和网络请求
2. **Postman**：用于测试API和网络请求
3. **VS Code**：用于编辑和调试书源代码
4. **JSON格式化工具**：用于格式化和验证JSON
5. **正则表达式测试工具**：用于测试和调试正则表达式

## 10. 常见问题解答

### Q: 书源导入失败怎么办？
A: 检查书源格式是否正确，确保是有效的JSON格式，并且包含必要的字段。

### Q: 搜索结果为空怎么办？
A: 检查搜索URL是否正确，选择器是否匹配网站结构，以及是否需要登录。

### Q: 章节内容加载失败怎么办？
A: 检查内容选择器是否正确，网站是否需要登录，或者内容是否是动态加载的。

### Q: 书源突然失效怎么办？
A: 检查网站是否改版，更新书源规则以适应新的网站结构。

### Q: 如何提高书源的稳定性？
A: 添加错误处理，设置合理的请求头，使用缓存，以及定期更新书源规则。

## 11. 贡献指南

1. ** Fork 仓库**：在GitHub上Fork轻墨仓库
2. **创建分支**：创建一个新的分支用于书源开发
3. **提交书源**：将书源文件提交到仓库
4. **创建PR**：创建Pull Request，描述书源的功能和特点
5. **审核通过**：经过审核后，书源将被合并到主仓库

## 12. 许可证

本开发文档采用 [MIT License](https://opensource.org/licenses/MIT) 许可证。

## 13. 更新日志

### v1.0.0 (2026-03-27)
- 初始版本
- 包含基础模板和高级模板
- 提供完整的内置方法列表
- 详细的选择器语法说明
- 多个示例书源
- 书源测试和优化指南
- 常见问题解答

---

**文档版本**: 1.0.0  
**最后更新**: 2026-03-27  
**维护状态**: ✅ 持续更新
