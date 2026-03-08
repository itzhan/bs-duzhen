import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      name: "Login",
      component: () => import("@/views/Login.vue"),
      meta: { public: true }
    },
    {
      path: "/register",
      name: "Register",
      component: () => import("@/views/Register.vue"),
      meta: { public: true }
    },
    // 顾客端路由
    {
      path: "/customer",
      component: () => import("@/layout/CustomerLayout.vue"),
      meta: { role: "CUSTOMER" },
      children: [
        { path: "", name: "CustomerHome", component: () => import("@/views/customer/Home.vue") },
        { path: "vehicles", name: "CustomerVehicles", component: () => import("@/views/customer/Vehicles.vue") },
        { path: "orders", name: "CustomerOrders", component: () => import("@/views/customer/Orders.vue") },
        { path: "orders/:id", name: "CustomerOrderDetail", component: () => import("@/views/customer/OrderDetail.vue") },
        { path: "appointment", name: "CustomerAppointment", component: () => import("@/views/customer/Appointment.vue") },
        { path: "reminders", name: "CustomerReminders", component: () => import("@/views/customer/Reminders.vue") },
        { path: "profile", name: "CustomerProfile", component: () => import("@/views/Profile.vue") }
      ]
    },
    // 技师端路由
    {
      path: "/technician",
      component: () => import("@/layout/TechnicianLayout.vue"),
      meta: { role: "TECHNICIAN" },
      children: [
        { path: "", name: "TechHome", component: () => import("@/views/technician/Home.vue") },
        { path: "available", name: "TechAvailable", component: () => import("@/views/technician/AvailableOrders.vue") },
        { path: "my-orders", name: "TechMyOrders", component: () => import("@/views/technician/MyOrders.vue") },
        { path: "orders/:id", name: "TechOrderDetail", component: () => import("@/views/technician/OrderDetail.vue") },
        { path: "reminders", name: "TechReminders", component: () => import("@/views/technician/Reminders.vue") },
        { path: "profile", name: "TechProfile", component: () => import("@/views/Profile.vue") }
      ]
    },
    // 默认重定向
    { path: "/", redirect: "/login" }
  ]
});

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const isPublicRoute = to.meta.public === true;
  const hasToken = !!userStore.token;

  if (!isPublicRoute && !hasToken) {
    return next({ name: "Login", query: { redirect: to.fullPath } });
  }

  if (hasToken && !userStore.userInfo) {
    await userStore.getUserInfo();
  }

  // 角色路由守卫
  const routeRole = to.matched.find(r => r.meta.role)?.meta.role;
  if (routeRole && userStore.userInfo) {
    const userRole = userStore.userInfo.roleKey;
    if (userRole !== routeRole) {
      // 重定向到正确的角色首页
      if (userRole === "CUSTOMER") return next("/customer");
      if (userRole === "TECHNICIAN") return next("/technician");
      return next("/login");
    }
  }

  next();
});

export default router;
