<template>
  <div class="table-box">
    <el-card shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名/姓名/手机号"
          clearable
          style="width: 240px"
          @clear="fetchData"
          @keyup.enter="fetchData"
        />
        <el-select
          v-model="filterRoleId"
          placeholder="选择角色"
          clearable
          style="width: 160px; margin-left: 10px"
          @change="fetchData"
        >
          <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
        </el-select>
        <el-button type="primary" style="margin-left: 10px" @click="fetchData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%; margin-top: 15px">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.roleId)">{{ getRoleName(scope.row.roleId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? "启用" : "禁用" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="170" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleResetPwd(scope.row.id)">重置密码</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 15px; justify-content: flex-end"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（默认123456）" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getUserList, addUser, updateUser, deleteUser, resetPassword, getRoles } from "@/api/modules/sysUser";

const keyword = ref("");
const filterRoleId = ref<number | undefined>(undefined);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const tableData = ref<any[]>([]);
const roleList = ref<any[]>([]);

const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();
const form = reactive({
  id: undefined as number | undefined,
  username: "",
  password: "",
  realName: "",
  phone: "",
  email: "",
  roleId: undefined as number | undefined,
  status: 1
});
const formRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  roleId: [{ required: true, message: "请选择角色", trigger: "change" }]
};

const roleMap: Record<number, { name: string; tagType: string }> = {
  1: { name: "系统管理员", tagType: "danger" },
  2: { name: "服务顾问", tagType: "" },
  3: { name: "维修技师", tagType: "warning" },
  4: { name: "仓库管理员", tagType: "success" }
};

const getRoleName = (roleId: number) => roleMap[roleId]?.name || "未知";
const getRoleTagType = (roleId: number) => (roleMap[roleId]?.tagType || "info") as any;

const fetchData = async () => {
  const params: any = { page: page.value, size: size.value };
  if (keyword.value) params.keyword = keyword.value;
  if (filterRoleId.value) params.roleId = filterRoleId.value;
  const { data } = await getUserList(params);
  tableData.value = data.records;
  total.value = data.total;
};

const loadRoles = async () => {
  const { data } = await getRoles();
  roleList.value = data;
};

const resetForm = () => {
  form.id = undefined;
  form.username = "";
  form.password = "";
  form.realName = "";
  form.phone = "";
  form.email = "";
  form.roleId = undefined;
  form.status = 1;
};

const handleAdd = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  isEdit.value = true;
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: "",
    realName: row.realName,
    phone: row.phone,
    email: row.email,
    roleId: row.roleId,
    status: row.status
  });
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  await formRef.value?.validate();
  if (isEdit.value) {
    await updateUser(form.id!, form);
  } else {
    await addUser({ ...form, password: form.password || "123456" });
  }
  ElMessage.success(isEdit.value ? "更新成功" : "创建成功");
  dialogVisible.value = false;
  fetchData();
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm("确定删除该用户吗？", "提示", { type: "warning" }).then(async () => {
    await deleteUser(id);
    ElMessage.success("删除成功");
    fetchData();
  });
};

const handleResetPwd = (id: number) => {
  ElMessageBox.confirm("确定将密码重置为 123456 吗？", "提示", { type: "warning" }).then(async () => {
    await resetPassword(id);
    ElMessage.success("密码已重置为 123456");
  });
};

onMounted(() => {
  loadRoles();
  fetchData();
});
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
}
</style>
