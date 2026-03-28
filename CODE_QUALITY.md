# 代码质量工具使用指南

本项目已集成多种代码质量检查工具，帮助保持代码质量和一致性。

## 📋 集成的工具

### 1. KtLint
- **用途**: Kotlin代码风格检查和自动格式化
- **特点**: 遵循Kotlin官方代码风格指南
- **自动修复**: ✅ 支持

### 2. Detekt
- **用途**: Kotlin静态代码分析
- **特点**: 检测代码异味、潜在bug、复杂度问题
- **自动修复**: ❌ 部分支持

### 3. Android Lint
- **用途**: Android项目特定问题检查
- **特点**: 检测性能、安全、版本兼容性等问题
- **自动修复**: ❌ 不支持

## 🚀 快速开始

### 安装代码质量工具

在Windows上运行：
```bash
setup-code-quality.bat
```

在macOS/Linux上运行：
```bash
chmod +x .git/hooks/pre-commit
chmod +x .git/hooks/pre-push
```

## 📖 常用命令

### 代码检查命令

```bash
# 检查Kotlin代码风格
./gradlew ktlintCheck

# 运行静态代码分析
./gradlew detekt

# 运行Android Lint检查
./gradlew lint

# 运行所有代码检查（推荐在提交前使用）
./gradlew codeCheck

# 提交前检查（包含所有检查）
./gradlew preCommitCheck
```

### 代码自动修复命令

```bash
# 自动修复Kotlin代码格式问题
./gradlew ktlintFormat

# 自动修复所有可修复的问题
./gradlew codeFix
```

## 🔧 Git钩子

项目已配置Git钩子，在代码提交和推送时自动运行检查：

### pre-commit（提交前）
- 自动运行KtLint格式修复
- 检查Kotlin代码风格
- 运行Detekt静态分析
- 运行Android Lint检查

### pre-push（推送前）
- 运行完整的代码检查
- 运行单元测试

## 📊 检查报告

运行检查后，可以在以下位置查看详细报告：

- **KtLint**: 控制台输出
- **Detekt**: `build/reports/detekt/detekt.html`
- **Android Lint**: `app/build/reports/lint-results.html`

## ⚙️ 配置说明

### KtLint配置

在 `app/build.gradle` 中配置：
```groovy
ktlint {
    version = "0.50.0"
    android = true
    outputToConsole = true
    ignoreFailures = false
    enableExperimentalRules = true
}
```

### Detekt配置

配置文件位于 `config/detekt/detekt.yml`，可以自定义：
- 规则启用/禁用
- 复杂度阈值
- 代码风格规则
- 忽略的文件路径

### 忽略特定检查

在代码中忽略特定检查：
```kotlin
// 忽略特定Detekt规则
@Suppress("MagicNumber")
fun calculate(value: Int) = value * 2

// 忽略KtLint格式检查
// ktlint-disable max_line_length
val veryLongString = "..."
// ktlint-enable max_line_length
```

## 🎯 最佳实践

### 开发流程建议

1. **编写代码** - 正常开发功能
2. **自动修复** - 运行 `./gradlew codeFix` 自动修复格式问题
3. **代码检查** - 运行 `./gradlew codeCheck` 检查代码质量
4. **修复问题** - 根据报告手动修复无法自动修复的问题
5. **提交代码** - Git钩子会自动运行检查

### 常见问题

#### Q: 提交时检查失败怎么办？

A: 运行以下命令自动修复：
```bash
./gradlew codeFix
```

#### Q: 如何跳过Git钩子检查？

A: 在紧急情况下可以使用 `--no-verify` 跳过：
```bash
git commit -m "紧急修复" --no-verify
```

⚠️ **注意**: 跳过检查可能导致代码质量问题进入仓库，请谨慎使用。

#### Q: Detekt报告太多警告怎么办？

A: 可以调整 `config/detekt/detekt.yml` 中的规则配置：
- 提高阈值
- 禁用不需要的规则
- 添加排除文件

#### Q: 如何添加自定义规则？

A: 可以通过创建自定义Detekt规则或KtLint规则来扩展检查功能。

## 📈 持续集成

建议在CI/CD流程中集成代码质量检查：

```yaml
# 示例: GitHub Actions
- name: Code Quality Check
  run: ./gradlew codeCheck

- name: Upload Detekt Report
  uses: actions/upload-artifact@v2
  with:
    name: detekt-report
    path: build/reports/detekt/
```

## 🔗 参考链接

- [KtLint 官方文档](https://pinterest.github.io/ktlint/)
- [Detekt 官方文档](https://detekt.dev/)
- [Android Lint 官方文档](https://developer.android.com/studio/write/lint)
- [Kotlin 代码风格指南](https://kotlinlang.org/docs/coding-conventions.html)

## 🆘 获取帮助

如果遇到问题：

1. 查看检查报告了解详细信息
2. 参考官方文档
3. 在项目中创建Issue寻求帮助

---

**提示**: 保持代码质量是一个持续的过程，建议在每次提交前都运行代码检查。
