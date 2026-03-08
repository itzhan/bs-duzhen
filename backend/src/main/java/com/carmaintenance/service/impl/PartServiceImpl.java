package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.common.BusinessException;
import com.carmaintenance.entity.InventoryRecord;
import com.carmaintenance.entity.Part;
import com.carmaintenance.mapper.InventoryRecordMapper;
import com.carmaintenance.mapper.PartMapper;
import com.carmaintenance.service.PartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PartServiceImpl extends ServiceImpl<PartMapper, Part> implements PartService {

    private final InventoryRecordMapper inventoryRecordMapper;

    @Override
    public IPage<Part> pageList(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Part::getPartCode, keyword)
                    .or().like(Part::getPartName, keyword)
                    .or().like(Part::getBrand, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Part::getCategory, category);
        }
        wrapper.orderByDesc(Part::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Map<String, Object>> getLowStockParts() {
        return baseMapper.findLowStockParts();
    }

    @Override
    @Transactional
    public void adjustStock(Long partId, int quantity, int type, Long operatorId, Long orderId, String remark) {
        Part part = getById(partId);
        if (part == null) {
            throw new BusinessException("配件不存在");
        }

        int beforeQty = part.getStockQty();
        int afterQty;

        if (type == 1) { // 入库
            afterQty = beforeQty + quantity;
        } else if (type == 2) { // 出库
            if (beforeQty < quantity) {
                throw new BusinessException("库存不足，当前库存: " + beforeQty);
            }
            afterQty = beforeQty - quantity;
        } else { // 盘点调整
            afterQty = quantity;
        }

        part.setStockQty(afterQty);
        updateById(part);

        // 记录库存流水
        InventoryRecord record = new InventoryRecord();
        record.setPartId(partId);
        record.setType(type);
        record.setQuantity(type == 2 ? -quantity : quantity);
        record.setBeforeQty(beforeQty);
        record.setAfterQty(afterQty);
        record.setUnitPrice(type == 1 ? part.getPurchasePrice() : part.getSalePrice());
        record.setRelatedOrderId(orderId);
        record.setOperatorId(operatorId);
        record.setRemark(remark);
        inventoryRecordMapper.insert(record);
    }
}
