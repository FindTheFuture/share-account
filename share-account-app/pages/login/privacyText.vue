<template>
  <view class="privacy-page">
    <scroll-view scroll-y class="scroll-container">
      <view class="content">
        <text class="title">小程序隐私保护指引</text>
        <text class="paragraph">本指引是清甜生活圈子共享账单小程序开发者 三河市清甜纯水站(个体工商户)（以下简称"开发者"）为处理你的个人信息而制定。</text>
        
        <text class="section-title">开发者处理的信息</text>
        <text class="paragraph">根据法律规定，开发者仅处理实现小程序功能所必要的信息。</text>
        
        <text class="paragraph">开发者 收集你选中的照片信息，用于上传头像图片和AI识别账单。</text>
        
        <text class="section-title">第三方插件信息/SDK信息</text>
        <text class="paragraph">关于你的个人信息，你可以通过以下方式与开发者联系，行使查阅、复制、更正、删除等法定权利。</text>
        
        <text class="paragraph">若你在小程序中注册了账号，你可以通过以下方式与开发者联系，申请注销你在小程序中使用的账号。在受理你的申请后，开发者承诺在十五个工作日内完成核查和处理，并按照法律法规要求处理你的相关信息。</text>
        
        <text class="paragraph">微信号:a526140450</text>
        
        <text class="section-title">你的权益</text>
        <text class="paragraph">关于你的个人信息，你可以通过以下方式与开发者联系，行使查阅、复制、更正、删除等法定权利。</text>
        
        <text class="section-title">开发者对信息的存储</text>
        <text class="paragraph">开发者将会在本指引所明示的用途内使用收集的信息</text>
        
        <text class="paragraph">如开发者使用你的信息超出本指引目的或合理范围，开发者必须在变更使用目的或范围前，再次以微信方式告知并征得你的明示同意。</text>
        
        <text class="section-title">信息的使用规则</text>
        <text class="paragraph">开发者将会在本指引所明示的用途内使用收集的信息</text>
        
        <text class="paragraph">开发者承诺，不会对外公开披露你的信息，如必须公开披露时，开发者应当向你告知公开披露的目的、披露信息的类型及可能涉及的信息，并征得你的单独同意。</text>
        
        <text class="section-title">信息对外提供</text>
        <text class="paragraph">微信号 : a526140450</text>
        
        <text class="footer">更新日期：2025-01-03</text>
        <text class="footer">生效日期：2025-01-03</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {};
  },
  onLoad() {
    uni.setNavigationBarTitle({
      title: '隐私保护指引'
    });
  }
};
</script>

<style scoped>
.privacy-page {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.scroll-container {
  height: 100vh;
}

.content {
  padding: 32rpx;
}

.title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 32rpx;
  text-align: center;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-top: 40rpx;
  margin-bottom: 24rpx;
}

.paragraph {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 24rpx;
  text-align: justify;
}

.footer {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 60rpx;
  text-align: center;
  padding-bottom: 40rpx;
}
</style>
