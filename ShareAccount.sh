#!/bin/bash

ensure_log_directory() {
    local TARGET_DIR=$1
    local LOG_PATH="$TARGET_DIR/logs"
    echo "Ensuring log directory exists..."
    mkdir -p "$LOG_PATH"
    chown -R springboot:springboot "$LOG_PATH"
    chmod -R 755 "$LOG_PATH"
}

check_service_status() {
    local SERVICE_NAME=$1
    echo "Checking $SERVICE_NAME service status..."
    sudo systemctl status "$SERVICE_NAME"
}

stop_service() {
    local SERVICE_NAME=$1
    echo "Stopping $SERVICE_NAME service..."
    sudo systemctl stop "$SERVICE_NAME"
}

start_service() {
    local SERVICE_NAME=$1
    echo "Starting $SERVICE_NAME service..."
    sudo systemctl start "$SERVICE_NAME"
}

get_latest_jar() {
    local JAR_DIR=$1
    local LATEST_JAR=$(find "$JAR_DIR" -maxdepth 1 -type f -name "share-account*.jar" | sort -k1,1nr | head -n1)
    
    if [ -z "$LATEST_JAR" ]; then
        echo "ERROR: No JAR file starting with 'share-account' found in $JAR_DIR!"
        exit 1
    fi
    
    echo "$(basename "$LATEST_JAR")"
}

if [ "$1" == "restart" ]; then
    echo "shareaccount服务重启开始 start---------------------."
    TARGET_DIR="/srv/shareaccount"
    SERVICE_NAME="shareaccount"

    ensure_log_directory "$TARGET_DIR"
    stop_service "$SERVICE_NAME"
    start_service "$SERVICE_NAME"
    check_service_status "$SERVICE_NAME"
    echo "shareaccount服务重启完成 completed.---------------------------"

elif [ "$1" == "stop" ]; then
    echo "shareaccount服务停止开始 start---------------------."
    SERVICE_NAME="shareaccount"
    stop_service "$SERVICE_NAME"
    check_service_status "$SERVICE_NAME"
    echo "shareaccount服务停止完成 completed.---------------------------"

else
    echo "shareaccount部署开始 start---------------------."

    JAR_SOURCE_DIR="/root"
    JAR_NAME=$(get_latest_jar "$JAR_SOURCE_DIR")
    
    BACKUP_DIR="/srv/shareaccount/backups"
    TARGET_DIR="/srv/shareaccount"
    SERVICE_NAME="shareaccount"

    mkdir -p "$BACKUP_DIR"
    mkdir -p "$TARGET_DIR"

    stop_service "$SERVICE_NAME"

    echo "Backing up old version..."
    if [ -f "$TARGET_DIR/$JAR_NAME" ]; then
        cp "$TARGET_DIR/$JAR_NAME" "$BACKUP_DIR/${JAR_NAME}_$(date +%Y%m%d%H%M%S).bak"
    else
        echo "No old JAR file found, skipping backup."
    fi

    echo "Moving latest JAR file ($JAR_NAME) to target directory..."
    mv "$JAR_SOURCE_DIR/$JAR_NAME" "$TARGET_DIR/"

    echo "Setting file permissions..."
    chown springboot:springboot "$TARGET_DIR/$JAR_NAME"
    chmod 644 "$TARGET_DIR/$JAR_NAME"

    ensure_log_directory "$TARGET_DIR"

    echo "Configuring systemd service..."
    SERVICE_FILE="/etc/systemd/system/$SERVICE_NAME.service"
    sudo tee "$SERVICE_FILE" > /dev/null <<EOL
[Unit]
Description=ShareAccount Service
After=network.target mysql.service

[Service]
User=springboot
Group=springboot
WorkingDirectory=$TARGET_DIR
# 1. 修改 ExecStart：使用 JDK 21 的 java 路径（替换为你的实际路径）
ExecStart=/opt/jdk-21.0.5/bin/java -Dspring.profiles.active=prod -jar $TARGET_DIR/$JAR_NAME
# 2. （可选）添加 JAVA_HOME 环境变量（与另一个项目保持一致）
Environment="JAVA_HOME=/opt/jdk-21.0.5"
# 3. （可选）添加日志目录预处理（确保权限）
ExecStartPre=/bin/mkdir -p $TARGET_DIR/logs
ExecStartPre=/bin/chown -R springboot:springboot $TARGET_DIR/logs
ExecStartPre=/bin/chmod -R 755 $TARGET_DIR/logs
SuccessExitStatus=143
Restart=always  # 可改为 always，提高服务稳定性
RestartSec=5

[Install]
WantedBy=multi-user.target
EOL

    echo "Reloading systemd daemon..."
    sudo systemctl daemon-reload
    start_service "$SERVICE_NAME"
    check_service_status "$SERVICE_NAME"

    echo "shareaccount部署完成 completed.---------------------------"
fi
