<template>
  <div class="repair-detail-page">
    <el-card shadow="never">
      <!-- 返回按钮 -->
      <div class="header-actions">
        <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
      </div>

      <!-- 工单信息卡片 -->
      <el-card v-if="orderInfo" shadow="never" class="order-info-card">
        <template #header>
          <div class="card-header">
            <span>工单信息</span>
            <el-tag :type="getStatusTagType(orderInfo.status)" size="large">
              {{ getStatusText(orderInfo.status) }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单编号">{{ orderInfo.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(orderInfo.status)">
              {{ getStatusText(orderInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户">{{ orderInfo.customerName }}</el-descriptions-item>
          <el-descriptions-item label="车辆">{{ orderInfo.plateNumber }}</el-descriptions-item>
          <el-descriptions-item label="维修技师">{{ orderInfo.technicianName || "-" }}</el-descriptions-item>
          <el-descriptions-item label="总费用">
            <span style="color: #f56c6c; font-weight: bold">¥{{ (orderInfo.totalCost || 0).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="是否结算">
            <el-tag :type="orderInfo.isPaid === 1 ? 'success' : 'warning'">
              {{ orderInfo.isPaid === 1 ? "已结算" : "未结算" }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="故障描述" :span="2">
            {{ orderInfo.faultDesc || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="诊断" :span="2">
            {{ orderInfo.diagnosis || "-" }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 维修项目列表 -->
      <el-card shadow="never" class="repair-items-card" style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>维修项目列表</span>
            <el-button type="primary" :icon="Plus" size="small" @click="handleAddRepairItem"> 新增项目 </el-button>
          </div>
        </template>
        <el-table v-loading="repairItemsLoading" :data="repairItems" border stripe style="width: 100%">
          <el-table-column prop="itemName" label="项目名称" min-width="150" />
          <el-table-column prop="itemType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getItemTypeTagType(row.itemType)">
                {{ getItemTypeText(row.itemType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="laborHours" label="工时数" width="100" />
          <el-table-column prop="laborPrice" label="工时单价" width="120">
            <template #default="{ row }">¥{{ (row.laborPrice || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }"> ¥{{ ((row.laborHours || 0) * (row.laborPrice || 0)).toFixed(2) }} </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? "已完成" : "进行中" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row, $index }">
              <el-button v-if="!row.editing" type="primary" link size="small" @click="handleEditRepairItem(row, $index)">
                编辑
              </el-button>
              <el-button v-if="!row.editing" type="danger" link size="small" @click="handleDeleteRepairItem(row)">
                删除
              </el-button>
              <template v-else>
                <el-button type="success" link size="small" @click="handleSaveRepairItem(row, $index)"> 保存 </el-button>
                <el-button type="info" link size="small" @click="handleCancelEditRepairItem(row, $index)"> 取消 </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 配件使用记录 -->
      <el-card shadow="never" class="parts-usage-card" style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>配件使用记录</span>
            <el-button type="primary" :icon="Plus" size="small" @click="handleAddPartUsage"> 新增配件 </el-button>
          </div>
        </template>
        <el-table v-loading="partsUsageLoading" :data="partsUsage" border stripe style="width: 100%">
          <el-table-column prop="partName" label="配件名称" min-width="150" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="unitPrice" label="单价" width="120">
            <template #default="{ row }">¥{{ (row.unitPrice || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }"> ¥{{ ((row.quantity || 0) * (row.unitPrice || 0)).toFixed(2) }} </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDeletePartUsage(row)"> 删除 </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons" style="margin-top: 20px">
        <el-button v-if="orderInfo?.status === 0" type="warning" @click="handleStatusChange(1)"> 开始维修 </el-button>
        <el-button v-if="orderInfo?.status === 1" type="primary" @click="handleStatusChange(2)"> 提交质检 </el-button>
        <el-button v-if="orderInfo?.status === 2" type="success" @click="handleStatusChange(3)"> 完成工单 </el-button>
        <el-button v-if="orderInfo?.status === 3 && orderInfo?.isPaid !== 1" type="success" @click="handleSettle"> 结算 </el-button>
      </div>
    </el-card>

    <!-- 新增/编辑维修项目对话框 -->
    <el-dialog
      v-model="repairItemDialogVisible"
      :title="repairItemDialogTitle"
      width="600px"
      @close="handleRepairItemDialogClose"
    >
      <el-form ref="repairItemFormRef" :model="repairItemForm" :rules="repairItemFormRules" label-width="100px">
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="repairItemForm.itemName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="类型" prop="itemType">
          <el-select v-model="repairItemForm.itemType" placeholder="请选择类型" style="width: 100%">
            <el-option label="维修" :value="1" />
            <el-option label="保养" :value="2" />
            <el-option label="钣喷" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="工时数" prop="laborHours">
          <el-input-number
            v-model="repairItemForm.laborHours"
            :min="0"
            :precision="1"
            style="width: 100%"
            placeholder="请输入工时数"
          />
        </el-form-item>
        <el-form-item label="工时单价" prop="laborPrice">
          <el-input-number
            v-model="repairItemForm.laborPrice"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入工时单价"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="repairItemForm.status">
            <el-radio :label="0">进行中</el-radio>
            <el-radio :label="1">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRepairItemSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增配件使用对话框 -->
    <el-dialog v-model="partUsageDialogVisible" title="新增配件" width="500px" @close="handlePartUsageDialogClose">
      <el-form ref="partUsageFormRef" :model="partUsageForm" :rules="partUsageFormRules" label-width="100px">
        <el-form-item label="配件" prop="partId">
          <el-select
            v-model="partUsageForm.partId"
            placeholder="请选择配件"
            filterable
            style="width: 100%"
            @change="handlePartChange"
          >
            <el-option
              v-for="part in partList"
              :key="part.id"
              :label="`${part.partName} (库存: ${part.stockQty || 0})`"
              :value="part.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number
            v-model="partUsageForm.quantity"
            :min="1"
            :precision="0"
            style="width: 100%"
            placeholder="请输入数量"
          />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="partUsageForm.unitPrice" :min="0" :precision="2" style="width: 100%" placeholder="单价" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="partUsageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePartUsageSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { ArrowLeft, Plus } from "@element-plus/icons-vue";
import {
  getRepairOrderDetail,
  addRepairItem,
  updateRepairItem,
  deleteRepairItem,
  addPartUsage,
  deletePartUsage,
  updateOrderStatus,
  settleOrder,
  getRepairItems,
  getPartUsages
} from "@/api/modules/repairOrder";
import { getAllParts } from "@/api/modules/part";

const route = useRoute();
const router = useRouter();

const orderId = computed(() => Number(route.query.id));

// 工单信息
const orderInfo = ref<any>(null);
const loading = ref(false);

// 维修项目
const repairItems = ref<any[]>([]);
const repairItemsLoading = ref(false);
const repairItemDialogVisible = ref(false);
const repairItemDialogTitle = ref("新增项目");
const repairItemFormRef = ref();
const repairItemForm = reactive({
  id: undefined as number | undefined,
  orderId: undefined as number | undefined,
  itemName: "",
  itemType: undefined as number | undefined,
  laborHours: undefined as number | undefined,
  laborPrice: undefined as number | undefined,
  status: 0
});

const repairItemFormRules = {
  itemName: [{ required: true, message: "请输入项目名称", trigger: "blur" }],
  itemType: [{ required: true, message: "请选择类型", trigger: "change" }],
  laborHours: [{ required: true, message: "请输入工时数", trigger: "blur" }],
  laborPrice: [{ required: true, message: "请输入工时单价", trigger: "blur" }]
};

// 配件使用
const partsUsage = ref<any[]>([]);
const partsUsageLoading = ref(false);
const partUsageDialogVisible = ref(false);
const partUsageFormRef = ref();
const partUsageForm = reactive({
  orderId: undefined as number | undefined,
  partId: undefined as number | undefined,
  quantity: undefined as number | undefined,
  unitPrice: undefined as number | undefined
});

const partUsageFormRules = {
  partId: [{ required: true, message: "请选择配件", trigger: "change" }],
  quantity: [{ required: true, message: "请输入数量", trigger: "blur" }],
  unitPrice: [{ required: true, message: "请输入单价", trigger: "blur" }]
};

// 配件列表
const partList = ref<any[]>([]);

// 状态相关
const statusMap: Record<number, string> = {
  0: "待接单",
  1: "维修中",
  2: "待质检",
  3: "已完成",
  4: "已取消"
};

const statusTagMap: Record<number, "info" | "warning" | "primary" | "success" | "danger"> = {
  0: "info",
  1: "warning",
  2: "primary",
  3: "success",
  4: "danger"
};

const getStatusText = (status?: number) => {
  return status !== undefined ? statusMap[status] || "未知" : "";
};

const getStatusTagType = (status?: number) => {
  return status !== undefined ? statusTagMap[status] || "info" : "info";
};

const itemTypeMap: Record<number, string> = {
  1: "维修",
  2: "保养",
  3: "钣喷",
  4: "其他"
};

const itemTypeTagMap: Record<number, "success" | "primary" | "warning" | "info"> = {
  1: "success",
  2: "primary",
  3: "warning",
  4: "info"
};

const getItemTypeText = (type?: number) => {
  return type !== undefined ? itemTypeMap[type] || "未知" : "";
};

const getItemTypeTagType = (type?: number) => {
  return type !== undefined ? itemTypeTagMap[type] || "info" : "info";
};

// 加载工单详情
const loadOrderDetail = async () => {
  if (!orderId.value) return;
  loading.value = true;
  try {
    const res = await getRepairOrderDetail(orderId.value);
    if (res.code === 200 || res.code === 0) {
      const detail = res.data || {};
      // 后端返回 { order, items, partUsages, customerName, plateNumber, advisorName, technicianName }
      const order = detail.order || {};
      order.customerName = detail.customerName || "";
      order.plateNumber = detail.plateNumber || "";
      order.advisorName = detail.advisorName || "";
      order.technicianName = detail.technicianName || "";
      orderInfo.value = order;
    }
  } catch (error: any) {
    ElMessage.error(error.message || "加载工单详情失败");
  } finally {
    loading.value = false;
  }
};

// 加载维修项目
const loadRepairItems = async () => {
  if (!orderId.value) return;
  repairItemsLoading.value = true;
  try {
    const res = await getRepairItems(orderId.value);
    if (res.code === 200 || res.code === 0) {
      repairItems.value = (res.data || []).map((item: any) => ({ ...item, editing: false }));
    }
  } catch (error: any) {
    ElMessage.error(error.message || "加载维修项目失败");
  } finally {
    repairItemsLoading.value = false;
  }
};

// 加载配件使用记录
const loadPartsUsage = async () => {
  if (!orderId.value) return;
  partsUsageLoading.value = true;
  try {
    const res = await getPartUsages(orderId.value);
    if (res.code === 200 || res.code === 0) {
      partsUsage.value = res.data || [];
    }
  } catch (error: any) {
    ElMessage.error(error.message || "加载配件使用记录失败");
  } finally {
    partsUsageLoading.value = false;
  }
};

// 加载配件列表
const loadParts = async () => {
  try {
    const res = await getAllParts();
    if (res.code === 200 || res.code === 0) {
      partList.value = res.data || [];
    }
  } catch (error) {
    console.error("加载配件列表失败", error);
  }
};

// 返回
const handleBack = () => {
  router.back();
};

// 新增维修项目
const handleAddRepairItem = () => {
  repairItemDialogTitle.value = "新增项目";
  Object.assign(repairItemForm, {
    id: undefined,
    orderId: orderId.value,
    itemName: "",
    itemType: undefined,
    laborHours: undefined,
    laborPrice: undefined,
    status: 0
  });
  repairItemDialogVisible.value = true;
};

// 编辑维修项目
const handleEditRepairItem = (row: any, index: number) => {
  repairItems.value[index].editing = true;
  repairItems.value[index]._original = { ...row };
};

// 保存维修项目（行内编辑）
const handleSaveRepairItem = async (row: any, index: number) => {
  try {
    const params: any = {
      orderId: orderId.value,
      itemName: row.itemName,
      itemType: row.itemType,
      laborHours: row.laborHours,
      laborPrice: row.laborPrice,
      status: row.status
    };
    if (row.id) {
      await updateRepairItem(row.id, params);
      ElMessage.success("更新成功");
    } else {
      await addRepairItem(params);
      ElMessage.success("新增成功");
    }
    repairItems.value[index].editing = false;
    await loadRepairItems();
    await loadOrderDetail();
  } catch (error: any) {
    ElMessage.error(error.message || "操作失败");
  }
};

// 取消编辑
const handleCancelEditRepairItem = (row: any, index: number) => {
  if (row._original) {
    Object.assign(row, row._original);
    delete row._original;
  }
  repairItems.value[index].editing = false;
};

// 删除维修项目
const handleDeleteRepairItem = async (row: any) => {
  if (!row.id) {
    repairItems.value = repairItems.value.filter(item => item !== row);
    return;
  }
  try {
    await ElMessageBox.confirm("确认删除该项目？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });
    await deleteRepairItem(row.id);
    ElMessage.success("删除成功");
    await loadRepairItems();
    await loadOrderDetail();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "删除失败");
    }
  }
};

// 提交维修项目表单
const handleRepairItemSubmit = async () => {
  if (!repairItemFormRef.value) return;
  await repairItemFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const params: any = {
          orderId: orderId.value,
          itemName: repairItemForm.itemName,
          itemType: repairItemForm.itemType,
          laborHours: repairItemForm.laborHours,
          laborPrice: repairItemForm.laborPrice,
          status: repairItemForm.status
        };
        if (repairItemForm.id) {
          await updateRepairItem(repairItemForm.id, params);
          ElMessage.success("更新成功");
        } else {
          await addRepairItem(params);
          ElMessage.success("新增成功");
        }
        repairItemDialogVisible.value = false;
        await loadRepairItems();
        await loadOrderDetail();
      } catch (error: any) {
        ElMessage.error(error.message || "操作失败");
      }
    }
  });
};

// 维修项目对话框关闭
const handleRepairItemDialogClose = () => {
  repairItemFormRef.value?.resetFields();
};

// 新增配件使用
const handleAddPartUsage = () => {
  Object.assign(partUsageForm, {
    orderId: orderId.value,
    partId: undefined,
    quantity: undefined,
    unitPrice: undefined
  });
  partUsageDialogVisible.value = true;
};

// 配件选择变化
const handlePartChange = (partId: number) => {
  const part = partList.value.find(p => p.id === partId);
  if (part) {
    partUsageForm.unitPrice = part.salePrice || part.price || 0;
  }
};

// 提交配件使用
const handlePartUsageSubmit = async () => {
  if (!partUsageFormRef.value) return;
  await partUsageFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await addPartUsage({
          orderId: orderId.value,
          partId: partUsageForm.partId,
          quantity: partUsageForm.quantity,
          unitPrice: partUsageForm.unitPrice
        });
        ElMessage.success("新增成功");
        partUsageDialogVisible.value = false;
        await loadPartsUsage();
        await loadOrderDetail();
      } catch (error: any) {
        ElMessage.error(error.message || "操作失败");
      }
    }
  });
};

// 配件使用对话框关闭
const handlePartUsageDialogClose = () => {
  partUsageFormRef.value?.resetFields();
};

// 删除配件使用
const handleDeletePartUsage = async (row: any) => {
  try {
    await ElMessageBox.confirm("确认删除该配件使用记录？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });
    await deletePartUsage(row.id);
    ElMessage.success("删除成功");
    await loadPartsUsage();
    await loadOrderDetail();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "删除失败");
    }
  }
};

// 状态变更
const handleStatusChange = async (status: number) => {
  try {
    await updateOrderStatus(orderId.value, status);
    ElMessage.success("状态更新成功");
    await loadOrderDetail();
  } catch (error: any) {
    ElMessage.error(error.message || "状态更新失败");
  }
};

// 结算
const handleSettle = async () => {
  try {
    await ElMessageBox.confirm("确认结算该工单？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });
    await settleOrder(orderId.value);
    ElMessage.success("结算成功");
    await loadOrderDetail();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "结算失败");
    }
  }
};

// 初始化
onMounted(async () => {
  if (!orderId.value) {
    ElMessage.error("工单ID不存在");
    router.back();
    return;
  }
  await Promise.all([loadOrderDetail(), loadRepairItems(), loadPartsUsage(), loadParts()]);
});
</script>

<style scoped lang="scss">
.repair-detail-page {
  .header-actions {
    margin-bottom: 20px;
  }

  .order-info-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .repair-items-card,
  .parts-usage-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .action-buttons {
    display: flex;
    gap: 10px;
    justify-content: center;
  }
}
</style>
