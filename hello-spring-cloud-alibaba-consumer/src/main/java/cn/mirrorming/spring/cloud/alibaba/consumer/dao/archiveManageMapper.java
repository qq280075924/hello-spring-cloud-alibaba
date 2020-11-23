package cn.mirrorming.spring.cloud.alibaba.consumer.dao;

import cn.mirrorming.spring.cloud.alibaba.consumer.entity.archiveManage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface archiveManageMapper {
    int deleteByPrimaryKey(Long id);

    int insert(archiveManage record);

    int insertSelective(archiveManage record);

    archiveManage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(archiveManage record);

    int updateByPrimaryKey(archiveManage record);
}