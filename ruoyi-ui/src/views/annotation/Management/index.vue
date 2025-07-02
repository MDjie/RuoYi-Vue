<!--

标注管理页面，用于管理用户的标注情况，包括为用户分配标注任务，显示用户标注的所有信息。具体页面结构为：一个表格，用于显示用户的标注信息，表格上有检索模块，用于检索用户标注信息；表的下方有分页模块。

-->


<template>
  <div class="annotation-management-container">
    <!-- 检索模块 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable></el-input>
      </el-form-item>
      <el-form-item label="数据集名称">
        <el-input v-model="searchForm.datasetName" placeholder="请输入数据集名称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe style="width: 100%; margin-top: 20px;" height="600px">
      <el-table-column prop="userName" label="用户名" min-width="120"/>
      <el-table-column prop="datasetName" label="数据集名称" min-width="120"/>
      <el-table-column prop="datasetSubSet" label="子集编号" min-width="80"/>
      <el-table-column prop="currentIndex" label="当前标注索引" min-width="100"/>
      <el-table-column prop="relabelRound" label="标注轮次" min-width="80"/>
      <el-table-column prop="createTime" label="创建时间" min-width="160">
        <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="160">
        <template #default="scope">{{ formatDate(scope.row.updateTime) }}</template>
      </el-table-column>
      <el-table-column prop="round1Result" label="第一轮评分" min-width="200"/>
      <el-table-column prop="round2Result" label="第二轮评分" min-width="200"/>
      <el-table-column prop="round3Result" label="第三轮评分" min-width="200"/>
      <el-table-column label="操作" fixed="right" min-width="300">
        <template #default="scope">
          <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
          <el-button type="primary" size="mini" @click="handleEdit(scope.row)">修改</el-button>
          <el-button type="warning" size="mini" @click="handleRedo(scope.row)">重做最后一轮</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页模块 -->
    <div class="pagination-wrapper">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :page-sizes="[10, 20, 50, 100]"
        style="margin-top: 20px;"
      />
    </div>

    <el-dialog title="修改标注信息" :visible.sync="editDialogVisible" width="400px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户名">
          <el-select v-model="editForm.userId" placeholder="请选择用户">
            <el-option v-for="user in userOptions" :key="user.userId" :label="user.userName" :value="user.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前标注索引">
          <el-input-number v-model="editForm.currentIndex" :min="0" :max="999999"/>
        </el-form-item>
        <el-form-item label="标注轮次">
          <el-input-number v-model="editForm.relabelRound" :min="1" :max="10"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAnnotationManagementList, deleteAnnotation, updateAnnotation, getNormalUserList, relabelAnnotation } from '@/api/annotation/annotation'

export default {
  name: 'AnnotationManagement',
  data() {
    return {
      searchForm: {
        userName: '',
        datasetName: ''
      },
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      editDialogVisible: false,
      editForm: {
        userId: '',
        datasetName: '',
        datasetSubSet: '',
        currentIndex: 0,
        relabelRound: 1
      },
      userOptions: []
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        userName: this.searchForm.userName,
        datasetName: this.searchForm.datasetName
      }
      try {
        const res = await getAnnotationManagementList(params)
        this.tableData = res.rows || []
        this.total = res.total || 0
      } catch (e) {
        this.$message.error('获取标注管理数据失败')
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.fetchData()
    },
    handleReset() {
      this.searchForm.userName = ''
      this.searchForm.datasetName = ''
      this.pageNum = 1
      this.fetchData()
    },
    handlePageChange(val) {
      this.pageNum = val
      this.fetchData()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.fetchData()
    },
    formatDate(val) {
      if (!val) return ''
      const d = new Date(val)
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0') + ' ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0') + ':' + String(d.getSeconds()).padStart(2, '0')
    },
    async handleDelete(row) {
      this.$confirm('确定要删除该条标注信息吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.delete(row)
          this.$message.success('删除成功')
          this.fetchData()
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    async delete(row) {
      // 调用API
      await deleteAnnotation({
        userId: row.userId,
        datasetName: row.datasetName,
        datasetSubSet: row.datasetSubSet
      })
    },
    async handleEdit(row) {
      this.editForm = {
        userId: row.userId,
        datasetName: row.datasetName,
        datasetSubSet: row.datasetSubSet,
        currentIndex: row.currentIndex,
        relabelRound: row.relabelRound
      }
      // 获取正常用户列表
      try {
        const res = await getNormalUserList()
        this.userOptions = res
      } catch (e) {
        this.userOptions = []
      }
      this.editDialogVisible = true
    },
    async submitEdit() {
      try {
        await updateAnnotation(this.editForm)
        this.$message.success('修改成功')
        this.editDialogVisible = false
        this.fetchData()
      } catch (e) {
        this.$message.error('修改失败')
      }
    },
    handleRedo(row) {
      if (row.relabelRound < 2) {
        this.$message.warning('当前轮次小于2，不能重做最后一轮！')
        return
      }
      this.$confirm('确定要重做最后一轮吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await relabelAnnotation({
            datasetName: row.datasetName,
            datasetSubSet: row.datasetSubSet
          })
          this.$message.success('重做成功')
          this.fetchData()
        } catch (e) {
          this.$message.error('重做失败')
        }
      }).catch(() => {})
    },
    relabelAnnotation(data) {
      return this.$apiRelabelAnnotation ? this.$apiRelabelAnnotation(data) : this.$options.api.relabelAnnotation(data)
    },
    getNormalUserList() {
      return this.$apiGetNormalUserList ? this.$apiGetNormalUserList() : this.$options.api.getNormalUserList()
    }
  }
}
</script>

<style scoped>
.annotation-management-container {
  padding: 30px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.search-form {
  margin-bottom: 10px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>