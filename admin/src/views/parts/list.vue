<template>
  <div class="parts-list-page">
    <el-card shadow="never">
      <!-- 搜索区域 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="配件编号/名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="searchForm.category" placeholder="配件分类" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="table-header">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增配件</el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="partCode" label="配件编号" width="150" />
        <el-table-column prop="partName" label="配件名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="specification" label="规格" width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="purchasePrice" label="进价" width="120">
          <template #default="{ row }">¥{{ (row.purchasePrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="salePrice" label="售价" width="120">
          <template #default="{ row }">¥{{ (row.salePrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stockQty" label="库存数量" width="120">
          <template #default="{ row }">
            <span :style="{ color: isLowStock(row) ? '#f56c6c' : '' }">
              {{ row.stockQty || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="120" />
        <el-table-column prop="location" label="存放位置" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleStockAdjust(row)"> 库存调整 </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageable.pageNum"
          v-model:page-size="pageable.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pageable.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="配件编号" prop="partCode">
              <el-input v-model="formData.partCode" placeholder="请输入配件编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配件名称" prop="partName">
              <el-input v-model="formData.partName" placeholder="请输入配件名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="formData.category" placeholder="请输入分类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="formData.brand" placeholder="请输入品牌" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="formData.specification" placeholder="请输入规格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="formData.unit" placeholder="如：个、件、套" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="进价" prop="purchasePrice">
              <el-input-number
                v-model="formData.purchasePrice"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入进价"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价" prop="salePrice">
              <el-input-number
                v-model="formData.salePrice"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入售价"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stockQty">
              <el-input-number
                v-model="formData.stockQty"
                :min="0"
                :precision="0"
                style="width: 100%"
                placeholder="请输入库存数量"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低库存" prop="minStock">
              <el-input-number
                v-model="formData.minStock"
                :min="0"
                :precision="0"
                style="width: 100%"
                placeholder="请输入最低库存"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="formData.location" placeholder="请输入存放位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库存调整对话框 -->
    <el-dialog v-model="stockDialogVisible" title="库存调整" width="500px" @close="handleStockDialogClose">
      <el-form ref="stockFormRef" :model="stockForm" :rules="stockFormRules" label-width="120px">
        <el-form-item label="配件名称">
          <el-input :value="currentPart?.partName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="currentPart?.stockQty || 0" disabled />
        </el-form-item>
        <el-form-item label="调整类型" prop="type">
          <el-select v-model="stockForm.type" placeholder="请选择调整类型" style="width: 100%">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="2" />
            <el-option label="盘点调整" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number
            v-model="stockForm.quantity"
            :min="stockForm.type === 3 ? undefined : 1"
            :precision="0"
            style="width: 100%"
            placeholder="请输入数量"
          />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number
            v-model="stockForm.unitPrice"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入单价（可选）"
          />
        </el-form-item>
        <el-form-item label="关联工单">
          <el-input-number
            v-model="stockForm.orderId"
            :min="0"
            :precision="0"
            style="width: 100%"
            placeholder="关联工单ID（可选）"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="stockForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Refresh, Plus } from "@element-plus/icons-vue";
import { getPartList, addPart, updatePart, deletePart, adjustStock } from "@/api/modules/part";

// 搜索表单
const searchForm = reactive({
  keyword: "",
  category: ""
});

// 表格数据
const loading = ref(false);
const tableData = ref<any[]>([]);
const selectedRows = ref<any[]>([]);

// 分页
const pageable = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref("新增配件");
const formRef = ref();
const formData = reactive({
  id: undefined as number | undefined,
  partCode: "",
  partName: "",
  category: "",
  brand: "",
  specification: "",
  unit: "",
  purchasePrice: undefined as number | undefined,
  salePrice: undefined as number | undefined,
  stockQty: undefined as number | undefined,
  minStock: undefined as number | undefined,
  location: "",
  status: 1
});

const formRules = {
  partCode: [{ required: true, message: "请输入配件编号", trigger: "blur" }],
  partName: [{ required: true, message: "请输入配件名称", trigger: "blur" }],
  category: [{ required: true, message: "请输入分类", trigger: "blur" }],
  unit: [{ required: true, message: "请输入单位", trigger: "blur" }],
  purchasePrice: [{ required: true, message: "请输入进价", trigger: "blur" }],
  salePrice: [{ required: true, message: "请输入售价", trigger: "blur" }],
  stockQty: [{ required: true, message: "请输入库存数量", trigger: "blur" }],
  minStock: [{ required: true, message: "请输入最低库存", trigger: "blur" }]
};

// 库存调整对话框
const stockDialogVisible = ref(false);
const stockFormRef = ref();
const currentPart = ref<any>(null);
const stockForm = reactive({
  partId: undefined as number | undefined,
  type: undefined as number | undefined,
  quantity: undefined as number | undefined,
  unitPrice: undefined as number | undefined,
  orderId: undefined as number | undefined,
  remark: ""
});

const stockFormRules = {
  type: [{ required: true, message: "请选择调整类型", trigger: "change" }],
  quantity: [{ required: true, message: "请输入数量", trigger: "blur" }]
};

// 判断是否低库存
const isLowStock = (row: any) => {
  return row.stockQty !== undefined && row.minStock !== undefined && row.stockQty <= row.minStock;
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pageable.pageNum,
      size: pageable.pageSize
    };
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword;
    }
    if (searchForm.category) {
      params.category = searchForm.category;
    }
    const res = await getPartList(params);
    if (res.code === 200 || res.code === 0) {
      const data = res.data || {};
      tableData.value = data.records || data.list || [];
      pageable.total = data.total || 0;
    }
  } catch (error: any) {
    ElMessage.error(error.message || "加载数据失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pageable.pageNum = 1;
  loadData();
};

// 重置
const handleReset = () => {
  searchForm.keyword = "";
  searchForm.category = "";
  handleSearch();
};

// 分页变化
const handleSizeChange = (size: number) => {
  pageable.pageSize = size;
  pageable.pageNum = 1;
  loadData();
};

const handleCurrentChange = (page: number) => {
  pageable.pageNum = page;
  loadData();
};

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection;
};

// 新增
const handleAdd = () => {
  dialogTitle.value = "新增配件";
  Object.assign(formData, {
    id: undefined,
    partCode: "",
    partName: "",
    category: "",
    brand: "",
    specification: "",
    unit: "",
    purchasePrice: undefined,
    salePrice: undefined,
    stockQty: undefined,
    minStock: undefined,
    location: "",
    status: 1
  });
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = "编辑配件";
  Object.assign(formData, {
    id: row.id,
    partCode: row.partCode,
    partName: row.partName,
    category: row.category,
    brand: row.brand,
    specification: row.specification,
    unit: row.unit,
    purchasePrice: row.purchasePrice,
    salePrice: row.salePrice,
    stockQty: row.stockQty,
    minStock: row.minStock,
    location: row.location,
    status: row.status
  });
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const params: any = {
          partCode: formData.partCode,
          partName: formData.partName,
          category: formData.category,
          brand: formData.brand,
          specification: formData.specification,
          unit: formData.unit,
          purchasePrice: formData.purchasePrice,
          salePrice: formData.salePrice,
          stockQty: formData.stockQty,
          minStock: formData.minStock,
          location: formData.location,
          status: formData.status
        };
        if (formData.id) {
          await updatePart(formData.id, params);
          ElMessage.success("编辑成功");
        } else {
          await addPart(params);
          ElMessage.success("新增成功");
        }
        dialogVisible.value = false;
        loadData();
      } catch (error: any) {
        ElMessage.error(error.message || "操作失败");
      }
    }
  });
};

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields();
};

// 库存调整
const handleStockAdjust = (row: any) => {
  currentPart.value = row;
  Object.assign(stockForm, {
    partId: row.id,
    type: undefined,
    quantity: undefined,
    unitPrice: undefined,
    orderId: undefined,
    remark: ""
  });
  stockDialogVisible.value = true;
};

// 库存调整提交
const handleStockSubmit = async () => {
  if (!stockFormRef.value) return;
  await stockFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await adjustStock({
          partId: stockForm.partId,
          type: stockForm.type,
          quantity: stockForm.quantity,
          unitPrice: stockForm.unitPrice,
          orderId: stockForm.orderId,
          remark: stockForm.remark
        });
        ElMessage.success("库存调整成功");
        stockDialogVisible.value = false;
        loadData();
      } catch (error: any) {
        ElMessage.error(error.message || "库存调整失败");
      }
    }
  });
};

// 库存调整对话框关闭
const handleStockDialogClose = () => {
  stockFormRef.value?.resetFields();
  currentPart.value = null;
};

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm("确认删除该配件？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });
    await deletePart(row.id);
    ElMessage.success("删除成功");
    loadData();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "删除失败");
    }
  }
};

// 初始化
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.parts-list-page {
  .search-form {
    margin-bottom: 20px;
  }

  .table-header {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
