<template>
  <div class="reminder-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务提醒管理</span>
          <el-button type="primary" @click="handleAdd">新增提醒</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <el-form :model="filters" inline class="filter-form">
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="定期保养" :value="1" />
            <el-option label="保险到期" :value="2" />
            <el-option label="维修进度" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="待发送" :value="0" />
            <el-option label="已发送" :value="1" />
            <el-option label="已确认" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="vehiclePlateNumber" label="车辆" width="120" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="content" label="提醒内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="remindDate" label="提醒日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.remindDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="handleSend(row)"> 发送 </el-button>
            <el-button v-if="row.status === 1" type="success" link size="small" @click="handleConfirm(row)"> 确认 </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-box">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑提醒' : '新增提醒'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="form.customerId"
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
            v-model="form.vehicleId"
            placeholder="请先选择客户"
            filterable
            :disabled="!form.customerId"
            style="width: 100%"
          >
            <el-option v-for="vehicle in vehicleList" :key="vehicle.id" :label="vehicle.plateNumber" :value="vehicle.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="定期保养" :value="1" />
            <el-option label="保险到期" :value="2" />
            <el-option label="维修进度" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="提醒内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入提醒内容" />
        </el-form-item>
        <el-form-item label="提醒日期" prop="remindDate">
          <el-date-picker
            v-model="form.remindDate"
            type="date"
            placeholder="请选择提醒日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="reminderList">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { getReminderList, addReminder, updateReminder, deleteReminder, updateReminderStatus } from "@/api/modules/reminder";
import { getAllCustomers, getCustomerVehicles } from "@/api/modules/customer";

// 分页
const page = ref(1);
const size = ref(10);
const total = ref(0);
const tableData = ref<any[]>([]);
const loading = ref(false);

// 筛选条件
const filters = reactive({
  type: undefined as number | undefined,
  status: undefined as number | undefined
});

// 对话框
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({
  id: undefined as number | undefined,
  customerId: undefined as number | undefined,
  vehicleId: undefined as number | undefined,
  type: undefined as number | undefined,
  title: "",
  content: "",
  remindDate: ""
});

// 客户和车辆列表
const customerList = ref<any[]>([]);
const vehicleList = ref<any[]>([]);

// 表单验证规则
const formRules: FormRules = {
  customerId: [{ required: true, message: "请选择客户", trigger: "change" }],
  vehicleId: [{ required: true, message: "请选择车辆", trigger: "change" }],
  type: [{ required: true, message: "请选择类型", trigger: "change" }],
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入提醒内容", trigger: "blur" }],
  remindDate: [{ required: true, message: "请选择提醒日期", trigger: "change" }]
};

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: page.value,
      size: size.value
    };
    if (filters.type !== undefined) params.type = filters.type;
    if (filters.status !== undefined) params.status = filters.status;

    const { data } = await getReminderList(params);
    tableData.value = data.records || [];
    total.value = data.total || 0;
  } catch (error) {
    ElMessage.error("获取数据失败");
  } finally {
    loading.value = false;
  }
};

// 加载客户列表
const loadCustomers = async () => {
  try {
    const { data } = await getAllCustomers();
    customerList.value = data || [];
  } catch (error) {
    ElMessage.error("加载客户列表失败");
  }
};

// 客户变化时加载车辆
const handleCustomerChange = async (customerId: number) => {
  form.vehicleId = undefined;
  vehicleList.value = [];
  if (customerId) {
    try {
      const { data } = await getCustomerVehicles(customerId);
      vehicleList.value = data || [];
    } catch (error) {
      ElMessage.error("加载车辆列表失败");
    }
  }
};

// 重置筛选
const resetFilters = () => {
  filters.type = undefined;
  filters.status = undefined;
  page.value = 1;
  fetchData();
};

// 重置表单
const resetForm = () => {
  form.id = undefined;
  form.customerId = undefined;
  form.vehicleId = undefined;
  form.type = undefined;
  form.title = "";
  form.content = "";
  form.remindDate = "";
  vehicleList.value = [];
  formRef.value?.resetFields();
};

// 新增
const handleAdd = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 编辑
const handleEdit = async (row: any) => {
  isEdit.value = true;
  Object.assign(form, {
    id: row.id,
    customerId: row.customerId,
    vehicleId: row.vehicleId,
    type: row.type,
    title: row.title,
    content: row.content,
    remindDate: row.remindDate
  });
  // 加载该客户的车辆列表
  if (row.customerId) {
    await handleCustomerChange(row.customerId);
  }
  dialogVisible.value = true;
};

// 删除
const handleDelete = (id: number) => {
  ElMessageBox.confirm("确定删除该提醒吗？", "提示", {
    type: "warning"
  })
    .then(async () => {
      try {
        await deleteReminder(id);
        ElMessage.success("删除成功");
        fetchData();
      } catch (error) {
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {
      // cancelled
    });
};

// 发送
const handleSend = (row: any) => {
  ElMessageBox.confirm("确定发送该提醒吗？", "提示", {
    type: "warning"
  })
    .then(async () => {
      try {
        await updateReminderStatus(row.id, 1);
        ElMessage.success("发送成功");
        fetchData();
      } catch (error) {
        ElMessage.error("发送失败");
      }
    })
    .catch(() => {
      // cancelled
    });
};

// 确认
const handleConfirm = (row: any) => {
  ElMessageBox.confirm("确定确认该提醒吗？", "提示", {
    type: "warning"
  })
    .then(async () => {
      try {
        await updateReminderStatus(row.id, 2);
        ElMessage.success("确认成功");
        fetchData();
      } catch (error) {
        ElMessage.error("确认失败");
      }
    })
    .catch(() => {
      // cancelled
    });
};

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async valid => {
    if (!valid) return;
    try {
      const params: any = {
        customerId: form.customerId,
        vehicleId: form.vehicleId,
        type: form.type,
        title: form.title,
        content: form.content,
        remindDate: form.remindDate
      };
      if (isEdit.value) {
        await updateReminder(form.id!, params);
        ElMessage.success("更新成功");
      } else {
        await addReminder(params);
        ElMessage.success("创建成功");
      }
      dialogVisible.value = false;
      fetchData();
    } catch (error) {
      ElMessage.error(isEdit.value ? "更新失败" : "创建失败");
    }
  });
};

// 分页变化
const handlePageChange = (val: number) => {
  page.value = val;
  fetchData();
};

const handleSizeChange = (val: number) => {
  size.value = val;
  page.value = 1;
  fetchData();
};

// 状态标签类型
const getStatusTagType = (status: number) => {
  const map: Record<number, string> = {
    0: "info",
    1: "warning",
    2: "success"
  };
  return map[status] || "info";
};

// 状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: "待发送",
    1: "已发送",
    2: "已确认"
  };
  return map[status] || "未知";
};

// 类型标签类型
const getTypeTagType = (type: number) => {
  const map: Record<number, string> = {
    1: "primary",
    2: "warning",
    3: "success",
    4: "info"
  };
  return map[type] || "info";
};

// 类型文本
const getTypeText = (type: number) => {
  const map: Record<number, string> = {
    1: "定期保养",
    2: "保险到期",
    3: "维修进度",
    4: "其他"
  };
  return map[type] || "未知";
};

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return "";
  return date.split("T")[0];
};

onMounted(() => {
  fetchData();
  loadCustomers();
});
</script>

<style scoped lang="scss">
.reminder-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .filter-form {
    margin-bottom: 20px;
  }

  .pagination-box {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
