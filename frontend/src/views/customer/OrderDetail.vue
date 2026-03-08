<template>
  <div>
    <n-button text @click="router.back()" style="margin-bottom: 16px; color: #6b7280;">
      <n-icon :size="18"><ArrowBackOutline /></n-icon> 返回
    </n-button>
    <n-spin :show="loading">
      <template v-if="order">
        <n-card title="工单信息" style="border-radius: 12px; margin-bottom: 16px;">
          <template #header-extra>
            <n-tag :type="statusTypes[order.status]" size="large">{{ statusTexts[order.status] }}</n-tag>
          </template>
          <n-descriptions :column="2" label-placement="left">
            <n-descriptions-item label="工单号">{{ order.orderNo }}</n-descriptions-item>
            <n-descriptions-item label="状态">{{ statusTexts[order.status] }}</n-descriptions-item>
            <n-descriptions-item label="故障描述" :span="2">{{ order.faultDesc || '-' }}</n-descriptions-item>
            <n-descriptions-item label="诊断结果" :span="2">{{ order.diagnosis || '-' }}</n-descriptions-item>
            <n-descriptions-item label="工时费">¥{{ Number(order.laborCost || 0).toFixed(2) }}</n-descriptions-item>
            <n-descriptions-item label="配件费">¥{{ Number(order.partsCost || 0).toFixed(2) }}</n-descriptions-item>
            <n-descriptions-item label="总费用"><span style="color: #dc2626; font-weight: 700; font-size: 18px;">¥{{ Number(order.totalCost || 0).toFixed(2) }}</span></n-descriptions-item>
            <n-descriptions-item label="支付状态">
              <n-tag :type="order.isPaid === 1 ? 'success' : 'warning'">{{ order.isPaid === 1 ? '已支付' : '未支付' }}</n-tag>
              <span v-if="order.paymentMethod" style="margin-left: 8px; color: #6b7280;">{{ payMethodText[order.paymentMethod] }}</span>
            </n-descriptions-item>
          </n-descriptions>
          <div v-if="order.status === 3 && order.isPaid === 0" style="margin-top: 16px; text-align: center;">
            <n-button type="primary" size="large" @click="handlePay">立即支付 ¥{{ Number(order.totalCost || 0).toFixed(2) }}</n-button>
          </div>
        </n-card>
        <n-card v-if="items.length > 0" title="维修项目" style="border-radius: 12px; margin-bottom: 16px;">
          <n-list>
            <n-list-item v-for="item in items" :key="item.id">
              <div style="display: flex; justify-content: space-between;">
                <span>{{ item.itemName }}</span>
                <span style="font-weight: 600;">¥{{ Number(item.amount || 0).toFixed(2) }}</span>
              </div>
            </n-list-item>
          </n-list>
        </n-card>
        <n-card v-if="partUsages.length > 0" title="使用配件" style="border-radius: 12px;">
          <n-list>
            <n-list-item v-for="p in partUsages" :key="p.id">
              <div style="display: flex; justify-content: space-between;">
                <span>配件 #{{ p.partId }} × {{ p.quantity }}</span>
                <span style="font-weight: 600;">¥{{ Number(p.amount || 0).toFixed(2) }}</span>
              </div>
            </n-list-item>
          </n-list>
        </n-card>
      </template>
    </n-spin>

    <n-modal v-model:show="payVisible" preset="dialog" title="工单支付" positive-text="确认支付" negative-text="取消" @positive-click="confirmPay">
      <div style="padding: 16px 0;">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; text-align: center;">
          支付金额：<span style="color: #dc2626; font-size: 24px;">¥{{ Number(order?.totalCost || 0).toFixed(2) }}</span>
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
import { useRoute, useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import { getMyOrderDetail, payOrder } from "@/api";
import { ArrowBackOutline } from "@vicons/ionicons5";

const route = useRoute();
const router = useRouter();
const message = useMessage();
const loading = ref(true);
const order = ref<any>(null);
const items = ref<any[]>([]);
const partUsages = ref<any[]>([]);

const statusTexts: Record<number, string> = { 0: "待接单", 1: "维修中", 2: "待质检", 3: "已完成", 4: "已取消" };
const statusTypes: Record<number, "default"|"info"|"warning"|"success"|"error"> = { 0: "warning", 1: "info", 2: "warning", 3: "success", 4: "error" };
const payMethodText: Record<string, string> = { WECHAT: "微信支付", ALIPAY: "支付宝", CARD: "银行卡", CASH: "现金" };

const payVisible = ref(false);
const paymentMethod = ref("WECHAT");

const handlePay = () => { payVisible.value = true; };
const confirmPay = async () => {
  try {
    await payOrder(order.value.id, { paymentMethod: paymentMethod.value });
    message.success("支付成功！");
    payVisible.value = false;
    await loadDetail();
    return true;
  } catch (e: any) { message.error("支付失败"); return false; }
};

const loadDetail = async () => {
  loading.value = true;
  try {
    const id = Number(route.params.id);
    const res = await getMyOrderDetail(id) as any;
    const data = res?.data || {};
    order.value = data.order || {};
    items.value = data.items || [];
    partUsages.value = data.partUsages || [];
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
};

onMounted(() => loadDetail());
</script>
