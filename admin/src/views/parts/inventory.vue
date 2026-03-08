<template>
  <div class="inventory-records-page">
    <el-card shadow="never">
      <!-- 搜索区域 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="配件">
          <el-select v-model="searchForm.partId" placeholder="请选择配件" clearable filterable style="width: 250px">
            <el-option v-for="part in partList" :key="part.id" :label="`${part.name} (${part.partNo})`" :value="part.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 150px">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="2" />
            <el-option label="盘点调整" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="partName" label="配件" min-width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.type === 2 ? '#f56c6c' : '#67c23a' }">
              {{ row.type === 2 ? "-" : "+" }}{{ row.quantity || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQty" label="变动前" width="120" />
        <el-table-column prop="afterQty" label="变动后" width="120" />
        <el-table-column prop="unitPrice" label="单价" width="120">
          <template #default="{ row }">
            <span v-if="row.unitPrice">¥{{ (row.unitPrice || 0).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联工单" width="150">
          <template #default="{ row }">
            <span v-if="row.orderNo">{{ row.orderNo }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="createTime" label="时间" width="180" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Search, Refresh } from "@element-plus/icons-vue";
import { getInventoryRecords, getAllParts } from "@/api/modules/part";

// 搜索表单
const searchForm = reactive({
  partId: undefined as number | undefined,
  type: undefined as number | undefined
});

// 表格数据
const loading = ref(false);
const tableData = ref<any[]>([]);

// 分页
const pageable = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

// 配件列表（用于下拉选择）
const partList = ref<any[]>([]);

// 类型映射
const typeMap: Record<number, string> = {
  1: "入库",
  2: "出库",
  3: "盘点调整"
};

const typeTagMap: Record<number, "success" | "danger" | "warning"> = {
  1: "success",
  2: "danger",
  3: "warning"
};

const getTypeText = (type?: number) => {
  return type !== undefined ? typeMap[type] || "未知" : "";
};

const getTypeTagType = (type?: number) => {
  return type !== undefined ? typeTagMap[type] || "info" : "info";
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pageable.pageNum,
      size: pageable.pageSize
    };
    if (searchForm.partId) {
      params.partId = searchForm.partId;
    }
    if (searchForm.type !== undefined) {
      params.type = searchForm.type;
    }
    const res = await getInventoryRecords(params);
    if (res.code === 200 || res.code === 0) {
      const data = res.data || {};
      const records = data.records || data.list || [];
      // 将配件ID转换为配件名称
      const partMap = new Map(partList.value.map(p => [p.id, p.name]));
      tableData.value = records.map((record: any) => ({
        ...record,
        partName: record.partName || partMap.get(record.partId) || `配件ID: ${record.partId}`
      }));
      pageable.total = data.total || 0;
    }
  } catch (error: any) {
    ElMessage.error(error.message || "加载数据失败");
  } finally {
    loading.value = false;
  }
};

// 加载配件列表
const loadParts = async () => {
  try {
    const res = await getAllParts();
    if (res.code === 200 || res.code === 0) {
      partList.value = res.data || [];
    }
  } catch (error) {
    console.error("加载配件列表失败", error);
  }
};

// 搜索
const handleSearch = () => {
  pageable.pageNum = 1;
  loadData();
};

// 重置
const handleReset = () => {
  searchForm.partId = undefined;
  searchForm.type = undefined;
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

// 初始化
onMounted(async () => {
  await loadParts();
  loadData();
});
</script>

<style scoped lang="scss">
.inventory-records-page {
  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
