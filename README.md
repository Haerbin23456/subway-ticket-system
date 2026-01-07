# 🚇 地铁自助取票系统 (Subway Ticket System)

这是一个基于 **Spring Boot 3 + Vue 3** 的全栈地铁自助购票系统演示项目，模拟了从手机端购票到线下自助机取票的完整业务流程。项目基于杭州地铁真实数据，实现了基于图算法的路径规划与计费。

## ✨ 核心功能

*   **🗺️ 真实路网计费**：内置杭州地铁线路数据，使用 BFS 算法计算任意两站间的最短路径与票价。
*   **📱 手机端购票 (Mobile)**：
    *   可视化地铁线路图选择站点
    *   历史购票记录查询
    *   模拟支付与二维码生成
*   **🤖 自助机取票 (Kiosk)**：
    *   模拟线下自助终端界面
    *   摄像头扫码核销
    *   出票动画演示
*   **⚙️ 自动化运维**：
    *   **自动建表**：提供 SQL 脚本快速初始化。
    *   **自动数据导入**：后端启动时自动检测并加载内置的地铁线路 JSON 数据到数据库。

## 🛠️ 技术栈

*   **后端**：Java 17+, Spring Boot 3, MyBatis-Plus, MySQL 8.0
*   **前端**：Vue 3, Vite, Element Plus, QR Code
*   **工具**：Maven, npm/yarn

## 📂 项目结构

```text
subway-ticket-system/
├── backend/          # 后端服务 (Spring Boot)
│   ├── src/main/resources/
│   │   ├── application-db.yml  # 数据库配置
│   │   └── hangzhou_subway.json # 内置地铁数据
├── mobile/           # 手机端前端 (Vue 3 Mobile)
├── kiosk/            # 自助机前端 (Vue 3 Web)
├── db/               # 数据库脚本
│   └── schema.sql    # 初始化建表语句
└── README.md         # 项目说明文档
```

## 🚀 快速启动 (Quick Start)

### 1. 环境准备
确保你的开发环境已安装：
*   Java 17+
*   Maven 3.6+
*   Node.js 18+
*   MySQL 8.0+

### 2. 数据库配置
1.  **创建数据库**：
    ```sql
    CREATE DATABASE subway_ticket_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  **初始化表结构**：
    运行项目根目录下的 `db/schema.sql` 脚本，创建所需的数据表。
3.  **配置连接**：
    打开 `backend/src/main/resources/application-db.yml`，修改数据库连接信息（如果你的密码不是 `123456`）：
    ```yaml
    spring:
      datasource:
        password: YOUR_PASSWORD
    ```

### 3. 运行项目

#### 方案 A：IntelliJ IDEA 一键运行（推荐）
本项目包含预置的 IntelliJ IDEA 运行配置 (`.idea/runConfigurations`)，无需手动输入命令。
1.  **Backend**: 在 IDEA 顶部运行配置下拉框选择 `Backend`，点击运行 ▶。
2.  **Mobile**: 选择 `Mobile` 并运行。启动后访问 `http://localhost:5173`。
3.  **Kiosk**: 选择 `Kiosk` 并运行。启动后访问 `http://localhost:5174`。

#### 方案 B：命令行运行

**1. 启动后端 (Backend)**
进入 `backend` 目录并运行：
```powershell
cd backend
mvn spring-boot:run
```
> **提示**：首次启动时，系统会自动读取 classpath 下的 `hangzhou_subway.json` 并导入地铁线路数据到数据库。

**2. 启动前端 (Frontends)**

**启动手机端 (Mobile)**：
```powershell
cd mobile
npm install
npm run dev
```
*   访问地址：`http://localhost:5173`
*   功能：选择起终点 -> 支付 -> 获取二维码

**启动自助机端 (Kiosk)**：
```powershell
cd kiosk
npm install
npm run dev
```
*   访问地址：`http://localhost:5174`
*   功能：点击屏幕 -> 扫描手机端的二维码 -> 出票

## 📱 演示流程 (Demo Script)

1.  **手机下单**：
    *   在浏览器打开 **手机端** (`http://localhost:5173`)。
    *   点击“开始购票”，选择 **起点** 和 **终点**。
    *   点击“确认支付”，系统生成取票二维码。
2.  **线下取票**：
    *   在浏览器打开 **自助机端** (`http://localhost:5174`)。
    *   点击屏幕进入扫码状态（请允许浏览器使用摄像头）。
    *   将手机端的二维码对准电脑摄像头（或者用手机拍下屏幕上的二维码，再对准摄像头）。
    *   系统识别成功后，显示“出票成功”并打印票据详情。

## ❓ 常见问题

**Q: 启动后端时提示 "Address already in use"？**
A: 8080 端口被占用。请关闭占用该端口的进程，或在 `application.yml` 中修改 `server.port`。

**Q: 数据库没有数据？**
A: 请检查后端启动日志。正常情况下会看到 `Starting data import from classpath: hangzhou_subway.json`。如果表已存在数据，系统可能会跳过重复导入。可以尝试清空数据库表后重启后端。
