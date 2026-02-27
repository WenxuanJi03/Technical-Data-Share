/**
 * 环境配置文件
 * 
 * 开发环境: 使用局域网IP直接访问后端
 * 生产环境: 使用HTTPS域名通过Nginx代理访问后端
 * 
 * 切换方式: 修改下方 IS_PRODUCTION 变量
 * - 开发调试时设为 false
 * - 发布上线时设为 true
 */

// ★★★ 发布前改为 true ★★★
const IS_PRODUCTION = false

// 开发环境配置（局域网IP）
// 支持多种配置方式,按优先级使用:
// 1. 主机名(推荐,IP变化后无需修改): http://JWX.local:8080
// 2. 固定IP(需在路由器设置静态绑定): http://192.168.1.193:8080
// 3. 当前IP(换WiFi后需更新): http://192.168.1.66:8080
const DEV_CONFIG = {
  // 默认服务器列表(可选择)
  serverList: [
    { name: '主机名(JWX)', url: 'http://JWX.local:8080' },
    { name: '主机名(JWX)', url: 'http://JWX:8080' },
    { name: '固定IP(193)', url: 'http://192.168.1.193:8080' },
    { name: '当前IP(66)', url: 'http://192.168.1.66:8080' },
  ],
  // 默认使用主机名
  baseUrl: 'http://JWX.local:8080',
}

// 生产环境配置(正式域名,必须HTTPS)
const PROD_CONFIG = {
  // ★★★ 替换为你的正式域名 ★★★
  baseUrl: 'https://your-domain.com/prod-api',
}

const config = IS_PRODUCTION ? PROD_CONFIG : DEV_CONFIG

export default config
