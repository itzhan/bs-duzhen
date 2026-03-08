<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f5f7fa; padding: 24px;">
    <n-card style="width: 100%; max-width: 420px; border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.1);">
      <div style="text-align: center; margin-bottom: 32px;">
        <div style="width: 64px; height: 64px; background: #1a365d; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: white; font-size: 32px; font-weight: bold;">
          汽
        </div>
        <h2 style="font-size: 24px; font-weight: 600; color: #1a365d; margin-bottom: 8px;">登录</h2>
        <p style="color: #6b7280; font-size: 14px;">汽车售后维修服务平台</p>
      </div>

      <n-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="handleLogin">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" size="large" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            @keyup.enter="handleLogin"
          />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" size="large" block :loading="loading" @click="handleLogin">
            登录
          </n-button>
        </n-form-item>
      </n-form>

      <div style="text-align: center; margin-top: 24px; padding-top: 24px; border-top: 1px solid #e5e7eb;">
        <span style="color: #6b7280; font-size: 14px;">还没有账号？</span>
        <n-button text type="primary" @click="goToRegister" style="padding: 0 4px;">
          立即注册
        </n-button>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";
import { useMessage } from "naive-ui";
import type { FormInst, FormRules } from "naive-ui";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const message = useMessage();
const formRef = ref<FormInst | null>(null);
const loading = ref(false);

const formData = reactive({
  username: "",
  password: ""
});

const rules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" }
  ]
};

const handleLogin = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    const loginData = await userStore.login(formData.username, formData.password);
    message.success("登录成功");

    // 根据角色跳转到对应首页
    const roleKey = loginData.roleKey || userStore.userInfo?.roleKey;
    if (roleKey === "CUSTOMER") {
      router.push("/customer");
    } else if (roleKey === "TECHNICIAN") {
      router.push("/technician");
    } else {
      router.push("/login");
    }
  } catch (error: any) {
    if (error?.errors) {
      // Form validation errors
      return;
    }
    message.error(error?.response?.data?.message || "登录失败，请检查用户名和密码");
  } finally {
    loading.value = false;
  }
};

const goToRegister = () => {
  router.push({ name: "Register" });
};
</script>
