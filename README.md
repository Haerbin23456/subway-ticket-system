# 地铁自助取票系统 (Subway Ticket System)

这是一个基于 **Spring Boot + Vue 3** 的地铁自助购票系统演示项目，包含**手机端购票**和**自助机取票**两个端。

核心功能：
- **真实路网计费**：基于长春地铁真实数据，支持任意站点间的最短路径计费（BFS算法）。
- **全流程演示**：手机选站 -> 计费 -> 下单支付 -> 生成二维码 -> 自助机扫码 -> 验证出票。

## 🚀 快速启动 (Quick Start)

### 1. 环境准备
确保你的电脑安装了：
- Java 17+ (推荐 Java 21)
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 2. 数据库配置
1. 登录 MySQL，创建一个空数据库 `subway_ticket_system`：
   ```sql
   CREATE DATABASE subway_ticket_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. 确保 root 密码为 `123456`（如果不是，请修改 `backend/src/main/resources/application-db.yml`，把密码改成你自己的 root 密码）。

### 3. 运行项目

#### 方案 A：IDEA 一键运行（首选 / Preferred）
本项目包含预置的 IntelliJ IDEA 运行配置 (`.idea/runConfigurations`)，无需手动输入命令。

1. **Backend**: 在顶部运行配置下拉框选择 `Backend`，点击运行 ▶。
2. **Mobile**: 选择 `Mobile` 并运行。启动后访问 `http://localhost:5173`。
3. **Kiosk**: 选择 `Kiosk` 并运行。启动后访问 `http://localhost:5174`。

> **提示**: 前端项目已配置为监听 `0.0.0.0`，因此你也可以使用局域网 IP (如 `http://192.168.x.x:5173`) 在真实手机上访问。

#### 方案 B：命令行运行 (Command Line)

### 3. 启动后端 (Backend)
打开一个终端 (Terminal 1)，运行：
```powershell
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE='db'; mvn -f backend/pom.xml spring-boot:run
```
> **启动之后请不要关闭！**
> **注意**：首次启动时，系统会自动导入 `RMP_*.json` 中的地铁数据。如果遇到 `Data truncation` 错误，请尝试删除数据库重新创建，系统会自动修复字段长度。

### 4. 启动“手机端” (Mobile Web) （实则目前是在电脑浏览器打开）
打开另一个终端 (Terminal 2)，运行：
```powershell
cd mobile
npm install
npm run dev
```
启动之后也不要关闭
然后访问：`http://localhost:5173`
直接在电脑浏览器打开就行
其实没适配手机尺寸

### 3.3 启动自助机端 (Kiosk Web)
打开第三个终端 (Terminal 3)，运行：
```powershell
cd kiosk
npm install
npm run dev
```
启动之后也不要关闭
访问：`http://localhost:5174`
还是在电脑浏览器打开就行

---

## 📱 演示流程 (Demo Script)

1. **手机端 (Terminal 2 -> 5173)**：
   - 选择起点（例如：1号线-红嘴子）、终点（例如：1号线-华庆路）。
   - 点击 **“试算票价”**，查看价格（基于真实距离计算）。
   - 点击 **“下单并支付”**，生成二维码。

2. **自助机端 (Terminal 3 -> 5174)**：
   - 在电脑浏览器打开，**允许摄像头权限**。
   - 拿展示刚才生成的二维码，对着电脑摄像头扫一扫。
   - (如果你是电脑打开的手机端，你可以先用手机拍下二维码，然后再去自助机端，把手机对着电脑摄像头扫描)
   - 系统验证通过，显示“出票成功”。

## 🛠️ 常见问题 (FAQ)

**Q: 为什么有些站点顺序看起来是乱的？**
A: 数据库中的显示顺序可能与物理顺序不一致，但**不影响计费**。后端已升级为基于图算法（Graph）计费，只要物理连接存在，票价计算就是正确的。

**Q: 端口被占用？**
A: 运行以下命令清理占用 8080 端口的进程：
```powershell
Get-NetTCPConnection -LocalPort 8080 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```
