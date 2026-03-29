---
name: "document-generator"
description: "Automatically generates project documentation and migration records. Invoke when user needs documentation for changes, migration records, or project summaries."
---

# 文档生成器

## 功能

此智能体能够自动生成项目文档和迁移记录，包括：

- 迁移工作文档
- 修改文件清单
- 代码变更示例
- 后续改进建议
- 项目总结报告
- API 文档
- 架构设计文档

## 使用方法

当遇到以下情况时调用此智能体：

1. 用户需要变更的文档记录
2. 完成迁移工作后
3. 需要项目总结报告
4. 生成架构或 API 文档
5. 需要技术分享或知识传承材料

## 文档结构

### 迁移文档模板

```markdown
# [迁移名称] 文档

## 概述
简要描述迁移工作

## 迁移时间
YYYY-MM-DD

## 修改文件清单
- 文件路径 1
- 文件路径 2
- ...

## 主要变更内容
### 1. [变更类型 1]
详细描述变更内容和代码示例

### 2. [变更类型 2]
详细描述变更内容和代码示例

## Git 提交信息
相关的 git commit 记录

## 后续改进建议
- 建议 1
- 建议 2

## 注意事项
重要的注意事项

## 参考资料
相关链接和参考
```

## 文档类型

### 1. 迁移文档
适用于：
- 库迁移（如 ViewBinding 迁移）
- 架构重构
- 技术栈升级

内容包括：
- 修改文件完整清单
- 迁移前后代码对比
- 遇到的问题和解决方案
- 注意事项和最佳实践

### 2. 项目总结文档
适用于：
- 项目里程碑
- 季度/年度总结
- 功能发布总结

内容包括：
- 完成的功能列表
- 技术亮点
- 性能改进
- 团队贡献

### 3. 架构文档
适用于：
- 新系统设计
- 架构重构
- 模块设计

内容包括：
- 架构图描述
- 模块关系
- 数据流
- 设计决策

## 生成示例

### ViewBinding 迁移文档示例

```markdown
# Kotlin Android Extensions 到 ViewBinding 迁移文档

## 概述
将项目从 `kotlinx.android.synthetic` 迁移到 ViewBinding

## 修改文件清单
- `app/src/main/java/cn/lightink/reader/ui/main/MainActivity.kt`
- `app/src/main/java/cn/lightink/reader/ui/discover/DiscoverFragment.kt`
- ... (60+ 文件)

## 主要变更内容

### Activity 迁移
```kotlin
// 迁移前
import kotlinx.android.synthetic.main.activity_main.*

// 迁移后
import cn.lightink.reader.databinding.ActivityMainBinding
private lateinit var binding: ActivityMainBinding
```

## 后续改进建议
1. 恢复 QuickJS 库
2. 恢复 BuildConfig.DEBUG
```

## 最佳实践

1. **详细记录** - 记录所有变更，包括小的调整
2. **代码示例** - 提供迁移前后的代码对比
3. **问题记录** - 记录遇到的问题和解决方案
4. **结构化** - 使用清晰的标题和章节组织内容
5. **可操作** - 提供具体的后续改进建议

## 注意事项

- 文档应该在工作完成后尽快生成
- 确保文件清单的完整性和准确性
- 代码示例应该真实可运行
- 考虑目标读者的技术背景
- 中英文项目可以提供双语文档

## 输出位置

文档通常生成在项目根目录，文件名格式：
- `[迁移名称]文档.md`
- `[项目名称]总结.md`
- `[功能名称]架构.md`
