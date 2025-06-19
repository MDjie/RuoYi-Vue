package com.ruoyi.hsc.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class RedisDatasetService {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${ruoyi.profile}")
    private String profilePath;

    private static final String DATASET_KEY_PREFIX = "dataset:";
    private static final String DATASET_INDEX_KEY_PREFIX = "dataset_index:";

    private String getDatasetDir() {
        return profilePath + File.separator + "annotation" + File.separator + "datasets";
    }
    private String getLabeledDir() {
        return profilePath + File.separator + "annotation" + File.separator + "labeled_datasets";
    }
    private String getAnswerDir() {
        return profilePath + File.separator + "annotation" + File.separator + "answer_datasets";
    }

    /**
     * 应用启动时初始化数据集到Redis
     */
    @PostConstruct
    public void init() {
        try {
            // 遍历profile/annotation/datasets目录下所有json文件
            File datasetDir = new File(getDatasetDir());
            if (!datasetDir.exists()) {
                datasetDir.mkdirs();
            }
            File[] files = datasetDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    loadDatasetToRedis(file.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载数据集到Redis
     */
    public void loadDatasetToRedis(String fileName) {
        try {
            File file = new File(getDatasetDir(), fileName);
            List<JSONObject> jsonList = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonList.add(JSON.parseObject(line));
                }
            }

            // 将每个JSON对象单独存入Redis
            for (int i = 0; i < jsonList.size(); i++) {
                String key = DATASET_KEY_PREFIX + fileName + ":" + (i+1);
                redisTemplate.opsForValue().set(key, jsonList.get(i));
            }

            // 存储数据集大小
            String sizeKey = DATASET_INDEX_KEY_PREFIX + fileName;
            redisTemplate.opsForValue().set(sizeKey, jsonList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据集中的某一行
     */
    public JSONObject getDatasetLine(String fileName, int index) {
        String key = DATASET_KEY_PREFIX + fileName + ":" + index;
        return (JSONObject) redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取数据集大小
     */
    public Integer getDatasetSize(String fileName) {
        String key = DATASET_INDEX_KEY_PREFIX + fileName;
        return (Integer) redisTemplate.opsForValue().get(key);
    }

    /**
     * 更新数据集中的某一行
     */
    public void updateDatasetLine(String fileName, int index, JSONObject newLine) {
        String key = DATASET_KEY_PREFIX + fileName + ":" + index;
        redisTemplate.opsForValue().set(key, newLine);
    }

    /**
     * 导出标注数据
     */
    public String exportAnnotatedData(String fileName) {
        try {
            // 创建输出目录 - 使用profile路径
            String outputDirPath = getLabeledDir();
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            // 生成输出文件名
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputFileName = fileName.replace(".json", "_" + timestamp + ".json");
            File outputFile = new File(outputDir, outputFileName);
            // 写入数据，使用UTF-8编码
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
                Integer datasetSize = getDatasetSize(fileName);
                if (datasetSize != null) {
                    for (int i = 1; i <= datasetSize; i++) {
                        JSONObject line = getDatasetLine(fileName, i);
                        if (line != null) {
                            writer.write(line.toJSONString());
                            writer.newLine();
                        }
                    }
                }
            }
            // 确保文件被正确写入
            if (!outputFile.exists()) {
                throw new RuntimeException("Failed to create output file: " + outputFile.getAbsolutePath());
            }
            // 返回文件路径
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈信息
            throw new RuntimeException("导出标注数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出当前已标注的数据
     */
    public String exportCurrentAnnotatedData(String fileName, Integer currentIndex) {
        try {
            // 创建输出目录 - 使用profile路径
            String outputDirPath = getLabeledDir();
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            // 生成输出文件名
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputFileName = "current_" + fileName.replace(".json", "_" + timestamp + ".json");
            File outputFile = new File(outputDir, outputFileName);
            // 写入数据，使用UTF-8编码
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
                for (int i = 1; i <= currentIndex; i++) {
                    JSONObject line = getDatasetLine(fileName, i);
                    if (line != null) {
                        writer.write(line.toJSONString());
                        writer.newLine();
                    }
                }
            }
            // 确保文件被正确写入
            if (!outputFile.exists()) {
                throw new RuntimeException("Failed to create output file: " + outputFile.getAbsolutePath());
            }
            // 返回文件路径
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈信息
            throw new RuntimeException("导出当前已标注数据失败: " + e.getMessage());
        }
    }
} 