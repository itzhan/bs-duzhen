<template>
  <div class="dashboard-container">
    <!-- 统计卡片行 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="(stat, index) in statsCards" :key="index">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <el-icon :size="24">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <el-statistic :value="stat.value" :precision="stat.precision || 0">
                <template #title>
                  <span class="stat-title">{{ stat.title }}</span>
                </template>
              </el-statistic>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">工单状态分布</span>
          </template>
          <ECharts :option="statusPieOption" :style="{ height: '350px' }" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">近6月工单趋势</span>
          </template>
          <ECharts :option="ordersLineOption" :style="{ height: '350px' }" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行2 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">近6月营收趋势</span>
          </template>
          <ECharts :option="revenueBarOption" :style="{ height: '350px' }" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">常用配件Top10</span>
          </template>
          <ECharts :option="partsBarOption" :style="{ height: '350px' }" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 库存预警表格 -->
    <el-row class="table-row">
      <el-col :span="24">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <span class="card-title">库存预警</span>
          </template>
          <el-table :data="lowStockParts" border stripe style="width: 100%">
            <el-table-column prop="part_code" label="配件编码" width="120" />
            <el-table-column prop="part_name" label="配件名称" />
            <el-table-column prop="stock_qty" label="当前库存" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="danger">{{ row.stock_qty }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="min_stock" label="最低库存" width="100" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default>
                <el-tag type="warning">库存不足</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="Dashboard">
import { ref, onMounted, computed } from "vue";
import { ElMessage } from "element-plus";
import { User, Van, Document, Money } from "@element-plus/icons-vue";
import ECharts from "@/components/ECharts/index.vue";
import { getDashboardData } from "@/api/modules/dashboard";
import type { ECOption } from "@/components/ECharts/config";

// 状态标签映射
const statusLabels: Record<number, string> = {
  0: "待接单",
  1: "维修中",
  2: "待质检",
  3: "已完成",
  4: "已取消"
};

// 状态颜色映射
const statusColors: Record<number, string> = {
  0: "#909399",
  1: "#409EFF",
  2: "#E6A23C",
  3: "#67C23A",
  4: "#F56C6C"
};

// 数据
const dashboardData = ref<any>({});
const loading = ref(false);

// 统计卡片数据
const statsCards = computed(() => [
  {
    title: "客户总数",
    value: dashboardData.value.customerCount || 0,
    icon: User,
    color: "#409EFF",
    precision: 0
  },
  {
    title: "车辆总数",
    value: dashboardData.value.vehicleCount || 0,
    icon: Van,
    color: "#67C23A",
    precision: 0
  },
  {
    title: "工单总数",
    value: dashboardData.value.totalOrders || 0,
    icon: Document,
    color: "#E6A23C",
    precision: 0
  },
  {
    title: "总营收",
    value: dashboardData.value.totalRevenue || 0,
    icon: Money,
    color: "#F56C6C",
    precision: 2
  }
]);

// 库存预警列表
const lowStockParts = computed(() => dashboardData.value.lowStockParts || []);

// 工单状态分布饼图配置
const statusPieOption = computed<ECOption>(() => {
  const distribution = dashboardData.value.statusDistribution || [];
  return {
    tooltip: {
      trigger: "item",
      formatter: "{a} <br/>{b}: {c} ({d}%)"
    },
    legend: {
      orient: "vertical",
      left: "left"
    },
    series: [
      {
        name: "工单状态",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: "{b}: {c}"
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: "bold"
          }
        },
        data: distribution.map((item: any) => ({
          value: item.count,
          name: statusLabels[item.status] || `状态${item.status}`,
          itemStyle: {
            color: statusColors[item.status] || "#909399"
          }
        }))
      }
    ]
  };
});

// 近6月工单趋势折线图配置
const ordersLineOption = computed<ECOption>(() => {
  const monthlyOrders = dashboardData.value.monthlyOrders || [];
  return {
    tooltip: {
      trigger: "axis"
    },
    xAxis: {
      type: "category",
      data: monthlyOrders.map((item: any) => item.month),
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: "value",
      name: "工单数"
    },
    series: [
      {
        name: "工单数",
        type: "line",
        smooth: true,
        data: monthlyOrders.map((item: any) => item.count),
        itemStyle: {
          color: "#409EFF"
        },
        areaStyle: {
          color: {
            type: "linear",
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: "rgba(64, 158, 255, 0.3)" },
              { offset: 1, color: "rgba(64, 158, 255, 0.1)" }
            ]
          }
        }
      }
    ]
  };
});

// 近6月营收趋势柱状图配置
const revenueBarOption = computed<ECOption>(() => {
  const monthlyRevenue = dashboardData.value.monthlyRevenue || [];
  return {
    tooltip: {
      trigger: "axis",
      formatter: (params: any) => {
        const param = params[0];
        return `${param.name}<br/>${param.seriesName}: ¥${param.value}`;
      }
    },
    xAxis: {
      type: "category",
      data: monthlyRevenue.map((item: any) => item.month),
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: "value",
      name: "营收(元)"
    },
    series: [
      {
        name: "营收",
        type: "bar",
        data: monthlyRevenue.map((item: any) => item.revenue),
        itemStyle: {
          color: {
            type: "linear",
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: "#67C23A" },
              { offset: 1, color: "#85CE61" }
            ]
          }
        }
      }
    ]
  };
});

// 常用配件Top10横向柱状图配置
const partsBarOption = computed<ECOption>(() => {
  const topUsedParts = dashboardData.value.topUsedParts || [];
  return {
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow"
      },
      formatter: (params: any) => {
        const param = params[0];
        return `${param.name}<br/>使用量: ${param.value}`;
      }
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true
    },
    xAxis: {
      type: "value",
      name: "使用量"
    },
    yAxis: {
      type: "category",
      data: topUsedParts.map((item: any) => item.part_name).reverse(),
      axisLabel: {
        interval: 0
      }
    },
    series: [
      {
        name: "使用量",
        type: "bar",
        data: topUsedParts.map((item: any) => item.total_qty).reverse(),
        itemStyle: {
          color: {
            type: "linear",
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: "#E6A23C" },
              { offset: 1, color: "#F0A020" }
            ]
          }
        }
      }
    ]
  };
});

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    loading.value = true;
    const res = await getDashboardData();
    if (res.code === 200) {
      dashboardData.value = res.data || {};
    } else {
      ElMessage.error(res.message || "获取数据失败");
    }
  } catch (error: any) {
    ElMessage.error(error.message || "获取数据失败");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadDashboardData();
});
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      flex-shrink: 0;
    }

    .stat-info {
      flex: 1;

      .stat-title {
        font-size: 14px;
        color: #909399;
        font-weight: normal;
      }

      :deep(.el-statistic__content) {
        .el-statistic__number {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }
      }
    }
  }
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.table-row {
  margin-bottom: 20px;
}

.table-card {
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
