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
const DEV_CONFIG = {
  baseUrl: 'http://192.168.1.193:8080',
}

// 生产环境配置（正式域名，必须HTTPS）
const PROD_CONFIG = {
  // ★★★ 替换为你的正式域名 ★★★
  baseUrl: 'https://your-domain.com/prod-api',
}

const config = IS_PRODUCTION ? PROD_CONFIG : DEV_CONFIG

export default config
