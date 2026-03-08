<template>
  <div>
    <n-card title="我的提醒" style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
      <n-tabs v-model:value="activeTab" @update:value="handleTabChange" style="margin-bottom: 24px;">
        <n-tab-pane name="all" tab="全部" />
        <n-tab-pane name="1" tab="保养提醒" />
        <n-tab-pane name="2" tab="保险提醒" />
        <n-tab-pane name="3" tab="维修进度" />
      </n-tabs>

      <n-empty v-if="!loading && filteredReminders.length === 0" description="暂无提醒" />

      <n-spin :show="loading">
        <n-grid :cols="1" :x-gap="16" :y-gap="16">
          <n-gi v-for="reminder in paginatedReminders" :key="reminder.id">
            <n-card hoverable style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
              <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div style="flex: 1;">
                  <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                    <n-tag :type="getTypeColor(reminder.type)" size="small">
                      {{ typeTextMap[reminder.type] || reminder.type }}
                    </n-tag>
                    <span style="font-size: 16px; font-weight: 600; color: #1a365d;">{{ reminder.title }}</span>
                  </div>
                  <p style="color: #6b7280; font-size: 14px; line-height: 1.6; margin-bottom: 12px;">
                    {{ reminder.content }}
                  </p>
                  <div style="display: flex; gap: 24px; color: #6b7280; font-size: 13px;">
                    <span>提醒时间：{{ formatDate(reminder.remindDate) }}</span>
                    <n-tag :type="reminder.status >= 1 ? 'success' : 'warning'" size="small">
                      {{ reminder.status === 0 ? '待发送' : reminder.status === 1 ? '已发送' : '已确认' }}
                    </n-tag>
                  </div>
                </div>
              </div>
            </n-card>
          </n-gi>
        </n-grid>
      </n-spin>

      <!-- 分页 -->
      <div v-if="filteredReminders.length > 0" style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <n-pagination
          v-model:page="currentPage"
          v-model:page-size="pageSize"
          :item-count="filteredReminders.length"
          :page-sizes="[5, 10, 20]"
          show-size-picker
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { getMyReminders } from "@/api";
import { useMessage } from "naive-ui";
import type { Reminder } from "@/types";

const message = useMessage();
const loading = ref(false);
const activeTab = ref("all");
const reminders = ref<Reminder[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);

const filteredReminders = computed(() => {
  if (activeTab.value === "all") {
    return reminders.value;
  }
  return reminders.value.filter(r => String(r.type) === activeTab.value);
});

const paginatedReminders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredReminders.value.slice(start, end);
});

const typeTextMap: Record<number, string> = {
  1: "定期保养",
  2: "保险到期",
  3: "维修进度",
  4: "其他"
};

const getTypeColor = (type: number): "default" | "info" | "success" | "warning" | "error" => {
  const colorMap: Record<number, "default" | "info" | "success" | "warning" | "error"> = {
    1: "info",
    2: "success",
    3: "warning",
    4: "default"
  };
  return colorMap[type] || "default";
};

const formatDate = (dateString: string) => {
  if (!dateString) return "-";
  const date = new Date(dateString);
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit"
  });
};

const handleTabChange = (value: string) => {
  activeTab.value = value;
  currentPage.value = 1;
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
};

const handlePageSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const loadReminders = async () => {
  loading.value = true;
  try {
    const response = await getMyReminders({ page: 1, size: 1000 });
    // 后端返回 { code, data: { records, total } }
    const payload = (response as any)?.data || response;
    const list = payload?.records || payload?.list || (Array.isArray(payload) ? payload : []);
    reminders.value = list;
  } catch (error) {
    console.error("Failed to load reminders:", error);
    message.error("加载提醒失败");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadReminders();
});
</script>
