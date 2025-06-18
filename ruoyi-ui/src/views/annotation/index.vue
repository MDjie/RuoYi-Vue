<template>
<div style="width:100%;height:500px;display: flex; flex-direction: column;justify-content: center;align-items: center; " >
  <el-form ref="form" :model="form" label-width="80px" style="width: 50%;background-color: #c1cdd9;border-radius: 20px" >

    <el-form-item label="数据集">
        <span> ：{{form.datasetName}}  序号： {{form.currentIndex}}/{{form.total}} 要求准确率：{{form.accuracy}}</span>
    </el-form-item>

    <el-form-item label="文本" >
      <el-input disabled="true" type="textarea" :autosize="{ minRows: 2, maxRows: 8}"  v-model="form.text"></el-input>
    </el-form-item>
    <el-form-item label="标签">
      <el-select v-model="form.label" placeholder="请选择标签">
        <el-option v-for="item in form.labelOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="onSubmit">提交</el-button>
      <el-button type="info" @click="checkAccuracy">查看当前准确率</el-button>
    </el-form-item>
  </el-form>
</div>
</template>
<style>

</style>


<script>
import { getText, sendLabel, checkAccuracy } from "@/api/annotation/annotation";

export default {
  data() {
    return {
      form: {
        datasetName: '',
        currentIndex: 0,
        accuracy: 0,
        total:0,
        text: '',
        label: '',
        labelOptions: []
      }
    }
  },
  created() {
    this.getData()
  },
  methods: {
    async getData() {
      try {
        const response = await getText()
        if (response.code === 200) {
          const data = response.data
          this.form.datasetName = data.datasetName
          this.form.currentIndex = data.currentIndex
          this.form.accuracy = data.accuracy
          this.form.total = data.total
          this.form.text = data.text
          this.form.labelOptions = data.labelOptions
        }
      } catch (error) {
        console.error('获取数据失败:', error)
      }
    },
    async onSubmit() {
      if (!this.form.label) {
        this.$message.warning('请选择标签')
        return
      }
      try {
        const response = await sendLabel({
          "label": this.form.label
        })
        if (response.code === 200) {
          // 显示评分结果
          if (response.msg && response.msg.includes("标注完成")) {
            this.$alert(response.msg, '标注完成', {
              confirmButtonText: '确定',
              callback: action => {
                // 重新获取数据
                this.getData()
                // 清空标签选择
                this.form.label = ''
              }
            })
          } else {
            this.$message.success('提交成功')
            // 重新获取数据
            await this.getData()
            // 清空标签选择
            this.form.label = ''
          }
        }
      } catch (error) {
        console.error('提交失败:', error)
        this.$message.error('提交失败')
      }
    },
    async checkAccuracy() {
      try {
        const response = await checkAccuracy()
        if (response.code === 200) {
          this.$alert(response.msg, '当前准确率', {
            confirmButtonText: '确定'
          })
        }
      } catch (error) {
        console.error('获取准确率失败:', error)
        this.$message.error('获取准确率失败')
      }
    }
  }
}
</script>

