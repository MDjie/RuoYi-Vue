package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.consts.DatasetConfig;
import com.ruoyi.system.domain.SysUserAnnotationInfo;
import com.ruoyi.system.mapper.SysUserAnnotationInfoMapper;
import com.ruoyi.system.service.AnnotationService;
import com.ruoyi.system.service.RedisDatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnotationServiceImpl implements AnnotationService {

    @Autowired
    private SysUserAnnotationInfoMapper annotationInfoMapper;

    @Autowired
    private RedisDatasetService redisDatasetService;

    @Override
    public AjaxResult getText(Long userId) {
        try {
            // 获取用户标注信息
            SysUserAnnotationInfo annotationInfo = annotationInfoMapper.selectByUserId(userId);
            if (annotationInfo == null) {
                return AjaxResult.error("未找到用户标注信息");
            }

            // 构建JSON文件名
            String fileName = annotationInfo.getDatasetName() + "-" + annotationInfo.getDatasetSubSet() + ".json";
            
            // 获取数据集大小
            Integer datasetSize = redisDatasetService.getDatasetSize(fileName);
            if (datasetSize == null) {
                return AjaxResult.error("未找到数据集");
            }

            // 检查是否已完成标注
            if (annotationInfo.getCurrentIndex() >= datasetSize) {
                return AjaxResult.error("标注已完成");
            }

            // 从Redis获取当前行的JSON对象
            JSONObject currentText = redisDatasetService.getDatasetLine(fileName, annotationInfo.getCurrentIndex());
            if (currentText == null) {
                return AjaxResult.error("获取标注文本失败");
            }

            // 获取数据集配置
            JSONObject datasetConfig = DatasetConfig.DATASET_CONFIG.getJSONObject(annotationInfo.getDatasetName());
            if (datasetConfig == null) {
                return AjaxResult.error("未找到数据集配置信息");
            }

            // 构建返回数据
            JSONObject result = new JSONObject();
            result.put("datasetName", annotationInfo.getDatasetName());
            result.put("currentIndex", annotationInfo.getCurrentIndex());
            result.put("accuracy", datasetConfig.getInteger("accuracy"));
            result.put("text", currentText.getString("text"));
            result.put("labelOptions", getLabelOptions(datasetConfig));

            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("获取标注文本失败：" + e.getMessage());
        }
    }

    @Override
    public AjaxResult sendLabel(Long userId, String label) {
        try {
            // 获取用户标注信息
            SysUserAnnotationInfo annotationInfo = annotationInfoMapper.selectByUserId(userId);
            if (annotationInfo == null) {
                return AjaxResult.error("未找到用户标注信息");
            }

            // 构建JSON文件名
            String fileName = annotationInfo.getDatasetName() + "-" + annotationInfo.getDatasetSubSet() + ".json";
            
            // 从Redis获取当前行的JSON对象
            JSONObject currentJson = redisDatasetService.getDatasetLine(fileName, annotationInfo.getCurrentIndex());
            if (currentJson == null) {
                return AjaxResult.error("获取标注文本失败");
            }
            
            // 更新Human_Answer字段
            currentJson.put("Human_Answer", label);
            
            // 更新Redis中的数据
            redisDatasetService.updateDatasetLine(fileName, annotationInfo.getCurrentIndex(), currentJson);

            // 更新数据库中的current_index
            annotationInfo.setCurrentIndex(annotationInfo.getCurrentIndex() + 1);
            annotationInfoMapper.updateAnnotationInfo(annotationInfo);

            // 检查是否完成标注
            Integer datasetSize = redisDatasetService.getDatasetSize(fileName);
            if (annotationInfo.getCurrentIndex() >= datasetSize) {
                // 导出标注数据
                String exportedFileName = redisDatasetService.exportAnnotatedData(fileName);
                return AjaxResult.success("标注完成，数据已导出到文件：" + exportedFileName);
            }

            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error("提交标注失败：" + e.getMessage());
        }
    }

    /**
     * 获取标签选项
     */
    private List<JSONObject> getLabelOptions(JSONObject datasetConfig) {
        List<JSONObject> options = new ArrayList<>();
        
        // 遍历数据集配置中的所有键值对
        for (String key : datasetConfig.keySet()) {
            // 跳过accuracy字段
            if ("accuracy".equals(key)) {
                continue;
            }
            
            JSONObject option = new JSONObject();
            option.put("label", datasetConfig.getString(key));
            option.put("value", key);
            options.add(option);
        }
        
        return options;
    }
}
