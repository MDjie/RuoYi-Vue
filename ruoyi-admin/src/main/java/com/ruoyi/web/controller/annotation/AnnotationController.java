package com.ruoyi.web.controller.annotation;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.domain.server.Sys;
import com.ruoyi.system.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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
    public AjaxResult sendLabel(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getUserId();
        String label = params.get("label");
        return annotationService.sendLabel(userId, label);
    }

    /**
     * 查看当前准确率
     */
    @GetMapping("/checkAccuracy")
    public AjaxResult checkAccuracy() {
        Long userId = SecurityUtils.getUserId();
        return annotationService.checkAccuracy(userId);
    }
}
