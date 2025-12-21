// 测试@dcloudio/vite-plugin-uni的导入方式
import * as plugin from '@dcloudio/vite-plugin-uni'
console.log('插件导出内容:', plugin)
console.log('默认导出是否是函数:', typeof plugin.default === 'function')
console.log('是否有uniPlugin属性:', 'uniPlugin' in plugin)
console.log('uniPlugin是否是函数:', typeof plugin.uniPlugin === 'function')