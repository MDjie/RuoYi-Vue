import request from '@/utils/request'

// 获取用户可用的数据集列表
export function getUserDatasets() {
  return request({
    url: '/annotation/getUserDatasets',
    method: 'get'
  })
}

// 获取标注文本
export function getText(datasetName, datasetSubSet) {
  return request({
    url: '/annotation/getText',
    method: 'get',
    params: {
      datasetName: datasetName,
      datasetSubSet: datasetSubSet
    }
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
export function checkAccuracy(datasetName, datasetSubSet) {
  return request({
    url: '/annotation/checkAccuracy',
    method: 'get',
    params: {
      datasetName: datasetName,
      datasetSubSet: datasetSubSet
    }
  })
}