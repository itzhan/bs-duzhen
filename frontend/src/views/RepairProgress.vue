<template>
  <div>
    <n-card title="维修进度查询" style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
      <n-empty v-if="!loading && orders.length === 0" description="暂无维修工单" style="padding: 40px 0;">
        <template #extra>
          <n-button type="primary" @click="goToAppointment">立即预约</n-button>
        </template>
      </n-empty>

      <n-spin :show="loading">
        <div v-for="order in orders" :key="order.id" style="margin-bottom: 24px;">
          <n-card hoverable style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
              <div>
                <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 8px;">
                  <span style="font-size: 16px; font-weight: 600; color: #1a365d;">工单号：{{ order.orderNo }}</span>
                  <n-tag :type="getStatusType(order.status)" size="large">{{ getStatusText(order.status) }}</n-tag>
                </div>
                <div style="color: #6b7280; font-size: 14px; margin-top: 8px;">
                  <span>故障描述：{{ order.faultDesc || '-' }}</span>
                </div>
              </div>
              <div style="text-align: right; color: #6b7280; font-size: 14px;">
                <div v-if="order.createdAt">创建时间：{{ formatDate(order.createdAt) }}</div>
                <div v-if="order.actualFinishTime" style="margin-top: 4px;">完成时间：{{ formatDate(order.actualFinishTime) }}</div>
                <div v-if="order.totalCost" style="margin-top: 8px; font-size: 18px; font-weight: 600; color: #1a365d;">
                  费用：¥{{ Number(order.totalCost).toFixed(2) }}
                </div>
              </div>
            </div>

            <!-- Status Timeline -->
            <n-steps :current="getStatusStep(order.status)" size="small" style="margin: 24px 0;">
              <n-step title="待接单" />
              <n-step title="维修中" />
              <n-step title="待质检" />
              <n-step title="已完成" />
            </n-steps>

            <!-- Expandable Details -->
            <n-collapse @item-header-click="(data: any) => handleCollapseChange(data, order)">
              <n-collapse-item title="查看详情" name="details">
                <n-spin :show="order._detailLoading" size="small">
                  <div v-if="order._detailItems && order._detailItems.length > 0" style="margin-bottom: 16px;">
                    <h4 style="color: #1a365d; margin-bottom: 8px;">维修项目</h4>
                    <n-list>
                      <n-list-item v-for="item in order._detailItems" :key="item.id">
                        <div style="display: flex; justify-content: space-between;">
                          <span>{{ item.itemName }}</span>
                          <span style="color: #1a365d; font-weight: 600;">¥{{ Number(item.amount || 0).toFixed(2) }}</span>
                        </div>
                      </n-list-item>
                    </n-list>
                  </div>
                  <div v-if="order._detailParts && order._detailParts.length > 0">
                    <h4 style="color: #1a365d; margin-bottom: 8px;">使用配件</h4>
                    <n-list>
                      <n-list-item v-for="part in order._detailParts" :key="part.id">
                        <div style="display: flex; justify-content: space-between;">
                          <span>配件ID: {{ part.partId }} × {{ part.quantity }}</span>
                          <span style="color: #1a365d; font-weight: 600;">¥{{ Number(part.amount || 0).toFixed(2) }}</span>
                        </div>
                      </n-list-item>
                    </n-list>
                  </div>
                  <n-empty
                    v-if="!order._detailLoading && (!order._detailItems || order._detailItems.length === 0) && (!order._detailParts || order._detailParts.length === 0)"
                    description="暂无详情数据"
                    style="padding: 20px 0;"
                  />
                </n-spin>
              </n-collapse-item>
            </n-collapse>
          </n-card>
        </div>
      </n-spin>

      <!-- 分页 -->
      <div v-if="totalCount > 0" style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <n-pagination
          v-model:page="currentPage"
          v-model:page-size="pageSize"
          :item-count="totalCount"
          :page-sizes="[5, 10, 20]"
          show-size-picker
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getMyRepairOrders, getRepairOrderDetail } from "@/api";
import { useMessage } from "naive-ui";

const router = useRouter();
const message = useMessage();
const loading = ref(false);
const orders = ref<any[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalCount = ref(0);

const statusTextMap: Record<number, string> = {
  0: "待接单",
  1: "维修中",
  2: "待质检",
  3: "已完成",
  4: "已取消"
};

const getStatusText = (status: number): string => {
  return statusTextMap[status] || "未知";
};

const getStatusType = (status: number): "default" | "info" | "success" | "warning" | "error" => {
  const statusMap: Record<number, "default" | "info" | "success" | "warning" | "error"> = {
    0: "warning",
    1: "info",
    2: "warning",
    3: "success",
    4: "error"
  };
  return statusMap[status] || "default";
};

const getStatusStep = (status: number): number => {
  if (status === 4) return -1;
  return status;
};

const formatDate = (dateString: string) => {
  if (!dateString) return "-";
  const date = new Date(dateString);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
};

const goToAppointment = () => {
  router.push({ name: "Appointment" });
};

const loadOrders = async () => {
  loading.value = true;
  try {
    const response = await getMyRepairOrders({ page: currentPage.value, size: pageSize.value });
    const payload = (response as any)?.data || response;
    const list = payload?.records || payload?.list || (Array.isArray(payload) ? payload : []);
    orders.value = list.map((item: any) => ({
      ...item,
      _detailItems: null,
      _detailParts: null,
      _detailLoading: false,
      _detailLoaded: false
    }));
    totalCount.value = payload?.total || list.length;
  } catch (error) {
    console.error("Failed to load repair orders:", error);
    message.error("加载维修工单失败");
  } finally {
    loading.value = false;
  }
};

const handleCollapseChange = async (data: any, order: any) => {
  // Only load when expanding and not already loaded
  if (order._detailLoaded) return;
  order._detailLoading = true;
  try {
    const res = await getRepairOrderDetail(order.id);
    const detail = (res as any)?.data || res;
    order._detailItems = detail?.items || [];
    order._detailParts = detail?.partUsages || [];
    order._detailLoaded = true;
  } catch (error) {
    console.error("Failed to load order detail:", error);
    message.error("加载工单详情失败");
  } finally {
    order._detailLoading = false;
  }
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadOrders();
};

const handlePageSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadOrders();
};

onMounted(() => {
  loadOrders();
});
</script>
