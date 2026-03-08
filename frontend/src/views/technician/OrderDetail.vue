<template>
  <div>
    <n-button text @click="router.back()" style="margin-bottom: 16px; color: #6b7280;">
      <n-icon :size="18"><ArrowBackOutline /></n-icon> 返回
    </n-button>
    <n-spin :show="loading">
      <template v-if="order">
        <n-card title="工单信息" style="border-radius: 12px; margin-bottom: 16px;">
          <template #header-extra>
            <div style="display: flex; gap: 8px;">
              <n-tag :type="statusTypes[order.status]" size="large">{{ statusTexts[order.status] }}</n-tag>
              <n-button v-if="order.status === 1" type="primary" @click="doUpdateStatus(2)">提交质检</n-button>
              <n-button v-if="order.status === 2" type="success" @click="doUpdateStatus(3)">完成工单</n-button>
            </div>
          </template>
          <n-descriptions :column="2" label-placement="left">
            <n-descriptions-item label="工单号">{{ order.orderNo }}</n-descriptions-item>
            <n-descriptions-item label="客户">{{ detail.customerName || '-' }}</n-descriptions-item>
            <n-descriptions-item label="联系电话">{{ detail.customerPhone || '-' }}</n-descriptions-item>
            <n-descriptions-item label="车牌号">{{ detail.plateNumber || '-' }}</n-descriptions-item>
            <n-descriptions-item label="车辆">{{ detail.vehicleInfo || '-' }}</n-descriptions-item>
            <n-descriptions-item label="进店里程">{{ order.intakeMileage ? order.intakeMileage + ' km' : '-' }}</n-descriptions-item>
            <n-descriptions-item label="故障描述" :span="2">{{ order.faultDesc || '-' }}</n-descriptions-item>
            <n-descriptions-item label="诊断结果" :span="2">{{ order.diagnosis || '-' }}</n-descriptions-item>
          </n-descriptions>
        </n-card>
        <n-card v-if="items.length > 0" title="维修项目" style="border-radius: 12px; margin-bottom: 16px;">
          <n-list>
            <n-list-item v-for="item in items" :key="item.id">
              <div style="display: flex; justify-content: space-between;">
                <div>
                  <span>{{ item.itemName }}</span>
                  <n-tag :type="item.status === 2 ? 'success' : item.status === 1 ? 'info' : 'default'" size="small" style="margin-left: 8px;">
                    {{ item.status === 2 ? '已完成' : item.status === 1 ? '进行中' : '待开始' }}
                  </n-tag>
                </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import { getTechOrderDetail, updateTechOrderStatus } from "@/api";
import { ArrowBackOutline } from "@vicons/ionicons5";

const route = useRoute();
const router = useRouter();
const message = useMessage();
const loading = ref(true);
const order = ref<any>(null);
const detail = ref<any>({});
const items = ref<any[]>([]);
const partUsages = ref<any[]>([]);

const statusTexts: Record<number, string> = { 0: "待接单", 1: "维修中", 2: "待质检", 3: "已完成", 4: "已取消" };
const statusTypes: Record<number, "default"|"info"|"warning"|"success"|"error"> = { 0: "warning", 1: "info", 2: "warning", 3: "success", 4: "error" };

const loadDetail = async () => {
  loading.value = true;
  try {
    const id = Number(route.params.id);
    const res = await getTechOrderDetail(id) as any;
    const data = res?.data || {};
    order.value = data.order || {};
    detail.value = data;
    items.value = data.items || [];
    partUsages.value = data.partUsages || [];
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
};

const doUpdateStatus = async (status: number) => {
  try {
    await updateTechOrderStatus(order.value.id, status);
    message.success("状态更新成功");
    await loadDetail();
  } catch (e: any) { message.error(e?.response?.data?.message || "更新失败"); }
};

onMounted(() => loadDetail());
</script>
