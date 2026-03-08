package com.carmaintenance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.Part;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PartMapper extends BaseMapper<Part> {

    @Select("SELECT id, part_code, part_name, stock_qty, min_stock FROM part " +
            "WHERE deleted_at IS NULL AND status = 1 AND stock_qty <= min_stock")
    List<Map<String, Object>> findLowStockParts();
}
