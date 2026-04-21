<template>
  <div class="cs-workbench">
    <!-- 会话列表 -->
    <div class="cs-sidebar">
      <div class="cs-sidebar-header">
        <span>会话列表</span>
        <el-tag v-if="totalUnread > 0" type="danger" size="small">{{ totalUnread }} 未读</el-tag>
        <el-tag v-else type="info" size="small">共 {{ conversations.length }}</el-tag>
      </div>
      <div class="cs-conv-list">
        <div
          v-for="c in conversations"
          :key="c.id"
          :class="['cs-conv-item', { active: activeId === c.id }]"
          @click="openConversation(c.id)"
        >
          <el-badge :value="c.unread" :hidden="!c.unread" :max="99">
            <el-avatar :size="40">{{ (c.realName || c.username || '?')[0] }}</el-avatar>
          </el-badge>
          <div class="cs-conv-meta">
            <div class="cs-conv-name">
              <span>{{ c.realName || c.username }}</span>
              <span class="cs-conv-time">{{ formatTime(c.lastMessageAt) }}</span>
            </div>
            <div class="cs-conv-preview">{{ c.lastMessage || '暂无消息' }}</div>
          </div>
        </div>
        <div v-if="conversations.length === 0" class="cs-empty">暂无会话</div>
      </div>
    </div>

    <!-- 消息面板 -->
    <div class="cs-main">
      <template v-if="activeId && activeDetail">
        <div class="cs-main-header">
          <el-avatar :size="32">{{ (activeDetail.realName || activeDetail.username || '?')[0] }}</el-avatar>
          <div>
            <div style="font-weight:600;">{{ activeDetail.realName || activeDetail.username }}</div>
            <div style="font-size:12px;color:#94a3b8;">用户ID: {{ activeDetail.userId }}</div>
          </div>
          <div style="flex:1;"></div>
          <el-tag :type="connected ? 'success' : 'info'" size="small">
            {{ connected ? '已连接' : '连接中...' }}
          </el-tag>
        </div>
        <div ref="msgListRef" class="cs-main-body">
          <div v-if="messages.length === 0" class="cs-empty">暂无消息</div>
          <div
            v-for="m in messages"
            :key="m.id"
            :class="['cs-msg-row', m.senderRole === 'CS' ? 'mine' : 'theirs']"
          >
            <div class="cs-msg-bubble">{{ m.content }}</div>
            <div class="cs-msg-time">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>
        <div class="cs-main-input">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="输入消息，Ctrl+Enter 发送"
            @keydown.ctrl.enter.prevent="send"
          />
          <el-button type="primary" :disabled="!draft.trim() || !connected" @click="send">
            发送
          </el-button>
        </div>
      </template>
      <div v-else class="cs-placeholder">
        <el-icon size="64" color="#cbd5e1"><ChatDotRound /></el-icon>
        <div style="margin-top:16px;color:#94a3b8;">请从左侧选择一个会话</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";
import { ElNotification } from "element-plus";
import { ChatDotRound } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";
import {
  getConversations,
  getConversationDetail,
  getConversationMessages,
  markConversationRead,
  getTotalUnread
} from "@/api/modules/chat";

interface Conv {
  id: number;
  userId: number;
  username: string;
  realName: string;
  avatar?: string;
  lastMessage?: string;
  lastMessageAt?: string;
  unread: number;
}
interface Msg {
  id: number;
  conversationId: number;
  senderRole: "USER" | "CS";
  senderId: number;
  content: string;
  createdAt: string;
}

const userStore = useUserStore();
const conversations = ref<Conv[]>([]);
const activeId = ref<number | null>(null);
const activeDetail = ref<any>(null);
const messages = ref<Msg[]>([]);
const draft = ref("");
const connected = ref(false);
const totalUnread = ref(0);
const msgListRef = ref<HTMLElement | null>(null);

let ws: WebSocket | null = null;
let reconnectTimer: number | null = null;
let pingTimer: number | null = null;

function formatTime(t?: string) {
  if (!t) return "";
  const d = new Date(t);
  const today = new Date();
  const sameDay = d.toDateString() === today.toDateString();
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  if (sameDay) return `${hh}:${mm}`;
  return `${d.getMonth() + 1}/${d.getDate()} ${hh}:${mm}`;
}

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight;
  });
}

async function loadConversations() {
  const res: any = await getConversations();
  conversations.value = res.data || [];
}

async function refreshUnread() {
  const res: any = await getTotalUnread();
  totalUnread.value = res.data || 0;
}

async function openConversation(id: number) {
  activeId.value = id;
  const [d, m] = await Promise.all([
    getConversationDetail(id),
    getConversationMessages(id)
  ]);
  activeDetail.value = (d as any).data;
  messages.value = (m as any).data || [];
  await markConversationRead(id);
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: "READ", conversationId: id }));
  }
  // 清除该会话的未读标记
  const c = conversations.value.find(x => x.id === id);
  if (c) c.unread = 0;
  refreshUnread();
  scrollToBottom();
}

function send() {
  const text = draft.value.trim();
  if (!text || !ws || ws.readyState !== WebSocket.OPEN || !activeId.value) return;
  ws.send(JSON.stringify({ type: "SEND", conversationId: activeId.value, content: text }));
  draft.value = "";
}

function connect() {
  if (!userStore.token) return;
  const proto = location.protocol === "https:" ? "wss:" : "ws:";
  const url = `${proto}//${location.host}/ws/chat?token=${encodeURIComponent(userStore.token)}`;
  try {
    ws = new WebSocket(url);
  } catch {
    scheduleReconnect();
    return;
  }
  ws.onopen = () => {
    connected.value = true;
    if (pingTimer) clearInterval(pingTimer);
    pingTimer = window.setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: "PING" }));
      }
    }, 30000);
  };
  ws.onmessage = (e) => {
    try {
      const payload = JSON.parse(e.data);
      if (payload.type === "MESSAGE") {
        const msg: Msg = payload.message;
        const convId = msg.conversationId;
        // 更新会话列表摘要 & 未读
        const c = conversations.value.find(x => x.id === convId);
        if (c) {
          c.lastMessage = msg.content;
          c.lastMessageAt = msg.createdAt;
          // 置顶
          const idx = conversations.value.indexOf(c);
          if (idx > 0) {
            conversations.value.splice(idx, 1);
            conversations.value.unshift(c);
          }
          if (msg.senderRole === "USER") {
            if (activeId.value === convId) {
              // 当前打开着 -> 直接已读
              ws?.send(JSON.stringify({ type: "READ", conversationId: convId }));
              c.unread = 0;
            } else {
              c.unread = (c.unread || 0) + 1;
              notify(c.realName || c.username, msg.content);
            }
          }
        } else if (msg.senderRole === "USER") {
          // 新会话，重新拉列表
          loadConversations();
          notify("新会话", msg.content);
        }
        if (activeId.value === convId) {
          if (!messages.value.find(x => x.id === msg.id)) {
            messages.value.push(msg);
            scrollToBottom();
          }
        }
      } else if (payload.type === "UNREAD") {
        totalUnread.value = payload.count || 0;
      }
    } catch {}
  };
  ws.onclose = () => {
    connected.value = false;
    if (pingTimer) { clearInterval(pingTimer); pingTimer = null; }
    scheduleReconnect();
  };
  ws.onerror = () => { ws?.close(); };
}

function scheduleReconnect() {
  if (reconnectTimer) return;
  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null;
    connect();
  }, 3000);
}

function notify(title: string, body: string) {
  ElNotification({
    title: `${title} 发来消息`,
    message: body.length > 40 ? body.slice(0, 40) + "..." : body,
    type: "info",
    duration: 4000
  });
  if ("Notification" in window && Notification.permission === "granted") {
    new Notification(`${title} 发来消息`, { body });
  }
}

onMounted(async () => {
  if ("Notification" in window && Notification.permission === "default") {
    Notification.requestPermission();
  }
  await loadConversations();
  refreshUnread();
  connect();
});

onBeforeUnmount(() => {
  if (ws) ws.close();
  if (pingTimer) clearInterval(pingTimer);
  if (reconnectTimer) clearTimeout(reconnectTimer);
});
</script>

<style scoped lang="scss">
.cs-workbench {
  display: flex;
  height: calc(100vh - 160px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.cs-sidebar {
  width: 300px;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}
.cs-sidebar-header {
  padding: 14px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cs-conv-list { flex: 1; overflow-y: auto; }
.cs-conv-item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f1f5f9;
  transition: background .2s;
}
.cs-conv-item:hover { background: #f8fafc; }
.cs-conv-item.active { background: #eef2ff; }
.cs-conv-meta { flex: 1; min-width: 0; }
.cs-conv-name {
  display: flex; justify-content: space-between;
  font-weight: 500; font-size: 14px; color: #1e293b;
}
.cs-conv-time { font-size: 12px; color: #94a3b8; font-weight: normal; }
.cs-conv-preview {
  font-size: 13px; color: #64748b; margin-top: 4px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.cs-empty {
  padding: 40px; text-align: center; color: #94a3b8;
}

.cs-main { flex: 1; display: flex; flex-direction: column; }
.cs-main-header {
  padding: 14px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex; align-items: center; gap: 12px;
}
.cs-main-body {
  flex: 1; overflow-y: auto; padding: 20px;
  background: #f5f7fa;
}
.cs-msg-row { display: flex; flex-direction: column; margin-bottom: 12px; }
.cs-msg-row.mine { align-items: flex-end; }
.cs-msg-row.theirs { align-items: flex-start; }
.cs-msg-bubble {
  max-width: 70%; padding: 10px 14px; border-radius: 10px;
  font-size: 14px; word-break: break-word; white-space: pre-wrap;
  line-height: 1.5;
}
.mine .cs-msg-bubble { background:#2563eb; color:#fff; border-bottom-right-radius:2px; }
.theirs .cs-msg-bubble { background:#fff; color:#1e293b; border:1px solid #e5e7eb; border-bottom-left-radius:2px; }
.cs-msg-time { font-size: 11px; color:#94a3b8; margin-top: 3px; }

.cs-main-input {
  padding: 12px 16px;
  border-top: 1px solid #e5e7eb;
  display: flex; gap: 12px; align-items: flex-end;
}
.cs-placeholder {
  flex: 1;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
</style>
