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
// 1. 主机名(仅电脑有效): http://JWX.local:8080
// 2. 固定IP(需在路由器设置静态绑定): http://192.168.1.193:8080
// 3. 当前IP(换WiFi后需更新): http://192.168.1.66:8080
// ★ 真机调试时：手机无法解析 JWX.local，请在登录页切换为「固定IP」或「当前IP」，或把下方 baseUrl 改为你电脑的局域网 IP
const DEV_CONFIG = {
  // 默认服务器列表(可选择)，真机调试请选带 IP 的项
  serverList: [
    { name: '主机名(JWX)', url: 'http://JWX.local:8080' },
    { name: '主机名(JWX)', url: 'http://JWX:8080' },
    { name: '固定IP(193)', url: 'http://192.168.1.193:8080' },
    { name: '当前IP(66)', url: 'http://192.168.1.66:8080' },
  ],
  // 默认使用主机名；真机调试请改为如 http://192.168.1.66:8080（你电脑在局域网中的 IP）
  baseUrl: 'http://JWX.local:8080',
}

// 生产环境配置（小程序上线必须满足）：
// 1. baseUrl 必须为 HTTPS，且为已备案域名（不能为 IP）
// 2. 微信公众平台 → 开发管理 → 开发设置 → 服务器域名：
//    - request 合法域名、uploadFile 合法域名、downloadFile 合法域名 均需配置该域名
// 3. 图片与文件下载均使用此 baseUrl，上线前请将 IS_PRODUCTION 改为 true 并填写下方地址
const PROD_CONFIG = {
  baseUrl: 'https://your-domain.com/prod-api',
}

const config = IS_PRODUCTION ? PROD_CONFIG : DEV_CONFIG

export default config
