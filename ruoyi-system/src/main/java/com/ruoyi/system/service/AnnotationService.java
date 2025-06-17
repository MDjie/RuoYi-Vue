package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.AjaxResult;

public interface AnnotationService {
    
    /**
     * 获取标注文本信息
     */
    AjaxResult getText(Long userId);
    
    /**
     * 提交标注信息
     */
    AjaxResult sendLabel(Long userId, String label);
} 
