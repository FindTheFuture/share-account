<template>
  <view
    :class="['custom-icon', iconClass]"
    :style="computedStyle"
    @tap="handleClick"
  ></view>
</template>

<script>
import { themeIconColor } from '../utils.js';

export default {
  props: {
    type: {
      type: String,
      required: true,
      description: "图标类型（对应iconfont.css中的类名，如'canyin'）"
    },
    size: {
      type: Number,
      default: 30,
      description: "图标尺寸（默认30px）"
    },
    color: {
      type: String,
      default: null,
      description: "图标颜色：设置则使用该颜色，不设置则使用默认"
    }
  },
  computed: {
    iconClass() {
      return `icon-${this.type}`; // 已包含iconfont基础类，无需重复
    },
    computedStyle() {
      const style = { 
        fontSize: `${this.size}px`,
        width: `${this.size}px`,
        height: `${this.size}px`,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center'
      };
      // 如果传入了颜色，直接使用该颜色；否则使用默认颜色
      if (this.color) {
        style.color = this.color;
      } else {
        // 未设置颜色时使用默认处理
        style.color = themeIconColor(this.color);
      }
      return style;
    }
  },
  methods: {
    // 统一转发原生点击/触摸事件到父组件，兼容 Vue2/Vue3
    handleClick(e) {
      this.$emit('click', e)
    }
  }
};
</script>

<style scoped>
/* 引入字体样式（路径根据实际存放位置调整） */

.custom-icon {
  /* 合并iconfont基础类样式 */
  font-family: "iconfont" !important;
  font-style: normal;
  -webkit-font-smoothing: antialiased; /* 优化字体渲染 */
  -moz-osx-font-smoothing: grayscale;
  user-select: none;
}
</style>