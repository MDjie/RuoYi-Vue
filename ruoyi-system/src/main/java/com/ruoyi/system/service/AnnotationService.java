package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.AjaxResult;

public interface AnnotationService {
    
    /**
     * 获取用户可用的数据集列表
     */
    AjaxResult getUserDatasets(Long userId);
    
    /**
     * 获取标注文本信息
     */
    AjaxResult getText(Long userId, String datasetName, Integer datasetSubSet);
    
    /**
     * 提交标注信息
     */
    AjaxResult sendLabel(Long userId, String datasetName, Integer datasetSubSet, String label);

    /**
     * 查看当前准确率
     */
    AjaxResult checkAccuracy(Long userId, String datasetName, Integer datasetSubSet);
} 
