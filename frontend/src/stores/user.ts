import { defineStore } from "pinia";
import { ref } from "vue";
import { login, getUserInfo } from "@/api";
import type { LoginResponse, UserInfo } from "@/types";

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export const useUserStore = defineStore("user", () => {
  const token = ref<string>(localStorage.getItem("token") || "");
  const userInfo = ref<UserInfo | null>(null);

  const setToken = (newToken: string) => {
    token.value = newToken;
    localStorage.setItem("token", newToken);
  };

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info;
  };

  const loginAction = async (username: string, password: string) => {
    const result = await login({ username, password }) as ApiResult<any>;
    const data = result?.data;
    if (!data?.token) {
      throw new Error(result?.message || "登录失败：未获取到 token");
    }
    setToken(data.token);
    // 先从 login 响应中设置基本用户信息（包含 roleKey）
    setUserInfo({
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      roleKey: data.roleKey,
      roleName: data.roleName,
      avatar: data.avatar,
      phone: data.phone,
      email: data.email
    } as UserInfo);
    return data;
  };

  const fetchUserInfo = async () => {
    try {
      const result = await getUserInfo() as ApiResult<UserInfo>;
      if (result?.data) {
        setUserInfo(result.data);
      }
    } catch (error) {
      console.error("Failed to fetch user info:", error);
    }
  };

  const logout = () => {
    token.value = "";
    userInfo.value = null;
    localStorage.removeItem("token");
  };

  return {
    token,
    userInfo,
    login: loginAction,
    getUserInfo: fetchUserInfo,
    logout
  };
});
