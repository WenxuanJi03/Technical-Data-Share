#!/bin/bash
# ========================
# 智慧戴湘 - 一键部署脚本
# 使用方法: bash deploy.sh
# 前提: 已安装宝塔面板、JDK1.8、MySQL、Redis、Nginx
# ========================

echo "===== 智慧戴湘技术数据共享系统 - 部署开始 ====="

# ===== 配置区域（根据实际情况修改）=====
APP_NAME="ruoyi-admin"
JAR_NAME="ruoyi-admin.jar"
DEPLOY_DIR="/www/wwwroot/ruoyi"
UPLOAD_DIR="/www/wwwroot/ruoyi-upload"
UI_DIR="/www/wwwroot/ruoyi-ui"
LOG_DIR="/www/wwwroot/ruoyi/logs"
JAVA_OPTS="-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"

# ===== 创建目录 =====
echo "[1/6] 创建部署目录..."
mkdir -p $DEPLOY_DIR
mkdir -p $UPLOAD_DIR
mkdir -p $UI_DIR
mkdir -p $LOG_DIR

# ===== 停止旧进程 =====
echo "[2/6] 停止旧进程..."
PID=$(ps -ef | grep $JAR_NAME | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "停止进程: $PID"
    kill -15 $PID
    sleep 5
    # 强制杀死
    PID=$(ps -ef | grep $JAR_NAME | grep -v grep | awk '{print $2}')
    if [ -n "$PID" ]; then
        kill -9 $PID
    fi
fi

# ===== 部署后端 =====
echo "[3/6] 部署后端 JAR 包..."
if [ -f "$DEPLOY_DIR/$JAR_NAME" ]; then
    mv $DEPLOY_DIR/$JAR_NAME $DEPLOY_DIR/${JAR_NAME}.bak
fi
# 将打包好的 jar 复制到部署目录（需要先 mvn package）
# cp target/ruoyi-admin.jar $DEPLOY_DIR/$JAR_NAME

# ===== 启动后端 =====
echo "[4/6] 启动后端服务..."
nohup java $JAVA_OPTS \
    -jar $DEPLOY_DIR/$JAR_NAME \
    --spring.profiles.active=druid \
    --ruoyi.profile=$UPLOAD_DIR \
    --server.port=8080 \
    > $LOG_DIR/ruoyi.log 2>&1 &

echo "后端服务已启动，PID: $!"

# ===== 部署前端 =====
echo "[5/6] 部署前端文件..."
# 将 ruoyi-ui/dist/ 目录的内容复制到 nginx 托管目录
# cp -rf dist/* $UI_DIR/

# ===== 完成 =====
echo "[6/6] 部署完成！"
echo ""
echo "===== 部署信息 ====="
echo "后端地址: http://localhost:8080"
echo "前端目录: $UI_DIR"
echo "上传目录: $UPLOAD_DIR"
echo "日志文件: $LOG_DIR/ruoyi.log"
echo ""
echo "===== 注意事项 ====="
echo "1. 修改 application-druid.yml 中的数据库连接地址和密码"
echo "2. 修改 application.yml 中的 ruoyi.profile 为 $UPLOAD_DIR"
echo "3. 配置 Nginx 反向代理（参考 nginx.conf）"
echo "4. 如需HTTPS，在宝塔面板中申请免费SSL证书"
echo "5. 微信小程序需要HTTPS域名才能正常访问"
echo "===================="
