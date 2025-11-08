#!/bin/bash

# GFMirror 约定检查脚本
echo "🔍 开始检查代码约定遵守情况..."

# 检查硬编码字符串
echo "📝 检查硬编码字符串..."
HARDCODED_STRINGS=$(grep -r '"[A-Z][a-z][^"]*"' app/src/main/java --exclude-dir=test | grep -v "import\|package\|//\|R\.string\|R\.drawable\|Exception\|English" | wc -l)
if [ $HARDCODED_STRINGS -gt 0 ]; then
    echo "❌ 发现 $HARDCODED_STRINGS 个硬编码字符串"
    grep -r '"[A-Z][a-z][^"]*"' app/src/main/java --exclude-dir=test | grep -v "import\|package\|//\|R\.string\|R\.drawable\|Exception\|English"
else
    echo "✅ 无硬编码字符串"
fi

# 检查演示代码
echo "🎭 检查演示代码..."
DEMO_CODE=$(grep -r "TODO\|FIXME\|placeholder\|demo\|mock" app/src/main/java -i | grep -v "// This will be localized\|// This is a placeholder\|camera_preview_placeholder\|toDouble" | wc -l)
if [ $DEMO_CODE -gt 0 ]; then
    echo "❌ 发现 $DEMO_CODE 个演示代码"
    grep -r "TODO\|FIXME\|placeholder\|demo\|mock" app/src/main/java -i | grep -v "// This will be localized\|// This is a placeholder\|camera_preview_placeholder\|toDouble"
else
    echo "✅ 无演示代码"
fi

# 检查重复实现
echo "🔄 检查重复实现..."
DUPLICATE_EXECUTORS=$(grep -r "Executors\.newSingleThreadExecutor\|Executors\.newSingleThreadScheduledExecutor" app/src/main/java | grep -v "ExecutorManager" | wc -l)
if [ $DUPLICATE_EXECUTORS -gt 0 ]; then
    echo "❌ 发现 $DUPLICATE_EXECUTORS 个重复Executor创建"
    grep -r "Executors\.newSingleThreadExecutor\|Executors\.newSingleThreadScheduledExecutor" app/src/main/java | grep -v "ExecutorManager"
else
    echo "✅ 无重复Executor创建"
fi

# 检查重复异常
echo "⚠️ 检查重复异常创建..."
DUPLICATE_EXCEPTIONS=$(grep -r "IllegalStateException.*not initialized" app/src/main/java | grep -v "ExceptionFactory" | wc -l)
if [ $DUPLICATE_EXCEPTIONS -gt 0 ]; then
    echo "❌ 发现 $DUPLICATE_EXCEPTIONS 个重复异常创建"
    grep -r "IllegalStateException.*not initialized" app/src/main/java | grep -v "ExceptionFactory"
else
    echo "✅ 无重复异常创建"
fi

# 检查重复颜色定义
echo "🎨 检查重复颜色定义..."
DUPLICATE_COLORS=$(grep -r "Color\." app/src/main/java | grep -v "ColorManager\|import.*Color" | wc -l)
if [ $DUPLICATE_COLORS -gt 5 ]; then
    echo "❌ 发现 $DUPLICATE_COLORS 个颜色定义，建议使用ColorManager"
    grep -r "Color\." app/src/main/java | grep -v "ColorManager\|import.*Color" | head -5
else
    echo "✅ 颜色定义合理"
fi

# 检查多语言支持
echo "🌍 检查多语言支持..."
MISSING_STRINGS=$(find app/src/main/res -name "strings.xml" | wc -l)
if [ $MISSING_STRINGS -lt 5 ]; then
    echo "❌ 多语言资源文件不完整，需要5个语言文件"
else
    echo "✅ 多语言支持完整"
fi

echo "🎯 检查完成！"