import request from '@/utils/request'

// 获取text
export function getText() {
    return request({
      url: '/annotation/gwtText',
      method: 'get',
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