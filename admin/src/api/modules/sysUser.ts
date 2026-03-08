import http from "@/api";

export const getUserList = (params: any) => {
  return http.get("/users", params, { loading: false });
};

export const getUserDetail = (id: number) => {
  return http.get(`/users/${id}`, {}, { loading: false });
};

export const addUser = (params: any) => {
  return http.post("/users", params);
};

export const updateUser = (id: number, params: any) => {
  return http.put(`/users/${id}`, params);
};

export const deleteUser = (id: number) => {
  return http.delete(`/users/${id}`);
};

export const resetPassword = (id: number) => {
  return http.put(`/users/${id}/reset-password`);
};

export const getRoles = () => {
  return http.get("/users/roles", {}, { loading: false });
};

export const getTechnicians = () => {
  return http.get("/users/technicians", {}, { loading: false });
};

export const getAdvisors = () => {
  return http.get("/users/advisors", {}, { loading: false });
};
