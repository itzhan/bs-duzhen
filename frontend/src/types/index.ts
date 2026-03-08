export interface LoginResponse {
  token: string;
  userInfo?: UserInfo;
}

export interface UserInfo {
  userId: number;
  username: string;
  realName: string;
  roleKey: string;
  roleName: string;
  avatar?: string;
  phone?: string;
  email?: string;
}

export interface RepairOrder {
  id: number;
  orderNumber: string;
  vehicleId: number;
  vehiclePlate?: string;
  status: string;
  statusName?: string;
  faultDescription: string;
  createTime?: string;
  startTime?: string;
  finishTime?: string;
  totalCost?: number;
  repairItems?: RepairItem[];
  partsUsed?: PartUsed[];
}

export interface RepairItem {
  id: number;
  itemName: string;
  cost: number;
}

export interface PartUsed {
  id: number;
  partName: string;
  quantity: number;
  unitPrice: number;
}

export interface Vehicle {
  id: number;
  plateNumber: string;
  brand?: string;
  model?: string;
  year?: number;
}

export interface Reminder {
  id: number;
  type: string;
  typeName?: string;
  title: string;
  content: string;
  remindDate: string;
  status: string;
  statusName?: string;
}
