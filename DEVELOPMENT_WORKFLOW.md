# GFMirror 开发工作流程

## 🎯 开发原则

### 零倒退原则
- 绝不允许版本质量下降
- 新功能必须建立在稳定基础上
- 每个改动都要确保不影响现有功能
- 现有用户体验永远是第一位的

## 📋 开发前检查清单

### 1. 需求分析阶段
- [ ] 理解功能需求
- [ ] 识别技术难点
- [ ] 设计解决方案
- [ ] 评估对现有功能的影响

### 2. 编码阶段
- [ ] 使用英语编写所有代码和注释
- [ ] 避免硬编码字符串，使用多语言资源
- [ ] 检查是否有重复功能实现
- [ ] 抽取公共方法，避免重复代码

### 3. 提交前检查
```bash
# 运行约定检查脚本
./check_conventions.sh

# 检查编译错误
./gradlew build

# 检查代码质量
./gradlew lint
```

## 🔍 代码质量检查

### 自动检查命令
```bash
# 检查硬编码字符串
grep -r "[A-Z][a-z].*" app/src/main/java --exclude-dir=test

# 检查演示代码
grep -r "TODO\|FIXME\|placeholder\|demo\|mock" app/src/main/java -i

# 检查重复实现
grep -r "ExecutorService\|SharedPreferences\|Bitmap\.create" app/src/main/java
```

### 手动检查项
- [ ] 所有用户可见文本使用字符串资源
- [ ] 无任何演示代码或TODO注释
- [ ] 同一功能只有唯一实现
- [ ] 重复使用的常量抽取为公共管理器
- [ ] 遵循Android开发规范

## 🏗️ 架构原则

### 模块化设计
- 每个模块职责单一
- 模块间低耦合
- 接口驱动开发

### 代码复用
- 公共功能抽取为管理器
- 避免重复实现
- 统一配置管理

### 质量保证
- 接口定义清晰
- 实现符合规范
- 测试覆盖完整

## 🚀 功能开发流程

### 1. 功能分析
- 理解功能需求
- 识别技术难点
- 设计实现方案

### 2. 接口设计
- 定义清晰的接口
- 考虑扩展性
- 遵循命名规范

### 3. 实现开发
- 实现接口功能
- 遵循代码规范
- 避免重复代码

### 4. 质量检查
- 运行检查脚本
- 验证约定遵守
- 测试功能正确性

### 5. 集成测试
- 验证不影响现有功能
- 确保性能稳定
- 检查用户体验

## 📚 参考资源

- [Android开发规范](https://developer.android.com/guide)
- [Kotlin编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- [Jetpack Compose最佳实践](https://developer.android.com/jetpack/compose)