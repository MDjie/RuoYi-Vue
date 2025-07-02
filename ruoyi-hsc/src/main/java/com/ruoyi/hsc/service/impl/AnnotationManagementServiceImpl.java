package com.ruoyi.hsc.service.impl;

import com.ruoyi.hsc.service.AnnotationManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hsc.mapper.SysUserAnnotationInfoMapper;
import com.ruoyi.hsc.domain.vo.UserAnnotationInfoVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.hsc.domain.SysUserAnnotationInfo;
import com.ruoyi.hsc.service.AnnotationService;
/**
 * 标注管理页面的service实现类，用于实现标注管理页面的业务逻辑。
 */
@Service
public class AnnotationManagementServiceImpl implements AnnotationManagementService {
    @Autowired
    private SysUserAnnotationInfoMapper mapper;
    @Autowired
    private AnnotationService annotationService;
    @Autowired
    private SysUserAnnotationInfoMapper annotationInfoMapper;

    @Override
    public Map<String, Object> getUserAnnotationInfoPage(int pageNum, int pageSize, String userName, String datasetName) {
        int offset = (pageNum - 1) * pageSize;
        List<UserAnnotationInfoVO> rows = mapper.selectPageWithUserName(offset, pageSize, userName, datasetName);
        int total = mapper.countWithUserName(userName, datasetName);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", rows);
        result.put("total", total);
        return result;
    }

    @Override
    public boolean deleteAnnotation(Long userId, String datasetName, Integer datasetSubSet) {
        int affected = mapper.deleteAnnotation(userId, datasetName, datasetSubSet);
        return affected > 0;
    }

    @Override
    public boolean updateAnnotation(Long userId, String datasetName, Integer datasetSubSet, Integer currentIndex, Integer relabelRound) {
        int affected = mapper.updateAnnotation(userId, datasetName, datasetSubSet, currentIndex, relabelRound);
        return affected > 0;
    }

    @Override
    public boolean relabelAnnotation(String datasetName, Integer datasetSubSet) {
        // 查找该数据集子集的所有用户标注信息
        java.util.List<SysUserAnnotationInfo> list = annotationInfoMapper.selectByDatasetAndSubSet(datasetName, datasetSubSet);
        boolean allSuccess = true;
        for (SysUserAnnotationInfo info : list) {
            // 只有relabelRound>=2才允许重做
            if (info.getRelabelRound() != null && info.getRelabelRound() >= 2) {
                // 复用AnnotationServiceImpl的relabel逻辑，重做最后一轮
                annotationService.relabel(info, info.getRelabelRound()-1);
            } else {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
}
