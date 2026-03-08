import axios from "axios";
import router from "@/router";
import { useUserStore } from "@/stores/user";
import { createDiscreteApi } from "naive-ui";

const { message } = createDiscreteApi(["message"]);

const request = axios.create({
  baseURL: "/api",
  timeout: 10000
});

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

request.interceptors.response.use(
  response => {
    const payload = response.data;
    if (payload && typeof payload === "object" && "code" in payload) {
      return payload;
    }
    return { code: 200, message: "success", data: payload };
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        const userStore = useUserStore();
        userStore.logout();
        router.push({ name: "Login" });
        message.error("登录已过期，请重新登录");
      } else {
        const errorMessage = error.response.data?.message || error.response.data?.msg || "请求失败";
        message.error(errorMessage);
      }
    } else {
      message.error("网络错误，请检查网络连接");
    }
    return Promise.reject(error);
  }
);

export default request;
