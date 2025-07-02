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

// 标注管理页面的分页查询API
export function getAnnotationManagementList(params) {
  return request({
    url: '/annotation/management/list',
    method: 'get',
    params
  })
}

// 删除标注信息
export function deleteAnnotation(data) {
  return request({
    url: '/annotation/management/delete',
    method: 'post',
    data
  })
}

// 修改标注信息
export function updateAnnotation(data) {
  return request({
    url: '/annotation/management/update',
    method: 'post',
    data
  })
}

// 获取正常状态用户列表
export function getNormalUserList() {
  return request({
    url: '/system/user/normalList',
    method: 'get'
  })
}

// 重做最后一轮
export function relabelAnnotation(data) {
  return request({
    url: '/annotation/management/relabel',
    method: 'post',
    data
  })
}

//标注管理页面的api生成在此处