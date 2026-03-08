import http from "@/api";

export const getPartList = (params: any) => {
  return http.get("/parts", params, { loading: false });
};

export const getAllParts = () => {
  return http.get("/parts/all", {}, { loading: false });
};

export const getPartDetail = (id: number) => {
  return http.get(`/parts/${id}`, {}, { loading: false });
};

export const addPart = (params: any) => {
  return http.post("/parts", params);
};

export const updatePart = (id: number, params: any) => {
  return http.put(`/parts/${id}`, params);
};

export const deletePart = (id: number) => {
  return http.delete(`/parts/${id}`);
};

export const getLowStockParts = () => {
  return http.get("/parts/low-stock", {}, { loading: false });
};

export const adjustStock = (params: any) => {
  return http.post("/parts/stock-adjust", params);
};

export const getInventoryRecords = (params: any) => {
  return http.get("/parts/inventory-records", params, { loading: false });
};
