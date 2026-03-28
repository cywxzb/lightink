# 轻墨 JS 书源示例

## 1. 基础书源示例

```javascript
{
  "name": "示例书源",
  "url": "https://example.com",
  "group": "小说",
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

## 2. 高级书源示例（带脚本）

```javascript
{
  "name": "高级示例书源",
  "url": "https://example.com",
  "group": "小说",
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

## 3. 动态加载书源示例

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
  "content": ".chapter-content"
}
```

## 4. 反爬虫书源示例

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
  "content": ".chapter-content"
}
```

## 5. 多页搜索书源示例

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
  "content": ".chapter-content"
}
```

## 6. 登录书源示例

```javascript
{
  "name": "登录书源",
  "url": "https://example.com",
  "group": "小说",
  "loginUrl": "https://example.com/login",
  "loginBody": "username={{username}}&password={{password}}",
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
  "content": ".chapter-content"
}
```

## 7. 带登录脚本的书源示例

```javascript
{
  "name": "带登录脚本的书源",
  "url": "https://example.com",
  "group": "小说",
  "login": "function(username, password) {
    return fetch('https://example.com/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: 'username=' + username + '&password=' + password
    })
    .then(r => r.text())
    .then(html => {
      // 检查登录是否成功
      if (html.includes('登录成功')) {
        return true;
      } else {
        return false;
      }
    });
  }",
  "search": "function(key) {
    return fetch('https://example.com/search?q=' + key)
    .then(r => r.text());
  }",
  "searchList": "div.book-item",
  "bookName": "h3.title",
  "bookUrl": "a@href",
  "author": ".author",
  "cover": ".cover img@src",
  "detail": "function(url) {
    return fetch(url)
    .then(r => r.text());
  }",
  "introduce": ".intro",
  "chapterList": "ul.chapter-list li",
  "chapterName": "a",
  "chapterUrl": "a@href",
  "content": "function(url) {
    return fetch(url)
    .then(r => r.text())
    .then(html => {
      let content = html.match(/<div class=\"chapter-content\">([\s\S]*?)<\/div>/)[1];
      return content;
    });
  }"
}
```

## 8. 带内容处理的书源示例

```javascript
{
  "name": "带内容处理的书源",
  "url": "https://example.com",
  "group": "小说",
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
  "content": "function(url) {
    return fetch(url)
    .then(r => r.text())
    .then(html => {
      // 提取内容
      let content = html.match(/<div class=\"content\">([\s\S]*?)<\/div>/)[1];
      // 清理内容
      content = content.replace(/<script[^>]*>[\S]*?<\/script>/g, '');
      content = content.replace(/<style[^>]*>[\S]*?<\/style>/g, '');
      content = content.replace(/&nbsp;/g, ' ');
      content = content.replace(/<br\s*\/>/g, '\n');
      // 添加章节标题
      let title = html.match(/<h1 class=\"chapter-title\">([\s\S]*?)<\/h1>/)[1];
      content = '<h1>' + title + '</h1>\n' + content;
      return content;
    });
  }"
}
```

## 9. 带分页的书源示例

```javascript
{
  "name": "带分页的书源",
  "url": "https://example.com",
  "group": "小说",
  "searchUrl": "https://example.com/search?q={{key}}&page={{page}}",
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
  "nextPage": ".next-page@href"
}
```

## 10. 带XPath选择器的书源示例

```javascript
{
  "name": "带XPath选择器的书源",
  "url": "https://example.com",
  "group": "小说",
  "searchUrl": "https://example.com/search?q={{key}}",
  "search": "function(key) {
    return fetch('https://example.com/search?q=' + key)
    .then(r => r.text())
    .then(html => {
      // 使用XPath选择器
      let books = xpath(html, '//div[@class="book-item"]');
      let results = '';
      books.forEach(book => {
        results += book.outerHTML;
      });
      return results;
    });
  }",
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
  "content": ".chapter-content"
}
```

---

**文档版本**: 1.0.0  
**最后更新**: 2026-03-27  
**维护状态**: ✅ 持续更新
