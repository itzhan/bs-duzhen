<template>
  <div class="vehicle-list-container">
    <el-card shadow="never" class="content-box">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入车牌号或VIN码"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.customerId" placeholder="请选择车主" clearable filterable style="width: 200px">
          <el-option v-for="customer in customerOptions" :key="customer.id" :label="customer.name" :value="customer.id" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增车辆
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="plateNumber" label="车牌号" width="110" />
        <el-table-column prop="brand" label="品牌" min-width="80" />
        <el-table-column prop="model" label="车型" min-width="110" show-overflow-tooltip />
        <el-table-column prop="color" label="颜色" width="70" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.color || "-" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vin" label="VIN码" width="180" show-overflow-tooltip />
        <el-table-column prop="customerName" label="车主" min-width="80" />
        <el-table-column prop="mileage" label="里程(km)" width="90" align="right">
          <template #default="{ row }">
            {{ row.mileage ? row.mileage.toLocaleString() : "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="购车日期" width="110" align="center">
          <template #default="{ row }">
            {{ formatDate(row.purchaseDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车牌号" prop="plateNumber">
              <el-input v-model="formData.plateNumber" placeholder="请输入车牌号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="formData.brand" placeholder="请输入品牌" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车型" prop="model">
              <el-input v-model="formData.model" placeholder="请输入车型" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input v-model="formData.color" placeholder="请输入颜色" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="VIN码" prop="vin">
              <el-input v-model="formData.vin" placeholder="请输入VIN码" clearable maxlength="17" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车主" prop="customerId">
              <el-select v-model="formData.customerId" placeholder="请选择车主" filterable style="width: 100%">
                <el-option v-for="customer in customerOptions" :key="customer.id" :label="customer.name" :value="customer.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="里程(km)" prop="mileage">
              <el-input-number v-model="formData.mileage" :min="0" :precision="0" placeholder="请输入里程" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购车日期" prop="purchaseDate">
              <el-date-picker
                v-model="formData.purchaseDate"
                type="date"
                placeholder="请选择购车日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="VehicleList">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import { Search, Refresh, Plus, Edit, Delete } from "@element-plus/icons-vue";
import { getVehicleList, addVehicle, updateVehicle, deleteVehicle } from "@/api/modules/vehicle";
import { getAllCustomers } from "@/api/modules/customer";

// 搜索表单
const searchForm = reactive({
  keyword: "",
  customerId: null as number | null
});

// 客户选项
const customerOptions = ref<any[]>([]);

// 表格数据
const tableData = ref<any[]>([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref("新增车辆");
const formRef = ref<FormInstance>();
const submitLoading = ref(false);
const editingId = ref<number | null>(null);

// 表单数据
const formData = reactive({
  plateNumber: "",
  brand: "",
  model: "",
  color: "",
  vin: "",
  customerId: null as number | null,
  mileage: null as number | null,
  purchaseDate: ""
});

// 表单验证规则
const formRules: FormRules = {
  plateNumber: [{ required: true, message: "请输入车牌号", trigger: "blur" }],
  brand: [{ required: true, message: "请输入品牌", trigger: "blur" }],
  model: [{ required: true, message: "请输入车型", trigger: "blur" }],
  vin: [
    { required: true, message: "请输入VIN码", trigger: "blur" },
    { min: 17, max: 17, message: "VIN码必须为17位", trigger: "blur" }
  ],
  customerId: [{ required: true, message: "请选择车主", trigger: "change" }],
  mileage: [{ type: "number", min: 0, message: "里程必须大于等于0", trigger: "blur" }],
  purchaseDate: [{ required: true, message: "请选择购车日期", trigger: "change" }]
};

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return "-";
  return date.split("T")[0];
};

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return "-";
  const date = new Date(dateTime);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 加载客户列表
const loadCustomers = async () => {
  try {
    const res = await getAllCustomers();
    if (res.code === 200) {
      customerOptions.value = res.data || [];
    }
  } catch (error: any) {
    console.error("加载客户列表失败:", error);
  }
};

// 加载列表数据
const loadData = async () => {
  try {
    loading.value = true;
    const params: any = {
      page: pagination.page,
      size: pagination.size
    };
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword;
    }
    if (searchForm.customerId) {
      params.customerId = searchForm.customerId;
    }
    const res = await getVehicleList(params);
    if (res.code === 200) {
      tableData.value = res.data?.records || [];
      pagination.total = res.data?.total || 0;
    } else {
      ElMessage.error(res.message || "获取数据失败");
    }
  } catch (error: any) {
    ElMessage.error(error.message || "获取数据失败");
  } finally {
    loading.value = false;
  }
};

// 查询
const handleSearch = () => {
  pagination.page = 1;
  loadData();
};

// 重置
const handleReset = () => {
  searchForm.keyword = "";
  searchForm.customerId = null;
  pagination.page = 1;
  loadData();
};

// 新增
const handleAdd = () => {
  dialogTitle.value = "新增车辆";
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = "编辑车辆";
  editingId.value = row.id;
  formData.plateNumber = row.plateNumber || "";
  formData.brand = row.brand || "";
  formData.model = row.model || "";
  formData.color = row.color || "";
  formData.vin = row.vin || "";
  formData.customerId = row.customerId || null;
  formData.mileage = row.mileage || null;
  formData.purchaseDate = row.purchaseDate ? formatDate(row.purchaseDate) : "";
  dialogVisible.value = true;
};

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm("确认删除该车辆吗？删除后无法恢复！", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消"
    });
    const res = await deleteVehicle(row.id);
    if (res.code === 200) {
      ElMessage.success("删除成功");
      loadData();
    } else {
      ElMessage.error(res.message || "删除失败");
    }
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "删除失败");
    }
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitLoading.value = true;
    const params: any = {
      plateNumber: formData.plateNumber,
      brand: formData.brand,
      model: formData.model,
      color: formData.color,
      vin: formData.vin,
      customerId: formData.customerId,
      mileage: formData.mileage,
      purchaseDate: formData.purchaseDate
    };
    let res;
    if (editingId.value) {
      res = await updateVehicle(editingId.value, params);
    } else {
      res = await addVehicle(params);
    }
    if (res.code === 200) {
      ElMessage.success(editingId.value ? "更新成功" : "新增成功");
      dialogVisible.value = false;
      loadData();
    } else {
      ElMessage.error(res.message || "操作失败");
    }
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || "操作失败");
    }
  } finally {
    submitLoading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  formData.plateNumber = "";
  formData.brand = "";
  formData.model = "";
  formData.color = "";
  formData.vin = "";
  formData.customerId = null;
  formData.mileage = null;
  formData.purchaseDate = "";
  formRef.value?.clearValidate();
};

// 对话框关闭
const handleDialogClose = () => {
  resetForm();
};

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  loadData();
};

// 页码改变
const handlePageChange = (page: number) => {
  pagination.page = page;
  loadData();
};

onMounted(() => {
  loadCustomers();
  loadData();
});
</script>

<style scoped lang="scss">
.vehicle-list-container {
  padding: 20px;
}

.content-box {
  min-height: calc(100vh - 100px);
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table) {
  .el-table__header {
    th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }
  }
}
</style>
