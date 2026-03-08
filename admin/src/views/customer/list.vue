<template>
  <div class="customer-list-container">
    <el-card shadow="never" class="content-box">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入姓名或手机号"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
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
          新增客户
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
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'primary' : 'danger'">
              {{ row.gender === 1 ? "男" : "女" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
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
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" label-position="right">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" clearable maxlength="11" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" type="textarea" :rows="3" placeholder="请输入地址" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="CustomerList">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import { Search, Refresh, Plus, Edit, Delete } from "@element-plus/icons-vue";
import { getCustomerList, addCustomer, updateCustomer, deleteCustomer } from "@/api/modules/customer";

// 搜索表单
const searchForm = reactive({
  keyword: ""
});

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
const dialogTitle = ref("新增客户");
const formRef = ref<FormInstance>();
const submitLoading = ref(false);
const editingId = ref<number | null>(null);

// 表单数据
const formData = reactive({
  name: "",
  phone: "",
  gender: 1,
  email: "",
  address: ""
});

// 表单验证规则
const formRules: FormRules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
  ],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: "blur" }]
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
    const res = await getCustomerList(params);
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
  pagination.page = 1;
  loadData();
};

// 新增
const handleAdd = () => {
  dialogTitle.value = "新增客户";
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = "编辑客户";
  editingId.value = row.id;
  formData.name = row.name || "";
  formData.phone = row.phone || "";
  formData.gender = row.gender || 1;
  formData.email = row.email || "";
  formData.address = row.address || "";
  dialogVisible.value = true;
};

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm("确认删除该客户吗？删除后无法恢复！", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消"
    });
    const res = await deleteCustomer(row.id);
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
    const params = {
      name: formData.name,
      phone: formData.phone,
      gender: formData.gender,
      email: formData.email,
      address: formData.address
    };
    let res;
    if (editingId.value) {
      res = await updateCustomer(editingId.value, params);
    } else {
      res = await addCustomer(params);
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
  formData.name = "";
  formData.phone = "";
  formData.gender = 1;
  formData.email = "";
  formData.address = "";
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
  loadData();
});
</script>

<style scoped lang="scss">
.customer-list-container {
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
