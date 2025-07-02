package com.ruoyi.hsc.service;
/**
 * 标注管理页面的service接口类，用于定义处理标注管理页面的业务逻辑方法。
 */
import com.ruoyi.hsc.domain.vo.UserAnnotationInfoVO;
import java.util.Map;

public interface AnnotationManagementService {
    
    Map<String, Object> getUserAnnotationInfoPage(int pageNum, int pageSize, String userName, String datasetName);

    boolean deleteAnnotation(Long userId, String datasetName, Integer datasetSubSet);

    boolean updateAnnotation(Long userId, String datasetName, Integer datasetSubSet, Integer currentIndex, Integer relabelRound);

    boolean relabelAnnotation(String datasetName, Integer datasetSubSet);
}
