package com.carmaintenance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count " +
            "FROM repair_order WHERE deleted_at IS NULL AND created_at >= #{startDate} " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> countByMonth(String startDate);

    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m') as month, " +
            "COALESCE(SUM(total_cost), 0) as revenue " +
            "FROM repair_order WHERE deleted_at IS NULL AND status = 3 AND is_paid = 1 " +
            "AND created_at >= #{startDate} " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> revenueByMonth(String startDate);

    @Select("SELECT status, COUNT(*) as count FROM repair_order " +
            "WHERE deleted_at IS NULL GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
