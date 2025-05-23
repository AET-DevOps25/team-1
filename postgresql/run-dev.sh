#!/bin/bash

# 构建镜像并启动服务
docker-compose -f docker-compose-dev.yml up --build -d

# 可选：打印数据库连接信息
echo "PostgreSQL is running at: localhost:5432"
echo "Database: hrapp | User: postgres | Password: postgres"
