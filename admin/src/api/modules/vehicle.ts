import http from "@/api";

export const getVehicleList = (params: any) => {
  return http.get("/vehicles", params, { loading: false });
};

export const getVehicleDetail = (id: number) => {
  return http.get(`/vehicles/${id}`, {}, { loading: false });
};

export const addVehicle = (params: any) => {
  return http.post("/vehicles", params);
};

export const updateVehicle = (id: number, params: any) => {
  return http.put(`/vehicles/${id}`, params);
};

export const deleteVehicle = (id: number) => {
  return http.delete(`/vehicles/${id}`);
};
