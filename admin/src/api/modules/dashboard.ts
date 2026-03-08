import http from "@/api";

export const getDashboardData = () => {
  return http.get("/dashboard", {}, { loading: false });
};
