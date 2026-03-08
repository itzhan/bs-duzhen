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
