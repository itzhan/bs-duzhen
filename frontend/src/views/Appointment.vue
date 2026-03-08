<template>
  <div>
    <n-card title="服务预约" style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
      <n-empty v-if="!loading && vehicles.length === 0" description="您还没有添加车辆，请联系管理员添加车辆信息" />

      <n-spin :show="loading">
        <n-form
          v-if="vehicles.length > 0"
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-placement="left"
          label-width="100"
          style="max-width: 600px;"
        >
          <n-form-item label="选择车辆" path="vehicleId">
            <n-select
              v-model:value="formData.vehicleId"
              placeholder="请选择车辆"
              :options="vehicleOptions"
              size="large"
            />
          </n-form-item>
          <n-form-item label="故障描述" path="faultDescription">
            <n-input
              v-model:value="formData.faultDescription"
              type="textarea"
              placeholder="请详细描述车辆故障情况"
              :rows="4"
              size="large"
            />
          </n-form-item>
          <n-form-item label="预约时间" path="preferredTime">
            <n-date-picker
              v-model:value="formData.preferredTime"
              type="datetime"
              placeholder="请选择预约时间"
              size="large"
              style="width: 100%;"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              提交预约
            </n-button>
            <n-button size="large" style="margin-left: 12px;" @click="handleReset">
              重置
            </n-button>
          </n-form-item>
        </n-form>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { getMyVehicles, createRepairOrder } from "@/api";
import { useMessage } from "naive-ui";
import type { FormInst, FormRules } from "naive-ui";
import type { Vehicle } from "@/types";

const message = useMessage();
const formRef = ref<FormInst | null>(null);
const loading = ref(false);
const submitting = ref(false);
const vehicles = ref<Vehicle[]>([]);

const formData = reactive({
  vehicleId: null as number | null,
  faultDescription: "",
  preferredTime: null as number | null
});

const vehicleOptions = computed(() => {
  return vehicles.value.map(v => ({
    label: `${v.plateNumber}${v.brand && v.model ? ` (${v.brand} ${v.model})` : ""}`,
    value: v.id
  }));
});

const rules: FormRules = {
  vehicleId: [
    { required: true, message: "请选择车辆", trigger: "change", type: "number" }
  ],
  faultDescription: [
    { required: true, message: "请输入故障描述", trigger: "blur" },
    { min: 10, message: "故障描述至少10个字符", trigger: "blur" }
  ],
  preferredTime: [
    { required: true, message: "请选择预约时间", trigger: "change", type: "number" }
  ]
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    submitting.value = true;

    // 从选中的车辆获取 customerId
    const selectedVehicle = vehicles.value.find(v => v.id === formData.vehicleId);
    const submitData = {
      customerId: selectedVehicle?.customerId,
      vehicleId: formData.vehicleId,
      faultDesc: formData.faultDescription,
      estimatedFinishTime: formData.preferredTime ? new Date(formData.preferredTime).toISOString() : null
    };

    await createRepairOrder(submitData);
    message.success("预约成功！");
    handleReset();
  } catch (error: any) {
    if (error?.errors) {
      return;
    }
    message.error(error?.response?.data?.message || "预约失败，请重试");
  } finally {
    submitting.value = false;
  }
};

const handleReset = () => {
  formData.vehicleId = null;
  formData.faultDescription = "";
  formData.preferredTime = null;
  formRef.value?.restoreValidation();
};

onMounted(async () => {
  loading.value = true;
  try {
    const response = await getMyVehicles({ page: 1, size: 100 });
    // 后端返回 { code, data: { records, total } }
    const payload = response?.data || response;
    const list = payload?.records || payload?.list || (Array.isArray(payload) ? payload : []);
    vehicles.value = list;
  } catch (error) {
    console.error("Failed to load vehicles:", error);
    message.error("加载车辆信息失败");
  } finally {
    loading.value = false;
  }
});
</script>
