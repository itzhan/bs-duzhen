import request from "@/utils/request";

// Auth
export const login = (data: any) => request.post("/auth/login", data);
export const getUserInfo = () => request.get("/auth/info");
export const changePassword = (data: any) => request.put("/auth/password", data);

// Public - customer registration
export const register = (data: any) => request.post("/public/register", data);

// ========================
// Customer Portal APIs
// ========================
export const getCustomerDashboard = () => request.get("/customer-portal/dashboard");
export const getCustomerProfile = () => request.get("/customer-portal/profile");
export const getMyVehicles = () => request.get("/customer-portal/vehicles");
export const getMyOrders = (params?: any) => request.get("/customer-portal/orders", { params });
export const getMyOrderDetail = (id: number) => request.get(`/customer-portal/orders/${id}`);
export const createAppointment = (data: any) => request.post("/customer-portal/orders", data);
export const payOrder = (id: number, data: any) => request.put(`/customer-portal/orders/${id}/pay`, data);
export const getMyReminders = () => request.get("/customer-portal/reminders");

// ========================
// Technician Portal APIs
// ========================
export const getTechDashboard = () => request.get("/technician-portal/dashboard");
export const getAvailableOrders = () => request.get("/technician-portal/available-orders");
export const acceptOrder = (id: number) => request.put(`/technician-portal/orders/${id}/accept`);
export const getTechMyOrders = (params?: any) => request.get("/technician-portal/my-orders", { params });
export const getTechOrderDetail = (id: number) => request.get(`/technician-portal/orders/${id}`);
export const updateTechOrderStatus = (id: number, status: number) =>
  request.put(`/technician-portal/orders/${id}/status`, null, { params: { status } });
export const getTechReminders = () => request.get("/technician-portal/reminders");

// ========================
// Chat APIs
// ========================
export const getMyConversation = () => request.get("/chat/my-conversation");
export const getMyChatMessages = () => request.get("/chat/my-messages");
export const markMyChatRead = () => request.post("/chat/my-read");
export const getMyChatUnread = () => request.get("/chat/my-unread");

export const getCsConversations = () => request.get("/chat/cs/conversations");
export const getCsConversationDetail = (id: number) => request.get(`/chat/cs/conversations/${id}`);
export const getCsConversationMessages = (id: number) => request.get(`/chat/cs/conversations/${id}/messages`);
export const markCsConversationRead = (id: number) => request.post(`/chat/cs/conversations/${id}/read`);
export const getCsTotalUnread = () => request.get("/chat/cs/unread");
