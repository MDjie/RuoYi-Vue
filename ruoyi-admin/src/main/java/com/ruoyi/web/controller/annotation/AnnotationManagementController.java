package com.ruoyi.web.controller.annotation;
/**
 * 生成标注页面的控制器，接收标注管理页面的请求，返回数据。
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.hsc.service.AnnotationManagementService;
import java.util.Map;

@RestController
@RequestMapping("/annotation/management")
public class AnnotationManagementController {
    @Autowired
    private AnnotationManagementService service;

    @GetMapping("/list")
    public Map<String, Object> list(
        @RequestParam int pageNum,
        @RequestParam int pageSize,
        @RequestParam(required = false) String userName,
        @RequestParam(required = false) String datasetName
    ) {
        return service.getUserAnnotationInfoPage(pageNum, pageSize, userName, datasetName);
    }

    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String datasetName = params.get("datasetName").toString();
        Integer datasetSubSet = Integer.valueOf(params.get("datasetSubSet").toString());
        boolean success = service.deleteAnnotation(userId, datasetName, datasetSubSet);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String datasetName = params.get("datasetName").toString();
        Integer datasetSubSet = Integer.valueOf(params.get("datasetSubSet").toString());
        Integer currentIndex = Integer.valueOf(params.get("currentIndex").toString());
        Integer relabelRound = Integer.valueOf(params.get("relabelRound").toString());
        boolean success = service.updateAnnotation(userId, datasetName, datasetSubSet, currentIndex, relabelRound);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        return result;
    }

    @PostMapping("/relabel")
    public Map<String, Object> relabel(@RequestBody Map<String, Object> params) {
        String datasetName = params.get("datasetName").toString();
        Integer datasetSubSet = Integer.valueOf(params.get("datasetSubSet").toString());
        boolean success = service.relabelAnnotation(datasetName, datasetSubSet);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        return result;
    }
}
