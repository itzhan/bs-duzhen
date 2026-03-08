<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #1a365d; margin-bottom: 24px;">服务预约</h2>
    <n-card style="border-radius: 12px; max-width: 600px;">
      <n-spin :show="vehiclesLoading">
        <n-empty v-if="!vehiclesLoading && vehicles.length === 0" description="暂无车辆信息，请联系管理员添加" />
        <n-form v-else ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
          <n-form-item label="选择车辆" path="vehicleId">
            <n-select v-model:value="formData.vehicleId" :options="vehicleOptions" placeholder="请选择车辆" size="large" />
          </n-form-item>
          <n-form-item label="故障描述" path="faultDesc">
            <n-input v-model:value="formData.faultDesc" type="textarea" :rows="4" placeholder="请详细描述车辆故障情况" size="large" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" size="large" :loading="submitting" @click="handleSubmit">提交预约</n-button>
          </n-form-item>
        </n-form>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { useMessage } from "naive-ui";
import type { FormInst, FormRules } from "naive-ui";
import { getMyVehicles, createAppointment } from "@/api";

const message = useMessage();
const formRef = ref<FormInst | null>(null);
const vehiclesLoading = ref(false);
const submitting = ref(false);
const vehicles = ref<any[]>([]);

const formData = reactive({ vehicleId: null as number | null, faultDesc: "" });

const vehicleOptions = computed(() => vehicles.value.map(v => ({
  label: `${v.plateNumber} (${v.brand} ${v.model})`, value: v.id
})));

const rules: FormRules = {
  vehicleId: [{ required: true, message: "请选择车辆", trigger: "change", type: "number" }],
  faultDesc: [{ required: true, message: "请输入故障描述", trigger: "blur" }, { min: 5, message: "至少5个字符", trigger: "blur" }]
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    await createAppointment({ vehicleId: formData.vehicleId, faultDesc: formData.faultDesc });
    message.success("预约成功！");
    formData.vehicleId = null;
    formData.faultDesc = "";
  } catch (e: any) {
    if (e?.errors) return;
    message.error(e?.response?.data?.message || "预约失败");
  } finally { submitting.value = false; }
};

onMounted(async () => {
  vehiclesLoading.value = true;
  try { const res = await getMyVehicles() as any; vehicles.value = res?.data || []; }
  catch (e) { console.error(e); }
  finally { vehiclesLoading.value = false; }
});
</script>
