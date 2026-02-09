# 智慧戴湘技术数据共享系统 - 部署指南

## 一、购买服务器

### 推荐配置
- **平台**: 阿里云/腾讯云 轻量应用服务器
- **配置**: 2核4G内存 + 50G系统盘 + 500G数据盘
- **系统**: CentOS 7.9 / Ubuntu 20.04
- **带宽**: 5Mbps
- **预算**: 约100-200元/月

### 购买步骤
1. 访问 https://www.aliyun.com/ 或 https://cloud.tencent.com/
2. 注册账号（使用公司营业执照注册企业账号更好）
3. 搜索"轻量应用服务器"
4. 选择配置，购买

## 二、安装宝塔面板

通过SSH连接服务器后执行:

```bash
# CentOS
yum install -y wget && wget -O install.sh https://download.bt.cn/install/install_6.0.sh && sh install.sh ed8484bec

# Ubuntu
wget -O install.sh https://download.bt.cn/install/install-ubuntu_6.0.sh && sudo bash install.sh ed8484bec
```

安装完成后会显示面板地址和账号密码，记下来。

## 三、在宝塔面板安装环境

登录宝塔面板后，安装以下软件:
1. **Nginx** (推荐)
2. **MySQL 8.0**
3. **Redis**
4. **Java项目管理器** (宝塔插件，安装JDK1.8)

## 四、配置数据库

1. 在宝塔面板 -> 数据库 -> 添加数据库
   - 数据库名: DWS
   - 用户名: WSD
   - 密码: 设置一个强密码
   - 权限: 所有人（或指定IP）

2. 导入SQL文件
   - 先导入 `sql/ry_20250522.sql`（主数据库结构）
   - 再导入 `sql/quartz.sql`（定时任务表）
   - 再导入 `sql/tech_menu.sql`（技术模块菜单）
   - 再导入 `sql/mobile_menu.sql`（移动端菜单）
   - 再导入 `sql/create_trial_notice.sql`（试制通知单表）

## 五、修改配置文件

### 5.1 修改数据库连接 (application-druid.yml)
```yaml
master:
    url: jdbc:mysql://localhost:3306/DWS?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8
    username: WSD
    password: 你的新数据库密码
```

### 5.2 修改上传路径 (application.yml)
```yaml
ruoyi:
  profile: /www/wwwroot/ruoyi-upload
```

### 5.3 修改Redis密码（如果设置了密码）
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 你的Redis密码
```

## 六、打包部署

### 6.1 后端打包（在开发电脑上执行）
```bash
cd "E:\HLDX2.0\Technical Data Share"
mvn clean package -DskipTests
```
打包后的文件在: `ruoyi-admin/target/ruoyi-admin.jar`

### 6.2 上传JAR包
将 `ruoyi-admin.jar` 上传到服务器 `/www/wwwroot/ruoyi/` 目录

### 6.3 启动后端
```bash
cd /www/wwwroot/ruoyi
nohup java -Xms256m -Xmx512m -jar ruoyi-admin.jar --spring.profiles.active=druid > logs/ruoyi.log 2>&1 &
```

### 6.4 前端打包（在开发电脑上执行）
```bash
cd ruoyi-ui
npm install
npm run build:prod
```
打包后的文件在: `ruoyi-ui/dist/` 目录

### 6.5 上传前端文件
将 `dist/` 目录下所有文件上传到服务器 `/www/wwwroot/ruoyi-ui/` 目录

### 6.6 配置Nginx
在宝塔面板 -> 网站 -> 添加站点，然后修改Nginx配置，参考 `nginx.conf` 文件

## 七、配置域名和HTTPS（微信小程序必须）

1. 购买域名（阿里云/腾讯云约50元/年）
2. 域名备案（企业备案约1-2周）
3. 在宝塔面板申请免费SSL证书（Let's Encrypt）
4. 开启强制HTTPS

## 八、微信小程序配置

### 8.1 修改小程序API地址
修改 `ruoyi-app/utils/request.js`:
```javascript
const BASE_URL = 'https://your-domain.com/prod-api'
```

### 8.2 微信公众平台配置
1. 登录 https://mp.weixin.qq.com/
2. 设置 -> 开发设置 -> 服务器域名
3. 添加 request 合法域名: `https://your-domain.com`
4. 添加 downloadFile 合法域名: `https://your-domain.com`

### 8.3 上传发布
1. 使用 HBuilderX 打开 ruoyi-app 项目
2. 发行 -> 小程序-微信
3. 在微信开发者工具中上传
4. 在微信公众平台提交审核

## 九、常见问题

### Q: 后端启动失败
A: 检查 MySQL 和 Redis 是否已启动，检查数据库连接配置是否正确

### Q: 前端页面空白
A: 检查 Nginx 配置是否正确，检查 dist 文件是否完整

### Q: 小程序无法访问
A: 确保域名已备案、已配置HTTPS、已在微信公众平台添加合法域名

### Q: 文件上传失败
A: 检查上传目录权限 `chmod -R 755 /www/wwwroot/ruoyi-upload`
