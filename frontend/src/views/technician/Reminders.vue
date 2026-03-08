<template>
  <div>
    <h2 style="font-size: 24px; font-weight: 600; color: #0e4429; margin-bottom: 24px;">我的提醒</h2>
    <n-spin :show="loading">
      <n-empty v-if="!loading && reminders.length === 0" description="暂无提醒" />
      <n-grid :cols="1" :y-gap="12">
        <n-gi v-for="r in reminders" :key="r.id">
          <n-card hoverable style="border-radius: 12px;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div>
                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                  <n-tag type="info" size="small">{{ typeTexts[r.type] }}</n-tag>
                  <span style="font-weight: 600; color: #0e4429;">{{ r.title }}</span>
                </div>
                <p style="color: #6b7280; font-size: 14px;">{{ r.content }}</p>
              </div>
              <div style="text-align: right; white-space: nowrap;">
                <div style="color: #6b7280; font-size: 13px;">{{ r.remindDate }}</div>
                <n-tag :type="r.status >= 1 ? 'success' : 'warning'" size="small" style="margin-top: 4px;">
                  {{ r.status === 0 ? '待处理' : '已处理' }}
                </n-tag>
              </div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getTechReminders } from "@/api";

const loading = ref(false);
const reminders = ref<any[]>([]);
const typeTexts: Record<number, string> = { 1: "定期保养", 2: "保险到期", 3: "维修进度", 4: "其他" };

onMounted(async () => {
  loading.value = true;
  try { const res = await getTechReminders() as any; reminders.value = res?.data || []; }
  catch (e) { console.error(e); }
  finally { loading.value = false; }
});
</script>
