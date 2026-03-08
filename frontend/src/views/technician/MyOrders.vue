<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #0e4429; margin-bottom: 24px;">我的工单</h2>
    <n-tabs v-model:value="activeTab" @update:value="loadOrders" style="margin-bottom: 16px;">
      <n-tab-pane name="all" tab="全部" />
      <n-tab-pane name="1" tab="维修中" />
      <n-tab-pane name="2" tab="待质检" />
      <n-tab-pane name="3" tab="已完成" />
    </n-tabs>
    <n-spin :show="loading">
      <n-empty v-if="!loading && orders.length === 0" description="暂无工单" />
      <n-grid :cols="1" :y-gap="12">
        <n-gi v-for="order in orders" :key="order.id">
          <n-card hoverable style="border-radius: 12px; cursor: pointer;" @click="goToDetail(order.id)">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div>
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                  <span style="font-weight: 600; color: #0e4429;">{{ order.orderNo }}</span>
                  <n-tag :type="statusTypes[order.status]">{{ statusTexts[order.status] }}</n-tag>
                </div>
                <div style="color: #6b7280; font-size: 14px;">{{ order.faultDesc || '无描述' }}</div>
              </div>
              <div style="display: flex; gap: 8px;">
                <n-button v-if="order.status === 1" type="primary" size="small" @click.stop="updateStatus(order.id, 2)">提交质检</n-button>
                <n-button v-if="order.status === 2" type="success" size="small" @click.stop="updateStatus(order.id, 3)">完成工单</n-button>
              </div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import { getTechMyOrders, updateTechOrderStatus } from "@/api";

const router = useRouter();
const message = useMessage();
const loading = ref(false);
const orders = ref<any[]>([]);
const activeTab = ref("all");

const statusTexts: Record<number, string> = { 1: "维修中", 2: "待质检", 3: "已完成" };
const statusTypes: Record<number, "info"|"warning"|"success"> = { 1: "info", 2: "warning", 3: "success" };

const goToDetail = (id: number) => router.push({ name: "TechOrderDetail", params: { id } });

const loadOrders = async () => {
  loading.value = true;
  try {
    const params = activeTab.value !== "all" ? { status: Number(activeTab.value) } : {};
    const res = await getTechMyOrders(params) as any;
    orders.value = res?.data || [];
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
};

const updateStatus = async (id: number, status: number) => {
  try {
    await updateTechOrderStatus(id, status);
    message.success("状态更新成功");
    await loadOrders();
  } catch (e: any) { message.error(e?.response?.data?.message || "更新失败"); }
};

onMounted(() => loadOrders());
</script>
