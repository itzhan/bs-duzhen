<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #1a365d; margin-bottom: 24px;">欢迎回来，{{ userStore.userInfo?.realName }}</h2>
    <n-grid :cols="4" :x-gap="16" :y-gap="16" style="margin-bottom: 32px;">
      <n-gi v-for="stat in stats" :key="stat.label">
        <n-card style="border-radius: 12px;">
          <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center;" :style="{ background: stat.bg }">
              <n-icon :size="24" :color="stat.color"><component :is="stat.icon" /></n-icon>
            </div>
            <div>
              <div style="font-size: 24px; font-weight: 700; color: #1a365d;">{{ stat.value }}</div>
              <div style="font-size: 13px; color: #6b7280;">{{ stat.label }}</div>
            </div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>
    <n-card title="最近工单" style="border-radius: 12px;">
      <n-empty v-if="recentOrders.length === 0" description="暂无工单记录" />
      <n-list v-else>
        <n-list-item v-for="order in recentOrders" :key="order.id" style="cursor: pointer;" @click="goToOrder(order.id)">
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
            <div>
              <div style="font-weight: 600; color: #1a365d;">{{ order.orderNo }}</div>
              <div style="font-size: 13px; color: #6b7280; margin-top: 4px;">{{ order.faultDesc || '无描述' }}</div>
            </div>
            <div style="text-align: right;">
              <n-tag :type="statusTypes[order.status]" size="small">{{ statusTexts[order.status] }}</n-tag>
              <div v-if="order.totalCost > 0" style="font-size: 14px; font-weight: 600; color: #1a365d; margin-top: 4px;">¥{{ Number(order.totalCost).toFixed(2) }}</div>
            </div>
          </div>
        </n-list-item>
      </n-list>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { getCustomerDashboard, getMyOrders } from "@/api";
import { CarSportOutline, ClipboardOutline, ConstructOutline, NotificationsOutline } from "@vicons/ionicons5";

const router = useRouter();
const userStore = useUserStore();

const statusTexts: Record<number, string> = { 0: "待接单", 1: "维修中", 2: "待质检", 3: "已完成", 4: "已取消" };
const statusTypes: Record<number, "default"|"info"|"warning"|"success"|"error"> = { 0: "warning", 1: "info", 2: "warning", 3: "success", 4: "error" };

const stats = ref([
  { label: "我的车辆", value: 0, icon: markRaw(CarSportOutline), bg: "#e6f2ff", color: "#2563eb" },
  { label: "全部工单", value: 0, icon: markRaw(ClipboardOutline), bg: "#fef3c7", color: "#d97706" },
  { label: "进行中", value: 0, icon: markRaw(ConstructOutline), bg: "#dcfce7", color: "#16a34a" },
  { label: "待支付", value: 0, icon: markRaw(NotificationsOutline), bg: "#fee2e2", color: "#dc2626" }
]);

const recentOrders = ref<any[]>([]);

const goToOrder = (id: number) => router.push({ name: "CustomerOrderDetail", params: { id } });

onMounted(async () => {
  try {
    const res = await getCustomerDashboard() as any;
    const d = res?.data || res;
    stats.value[0].value = d.vehicleCount || 0;
    stats.value[1].value = d.totalOrders || 0;
    stats.value[2].value = d.activeOrders || 0;
    stats.value[3].value = d.unpaidOrders || 0;
  } catch (e) { console.error(e); }
  try {
    const res = await getMyOrders() as any;
    recentOrders.value = (res?.data || []).slice(0, 5);
  } catch (e) { console.error(e); }
});
</script>
