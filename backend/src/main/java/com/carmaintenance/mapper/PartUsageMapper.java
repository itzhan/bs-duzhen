package com.carmaintenance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.PartUsage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PartUsageMapper extends BaseMapper<PartUsage> {

    @Select("SELECT p.part_name, SUM(pu.quantity) as total_qty, SUM(pu.amount) as total_amount " +
            "FROM part_usage pu JOIN part p ON pu.part_id = p.id " +
            "GROUP BY pu.part_id, p.part_name ORDER BY total_qty DESC LIMIT 10")
    List<Map<String, Object>> topUsedParts();
}
