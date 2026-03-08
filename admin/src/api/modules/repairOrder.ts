import http from "@/api";

export const getRepairOrderList = (params: any) => {
  return http.get("/repair-orders", params, { loading: false });
};

export const getRepairOrderDetail = (id: number) => {
  return http.get(`/repair-orders/${id}`, {}, { loading: false });
};

export const addRepairOrder = (params: any) => {
  return http.post("/repair-orders", params);
};

export const updateRepairOrder = (id: number, params: any) => {
  return http.put(`/repair-orders/${id}`, params);
};

export const deleteRepairOrder = (id: number) => {
  return http.delete(`/repair-orders/${id}`);
};

export const assignTechnician = (id: number, technicianId: number) => {
  return http.put(`/repair-orders/${id}/assign`, { technicianId }, { params: { technicianId } } as any);
};

export const updateOrderStatus = (id: number, status: number) => {
  return http.put(`/repair-orders/${id}/status`, {}, { params: { status } } as any);
};

export const settleOrder = (id: number) => {
  return http.put(`/repair-orders/${id}/settle`);
};

export const getOrderStatistics = () => {
  return http.get("/repair-orders/statistics", {}, { loading: false });
};

// 维修项目
export const getRepairItems = (orderId: number) => {
  return http.get("/repair-items", { orderId }, { loading: false });
};

export const addRepairItem = (params: any) => {
  return http.post("/repair-items", params);
};

export const updateRepairItem = (id: number, params: any) => {
  return http.put(`/repair-items/${id}`, params);
};

export const updateRepairItemStatus = (id: number, status: number) => {
  return http.put(`/repair-items/${id}/status`, {}, { params: { status } } as any);
};

export const deleteRepairItem = (id: number) => {
  return http.delete(`/repair-items/${id}`);
};

// 配件使用
export const getPartUsages = (orderId: number) => {
  return http.get("/part-usages", { orderId }, { loading: false });
};

export const addPartUsage = (params: any) => {
  return http.post("/part-usages", params);
};

export const deletePartUsage = (id: number) => {
  return http.delete(`/part-usages/${id}`);
};
