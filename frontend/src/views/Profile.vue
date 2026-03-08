<template>
  <div>
    <n-grid :cols="2" :x-gap="24" responsive="screen" :cols-m="1">
      <!-- User Info -->
      <n-gi>
        <n-card title="个人信息" style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
          <n-descriptions :column="1" label-style="color: #6b7280; font-weight: 500;">
            <n-descriptions-item label="用户名">
              {{ userStore.userInfo?.username || "-" }}
            </n-descriptions-item>
            <n-descriptions-item label="真实姓名">
              {{ userStore.userInfo?.realName || "-" }}
            </n-descriptions-item>
            <n-descriptions-item label="手机号">
              {{ userStore.userInfo?.phone || "-" }}
            </n-descriptions-item>
            <n-descriptions-item label="邮箱">
              {{ userStore.userInfo?.email || "-" }}
            </n-descriptions-item>
            <n-descriptions-item label="角色">
              {{ userStore.userInfo?.roleName || "-" }}
            </n-descriptions-item>
          </n-descriptions>
        </n-card>
      </n-gi>

      <!-- Change Password -->
      <n-gi>
        <n-card title="修改密码" style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
          <n-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-placement="left"
            label-width="100"
          >
            <n-form-item label="原密码" path="oldPassword">
              <n-input
                v-model:value="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                size="large"
              />
            </n-form-item>
            <n-form-item label="新密码" path="newPassword">
              <n-input
                v-model:value="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                size="large"
              />
            </n-form-item>
            <n-form-item label="确认密码" path="confirmPassword">
              <n-input
                v-model:value="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                size="large"
              />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" size="large" :loading="changingPassword" @click="handleChangePassword">
                修改密码
              </n-button>
              <n-button size="large" style="margin-left: 12px;" @click="handleResetPassword">
                重置
              </n-button>
            </n-form-item>
          </n-form>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useUserStore } from "@/stores/user";
import { changePassword, getUserInfo } from "@/api";
import { useMessage } from "naive-ui";
import type { FormInst, FormRules } from "naive-ui";

const userStore = useUserStore();
const message = useMessage();
const passwordFormRef = ref<FormInst | null>(null);
const changingPassword = ref(false);

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: "请输入原密码", trigger: "blur" }
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value) => {
        return value === passwordForm.newPassword;
      },
      message: "两次输入的密码不一致",
      trigger: "blur"
    }
  ]
};

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return;

  try {
    await passwordFormRef.value.validate();
    changingPassword.value = true;

    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });

    message.success("密码修改成功");
    handleResetPassword();
  } catch (error: any) {
    if (error?.errors) {
      return;
    }
    message.error(error?.response?.data?.message || "密码修改失败");
  } finally {
    changingPassword.value = false;
  }
};

const handleResetPassword = () => {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  passwordFormRef.value?.restoreValidation();
};

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.getUserInfo();
  }
});
</script>

<style scoped>
:deep(.n-descriptions-item-label) {
  width: 100px;
}
</style>
