package com.ruoyi.web.controller.annotation;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/annotation")
public class AnnotationController {
    
    @Autowired
    private AnnotationService annotationService;

    /**
     * 获取标注文本
     */
    @GetMapping("/gwtText")
    public AjaxResult getText() {
        Long userId = SecurityUtils.getUserId();
        return annotationService.getText(userId);
    }

    /**
     * 提交标注
     */
    @PostMapping("/sendLabel")
    public AjaxResult sendLabel(@RequestBody String label) {
        Long userId = SecurityUtils.getUserId();
        return annotationService.sendLabel(userId, label);
    }
}
