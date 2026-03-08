<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #1a365d; margin-bottom: 24px;">维修进度</h2>
    <n-tabs v-model:value="activeTab" @update:value="loadOrders" style="margin-bottom: 16px;">
      <n-tab-pane name="all" tab="全部" />
      <n-tab-pane name="0" tab="待接单" />
      <n-tab-pane name="1" tab="维修中" />
      <n-tab-pane name="2" tab="待质检" />
      <n-tab-pane name="3" tab="已完成" />
    </n-tabs>
    <n-spin :show="loading">
      <n-empty v-if="!loading && orders.length === 0" description="暂无工单" />
      <div v-for="order in orders" :key="order.id" style="margin-bottom: 16px;">
        <n-card hoverable style="border-radius: 12px; cursor: pointer;" @click="goToDetail(order.id)">
          <div style="display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
              <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                <span style="font-size: 16px; font-weight: 600; color: #1a365d;">{{ order.orderNo }}</span>
                <n-tag :type="statusTypes[order.status]">{{ statusTexts[order.status] }}</n-tag>
                <n-tag v-if="order.status === 3 && order.isPaid === 1" type="success" size="small">已支付</n-tag>
                <n-tag v-if="order.status === 3 && order.isPaid === 0" type="error" size="small">待支付</n-tag>
              </div>
              <div style="color: #6b7280; font-size: 14px;">{{ order.faultDesc || '无描述' }}</div>
            </div>
            <div style="text-align: right;">
              <div v-if="order.totalCost > 0" style="font-size: 18px; font-weight: 600; color: #1a365d;">¥{{ Number(order.totalCost).toFixed(2) }}</div>
              <n-button v-if="order.status === 3 && order.isPaid === 0" type="primary" size="small" style="margin-top: 8px;" @click.stop="handlePay(order)">
                立即支付
              </n-button>
            </div>
          </div>
          <n-steps :current="getStep(order.status)" size="small" style="margin-top: 16px;">
            <n-step title="待接单" /><n-step title="维修中" /><n-step title="待质检" /><n-step title="已完成" />
          </n-steps>
        </n-card>
      </div>
    </n-spin>

    <!-- 支付弹窗 -->
    <n-modal v-model:show="payVisible" preset="dialog" title="工单支付" positive-text="确认支付" negative-text="取消" @positive-click="confirmPay" :loading="payLoading">
      <div style="padding: 16px 0;">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; text-align: center;">
          支付金额：<span style="color: #dc2626; font-size: 24px;">¥{{ Number(payingOrder?.totalCost || 0).toFixed(2) }}</span>
        </div>
        <n-radio-group v-model:value="paymentMethod" style="display: flex; flex-direction: column; gap: 12px;">
          <n-radio value="WECHAT">微信支付</n-radio>
          <n-radio value="ALIPAY">支付宝</n-radio>
          <n-radio value="CARD">银行卡</n-radio>
          <n-radio value="CASH">现金</n-radio>
        </n-radio-group>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import { getMyOrders, payOrder } from "@/api";

const router = useRouter();
const message = useMessage();
const loading = ref(false);
const orders = ref<any[]>([]);
const activeTab = ref("all");

const statusTexts: Record<number, string> = { 0: "待接单", 1: "维修中", 2: "待质检", 3: "已完成", 4: "已取消" };
const statusTypes: Record<number, "default"|"info"|"warning"|"success"|"error"> = { 0: "warning", 1: "info", 2: "warning", 3: "success", 4: "error" };
const getStep = (s: number) => s === 4 ? -1 : s;

const payVisible = ref(false);
const payLoading = ref(false);
const payingOrder = ref<any>(null);
const paymentMethod = ref("WECHAT");

const goToDetail = (id: number) => router.push({ name: "CustomerOrderDetail", params: { id } });

const loadOrders = async () => {
  loading.value = true;
  try {
    const params = activeTab.value !== "all" ? { status: Number(activeTab.value) } : {};
    const res = await getMyOrders(params) as any;
    orders.value = res?.data || [];
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
};

const handlePay = (order: any) => { payingOrder.value = order; paymentMethod.value = "WECHAT"; payVisible.value = true; };

const confirmPay = async () => {
  if (!payingOrder.value) return false;
  payLoading.value = true;
  try {
    await payOrder(payingOrder.value.id, { paymentMethod: paymentMethod.value });
    message.success("支付成功！");
    payVisible.value = false;
    await loadOrders();
    return true;
  } catch (e: any) {
    message.error(e?.response?.data?.message || "支付失败");
    return false;
  } finally { payLoading.value = false; }
};

onMounted(() => loadOrders());
</script>
