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
// 手机无法解析 Windows 主机名，只能用 IP 地址
// - 固定IP(193): 路由器已做静态绑定，换WiFi也能用
// - 当前IP(66): 另一个 WiFi 下的地址，IP 变动时需更新
const DEV_CONFIG = {
  serverList: [
    { name: '固定IP(193)', url: 'http://192.168.1.193:8080' },
    { name: '当前IP(66)', url: 'http://192.168.1.66:8080' },
  ],
  baseUrl: 'http://192.168.1.193:8080',
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
