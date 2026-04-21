import http from "@/api";

export const getConversations = () => {
  return http.get("/chat/cs/conversations", {}, { loading: false });
};

export const getConversationDetail = (id: number) => {
  return http.get(`/chat/cs/conversations/${id}`, {}, { loading: false });
};

export const getConversationMessages = (id: number) => {
  return http.get(`/chat/cs/conversations/${id}/messages`, {}, { loading: false });
};

export const markConversationRead = (id: number) => {
  return http.post(`/chat/cs/conversations/${id}/read`, {}, { loading: false });
};

export const getTotalUnread = () => {
  return http.get("/chat/cs/unread", {}, { loading: false });
};
