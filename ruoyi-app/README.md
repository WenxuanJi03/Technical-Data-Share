# 技术管理系统 - 微信小程序

基于 uni-app 开发的微信小程序，对接 RuoYi 后端API。

## 快速开始

### 方式一：HBuilderX（推荐）

1. 下载安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)
2. 用 HBuilderX 打开 `ruoyi-app` 目录
3. 修改 `manifest.json` 中的微信小程序 AppID
4. 修改 `utils/request.js` 中的 `BASE_URL` 为你的后端地址
5. 菜单 -> 运行 -> 运行到小程序模拟器 -> 微信开发者工具

### 方式二：命令行

```bash
cd ruoyi-app
npm install
npm run dev:mp-weixin
```

然后用微信开发者工具打开 `dist/dev/mp-weixin` 目录。

## 重要配置

### 1. 后端地址
编辑 `utils/request.js`，修改 `BASE_URL`：
```javascript
const BASE_URL = 'http://你的服务器IP:8080'
```

### 2. 小程序AppID
编辑 `manifest.json`，在 `mp-weixin.appid` 处填入你的AppID。

### 3. 微信开发者工具设置
开发阶段需要在微信开发者工具中：
- 设置 -> 项目设置 -> 勾选"不校验合法域名"

### 4. TabBar图标
`static/tab/` 目录下需要放入PNG图标（81x81像素），
当前是占位文件，建议从 iconfont.cn 下载替换。

## 功能模块

- 登录页（账号密码 + 验证码）
- 首页（4宫格菜单 + 用户信息 + 统计数据）
- 试制任务列表（进行中/已完成切换 + 搜索 + 下拉刷新）
- 任务详情（产品信息概览 + 11道工序数据录入 + 历史记录）
- 新产品评审（框架）
- 文件管理（框架）
- 工单管理（框架）
- 我的（个人信息 + 退出登录）
