package com.ruoyi.hsc.mapper;

import com.ruoyi.hsc.domain.SysUserAnnotationInfo;
import com.ruoyi.hsc.domain.vo.UserAnnotationInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标注管理信息表的mapper类
 */
@Mapper
public interface SysUserAnnotationInfoMapper {
    
    /**
     * 根据用户ID查询所有标注信息
     */
    List<SysUserAnnotationInfo> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID、数据集名称和子集编号查询标注信息
     */
    SysUserAnnotationInfo selectByUserAndDataset(@Param("userId") Long userId, 
                                                @Param("datasetName") String datasetName, 
                                                @Param("datasetSubSet") Integer datasetSubSet);
    
    /**
     * 更新用户标注信息
     */
    int updateAnnotationInfo(SysUserAnnotationInfo info);
    
    /**
     * 插入用户标注信息
     */
    int insertAnnotationInfo(SysUserAnnotationInfo info);

    List<UserAnnotationInfoVO> selectPageWithUserName(@Param("offset") int offset, @Param("limit") int limit, @Param("userName") String userName, @Param("datasetName") String datasetName);
    int countWithUserName(@Param("userName") String userName, @Param("datasetName") String datasetName);

    int deleteAnnotation(@Param("userId") Long userId, @Param("datasetName") String datasetName, @Param("datasetSubSet") Integer datasetSubSet);

    int updateAnnotation(@Param("userId") Long userId, @Param("datasetName") String datasetName, @Param("datasetSubSet") Integer datasetSubSet, @Param("currentIndex") Integer currentIndex, @Param("relabelRound") Integer relabelRound);

    java.util.List<SysUserAnnotationInfo> selectByDatasetAndSubSet(@Param("datasetName") String datasetName, @Param("datasetSubSet") Integer datasetSubSet);
}
