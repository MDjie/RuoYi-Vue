package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SysUserAnnotationInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserAnnotationInfoMapper {
    
    /**
     * 根据用户ID查询标注信息
     */
    SysUserAnnotationInfo selectByUserId(@Param("userId") Long userId);
    
    /**
     * 更新用户标注信息
     */
    int updateAnnotationInfo(SysUserAnnotationInfo info);
    
    /**
     * 插入用户标注信息
     */
    int insertAnnotationInfo(SysUserAnnotationInfo info);
}
