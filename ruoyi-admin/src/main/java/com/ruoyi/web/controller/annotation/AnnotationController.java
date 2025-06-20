package com.ruoyi.web.controller.annotation;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.hsc.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/annotation")
public class AnnotationController {
    
    @Autowired
    private AnnotationService annotationService;

    /**
     * 获取用户可用的数据集列表
     */
    @GetMapping("/getUserDatasets")
    public AjaxResult getUserDatasets() {
        Long userId = SecurityUtils.getUserId();
        return annotationService.getUserDatasets(userId);
    }

    /**
     * 获取标注文本
     */
    @GetMapping("/getText")
    public AjaxResult getText(@RequestParam String datasetName, @RequestParam Integer datasetSubSet) {
        Long userId = SecurityUtils.getUserId();
        return annotationService.getText(userId, datasetName, datasetSubSet);
    }

    /**
     * 提交标注
     */
    @PostMapping("/sendLabel")
    public AjaxResult sendLabel(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getUserId();
        String datasetName = params.get("datasetName");
        Integer datasetSubSet = Integer.parseInt(params.get("datasetSubSet"));
        String label = params.get("label");
        return annotationService.sendLabel(userId, datasetName, datasetSubSet, label);
    }

    /**
     * 查看当前准确率
     */
    @GetMapping("/checkAccuracy")
    public AjaxResult checkAccuracy(@RequestParam String datasetName, @RequestParam Integer datasetSubSet) {
        Long userId = SecurityUtils.getUserId();
        return annotationService.checkAccuracy(userId, datasetName, datasetSubSet);
    }
}
