import request from '@/utils/request'

// 获取标注文本
export function getText() {
  return request({
    url: '/annotation/gwtText',
    method: 'get'
  })
}

// 提交标注
export function sendLabel(data) {
  return request({
    url: '/annotation/sendLabel',
    method: 'post',
    data: data
  })
}

// 查看当前准确率
export function checkAccuracy() {
  return request({
    url: '/annotation/checkAccuracy',
    method: 'get'
  })
}