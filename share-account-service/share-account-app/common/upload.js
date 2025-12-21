// common/upload.js
import { checkLoginStatusSync } from './request.js';
import $backUrlConfig from './back_url.js';

/**
 * 上传文件
 * @param {Object} options - 上传配置
 * @param {string} options.filePath - 要上传的文件路径
 * @param {number} options.fileType - 文件类型
 * @param {number} options.pathType - 路径类型
 * @param {string} options.path - 存储路径
 * @param {number} options.objectId - 关联对象ID
 * @param {Array<string>} [options.allowedTypes=['image/jpeg', 'image/png', 'image/gif']] - 允许的文件类型
 * @param {number} [options.maxSize=20 * 1024 * 1024] - 最大文件大小（字节）
 * @returns {Promise<Object>} - 返回上传结果的Promise
 */
export function uploadFile({
  filePath,
  fileType,
  pathType,
  path,
  objectId,
  allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/jpg', 'image/webp', 'image/avif'],
  maxSize = 20 * 1024 * 1024
}) {
  return new Promise((resolve, reject) => {
    // 检查登录状态
    if (!checkLoginStatusSync()) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      });
      // 清除登录状态
      uni.removeStorageSync('token');
      return reject(new Error('未登录'));
    }
    
    // 检查文件类型
    const fileExt = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
    const mimeType = getMimeType(fileExt);
    
    if (!allowedTypes.includes(mimeType)) {
      uni.showToast({
        title: `不支持的文件格式，仅支持 ${allowedTypes.join(', ')}`,
        icon: 'none'
      });
      return reject(new Error('文件格式不支持'));
    }
    
    // 检查文件大小并压缩
    uni.getFileInfo({
      filePath,
      success: (info) => {
        console.log('文件原始大小:', info.size / 1024 / 1024, 'MB');
        
        // 如果文件大小超过限制，进行压缩
        if (info.size > maxSize) {
          console.log('文件大小超出限制，开始压缩...');
          compressImage(filePath, maxSize).then(compressedFilePath => {
            console.log('图片压缩完成，开始上传...');
            doUpload(compressedFilePath, fileType, pathType, path, objectId, resolve, reject);
          }).catch(err => {
            uni.showToast({
              title: '图片压缩失败',
              icon: 'none'
            });
            reject(err);
          });
        } else {
          // 文件大小符合要求，直接上传
          doUpload(filePath, fileType, pathType, path, objectId, resolve, reject);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '获取文件信息失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

/**
 * 执行文件上传
 * @param {string} filePath - 文件路径
 * @param {number} fileType - 文件类型
 * @param {number} pathType - 路径类型
 * @param {string} path - 存储路径
 * @param {number} objectId - 关联对象ID
 * @param {Function} resolve - Promise resolve函数
 * @param {Function} reject - Promise reject函数
 */
function doUpload(filePath, fileType, pathType, path, objectId, resolve, reject) {
  // 显示加载中弹窗
  uni.showLoading({
    title: '上传中...',
    mask: true
  });
  
  // 获取存储的认证信息
  const token = uni.getStorageSync('token');
  const additionalId = uni.getStorageSync('additionalId');
  
  // 上传文件
  uni.uploadFile({
    url: $backUrlConfig.baseUrl + $backUrlConfig.endpoints.fileUpload,
    filePath,
    name: 'file',
    formData: {
      fileType,
      pathType,
      path,
      objectId
    },
    header: {
      Aboluo: `Aboluo ${token}`,
      Additional: `${additionalId}`
    },
    success: (res) => {
      
      // 检查状态码
      if (res.statusCode === 413) {
        uni.showToast({
          title: '文件过大，请尝试更压缩的图片',
          icon: 'none'
        });
        return reject(new Error('文件过大（413错误）'));
      }
      
      try {
        const result = JSON.parse(res.data);
        if (result.code == 200) {
          resolve(result.data);
        } else {
          const errorMsg = result.message || '上传失败';
          uni.showToast({
            title: errorMsg,
            icon: 'none'
          });
          reject(new Error(errorMsg));
        }
      } catch (e) {
        // 处理非JSON响应
        console.error('响应解析失败:', e);
        uni.showToast({
          title: '服务器响应格式错误',
          icon: 'none'
        });
        reject(new Error('解析响应失败'));
      }
    },
    fail: (err) => {
      console.error('上传失败:', err);
      uni.showToast({
        title: '网络错误，上传失败',
        icon: 'none'
      });
      reject(err);
    },
    complete: () => {
      // 隐藏加载中弹窗
      uni.hideLoading();
    }
  });
}

/**
 * 压缩图片
 * @param {string} filePath - 原图路径
 * @param {number} maxSize - 最大文件大小（字节）
 * @returns {Promise<string>} - 返回压缩后的图片路径
 */
function compressImage(filePath, maxSize) {
  return new Promise((resolve, reject) => {
    // 初始压缩质量
    let quality = 0.8;
    let attempts = 0;
    const maxAttempts = 3;
    
    // 递归压缩直到文件大小符合要求
    function compressWithQuality(q) {
      attempts++;
      console.log(`压缩尝试 ${attempts}，质量: ${q}`);
      
      uni.compressImage({
        src: filePath,
        quality: q,
        success: (res) => {
          const compressedFilePath = res.tempFilePath;
          
          // 检查压缩后的文件大小
          uni.getFileInfo({
            filePath: compressedFilePath,
            success: (info) => {
              console.log(`压缩后文件大小: ${info.size / 1024 / 1024}MB`);
              
              // 如果仍然超过限制且未达到最大尝试次数，继续压缩
              if (info.size > maxSize && attempts < maxAttempts) {
                // 每次降低质量
                compressWithQuality(Math.max(0.1, q - 0.2));
              } else {
                resolve(compressedFilePath);
              }
            },
            fail: (err) => {
              reject(err);
            }
          });
        },
        fail: (err) => {
          reject(err);
        }
      });
    }
    
    // 开始压缩
    compressWithQuality(quality);
  });
}

/**
 * 根据文件扩展名获取MIME类型
 * @param {string} ext - 文件扩展名
 * @returns {string} - MIME类型
 */
function getMimeType(ext) {
  const mimeMap = {
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    png: 'image/png',
    gif: 'image/gif',
    bmp: 'image/bmp',
    webp: 'image/webp',
    avif: 'image/avif'
  };
  
  return mimeMap[ext] || 'application/octet-stream';
}