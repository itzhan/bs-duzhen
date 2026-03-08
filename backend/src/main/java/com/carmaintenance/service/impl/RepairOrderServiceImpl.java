package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.common.BusinessException;
import com.carmaintenance.entity.RepairItem;
import com.carmaintenance.entity.RepairOrder;
import com.carmaintenance.entity.PartUsage;
import com.carmaintenance.mapper.RepairItemMapper;
import com.carmaintenance.mapper.RepairOrderMapper;
import com.carmaintenance.mapper.PartUsageMapper;
import com.carmaintenance.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    private final RepairItemMapper repairItemMapper;
    private final PartUsageMapper partUsageMapper;

    @Override
    public IPage<RepairOrder> pageList(int page, int size, String keyword, Integer status, Long customerId, Long technicianId) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(RepairOrder::getOrderNo, keyword)
                    .or().like(RepairOrder::getFaultDesc, keyword));
        }
        if (status != null) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (customerId != null) {
            wrapper.eq(RepairOrder::getCustomerId, customerId);
        }
        if (technicianId != null) {
            wrapper.eq(RepairOrder::getTechnicianId, technicianId);
        }
        wrapper.orderByDesc(RepairOrder::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public String generateOrderNo() {
        String prefix = "RO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = count(new LambdaQueryWrapper<RepairOrder>()
                .likeRight(RepairOrder::getOrderNo, prefix));
        return prefix + String.format("%04d", count + 1);
    }

    @Override
    @Transactional
    public void assignTechnician(Long orderId, Long technicianId) {
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只有待接单状态的工单才能派工");
        }
        order.setTechnicianId(technicianId);
        order.setStatus(1);
        updateById(order);
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, Integer status) {
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        // 状态流转验证: 0->1->2->3, 0->4
        boolean valid = switch (status) {
            case 1 -> order.getStatus() == 0;
            case 2 -> order.getStatus() == 1;
            case 3 -> order.getStatus() == 2;
            case 4 -> order.getStatus() == 0 || order.getStatus() == 1;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("无效的状态流转");
        }
        order.setStatus(status);
        if (status == 3) {
            order.setActualFinishTime(LocalDateTime.now());
            // 计算费用
            recalculateCost(order);
        }
        updateById(order);
    }

    @Override
    @Transactional
    public void settle(Long orderId) {
        RepairOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("只有已完成的工单才能结算");
        }
        if (order.getIsPaid() == 1) {
            throw new BusinessException("该工单已结算");
        }
        recalculateCost(order);
        order.setIsPaid(1);
        updateById(order);
    }

    private void recalculateCost(RepairOrder order) {
        // 计算工时费
        List<RepairItem> items = repairItemMapper.selectList(
                new LambdaQueryWrapper<RepairItem>().eq(RepairItem::getOrderId, order.getId()));
        BigDecimal laborCost = items.stream()
                .map(item -> item.getLaborHours().multiply(item.getLaborPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算配件费
        List<PartUsage> usages = partUsageMapper.selectList(
                new LambdaQueryWrapper<PartUsage>().eq(PartUsage::getOrderId, order.getId()));
        BigDecimal partsCost = usages.stream()
                .map(PartUsage::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setLaborCost(laborCost);
        order.setPartsCost(partsCost);
        order.setTotalCost(laborCost.add(partsCost));
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        // 工单总数
        stats.put("totalOrders", count());
        // 各状态工单数
        stats.put("statusCount", baseMapper.countByStatus());
        // 近6个月工单量趋势
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        stats.put("monthlyOrders", baseMapper.countByMonth(sixMonthsAgo));
        // 近6个月营收趋势
        stats.put("monthlyRevenue", baseMapper.revenueByMonth(sixMonthsAgo));
        // 常用配件Top10
        stats.put("topParts", partUsageMapper.topUsedParts());
        return stats;
    }
}
