import { Login } from "@/api/interface/index";
import authMenuList from "@/assets/json/authMenuList.json";
import authButtonList from "@/assets/json/authButtonList.json";
import http from "@/api";

/**
 * @name 登录模块
 */
// 用户登录
export const loginApi = (params: Login.ReqLoginForm) => {
  return http.post<Login.ResLogin>(`/auth/login`, params, { loading: false });
};

// 获取菜单列表 - 使用本地 JSON
export const getAuthMenuListApi = () => {
  // 使用本地JSON菜单数据
  return authMenuList;
};

// 获取按钮权限 - 使用本地 JSON
export const getAuthButtonListApi = () => {
  return authButtonList;
};

// 获取用户信息
export const getUserInfoApi = () => {
  return http.get(`/auth/info`, {}, { loading: false });
};

// 用户退出登录
export const logoutApi = () => {
  // 后端无退出接口，前端清除token即可
  return Promise.resolve({ code: 200, message: "退出成功", data: null });
};
