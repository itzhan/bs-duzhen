import http from "@/api";

export const getReminderList = (params: any) => {
  return http.get("/reminders", params, { loading: false });
};

export const getReminderDetail = (id: number) => {
  return http.get(`/reminders/${id}`, {}, { loading: false });
};

export const addReminder = (params: any) => {
  return http.post("/reminders", params);
};

export const updateReminder = (id: number, params: any) => {
  return http.put(`/reminders/${id}`, params);
};

export const updateReminderStatus = (id: number, status: number) => {
  return http.put(`/reminders/${id}/status`, {}, { params: { status } } as any);
};

export const deleteReminder = (id: number) => {
  return http.delete(`/reminders/${id}`);
};
