<template>
  <n-layout>
    <n-layout-header bordered style="height: 64px; padding: 0 24px; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.1); position: fixed; top: 0; left: 0; right: 0; z-index: 1000;">
      <div style="display: flex; align-items: center; justify-content: space-between; height: 100%;">
        <div style="display: flex; align-items: center; gap: 12px;">
          <div style="width: 40px; height: 40px; background: #0e4429; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;">
            <n-icon :size="22"><BuildOutline /></n-icon>
          </div>
          <span style="font-size: 18px; font-weight: 600; color: #0e4429;">维修技师工作台</span>
        </div>
        <n-menu mode="horizontal" :options="menuOptions" :value="activeKey" @update:value="handleMenuUpdate" style="flex: 1; justify-content: center;" />
        <div style="display: flex; align-items: center; gap: 16px;">
          <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
            <div style="display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 4px;">
              <n-avatar :size="32" round>{{ userStore.userInfo?.realName?.[0] || 'T' }}</n-avatar>
              <span style="color: #0e4429; font-weight: 500;">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
            </div>
          </n-dropdown>
        </div>
      </div>
    </n-layout-header>
    <n-layout style="margin-top: 64px; min-height: calc(100vh - 64px);">
      <n-layout-content style="padding: 24px; background: #f5f7fa;">
        <router-view />
      </n-layout-content>
    </n-layout>
    <n-layout-footer style="padding: 24px; text-align: center; background: white; border-top: 1px solid #e5e7eb;">
      <div style="color: #6b7280; font-size: 14px;">© 2026 汽车售后维修服务平台 - 技师端</div>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h, type Component } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";
import { NIcon } from "naive-ui";
import type { MenuOption } from "naive-ui";
import { HomeOutline, BuildOutline, ClipboardOutline, ListOutline, NotificationsOutline, PersonOutline, LogOutOutline } from "@vicons/ionicons5";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const activeKey = computed(() => route.name as string);
const renderIcon = (icon: Component) => () => h(NIcon, null, { default: () => h(icon) });

const menuOptions: MenuOption[] = [
  { label: () => h("span", "首页"), key: "TechHome", icon: renderIcon(HomeOutline) },
  { label: () => h("span", "待接单"), key: "TechAvailable", icon: renderIcon(ListOutline) },
  { label: () => h("span", "我的工单"), key: "TechMyOrders", icon: renderIcon(ClipboardOutline) },
  { label: () => h("span", "我的提醒"), key: "TechReminders", icon: renderIcon(NotificationsOutline) }
];

const userMenuOptions: MenuOption[] = [
  { label: () => h("span", "个人中心"), key: "profile", icon: renderIcon(PersonOutline) },
  { label: () => h("span", "退出登录"), key: "logout", icon: renderIcon(LogOutOutline) }
];

const handleMenuUpdate = (key: string) => { router.push({ name: key }); };
const handleUserMenuSelect = (key: string) => {
  if (key === "profile") router.push({ name: "TechProfile" });
  else if (key === "logout") { userStore.logout(); router.push({ name: "Login" }); }
};
</script>
