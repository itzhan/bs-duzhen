import http from "@/api";

// 客户列表（分页）
export const getCustomerList = (params: any) => {
  return http.get("/customers", params, { loading: false });
};

// 全部客户
export const getAllCustomers = () => {
  return http.get("/customers/all", {}, { loading: false });
};

// 客户详情
export const getCustomerDetail = (id: number) => {
  return http.get(`/customers/${id}`, {}, { loading: false });
};

// 客户的车辆
export const getCustomerVehicles = (id: number) => {
  return http.get(`/customers/${id}/vehicles`, {}, { loading: false });
};

// 新增客户
export const addCustomer = (params: any) => {
  return http.post("/customers", params);
};

// 编辑客户
export const updateCustomer = (id: number, params: any) => {
  return http.put(`/customers/${id}`, params);
};

// 删除客户
export const deleteCustomer = (id: number) => {
  return http.delete(`/customers/${id}`);
};
