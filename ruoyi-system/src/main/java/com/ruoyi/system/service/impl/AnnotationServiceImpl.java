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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnnotationServiceImpl implements AnnotationService {

    @Autowired
    private SysUserAnnotationInfoMapper annotationInfoMapper;

    @Autowired
    private RedisDatasetService redisDatasetService;

    // 用于存储用户最后一次查看准确率的时间
    private final Map<String, Long> lastCheckTimeMap = new ConcurrentHashMap<>();
    // 最小间隔时间（毫秒）
    private static final long MIN_INTERVAL = 30000; // 30秒

    @Override
    public AjaxResult getUserDatasets(Long userId) {
        try {
            List<SysUserAnnotationInfo> datasets = annotationInfoMapper.selectByUserId(userId);
            List<JSONObject> result = new ArrayList<>();
            
            for (SysUserAnnotationInfo dataset : datasets) {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", dataset.getDatasetName());
                datasetInfo.put("datasetSubSet", dataset.getDatasetSubSet());
                datasetInfo.put("currentIndex", dataset.getCurrentIndex());
                datasetInfo.put("displayName", dataset.getDatasetName() + "-" + dataset.getDatasetSubSet());
                result.add(datasetInfo);
            }
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("获取用户数据集列表失败：" + e.getMessage());
        }
    }

    @Override
    public AjaxResult getText(Long userId, String datasetName, Integer datasetSubSet) {
        try {
            // 获取用户标注信息
            SysUserAnnotationInfo annotationInfo = annotationInfoMapper.selectByUserAndDataset(userId, datasetName, datasetSubSet);
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
            if (annotationInfo.getCurrentIndex() > datasetSize) {
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
            result.put("total", datasetSize);
            result.put("labelOptions", getLabelOptions(datasetConfig));

            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("获取标注文本失败：" + e.getMessage());
        }
    }

    @Override
    public AjaxResult sendLabel(Long userId, String datasetName, Integer datasetSubSet, String label) {
        try {
            // 获取用户标注信息
            SysUserAnnotationInfo annotationInfo = annotationInfoMapper.selectByUserAndDataset(userId, datasetName, datasetSubSet);
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
            if (annotationInfo.getCurrentIndex() > datasetSize) {
                // 导出标注数据
                String exportedFileName = redisDatasetService.exportAnnotatedData(fileName);
                
                // 进行评分
                String referencePath = "answer_datasets/" + fileName;
                String testPath = exportedFileName;  // 现在是绝对路径
                
                // 打印文件路径，用于调试
                System.out.println("Reference path: " + referencePath);
                System.out.println("Test path: " + testPath);
                
                String scoreResult = evaluateAnswers(referencePath, testPath, annotationInfo);
                if(scoreResult.contains("未达到准确率要求")){
                    annotationInfo.setCurrentIndex(1);
                    annotationInfoMapper.updateAnnotationInfo(annotationInfo);
                    redisDatasetService.loadDatasetToRedis(fileName);
                    return AjaxResult.error("标注准确率未达到要求，请重新开始标注");
                }
                
                return AjaxResult.success("标注完成，数据已导出到文件：" + exportedFileName + "\n评分结果：" + scoreResult);
            }

            return AjaxResult.success();
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈信息
            return AjaxResult.error("提交标注失败：" + e.getMessage());
        }
    }

    @Override
    public AjaxResult checkAccuracy(Long userId, String datasetName, Integer datasetSubSet) {
        try {
            // 生成唯一的时间检查键
            String timeCheckKey = userId + "_" + datasetName + "_" + datasetSubSet;
            
            // 检查时间间隔
            long currentTime = System.currentTimeMillis();
            Long lastCheckTime = lastCheckTimeMap.get(timeCheckKey);
            if (lastCheckTime != null) {
                long timeDiff = currentTime - lastCheckTime;
                if (timeDiff < MIN_INTERVAL) {
                    return AjaxResult.error("请等待 " + ((MIN_INTERVAL - timeDiff) / 1000) + " 秒后再次查看");
                }
            }

            // 获取用户标注信息
            SysUserAnnotationInfo annotationInfo = annotationInfoMapper.selectByUserAndDataset(userId, datasetName, datasetSubSet);
            if (annotationInfo == null) {
                return AjaxResult.error("未找到用户标注信息");
            }

            // 构建JSON文件名
            String fileName = annotationInfo.getDatasetName() + "-" + annotationInfo.getDatasetSubSet() + ".json";
            
            // 导出当前已标注的数据
            String exportedFileName = redisDatasetService.exportCurrentAnnotatedData(fileName, annotationInfo.getCurrentIndex());
            
            // 进行评分
            String referencePath = "answer_datasets/" + fileName;
            String testPath = exportedFileName;  // 现在是绝对路径
            
            // 打印文件路径，用于调试
            System.out.println("Reference path: " + referencePath);
            System.out.println("Test path: " + testPath);
            
            String scoreResult = evaluateAnswers(referencePath, testPath, annotationInfo);
            
            // 更新最后查看时间
            lastCheckTimeMap.put(timeCheckKey, currentTime);

            // 删除临时文件
            File tempFile = new File(testPath);
            if (tempFile.exists()) {
                tempFile.delete();
            }
            
            return AjaxResult.success(scoreResult);
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈信息
            return AjaxResult.error("获取准确率失败：" + e.getMessage());
        }
    }

    /**
     * 评估标注结果
     */
    private String evaluateAnswers(String referencePath, String testPath, SysUserAnnotationInfo annotationInfo) {
        try {
            // 读取参考答案文件，建立text到answer的映射
            Map<String, String> referenceMap = new HashMap<>();
            ClassPathResource referenceResource = new ClassPathResource(referencePath);
            System.out.println("Reference file exists: " + referenceResource.exists());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(referenceResource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        JSONObject data = JSON.parseObject(line.trim());
                        if (data.containsKey("text") && data.containsKey("Human_Answer")) {
                            referenceMap.put(data.getString("text"), data.getString("Human_Answer"));
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            // 读取标注文件，收集所有非空Human_Answer
            List<String> trueLabels = new ArrayList<>();
            List<String> predLabels = new ArrayList<>();
            int totalQuestions = 0;
            int matchedQuestions = 0;

            // 使用File读取测试文件 - testPath现在是绝对路径
            File testFile = new File(testPath);
            System.out.println("Test file path: " + testFile.getAbsolutePath());
            System.out.println("Test file exists: " + testFile.exists());
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        JSONObject data = JSON.parseObject(line.trim());
                        if (data.containsKey("text") && data.containsKey("Human_Answer") && data.getString("Human_Answer") != null) {
                            totalQuestions++;
                            String text = data.getString("text");
                            String humanAnswer = data.getString("Human_Answer");

                            if (referenceMap.containsKey(text)) {
                                matchedQuestions++;
                                trueLabels.add(referenceMap.get(text));
                                predLabels.add(humanAnswer);
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            // 计算指标
            if (matchedQuestions == 0) {
                return "没有找到匹配的问题";
            }

            // 计算准确率
            int correctCount = 0;
            for (int i = 0; i < trueLabels.size(); i++) {
                if (trueLabels.get(i).equals(predLabels.get(i))) {
                    correctCount++;
                }
            }
            double accuracy = (double) correctCount / matchedQuestions;

            // 计算精确率、召回率和F1分数
            Map<String, Integer> truePositives = new HashMap<>();
            Map<String, Integer> falsePositives = new HashMap<>();
            Map<String, Integer> falseNegatives = new HashMap<>();
            Set<String> allLabels = new HashSet<>(trueLabels);
            allLabels.addAll(predLabels);

            for (String label : allLabels) {
                truePositives.put(label, 0);
                falsePositives.put(label, 0);
                falseNegatives.put(label, 0);
            }

            for (int i = 0; i < trueLabels.size(); i++) {
                String trueLabel = trueLabels.get(i);
                String predLabel = predLabels.get(i);

                if (trueLabel.equals(predLabel)) {
                    truePositives.put(trueLabel, truePositives.get(trueLabel) + 1);
                } else {
                    falsePositives.put(predLabel, falsePositives.get(predLabel) + 1);
                    falseNegatives.put(trueLabel, falseNegatives.get(trueLabel) + 1);
                }
            }

            double precision = 0.0;
            double recall = 0.0;
            double f1 = 0.0;

            for (String label : allLabels) {
                int tp = truePositives.get(label);
                int fp = falsePositives.get(label);
                int fn = falseNegatives.get(label);

                double labelPrecision = tp + fp == 0 ? 0 : (double) tp / (tp + fp);
                double labelRecall = tp + fn == 0 ? 0 : (double) tp / (tp + fn);
                double labelF1 = labelPrecision + labelRecall == 0 ? 0 : 2 * labelPrecision * labelRecall / (labelPrecision + labelRecall);

                precision += labelPrecision;
                recall += labelRecall;
                f1 += labelF1;
            }

            int labelCount = allLabels.size();
            precision /= labelCount;
            recall /= labelCount;
            f1 /= labelCount;

            // 构建结果字符串
            StringBuilder result = new StringBuilder();
            result.append("\n评估结果:\n");
            result.append("总问题数: ").append(totalQuestions).append("\n");
            result.append("匹配到参考答案的问题数: ").append(matchedQuestions).append("\n");
            result.append(String.format("准确率(Accuracy): %.4f\n", accuracy));
            result.append(String.format("精确率(Precision): %.4f\n", precision));
            result.append(String.format("召回率(Recall): %.4f\n", recall));
            result.append(String.format("F1分数: %.4f\n", f1));

            // 获取数据集配置中的准确率要求
            JSONObject datasetConfig = DatasetConfig.DATASET_CONFIG.getJSONObject(annotationInfo.getDatasetName());
            if (datasetConfig != null) {
                int requiredAccuracy = datasetConfig.getInteger("accuracy");
                result.append("\n准确率要求: ").append(requiredAccuracy).append("%\n");
                if (accuracy * 100 >= requiredAccuracy) {
                    result.append("恭喜！您已通过准确率要求！\n");
                } else {
                    result.append("很遗憾，您未达到准确率要求。\n");
                }
            }

            return result.toString();

        } catch (Exception e) {
            return "评分过程发生错误: " + e.getMessage();
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
