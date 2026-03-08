<template>
  <div>
    <!-- Hero Section -->
    <div style="background: white; border-radius: 12px; padding: 80px 40px; margin-bottom: 40px; text-align: center; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
      <h1 style="font-size: 48px; font-weight: 700; color: #1a365d; margin-bottom: 16px;">专业汽车维修服务</h1>
      <p style="font-size: 20px; color: #6b7280; margin-bottom: 32px;">值得信赖的汽车售后维修管理平台</p>
      <n-button type="primary" size="large" @click="goToAppointment" style="height: 48px; padding: 0 32px; font-size: 16px;">
        立即预约
      </n-button>
    </div>

    <!-- Features Section -->
    <n-grid :cols="4" :x-gap="24" :y-gap="24" style="margin-bottom: 40px;">
      <n-gi v-for="feature in features" :key="feature.title">
        <n-card hoverable style="height: 100%; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
          <div style="text-align: center;">
            <div style="width: 64px; height: 64px; background: #e6f2ff; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
              <n-icon :size="32" color="#1a365d">
                <component :is="feature.icon" />
              </n-icon>
            </div>
            <h3 style="font-size: 18px; font-weight: 600; color: #1a365d; margin-bottom: 8px;">{{ feature.title }}</h3>
            <p style="font-size: 14px; color: #6b7280; line-height: 1.6;">{{ feature.description }}</p>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- Stats Section -->
    <n-card title="平台数据" style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
      <n-grid :cols="4" :x-gap="24" responsive="screen" :cols-m="2" :cols-s="1">
        <n-gi v-for="stat in stats" :key="stat.label">
          <n-statistic :label="stat.label" :value="stat.value" style="text-align: center;">
            <template #prefix>
              <n-icon :size="24" color="#1a365d" style="margin-right: 8px; vertical-align: middle;">
                <component :is="stat.icon" />
              </n-icon>
            </template>
          </n-statistic>
        </n-gi>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw, type Component } from "vue";
import { useRouter } from "vue-router";
import { getDashboardData } from "@/api";
import { useMessage } from "naive-ui";
import {
  ClipboardOutline,
  CalendarOutline,
  CubeOutline,
  NotificationsOutline,
  PeopleOutline,
  CarSportOutline,
  CheckmarkCircleOutline,
  StarOutline
} from "@vicons/ionicons5";

const router = useRouter();
const message = useMessage();

const features = [
  {
    icon: markRaw(ClipboardOutline),
    title: "维修工单跟踪",
    description: "实时查看维修进度，了解每个环节的详细状态"
  },
  {
    icon: markRaw(CalendarOutline),
    title: "在线预约服务",
    description: "便捷的在线预约系统，快速安排维修时间"
  },
  {
    icon: markRaw(CubeOutline),
    title: "配件库存透明",
    description: "配件使用情况一目了然，价格公开透明"
  },
  {
    icon: markRaw(NotificationsOutline),
    title: "贴心服务提醒",
    description: "智能提醒保养时间，让爱车始终保持最佳状态"
  }
];

const stats = ref<{ label: string; value: number | string; icon: Component }[]>([
  { label: "客户数", value: 0, icon: markRaw(PeopleOutline) },
  { label: "车辆数", value: 0, icon: markRaw(CarSportOutline) },
  { label: "完成工单数", value: 0, icon: markRaw(CheckmarkCircleOutline) },
  { label: "服务满意度", value: "98%", icon: markRaw(StarOutline) }
]);

const goToAppointment = () => {
  router.push({ name: "Appointment" });
};

onMounted(async () => {
  try {
    const res = await getDashboardData();
    const data = res?.data || res;
    if (data) {
      stats.value[0].value = data.customerCount || 0;
      stats.value[1].value = data.vehicleCount || 0;
      stats.value[2].value = data.completedOrders || data.completedOrderCount || 0;
      if (data.satisfactionRate) {
        stats.value[3].value = `${data.satisfactionRate}%`;
      }
    }
  } catch (error) {
    console.error("Failed to load dashboard data:", error);
  }
});
</script>

<style scoped>
:deep(.n-statistic-value) {
  color: #1a365d;
  font-weight: 700;
}

:deep(.n-statistic-label) {
  color: #6b7280;
  font-size: 14px;
}
</style>
