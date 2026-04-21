<template>
  <div v-if="userStore.userInfo?.roleKey === 'CUSTOMER'">
    <!-- 悬浮按钮 -->
    <div class="chat-fab" @click="toggleOpen">
      <n-badge :value="unread" :max="99" :show="unread > 0">
        <div class="chat-fab-inner">
          <n-icon size="28" color="#fff"><ChatbubblesOutline /></n-icon>
        </div>
      </n-badge>
    </div>

    <!-- 聊天面板 -->
    <transition name="slide-fade">
      <div v-if="open" class="chat-panel">
        <div class="chat-header">
          <div style="display:flex;align-items:center;gap:8px;">
            <n-avatar :size="32" round style="background:#2563eb;">客</n-avatar>
            <div>
              <div style="font-weight:600;">在线客服</div>
              <div style="font-size:12px;color:#94a3b8;">
                {{ connected ? '在线' : '连接中...' }}
              </div>
            </div>
          </div>
          <n-icon size="20" style="cursor:pointer;color:#64748b;" @click="open = false">
            <CloseOutline />
          </n-icon>
        </div>

        <div ref="msgListRef" class="chat-body">
          <div v-if="messages.length === 0" style="text-align:center;color:#94a3b8;padding-top:30px;">
            发送消息开始与客服对话
          </div>
          <div
            v-for="m in messages"
            :key="m.id"
            :class="['msg-row', m.senderRole === 'USER' ? 'mine' : 'theirs']"
          >
            <div class="msg-bubble">{{ m.content }}</div>
            <div class="msg-time">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>

        <div class="chat-input">
          <n-input
            v-model:value="draft"
            type="textarea"
            placeholder="输入消息，Enter 发送，Shift+Enter 换行"
            :autosize="{ minRows: 1, maxRows: 3 }"
            @keydown="onKeydown"
          />
          <n-button type="primary" :disabled="!draft.trim() || !connected" @click="send">
            发送
          </n-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted, onBeforeUnmount } from "vue";
import { useUserStore } from "@/stores/user";
import { getMyChatMessages, getMyChatUnread, markMyChatRead } from "@/api";
import { ChatbubblesOutline, CloseOutline } from "@vicons/ionicons5";

interface Msg {
  id: number;
  conversationId: number;
  senderRole: "USER" | "CS";
  senderId: number;
  content: string;
  createdAt: string;
}

const userStore = useUserStore();
const open = ref(false);
const draft = ref("");
const messages = ref<Msg[]>([]);
const unread = ref(0);
const connected = ref(false);
const msgListRef = ref<HTMLElement | null>(null);

let ws: WebSocket | null = null;
let reconnectTimer: number | null = null;
let pingTimer: number | null = null;

const wsUrl = computed(() => {
  const proto = location.protocol === "https:" ? "wss:" : "ws:";
  return `${proto}//${location.host}/ws/chat?token=${encodeURIComponent(userStore.token)}`;
});

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight;
  });
}

function formatTime(t: string) {
  if (!t) return "";
  const d = new Date(t);
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${hh}:${mm}`;
}

async function loadHistory() {
  try {
    const res: any = await getMyChatMessages();
    messages.value = res.data || [];
    scrollToBottom();
  } catch {}
}

async function refreshUnread() {
  try {
    const res: any = await getMyChatUnread();
    unread.value = res.data || 0;
  } catch {}
}

function connect() {
  if (!userStore.token || userStore.userInfo?.roleKey !== "CUSTOMER") return;
  try {
    ws = new WebSocket(wsUrl.value);
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
        // 去重（自己发的本地也 push 了）
        if (!messages.value.find(m => m.id === msg.id)) {
          messages.value.push(msg);
          scrollToBottom();
        }
        if (msg.senderRole === "CS") {
          if (open.value) {
            // 在打开状态自动已读
            ws?.send(JSON.stringify({ type: "READ" }));
            unread.value = 0;
          } else {
            unread.value += 1;
            notify(msg.content);
          }
        }
      } else if (payload.type === "UNREAD") {
        if (!open.value) unread.value = payload.count || 0;
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

function send() {
  const text = draft.value.trim();
  if (!text || !ws || ws.readyState !== WebSocket.OPEN) return;
  ws.send(JSON.stringify({ type: "SEND", content: text }));
  draft.value = "";
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    send();
  }
}

function toggleOpen() {
  open.value = !open.value;
}

function notify(text: string) {
  if (!("Notification" in window)) return;
  if (Notification.permission === "granted") {
    new Notification("客服消息", { body: text });
  } else if (Notification.permission !== "denied") {
    Notification.requestPermission();
  }
}

watch(open, async (v) => {
  if (v) {
    await loadHistory();
    await markMyChatRead().catch(() => {});
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: "READ" }));
    }
    unread.value = 0;
  }
});

watch(() => userStore.token, (t) => {
  if (t && userStore.userInfo?.roleKey === "CUSTOMER") {
    if (ws) ws.close();
    connect();
    refreshUnread();
  } else {
    if (ws) ws.close();
    connected.value = false;
  }
});

onMounted(() => {
  if (userStore.userInfo?.roleKey === "CUSTOMER") {
    connect();
    refreshUnread();
    if ("Notification" in window && Notification.permission === "default") {
      Notification.requestPermission();
    }
  }
});

onBeforeUnmount(() => {
  if (ws) ws.close();
  if (pingTimer) clearInterval(pingTimer);
  if (reconnectTimer) clearTimeout(reconnectTimer);
});
</script>

<style scoped>
.chat-fab {
  position: fixed;
  right: 24px;
  bottom: 32px;
  z-index: 2000;
  cursor: pointer;
}
.chat-fab-inner {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #2563eb, #1e40af);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.4);
  transition: transform .2s;
}
.chat-fab-inner:hover { transform: scale(1.08); }

.chat-panel {
  position: fixed;
  right: 24px;
  bottom: 100px;
  width: 360px;
  height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  z-index: 2001;
  overflow: hidden;
}
.chat-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; border-bottom: 1px solid #e5e7eb; background:#f8fafc;
}
.chat-body {
  flex: 1; overflow-y: auto; padding: 12px; background:#f5f7fa;
}
.msg-row { display: flex; flex-direction: column; margin-bottom: 10px; }
.msg-row.mine { align-items: flex-end; }
.msg-row.theirs { align-items: flex-start; }
.msg-bubble {
  max-width: 75%; padding: 8px 12px; border-radius: 10px;
  font-size: 14px; word-break: break-word; white-space: pre-wrap;
}
.mine .msg-bubble { background:#2563eb; color:#fff; border-bottom-right-radius:2px; }
.theirs .msg-bubble { background:#fff; color:#1e293b; border:1px solid #e5e7eb; border-bottom-left-radius:2px; }
.msg-time { font-size: 11px; color:#94a3b8; margin-top:2px; }

.chat-input {
  padding: 10px; border-top: 1px solid #e5e7eb; display: flex; gap: 8px; align-items: flex-end;
}
.slide-fade-enter-active, .slide-fade-leave-active { transition: all .2s ease; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; transform: translateY(12px); }
</style>
