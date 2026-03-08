<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #0e4429; margin-bottom: 24px;">待接单工单</h2>
    <n-spin :show="loading">
      <n-empty v-if="!loading && orders.length === 0" description="暂无待接单工单" />
      <n-grid :cols="1" :y-gap="12">
        <n-gi v-for="order in orders" :key="order.id">
          <n-card hoverable style="border-radius: 12px;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div style="flex: 1;">
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                  <span style="font-size: 16px; font-weight: 600; color: #0e4429;">{{ order.orderNo }}</span>
                  <n-tag type="warning">待接单</n-tag>
                </div>
                <div style="color: #6b7280; font-size: 14px; margin-bottom: 4px;">故障描述：{{ order.faultDesc || '无描述' }}</div>
                <div style="color: #6b7280; font-size: 13px;">创建时间：{{ formatDate(order.createdAt) }}</div>
              </div>
              <n-button type="primary" @click="handleAccept(order)">
                <n-icon style="margin-right: 4px;"><HandLeftOutline /></n-icon>
                接单
              </n-button>
            </div>
          </n-card>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useMessage } from "naive-ui";
import { getAvailableOrders, acceptOrder } from "@/api";
import { HandLeftOutline } from "@vicons/ionicons5";

const message = useMessage();
const loading = ref(false);
const orders = ref<any[]>([]);

const formatDate = (d: string) => d ? new Date(d).toLocaleString("zh-CN") : "-";

const loadOrders = async () => {
  loading.value = true;
  try { const res = await getAvailableOrders() as any; orders.value = res?.data || []; }
  catch (e) { console.error(e); }
  finally { loading.value = false; }
};

const handleAccept = async (order: any) => {
  try {
    await acceptOrder(order.id);
    message.success(`已成功接取工单 ${order.orderNo}`);
    await loadOrders();
  } catch (e: any) { message.error(e?.response?.data?.message || "接单失败"); }
};

onMounted(() => loadOrders());
</script>
