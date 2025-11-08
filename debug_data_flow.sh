#!/bin/bash

echo "=== GFMirror 数据流调试脚本 ==="
echo "开始监控应用日志..."
echo "请启动应用并观察日志输出"
echo "按 Ctrl+C 停止监控"
echo ""

# 清除之前的日志
adb logcat -c

# 监控关键日志
adb logcat | grep -E "(GFMirror|UnifiedCameraPreview|SimpleOpenGLMirrorView|ImageAnalysis|Bitmap)" --line-buffered
