<template>
  <n-layout>
    <n-layout-header bordered style="height: 64px; padding: 0 24px; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.1); position: fixed; top: 0; left: 0; right: 0; z-index: 1000;">
      <div style="display: flex; align-items: center; justify-content: space-between; height: 100%;">
        <!-- Logo & Title -->
        <div style="display: flex; align-items: center; gap: 12px;">
          <div style="width: 40px; height: 40px; background: #1a365d; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;">
            汽
          </div>
          <span style="font-size: 18px; font-weight: 600; color: #1a365d;">汽车售后维修服务平台</span>
        </div>

        <!-- Navigation Menu -->
        <n-menu
          mode="horizontal"
          :options="menuOptions"
          :value="activeKey"
          @update:value="handleMenuUpdate"
          style="flex: 1; justify-content: center;"
        />

        <!-- User Info -->
        <div style="display: flex; align-items: center; gap: 16px;">
          <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
            <div style="display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.2s;" @mouseenter="(e: any) => e.currentTarget.style.background = '#f5f7fa'" @mouseleave="(e: any) => e.currentTarget.style.background = 'transparent'">
              <n-avatar :size="32" :src="userStore.userInfo?.avatar" round>
                {{ userStore.userInfo?.realName?.[0] || userStore.userInfo?.username?.[0] || 'U' }}
              </n-avatar>
              <span style="color: #1a365d; font-weight: 500;">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
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
      <div style="color: #6b7280; font-size: 14px;">
        © 2026 汽车售后维修服务平台. All rights reserved.
      </div>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h, type Component } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";
import { NIcon } from "naive-ui";
import type { MenuOption } from "naive-ui";
import {
  HomeOutline,
  ConstructOutline,
  CalendarOutline,
  NotificationsOutline,
  PersonOutline,
  LogOutOutline
} from "@vicons/ionicons5";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const activeKey = computed(() => route.name as string);

const renderIcon = (icon: Component) => () => h(NIcon, null, { default: () => h(icon) });

const menuOptions: MenuOption[] = [
  {
    label: () => h("span", "首页"),
    key: "Home",
    icon: renderIcon(HomeOutline)
  },
  {
    label: () => h("span", "维修进度"),
    key: "RepairProgress",
    icon: renderIcon(ConstructOutline)
  },
  {
    label: () => h("span", "服务预约"),
    key: "Appointment",
    icon: renderIcon(CalendarOutline)
  },
  {
    label: () => h("span", "我的提醒"),
    key: "Reminders",
    icon: renderIcon(NotificationsOutline)
  }
];

const userMenuOptions: MenuOption[] = [
  {
    label: () => h("span", "个人中心"),
    key: "profile",
    icon: renderIcon(PersonOutline)
  },
  {
    label: () => h("span", "退出登录"),
    key: "logout",
    icon: renderIcon(LogOutOutline)
  }
];

const handleMenuUpdate = (key: string) => {
  router.push({ name: key });
};

const handleUserMenuSelect = (key: string) => {
  if (key === "profile") {
    router.push({ name: "Profile" });
  } else if (key === "logout") {
    userStore.logout();
    router.push({ name: "Login" });
  }
};
</script>

<style scoped>
:deep(.n-menu) {
  background: transparent;
}

:deep(.n-menu-item) {
  color: #4b5563;
}

:deep(.n-menu-item--selected) {
  color: #1a365d;
  font-weight: 600;
}

:deep(.n-menu-item:hover) {
  color: #1a365d;
}
</style>
