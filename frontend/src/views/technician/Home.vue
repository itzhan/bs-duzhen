<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #0e4429; margin-bottom: 24px;">工作台 - {{ userStore.userInfo?.realName }}</h2>
    <n-grid :cols="4" :x-gap="16" :y-gap="16" style="margin-bottom: 32px;">
      <n-gi v-for="stat in stats" :key="stat.label">
        <n-card style="border-radius: 12px; cursor: pointer;" hoverable @click="stat.onClick?.()">
          <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center;" :style="{ background: stat.bg }">
              <n-icon :size="24" :color="stat.color"><component :is="stat.icon" /></n-icon>
            </div>
            <div>
              <div style="font-size: 24px; font-weight: 700; color: #0e4429;">{{ stat.value }}</div>
              <div style="font-size: 13px; color: #6b7280;">{{ stat.label }}</div>
            </div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>
    <n-card title="我的进行中工单" style="border-radius: 12px;">
      <n-empty v-if="activeOrders.length === 0" description="暂无进行中的工单" />
      <n-list v-else>
        <n-list-item v-for="order in activeOrders" :key="order.id" style="cursor: pointer;" @click="goToOrder(order.id)">
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
            <div>
              <div style="font-weight: 600; color: #0e4429;">{{ order.orderNo }}</div>
              <div style="font-size: 13px; color: #6b7280; margin-top: 4px;">{{ order.faultDesc || '无描述' }}</div>
            </div>
            <n-tag :type="order.status === 1 ? 'info' : 'warning'">{{ order.status === 1 ? '维修中' : '待质检' }}</n-tag>
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
import { getTechDashboard, getTechMyOrders } from "@/api";
import { ListOutline, ConstructOutline, CheckmarkCircleOutline, NotificationsOutline } from "@vicons/ionicons5";

const router = useRouter();
const userStore = useUserStore();

const stats = ref([
  { label: "待接单", value: 0, icon: markRaw(ListOutline), bg: "#fee2e2", color: "#dc2626", onClick: () => router.push({ name: "TechAvailable" }) },
  { label: "进行中", value: 0, icon: markRaw(ConstructOutline), bg: "#e6f2ff", color: "#2563eb", onClick: () => router.push({ name: "TechMyOrders" }) },
  { label: "已完成", value: 0, icon: markRaw(CheckmarkCircleOutline), bg: "#dcfce7", color: "#16a34a", onClick: undefined as (() => void) | undefined },
  { label: "待处理提醒", value: 0, icon: markRaw(NotificationsOutline), bg: "#fef3c7", color: "#d97706", onClick: () => router.push({ name: "TechReminders" }) }
]);

const activeOrders = ref<any[]>([]);
const goToOrder = (id: number) => router.push({ name: "TechOrderDetail", params: { id } });

onMounted(async () => {
  try {
    const res = await getTechDashboard() as any;
    const d = res?.data || res;
    stats.value[0].value = d.availableOrders || 0;
    stats.value[1].value = d.myActiveOrders || 0;
    stats.value[2].value = d.myCompletedOrders || 0;
    stats.value[3].value = d.pendingReminders || 0;
  } catch (e) { console.error(e); }
  try {
    const res = await getTechMyOrders({ status: 1 }) as any;
    const inProgress = res?.data || [];
    const res2 = await getTechMyOrders({ status: 2 }) as any;
    const pending = res2?.data || [];
    activeOrders.value = [...inProgress, ...pending].slice(0, 5);
  } catch (e) { console.error(e); }
});
</script>
