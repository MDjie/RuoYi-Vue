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
import java.util.Map;
import java.util.HashMap;

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
            // 1. 读取 profile/annotation/datasets/dataset-config 文件内容，写入Redis
            File configFile = new File(getDatasetDir(),"config"+File.separator+ "dataset-config.json");
            System.out.println("读取数据集配置文件路径：" + configFile.getAbsolutePath());
            if (configFile.exists()) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
            
                redisTemplate.opsForValue().set("DATASET_CONFIG", sb.toString());
            }
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
     * 获取完整数据集配置信息
     */
    public JSONObject getDatasetConfig() {
        String configJson = (String) redisTemplate.opsForValue().get("DATASET_CONFIG");
        return JSONObject.parseObject(configJson);
    }

    /**
     * 获取指定数据集的配置信息
     */
    public JSONObject getDatasetConfigByName(String datasetName) {
        JSONObject allConfig = getDatasetConfig();
        return allConfig != null ? allConfig.getJSONObject(datasetName) : null;
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
           
            File outputFile = new File(outputDir, fileName);
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

    /**
     * 清除某个数据集的所有缓存
     */
    public void clearDatasetCache(String fileName) {
        Integer size = getDatasetSize(fileName);
        if (size != null) {
            for (int i = 1; i <= size; i++) {
                String key = DATASET_KEY_PREFIX + fileName + ":" + i;
                redisTemplate.delete(key);
            }
        }
        String sizeKey = DATASET_INDEX_KEY_PREFIX + fileName;
        redisTemplate.delete(sizeKey);
    }

    /**
     * 更新数据集大小
     */
    public void updateDatasetSize(String fileName, int newSize) {
        String sizeKey = DATASET_INDEX_KEY_PREFIX + fileName;
        redisTemplate.opsForValue().set(sizeKey, newSize);
    }

    /**
     * 将Redis中的最新标注结果同步到第一次生成的json文件（labeled_datasets目录下的原始文件）
     * @param fileName 标注文件名（如xxx-1.json）
     */
    public String updateToAnswerFile(String fileName) {
        String labeledFilePath = getLabeledDir() + File.separator + fileName;
        File labeledFile = new File(labeledFilePath);
        try {
            if (!labeledFile.exists()) return labeledFile.getAbsolutePath();

            // 2. 读取原始文件到List
            List<JSONObject> originalList = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(labeledFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    originalList.add(JSON.parseObject(line));
                }
            }

            // 3. 获取Redis中当前数据集所有数据
            Integer size = getDatasetSize(fileName);
            if (size == null) return labeledFile.getAbsolutePath();
            Map<Integer, JSONObject> redisData = new HashMap<>();
            for (int i = 1; i <= size; i++) {
                JSONObject obj = getDatasetLine(fileName, i);
                if (obj != null && obj.containsKey("origin_line")) {
                    int originLine = obj.getIntValue("origin_line");
                    redisData.put(originLine, obj);
                }
            }

            // 4. 用Redis中的Human_Answer覆盖原始文件对应行
            for (int i = 0; i < originalList.size(); i++) {
                int lineNum = i + 1;
                if (redisData.containsKey(lineNum)) {
                    JSONObject redisObj = redisData.get(lineNum);
                    originalList.get(i).put("Human_Answer", redisObj.getString("Human_Answer"));
                }
            }

            // 5. 覆盖写回原始文件
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(labeledFile, false), StandardCharsets.UTF_8))) {
                for (JSONObject obj : originalList) {
                    writer.write(obj.toJSONString());
                    writer.newLine();
                }
            }
            return labeledFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return labeledFile.getAbsolutePath();
        }
    }
} 