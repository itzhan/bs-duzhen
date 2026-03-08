<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #1a365d; margin-bottom: 24px;">我的车辆</h2>
    <n-spin :show="loading">
      <n-empty v-if="!loading && vehicles.length === 0" description="暂无车辆信息" />
      <n-grid :cols="3" :x-gap="16" :y-gap="16">
        <n-gi v-for="v in vehicles" :key="v.id">
          <n-card hoverable style="border-radius: 12px;">
            <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
              <n-icon :size="32" color="#1a365d"><CarSportOutline /></n-icon>
              <div>
                <div style="font-size: 18px; font-weight: 600; color: #1a365d;">{{ v.plateNumber }}</div>
                <div style="font-size: 13px; color: #6b7280;">{{ v.brand }} {{ v.model }}</div>
              </div>
            </div>
            <n-descriptions :column="1" label-style="color: #6b7280; font-size: 13px;" content-style="font-size: 13px;">
              <n-descriptions-item label="颜色">{{ v.color || '-' }}</n-descriptions-item>
              <n-descriptions-item label="里程">{{ v.mileage ? v.mileage.toLocaleString() + ' km' : '-' }}</n-descriptions-item>
              <n-descriptions-item label="保险到期">{{ v.insuranceExpireDate || '-' }}</n-descriptions-item>
            </n-descriptions>
          </n-card>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getMyVehicles } from "@/api";
import { CarSportOutline } from "@vicons/ionicons5";

const loading = ref(false);
const vehicles = ref<any[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    const res = await getMyVehicles() as any;
    vehicles.value = res?.data || [];
  } catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>
