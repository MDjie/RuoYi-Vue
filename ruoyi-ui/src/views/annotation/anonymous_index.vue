<template>
  <div class="annotation-container">
    <div class="annotation-form-wrapper">
      <el-form v-if="datasetsLoaded" ref="form" :model="form" label-width="100px" class="annotation-form">
        <el-form-item label="选择数据集" class="dataset-select-item">
          <el-select v-model="form.selectedDatasetKey" placeholder="请选择数据集" @change="onDatasetChange" class="dataset-select">
            <el-option v-for="(item, key) in datasetOptions" :key="key" :label="key" :value="key"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据集信息" class="dataset-info-item" v-if="currentDataset">
          <div class="dataset-info">
            <span class="info-label">数据集：</span>
            <span class="info-value">{{form.selectedDatasetKey}}</span>
            <span class="info-label">要求准确率：</span>
            <span class="info-value">{{currentDataset.accuracy}}%</span>
            <span class="info-label">样本数：</span>
            <span class="info-value">{{currentDataset.content.length}}</span>
          </div>
        </el-form-item>
        <el-form-item label="文本" class="text-item" v-if="currentSample">
          <el-input :disabled="true" type="textarea" :autosize="{ minRows: 3, maxRows: 8}" v-model="currentSample.text" class="text-input" placeholder="等待加载文本内容..."></el-input>
        </el-form-item>
        <el-form-item label="标签" class="label-item" v-if="currentSample">
          <el-select v-model="form.label" placeholder="请选择标签" class="label-select">
            <el-option v-for="(label, value) in currentDataset.label" :key="value" :label="label" :value="value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="button-group" v-if="currentSample">
          <el-button type="primary" @click="onSubmit" :disabled="!form.label" class="submit-btn">提交</el-button>
        </el-form-item>
        <el-form-item v-if="showResult">
          <el-alert :title="resultMsg" type="success" show-icon :closable="false" />
        </el-form-item>
      </el-form>
      <div v-else class="loading">正在加载数据集...</div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      datasetsLoaded: false,
      datasetOptions: {},
      form: {
        selectedDatasetKey: '',
        label: ''
      },
      currentIndex: 0,
      userLabels: [],
      showResult: false,
      resultMsg: '',
    }
  },
  computed: {
    currentDataset() {
      return this.datasetOptions[this.form.selectedDatasetKey] || null;
    },
    currentSample() {
      if (!this.currentDataset) return null;
      return this.currentDataset.content[this.currentIndex] || null;
    }
  },
  created() {
    this.loadDatasets();
  },
  methods: {
    async loadDatasets() {
      try {
        const res = await fetch('/static-datasets.json');
        const data = await res.json();
        this.datasetOptions = data;
        this.datasetsLoaded = true;
        // 默认选中第一个数据集
        const firstKey = Object.keys(data)[0];
        if (firstKey) {
          this.form.selectedDatasetKey = firstKey;
          this.onDatasetChange();
        }
      } catch (e) {
        this.$message.error('加载数据集失败');
      }
    },
    onDatasetChange() {
      this.currentIndex = 0;
      this.form.label = '';
      this.userLabels = [];
      this.showResult = false;
      this.resultMsg = '';
    },
    async onSubmit() {
      if (!this.form.label) {
        this.$message.warning('请选择标签');
        return;
      }
      this.userLabels.push(this.form.label);
      this.form.label = '';
      this.currentIndex++;
      if (this.currentIndex >= this.currentDataset.content.length) {
        // 标注完成，前端评估
        this.evaluateResult();
      }
    },
    evaluateResult() {
      const trueLabels = this.currentDataset.content.map(item => item.Human_Answer);
      const predLabels = this.userLabels;
      let correct = 0;
      let total = trueLabels.length;
      let labelSet = new Set([...trueLabels, ...predLabels]);
      let matched = 0;
      let precision = 0, recall = 0, f1 = 0;
      let truePos = {}, falsePos = {}, falseNeg = {};
      labelSet.forEach(l => { truePos[l]=0; falsePos[l]=0; falseNeg[l]=0; });
      for(let i=0;i<total;i++){
        if(predLabels[i]!==undefined){
          if(trueLabels[i]===predLabels[i]){
            correct++;
            truePos[trueLabels[i]]++;
            matched++;
          }else{
            falsePos[predLabels[i]]++;
            falseNeg[trueLabels[i]]++;
          }
        }
      }
      labelSet.forEach(l=>{
        let p = truePos[l]+falsePos[l]===0?0:truePos[l]/(truePos[l]+falsePos[l]);
        let r = truePos[l]+falseNeg[l]===0?0:truePos[l]/(truePos[l]+falseNeg[l]);
        let f = p+r===0?0:2*p*r/(p+r);
        precision+=p; recall+=r; f1+=f;
      });
      let n = labelSet.size;
      precision/=n; recall/=n; f1/=n;
      let accuracy = correct/total;
      let msg = `评估结果：\n总问题数: ${total}\n准确率: ${(accuracy*100).toFixed(2)}%\n精确率: ${(precision*100).toFixed(2)}%\n召回率: ${(recall*100).toFixed(2)}%\nF1分数: ${(f1*100).toFixed(2)}%\n`;
      if(this.currentDataset.accuracy){
        msg += `要求准确率: ${this.currentDataset.accuracy}%\n`;
        if(accuracy*100>=this.currentDataset.accuracy){
          msg += '恭喜，已达到准确率要求！';
        }else{
          msg += '未达到准确率要求。';
        }
      }
      this.resultMsg = msg;
      this.showResult = true;
    }
  }
}
</script>

<style scoped>
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
.loading {
  text-align: center;
  font-size: 18px;
  color: #666;
  padding: 40px 0;
}
</style>
