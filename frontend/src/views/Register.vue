<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f5f7fa; padding: 24px;">
    <n-card style="width: 100%; max-width: 480px; border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.1);">
      <div style="text-align: center; margin-bottom: 32px;">
        <div style="width: 64px; height: 64px; background: #1a365d; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: white; font-size: 32px; font-weight: bold;">
          汽
        </div>
        <h2 style="font-size: 24px; font-weight: 600; color: #1a365d; margin-bottom: 8px;">注册</h2>
        <p style="color: #6b7280; font-size: 14px;">汽车售后维修服务平台</p>
      </div>

      <n-alert type="info" style="margin-bottom: 24px;">
        <template #header>提示</template>
        账号注册功能暂未开放，请联系管理员开通账号
      </n-alert>

      <n-form ref="formRef" :model="formData" :rules="rules" style="margin-bottom: 24px;">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" size="large" disabled />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            disabled
          />
        </n-form-item>
        <n-form-item path="confirmPassword" label="确认密码">
          <n-input
            v-model:value="formData.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            disabled
          />
        </n-form-item>
        <n-form-item path="realName" label="真实姓名">
          <n-input v-model:value="formData.realName" placeholder="请输入真实姓名" size="large" disabled />
        </n-form-item>
        <n-form-item path="phone" label="手机号">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号" size="large" disabled />
        </n-form-item>
      </n-form>

      <n-button type="primary" size="large" block disabled style="margin-bottom: 24px;">
        注册
      </n-button>

      <div style="text-align: center; padding-top: 24px; border-top: 1px solid #e5e7eb;">
        <span style="color: #6b7280; font-size: 14px;">已有账号？</span>
        <n-button text type="primary" @click="goToLogin" style="padding: 0 4px;">
          立即登录
        </n-button>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import type { FormInst, FormRules } from "naive-ui";

const router = useRouter();
const formRef = ref<FormInst | null>(null);

const formData = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  realName: "",
  phone: ""
});

const rules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value) => {
        return value === formData.password;
      },
      message: "两次输入的密码不一致",
      trigger: "blur"
    }
  ],
  realName: [
    { required: true, message: "请输入真实姓名", trigger: "blur" }
  ],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
  ]
};

const goToLogin = () => {
  router.push({ name: "Login" });
};
</script>
