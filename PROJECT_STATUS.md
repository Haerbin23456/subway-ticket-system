# 项目开发进度与技术文档 (Project Status & Documentation)

## 1. 项目概览 (Overview)
- **名称**：地铁网络自助取票系统 (Subway Network Self-Service Ticket System)
- **架构**：B/S 架构 (Browser/Server)
- **核心痛点**：解决地铁站高峰期排队购票慢的问题。
- **方案**：手机端提前选站购票拿二维码 -> 到站自助机扫码验证后出票。

## 2. 技术栈 (Tech Stack)
- **后端 (Backend)**: Java 17+, Spring Boot 3.3, MyBatis-Plus, MySQL 8.0
- **手机端 (Mobile)**: Vue 3 (Composition API), Vite, Native HTML/CSS
- **自助机端 (Kiosk)**: Vue 3, jsQR (扫码库)

---

## 3. 代码结构与功能解析 (Code Structure)
**（PPT 制作重点参考部分）**

### 3.1 后端 (`/backend`)
核心业务逻辑，负责计费、订单、支付和验票。

- **启动入口**: `src/main/java/com/subway/ticket/Application.java`
- **配置文件**: `src/main/resources/application-db.yml` (配置数据库连接，用户名密码在这里)
- **数据初始化**: `src/main/java/com/subway/ticket/config/DataInitializer.java`
  - **功能**: 系统启动时自动运行。
  - **核心逻辑**: 读取 `RMP_*.json` 文件，解析长春地铁线路数据，导入数据库。
  - **关键点**: 包含了自动修复数据库字段长度、自动识别复杂线路连接（图构建）的逻辑。

- **核心控制器 (Controller)**:
  - `web/FareController.java`: **票价计算引擎**。
    - **亮点**: 实现了 **BFS (广度优先搜索)** 图算法。不是简单的查表，而是根据物理连接关系实时计算最短路径（经过几站），从而算出票价。支持环线、支线等复杂路网。
  - `web/OrderController.java`: **订单处理**。处理下单请求，保存订单信息。
  - `web/KioskController.java`: **自助机接口**。负责验证二维码签名、检查订单状态、执行出票操作（幂等性设计）。

- **服务层 (Service)**:
  - `service/QrSignService.java`: **安全签名服务**。
    - **亮点**: 使用 HMAC-SHA256 算法对订单信息进行签名，防止二维码被伪造。

### 3.2 手机端 (`/mobile`)
乘客使用的购票页面。

- **主页面**: `src/App.vue`
  - **功能**: 
    1. 调用后端接口加载所有线路和站点。
    2. 用户选择起终点后，调用 `/api/fares/quote` 获取票价。
    3. 模拟支付后，调用 `/api/orders/{id}/qrcode` 获取二维码数据，并使用 `qrcode` 库生成二维码图片。

### 3.3 自助机端 (`/kiosk`)
地铁站里的取票终端。

- **主页面**: `src/App.vue`
  - **功能**:
    1. 调用摄像头 (navigator.mediaDevices) 获取视频流。
    2. 使用 `jsQR` 库实时分析视频帧，识别二维码。
    3. 识别成功后，调用后端 `/api/kiosk/validate` 接口验票。

---

## 4. 当前进度 (Current Progress)

### 已完成 (Done)
- [x] **后端基础架构**: Spring Boot + MyBatis-Plus + MySQL 环境搭建。
- [x] **数据库设计**: Line, Station, Order, Ticket 表结构设计。
- [x] **数据导入**: 支持从 JSON 文件自动导入长春地铁全量数据，并自动修复断连线路。
- [x] **核心算法**: 基于图 (Graph) 的 BFS 计费算法，支持复杂路网。
- [x] **手机端**: 选站、计费、下单、生成二维码流程跑通。
- [x] **自助机端**: 摄像头扫码、验票、出票流程跑通。

### 待优化 (To Do)
- [ ] 真实的支付网关接入（目前是 Mock 支付）。
- [ ] 界面 UI 美化（目前是原生极简风）。

---

## 5. 常见问题与排错 (Troubleshooting)
**（遇到问题先看这里！）**

### Q1: 端口 `8080` 被占用 (Web server failed to start)
- **原因**: 上一次运行的后端没关干净，或者开了多个终端。
- **解决**: 在 PowerShell 运行以下命令强行杀进程：
  ```powershell
  Get-NetTCPConnection -LocalPort 8080 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
  ```

### Q2: 站点顺序看起来是乱的 / 票价算不对
- **原因**: 原始数据源的顺序有问题。
- **解决**: 后端现在不依赖数据库里的顺序，而是依赖**物理连接图**。只要站点之间物理上连通（A连着B），BFS 算法就能算出正确距离。如果票价是对的，就不用管数据库里列表顺序乱不乱。

### Q3: Maven 编译报错 `MojoExecutionException` 或 `ClassNotFoundException`
- **原因**: 可能是依赖没下载全，或者命令输错了。
- **解决**: 
  1. 确保在项目根目录运行。
  2. 使用完整命令：`mvn -f backend/pom.xml spring-boot:run`
  3. 如果还不行，试着先清理再打包：`mvn -f backend/pom.xml clean package`

### Q5: 启动报错 `UnsatisfiedDependencyException` 或 `SqlSessionFactory` 无法初始化
- **原因**: Spring Boot 没有加载数据库配置。这通常是因为 `application.yml` 中配置了排除项，或者未激活 `db` profile。
- **解决**: 
  1. 确保 `backend/src/main/resources/application.yml` 中设置了 `spring.profiles.active: db`。
  2. 确保没有在 `autoconfigure.exclude` 中排除数据库相关类。
  3. 我们已经在代码库中修复了这个问题，确保拉取最新代码。

### Q6: 启动报错 `Communications link failure` 或 `Connection refused`
- **原因**: 后端连接不上数据库。
- **解决**:
  1. **检查 MySQL 是否启动**: 必须确保本地 MySQL 服务正在运行。
  2. **检查配置**: 查看 `backend/src/main/resources/application-db.yml` 中的用户名密码是否正确。
  3. **检查端口**: 默认连接 `localhost:3306`。

### Q4: 手机/自助机页面打不开或报错
- **原因**: 前端项目没有启动，或者 Node.js 依赖没装好。
- **解决**: 
  1. 确保进入了对应目录 (`cd mobile` 或 `cd kiosk`)。
  2. 运行 `npm install` (只需运行一次)。
  3. 运行 `npm run dev` 启动服务。
