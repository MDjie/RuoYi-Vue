<template>
<div class="annotation-container">
  <div class="annotation-form-wrapper">
    <el-form ref="form" :model="form" label-width="100px" class="annotation-form">

      <el-form-item label="选择数据集" class="dataset-select-item">
        <el-select v-model="form.selectedDatasetKey" placeholder="请选择要标注的数据集" @change="onDatasetChange" class="dataset-select">
          <el-option
            v-for="item in form.datasetOptions"
            :key="item.datasetName + '-' + item.datasetSubSet"
            :label="item.displayName"
            :value="item.datasetName + '-' + item.datasetSubSet">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="数据集信息" class="dataset-info-item">
        <div class="dataset-info">
          <span class="info-label">数据集：</span>
          <span class="info-value">{{form.datasetName}}</span>
          <span class="info-label">标注轮次：</span>
          <span class="info-value">{{form.relabel_round}}</span>
          <span class="info-label">序号：</span>
          <span class="info-value">{{form.currentIndex}}/{{form.total}}</span>
          <span class="info-label">要求准确率：</span>
          <span class="info-value">{{form.accuracy}}%</span>
        </div>
      </el-form-item>

      <el-form-item label="文本" class="text-item">
        <el-input
          :disabled="true"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 8}"
          v-model="form.text"
          class="text-input"
          placeholder="等待加载文本内容...">
        </el-input>
      </el-form-item>

      <el-form-item label="标签" class="label-item">
        <!-- 当选项少于5个时使用单选按钮框 -->
        <div v-if="form.labelOptions.length <= 5" class="radio-group">
          <el-radio-group v-model="form.label" class="label-radio-group">
            <el-radio
              v-for="item in form.labelOptions"
              :key="item.value"
              :label="item.value"
              class="label-radio">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </div>
        <!-- 当选项多于5个时使用下拉框 -->
        <el-select filterable v-else v-model="form.label" placeholder="请选择或搜索标签" class="label-select">
          <template v-for="item in groupedLabelOptions">
            <el-option-group v-if="item.options" :key="item.label" :label="item.label">
              <el-option v-for="opt in item.options" :key="opt.value" :label="opt.label" :value="opt.value"></el-option>
            </el-option-group>
            <el-option v-else :key="item.value" :label="item.label" :value="item.value"></el-option>
          </template>
        </el-select>
      </el-form-item>

      <el-form-item class="button-group">
        <el-button type="primary" @click="onSubmit" :disabled="!currentSelectedDataset" class="submit-btn">提交</el-button>
        <el-button type="info" @click="checkAccuracy" :disabled="!currentSelectedDataset" class="accuracy-btn">查看当前准确率</el-button>
      </el-form-item>
    </el-form>
  </div>
</div>
</template>

<script>
import { getUserDatasets, getText, sendLabel, checkAccuracy } from "@/api/annotation/annotation";

export default {
  data() {
    return {
      form: {
        selectedDatasetKey: '',
        datasetOptions: [],
        datasetName: '',
        currentIndex: 0,
        accuracy: 0,
        total: 0,
        text: '',
        label: '',
        labelOptions: [],
        relabel_round:1
      }
    }
  },
  computed: {
    currentSelectedDataset() {
      if (!this.form.selectedDatasetKey || this.form.datasetOptions.length === 0) {
        return null
      }
      return this.form.datasetOptions.find(item =>
        (item.datasetName + '-' + item.datasetSubSet) === this.form.selectedDatasetKey
      )
    },
    groupedLabelOptions() {
      const options = this.form.labelOptions || [];
      const groups = [];
      let currentGroup = null;
      for (const item of options) {
        if (/^-\d+$/.test(item.value)) {
          currentGroup = {
            label: item.label,
            options: []
          };
          groups.push(currentGroup);
        } else if (currentGroup) {
          currentGroup.options.push(item);
        } else {
          groups.push(item);
        }
      }
      return groups;
    }
  },
  created() {
    this.getUserDatasets()
  },
  methods: {
    async getUserDatasets() {
      try {
        const response = await getUserDatasets()
        if (response.code === 200) {
          this.form.datasetOptions = response.data
          if (this.form.datasetOptions.length > 0) {
            // 设置第一个数据集为默认选中
            this.form.selectedDatasetKey = this.form.datasetOptions[0].datasetName + '-' + this.form.datasetOptions[0].datasetSubSet
            this.onDatasetChange()
          }
        }
      } catch (error) {
        console.error('获取用户数据集失败:', error)
        this.$message.error('获取用户数据集失败')
      }
    },
    async onDatasetChange() {
      if (this.currentSelectedDataset) {
        await this.getData()
      }
    },
    async getData() {
      if (!this.currentSelectedDataset) {
        return
      }
      try {
        const response = await getText(this.currentSelectedDataset.datasetName, this.currentSelectedDataset.datasetSubSet)
        if (response.code === 200) {
          const data = response.data
          this.form.datasetName = data.datasetName
          this.form.currentIndex = data.currentIndex
          this.form.accuracy = data.accuracy
          this.form.total = data.total
          this.form.text = data.text
          this.form.labelOptions = data.labelOptions
          this.form.relabel_round=data.relabel_round
          if(response.msg && response.msg.includes("标注已完成")){
            this.form.currentIndex;
            this.$message.success('标注已完成')
            return
          }
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败')
      }
    },
    async onSubmit() {
      if (!this.currentSelectedDataset) {
        this.$message.warning('请先选择数据集')
        return
      }
      if (!this.form.label) {
        this.$message.warning('请选择标签')
        return
      }
      try {
        const response = await sendLabel({
          "datasetName": this.currentSelectedDataset.datasetName,
          "datasetSubSet": this.currentSelectedDataset.datasetSubSet,
          "label": this.form.label
        })
        if (response.code === 200) {
          // 显示评分结果
          if (response.msg && response.msg.includes("标注已完成")) {
            const isFailed = response.msg.includes("未达到准确率要求");
            this.$message({
              message: response.msg,
              duration: 5000
            });
            if(isFailed){
            await this.getData();
              // 清空标签选择
              this.form.label = '';
            }
          } else {
            this.$message.success('提交成功');
            // 重新获取数据
            await this.getData();
            // 清空标签选择
            this.form.label = '';
          }
        }
      } catch (error) {
        console.error('提交失败:', error)
        this.$message.error('提交失败')
      }
    },
    async checkAccuracy() {
      if (!this.currentSelectedDataset) {
        this.$message.warning('请先选择数据集')
        return
      }
      try {
        const response = await checkAccuracy(this.currentSelectedDataset.datasetName, this.currentSelectedDataset.datasetSubSet)
        if (response.code === 200) {
          this.$message({
            message: response.msg,
            type: 'success',
            duration: 5000
          });
        }
      } catch (error) {
        console.error('获取准确率失败:', error)
        this.$message.error('获取准确率失败')
      }
    }
  }
}
</script>
<style scoped>
/* 响应式容器 */
.annotation-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.annotation-form-wrapper {
  width: 100%;
  max-width: 800px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  padding: 30px;
}

.annotation-form {
  width: 100%;
}

/* 数据集选择器 */
.dataset-select-item {
  margin-bottom: 20px;
}

.dataset-select {
  width: 100%;
}

/* 数据集信息显示 */
.dataset-info-item {
  margin-bottom: 20px;
}

.dataset-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid #409eff;
}

.info-label {
  color: #606266;
  font-weight: 600;
  font-size: 14px;
}

.info-value {
  color: #1a1a1a;
  font-weight: 600;
  font-size: 14px;
  background: #e6f7ff;
  padding: 4px 8px;
  border-radius: 6px;
  margin-right: 10px;
}

/* 文本输入区域 */
.text-item {
  margin-bottom: 20px;
}

.text-input {
  width: 100%;
}

.text-input >>> .el-textarea__inner {
  color: #1a1a1a !important;
  font-weight: 500 !important;
  font-size: 14px !important;
  line-height: 1.6 !important;
  border: 2px solid #e4e7ed !important;
  border-radius: 10px !important;
  transition: all 0.3s ease !important;
}

.text-input >>> .el-textarea__inner:focus {
  border-color: #409eff !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
}

/* 标签选择器 */
.label-item {
  margin-bottom: 25px;
}

.label-select {
  width: 100%;
}

/* 单选按钮组样式 */
.radio-group {
  width: 100%;
}

.label-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  width: 100%;
}

.label-radio {
  margin-right: 0 !important;
  margin-bottom: 10px;
  padding: 12px 20px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  background: #f8f9fa;
  transition: all 0.3s ease;
  min-width: 120px;
  text-align: center;
  cursor: pointer;
}

.label-radio:hover {
  border-color: #409eff;
  background: #e6f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.label-radio.is-checked {
  border-color: #409eff;
  background: linear-gradient(135deg, #409eff 0%, #36a3f7 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.label-radio >>> .el-radio__input {
  display: none;
}

.label-radio >>> .el-radio__label {
  color: inherit;
  font-weight: 600;
  font-size: 14px;
  padding-left: 0;
}

.label-radio.is-checked >>> .el-radio__label {
  color: white;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
  align-items: center;
}

.submit-btn, .accuracy-btn {
  min-width: 120px;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.3s ease;
  flex: 1;
  max-width: 200px;
}

.submit-btn {
  background: linear-gradient(135deg, #409eff 0%, #36a3f7 100%);
  border: none;
  color: white !important;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
}

.accuracy-btn {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  border: none;
  color: white !important;
}

.accuracy-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(144, 147, 153, 0.3);
}

/* 表单标签样式 */
.el-form-item >>> .el-form-item__label {
  color: #1a1a1a !important;
  font-weight: 600 !important;
  font-size: 15px !important;
  line-height: 1.4 !important;
}

/* 选择框样式 */
.el-select >>> .el-input__inner {
  color: #1a1a1a !important;
  font-weight: 500 !important;
  border: 2px solid #e4e7ed !important;
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

.el-select >>> .el-input__inner:focus {
  border-color: #409eff !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
}

/* 下拉选项样式 */
.el-select >>> .el-select-dropdown__item {
  color: #1a1a1a !important;
  font-weight: 500 !important;
  padding: 12px 20px !important;
}

.el-select >>> .el-select-dropdown__item:hover {
  background-color: #f5f7fa !important;
}

/* 占位符样式 */
.el-input >>> .el-input__inner::placeholder,
.el-textarea >>> .el-textarea__inner::placeholder {
  color: #c0c4cc !important;
  font-style: italic !important;
}

/* 响应式设计 - 平板设备 */
@media (max-width: 768px) {
  .annotation-container {
    padding: 15px;
  }

  .annotation-form-wrapper {
    padding: 25px;
    border-radius: 15px;
  }

  .dataset-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-value {
    margin-right: 0;
  }

  .button-group {
    flex-direction: row;
    gap: 12px;
    justify-content: center;
  }

  .submit-btn, .accuracy-btn {
    flex: 0 1 auto;
    min-width: 120px;
    max-width: 200px;
  }

  .el-form-item >>> .el-form-item__label {
    font-size: 14px !important;
  }

  /* 平板设备单选按钮适配 */
  .label-radio-group {
    gap: 12px;
  }

  .label-radio {
    min-width: 100px;
    padding: 10px 15px;
    font-size: 13px;
  }
}

/* 响应式设计 - 手机设备 */
@media (max-width: 480px) {
  .annotation-container {
    padding: 10px;
  }

  .annotation-form-wrapper {
    padding: 20px;
    border-radius: 12px;
  }

  .annotation-form {
    padding: 0;
  }

  .dataset-info {
    padding: 12px;
    font-size: 13px;
  }

  .info-label, .info-value {
    font-size: 13px;
  }

  .text-input >>> .el-textarea__inner {
    font-size: 13px !important;
  }

  .button-group {
    flex-direction: column !important;
    align-items: center !important;
    gap: 12px !important;
    justify-content: center !important;
  }

  .submit-btn, .accuracy-btn {
    width: 100% !important;
    min-width: 0 !important;
    max-width: none !important;
    margin: 0 !important;
  }

  .el-form-item >>> .el-form-item__label {
    font-size: 13px !important;
    line-height: 1.3 !important;
  }

  .el-select >>> .el-input__inner {
    font-size: 13px !important;
  }

  .el-select >>> .el-select-dropdown__item {
    font-size: 13px !important;
    padding: 10px 15px !important;
  }

  /* 手机设备单选按钮适配 */
  .label-radio-group {
    flex-direction: column;
    gap: 8px;
  }

  .label-radio {
    width: 100% !important;
    min-width: 0 !important;
    padding: 12px 15px;
    font-size: 14px;
    text-align: center;
  }
}

/* 超小屏幕设备 */
@media (max-width: 320px) {
  .annotation-form-wrapper {
    padding: 15px;
  }

  .dataset-info {
    padding: 10px;
  }

  .button-group {
    gap: 8px;
    justify-content: center;
  }

  .submit-btn, .accuracy-btn {
    height: 38px;
    font-size: 13px;
    min-width: 90px;
    max-width: 120px;
  }

  /* 超小屏幕单选按钮适配 */
  .label-radio {
    padding: 10px 12px;
    font-size: 13px;
  }
}

/* 全局样式覆盖 */
:deep(.el-form-item__label) {
  color: #1a1a1a !important;
  font-weight: 600 !important;
}

:deep(.el-input__inner) {
  color: #1a1a1a !important;
  font-weight: 500 !important;
}

:deep(.el-textarea__inner) {
  color: #1a1a1a !important;
  font-weight: 500 !important;
}

:deep(.el-select-dropdown__item) {
  color: #1a1a1a !important;
  font-weight: 500 !important;
}

/* 加载状态样式 */
.el-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 动画效果 */
.annotation-form-wrapper {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 弹框适配样式 - 全局覆盖 */
:deep(.el-message-box) {
  width: 95vw !important;
  max-width: 95vw !important;
  margin: 2vh auto !important;
  border-radius: 10px !important;
}

:deep(.el-message-box__content) {
  max-height: 60vh !important;
  overflow-y: auto !important;
  word-wrap: break-word !important;
  white-space: pre-wrap !important;
  padding: 20px !important;
}

:deep(.el-message-box__message) {
  font-size: 14px !important;
  line-height: 1.6 !important;
  text-align: left !important;
  word-break: break-word !important;
}

:deep(.el-message-box__header) {
  padding: 20px 20px 0 !important;
}

:deep(.el-message-box__footer) {
  padding: 0 20px 20px !important;
}

:deep(.el-message-box .el-button) {
  min-width: 80px !important;
  height: 36px !important;
  font-size: 14px !important;
}

/* 移动端弹框自定义样式 */
:deep(.mobile-alert) {
  max-width: 95vw !important;
  width: 95vw !important;
  margin: 2vh auto !important;
  border-radius: 10px !important;
}

:deep(.mobile-alert .el-message-box__content) {
  max-height: 75vh !important;
  padding: 15px !important;
}

:deep(.mobile-alert .el-message-box__message) {
  font-size: 13px !important;
  line-height: 1.6 !important;
  word-break: break-word !important;
}

:deep(.mobile-alert .el-message-box__header) {
  padding: 15px 15px 0 !important;
}

:deep(.mobile-alert .el-message-box__footer) {
  padding: 0 15px 15px !important;
}

:deep(.mobile-alert .el-button) {
  width: 100% !important;
  margin: 0 !important;
  height: 40px !important;
  font-size: 14px !important;
}

@media (max-width: 480px) {
  :deep(.el-message-box) {
    width: 90vw !important;
    max-width: 90vw !important;
    min-width: unset !important;
    border-radius: 8px !important;
    margin: 0 auto !important;
    left: 0 !important;
    right: 0 !important;
  }

  :deep(.el-message-box__content) {
    padding: 10px 8px !important;
    font-size: 13px !important;
    max-height: 60vh !important;
    overflow-y: auto !important;
    word-break: break-all !important;
  }

  :deep(.el-message-box__message) {
    font-size: 13px !important;
    line-height: 1.5 !important;
    text-align: left !important;
    word-break: break-all !important;
  }

  :deep(.el-message-box .el-button) {
    width: 100% !important;
    margin: 8px 0 0 0 !important;
    height: 38px !important;
    font-size: 14px !important;
    display: block !important;
  }

  :deep(.el-message-box__btns) {
    display: flex !important;
    flex-direction: column !important;
    gap: 8px !important;
  }
}

@media (max-width: 320px) {
  :deep(.el-message-box) {
    width: 98vw !important;
    margin: 1vh auto !important;
  }

  :deep(.el-message-box__content) {
    padding: 12px !important;
  }

  :deep(.el-message-box__message) {
    font-size: 12px !important;
  }

  :deep(.el-message-box__header) {
    padding: 12px 12px 0 !important;
  }

  :deep(.el-message-box__footer) {
    padding: 0 12px 12px !important;
  }

  :deep(.el-message-box .el-button) {
    height: 38px !important;
    font-size: 13px !important;
  }
}
</style>



