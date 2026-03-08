<template>
  <div class="repair-order-page">
    <el-card shadow="never">
      <!-- 搜索区域 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="工单编号/客户/车辆" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="待接单" :value="0" />
            <el-option label="维修中" :value="1" />
            <el-option label="待质检" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable filterable style="width: 200px">
            <el-option v-for="customer in customerList" :key="customer.id" :label="customer.name" :value="customer.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="table-header">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增工单</el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="工单编号" width="150" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="plateNumber" label="车辆" width="120" />
        <el-table-column prop="technicianName" label="维修技师" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalCost" label="总费用" width="120">
          <template #default="{ row }"> ¥{{ (row.totalCost || 0).toFixed(2) }} </template>
        </el-table-column>
        <el-table-column prop="isPaid" label="是否结算" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPaid === 1 ? 'success' : 'warning'">
              {{ row.isPaid === 1 ? "已结算" : "未结算" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看详情</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="handleAssign(row)"> 派工 </el-button>
            <el-button type="info" link size="small" @click="handleStatusChange(row)"> 状态流转 </el-button>
            <el-button v-if="row.status === 3 && row.isPaid !== 1" type="success" link size="small" @click="handleSettle(row)">
              结算
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageable.pageNum"
          v-model:page-size="pageable.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pageable.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="formData.customerId"
            placeholder="请选择客户"
            filterable
            style="width: 100%"
            @change="handleCustomerChange"
          >
            <el-option v-for="customer in customerList" :key="customer.id" :label="customer.name" :value="customer.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆" prop="vehicleId">
          <el-select
            v-model="formData.vehicleId"
            placeholder="请先选择客户"
            :disabled="!formData.customerId"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="vehicle in vehicleList"
              :key="vehicle.id"
              :label="`${vehicle.plateNumber} - ${vehicle.brand} ${vehicle.model}`"
              :value="vehicle.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="formData.faultDesc" type="textarea" :rows="3" placeholder="请输入故障描述" />
        </el-form-item>
        <el-form-item label="接车里程" prop="intakeMileage">
          <el-input-number
            v-model="formData.intakeMileage"
            :min="0"
            :precision="0"
            style="width: 100%"
            placeholder="请输入接车里程"
          />
        </el-form-item>
        <el-form-item label="预计完成时间" prop="estimatedFinishTime">
          <el-date-picker
            v-model="formData.estimatedFinishTime"
            type="datetime"
            placeholder="选择预计完成时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 派工对话框 -->
    <el-dialog v-model="assignDialogVisible" title="派工" width="400px">
      <el-form ref="assignFormRef" :model="assignForm" label-width="100px">
        <el-form-item label="维修技师" prop="technicianId">
          <el-select v-model="assignForm.technicianId" placeholder="请选择维修技师" filterable style="width: 100%">
            <el-option v-for="tech in technicianList" :key="tech.id" :label="tech.realName || tech.username" :value="tech.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 状态流转对话框 -->
    <el-dialog v-model="statusDialogVisible" title="状态流转" width="400px">
      <el-form ref="statusFormRef" :model="statusForm" label-width="100px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentOrder?.status)">
            {{ getStatusText(currentOrder?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标状态" prop="status">
          <el-select v-model="statusForm.status" placeholder="请选择目标状态" style="width: 100%">
            <el-option
              v-for="option in getNextStatusOptions(currentOrder?.status)"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Refresh, Plus } from "@element-plus/icons-vue";
import {
  getRepairOrderList,
  addRepairOrder,
  updateRepairOrder,
  deleteRepairOrder,
  assignTechnician,
  updateOrderStatus,
  settleOrder
} from "@/api/modules/repairOrder";
import { getAllCustomers } from "@/api/modules/customer";
import { getCustomerVehicles } from "@/api/modules/customer";
import { getTechnicians } from "@/api/modules/sysUser";

const router = useRouter();

// 搜索表单
const searchForm = reactive({
  keyword: "",
  status: undefined as number | undefined,
  customerId: undefined as number | undefined
});

// 表格数据
const loading = ref(false);
const tableData = ref<any[]>([]);
const selectedRows = ref<any[]>([]);

// 分页
const pageable = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

// 客户列表
const customerList = ref<any[]>([]);

// 车辆列表
const vehicleList = ref<any[]>([]);

// 技师列表
const technicianList = ref<any[]>([]);

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref("新增工单");
const formRef = ref();
const formData = reactive({
  id: undefined as number | undefined,
  customerId: undefined as number | undefined,
  vehicleId: undefined as number | undefined,
  faultDesc: "",
  intakeMileage: undefined as number | undefined,
  estimatedFinishTime: ""
});

const formRules = {
  customerId: [{ required: true, message: "请选择客户", trigger: "change" }],
  vehicleId: [{ required: true, message: "请选择车辆", trigger: "change" }],
  faultDesc: [{ required: true, message: "请输入故障描述", trigger: "blur" }]
};

// 派工对话框
const assignDialogVisible = ref(false);
const assignFormRef = ref();
const assignForm = reactive({
  orderId: undefined as number | undefined,
  technicianId: undefined as number | undefined
});

// 状态流转对话框
const statusDialogVisible = ref(false);
const statusFormRef = ref();
const statusForm = reactive({
  orderId: undefined as number | undefined,
  status: undefined as number | undefined
});
const currentOrder = ref<any>(null);

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

const getNextStatusOptions = (currentStatus?: number) => {
  if (currentStatus === undefined) return [];
  const options: { label: string; value: number }[] = [];
  if (currentStatus === 0) {
    options.push({ label: "维修中", value: 1 });
  } else if (currentStatus === 1) {
    options.push({ label: "待质检", value: 2 });
  } else if (currentStatus === 2) {
    options.push({ label: "已完成", value: 3 });
  }
  return options;
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pageable.pageNum,
      size: pageable.pageSize
    };
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword;
    }
    if (searchForm.status !== undefined) {
      params.status = searchForm.status;
    }
    if (searchForm.customerId) {
      params.customerId = searchForm.customerId;
    }
    const res = await getRepairOrderList(params);
    if (res.code === 200 || res.code === 0) {
      const data = res.data || {};
      tableData.value = data.records || data.list || [];
      pageable.total = data.total || 0;
    }
  } catch (error: any) {
    ElMessage.error(error.message || "加载数据失败");
  } finally {
    loading.value = false;
  }
};

// 加载客户列表
const loadCustomers = async () => {
  try {
    const res = await getAllCustomers();
    if (res.code === 200 || res.code === 0) {
      customerList.value = res.data || [];
    }
  } catch (error) {
    console.error("加载客户列表失败", error);
  }
};

// 加载技师列表
const loadTechnicians = async () => {
  try {
    const res = await getTechnicians();
    if (res.code === 200 || res.code === 0) {
      technicianList.value = res.data || [];
    }
  } catch (error) {
    console.error("加载技师列表失败", error);
  }
};

// 加载车辆列表
const loadVehicles = async (customerId: number) => {
  try {
    const res = await getCustomerVehicles(customerId);
    if (res.code === 200 || res.code === 0) {
      vehicleList.value = res.data || [];
    }
  } catch (error) {
    ElMessage.error("加载车辆列表失败");
    vehicleList.value = [];
  }
};

// 搜索
const handleSearch = () => {
  pageable.pageNum = 1;
  loadData();
};

// 重置
const handleReset = () => {
  searchForm.keyword = "";
  searchForm.status = undefined;
  searchForm.customerId = undefined;
  handleSearch();
};

// 分页变化
const handleSizeChange = (size: number) => {
  pageable.pageSize = size;
  pageable.pageNum = 1;
  loadData();
};

const handleCurrentChange = (page: number) => {
  pageable.pageNum = page;
  loadData();
};

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection;
};

// 新增
const handleAdd = () => {
  dialogTitle.value = "新增工单";
  Object.assign(formData, {
    id: undefined,
    customerId: undefined,
    vehicleId: undefined,
    faultDesc: "",
    intakeMileage: undefined,
    estimatedFinishTime: ""
  });
  vehicleList.value = [];
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = "编辑工单";
  Object.assign(formData, {
    id: row.id,
    customerId: row.customerId,
    vehicleId: row.vehicleId,
    faultDesc: row.faultDesc,
    intakeMileage: row.intakeMileage,
    estimatedFinishTime: row.estimatedFinishTime
  });
  if (row.customerId) {
    loadVehicles(row.customerId);
  }
  dialogVisible.value = true;
};

// 客户变化
const handleCustomerChange = async (customerId: number) => {
  formData.vehicleId = undefined;
  if (customerId) {
    await loadVehicles(customerId);
  } else {
    vehicleList.value = [];
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const params: any = {
          customerId: formData.customerId,
          vehicleId: formData.vehicleId,
          faultDesc: formData.faultDesc,
          intakeMileage: formData.intakeMileage,
          estimatedFinishTime: formData.estimatedFinishTime
        };
        if (formData.id) {
          await updateRepairOrder(formData.id, params);
          ElMessage.success("编辑成功");
        } else {
          await addRepairOrder(params);
          ElMessage.success("新增成功");
        }
        dialogVisible.value = false;
        loadData();
      } catch (error: any) {
        ElMessage.error(error.message || "操作失败");
      }
    }
  });
};

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields();
  vehicleList.value = [];
};

// 查看详情
const handleView = (row: any) => {
  router.push({
    path: "/repair/detail",
    query: { id: row.id }
  });
};

// 派工
const handleAssign = (row: any) => {
  assignForm.orderId = row.id;
  assignForm.technicianId = undefined;
  assignDialogVisible.value = true;
};

// 派工提交
const handleAssignSubmit = async () => {
  if (!assignForm.technicianId) {
    ElMessage.warning("请选择维修技师");
    return;
  }
  try {
    await assignTechnician(assignForm.orderId!, assignForm.technicianId);
    ElMessage.success("派工成功");
    assignDialogVisible.value = false;
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || "派工失败");
  }
};

// 状态流转
const handleStatusChange = (row: any) => {
  currentOrder.value = row;
  statusForm.orderId = row.id;
  statusForm.status = undefined;
  statusDialogVisible.value = true;
};

// 状态流转提交
const handleStatusSubmit = async () => {
  if (statusForm.status === undefined) {
    ElMessage.warning("请选择目标状态");
    return;
  }
  try {
    await updateOrderStatus(statusForm.orderId!, statusForm.status);
    ElMessage.success("状态更新成功");
    statusDialogVisible.value = false;
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || "状态更新失败");
  }
};

// 结算
const handleSettle = async (row: any) => {
  try {
    await ElMessageBox.confirm("确认结算该工单？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });
    await settleOrder(row.id);
    ElMessage.success("结算成功");
    loadData();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "结算失败");
    }
  }
};

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm("确认删除该工单？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });
    await deleteRepairOrder(row.id);
    ElMessage.success("删除成功");
    loadData();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "删除失败");
    }
  }
};

// 初始化
onMounted(() => {
  loadData();
  loadCustomers();
  loadTechnicians();
});
</script>

<style scoped lang="scss">
.repair-order-page {
  .search-form {
    margin-bottom: 20px;
  }

  .table-header {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
