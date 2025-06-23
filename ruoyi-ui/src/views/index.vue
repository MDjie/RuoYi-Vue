<template>
  <div class="annotation-info-container">
    <h2 class="main-title">《HSC：Hierarchy-Aware Spurious Correlation Benchmarking for Text Classification Systems》</h2>
    <p class="subtitle">我们对下列 <b>8 个数据集</b>分别采样划分了多个人工测试子集：</p>
    <p class="desc">
      <span class="dataset-list">Ag_News、Amazon、Civil_Comment、ED、IMDB、Rotten_Tomato、Tweet、Yahoo。</span>
      其中每个子数据集使用 <b>350</b> 条数据构成人工测试数据集，内部标签划分比较平均模拟全环境测试环境。数据集名称后标注为了合理保证人工标注的质量要求需要达到的最低准确率，若不合格默认认该数据集的人工标注质量低、不认真，最后结果不予采纳，最后的答案视为无效。
    </p>
    <div class="table-image-row" style="display: flex;justify-content: center">
      <el-table :data="tableData" border style="width: 100%; max-width: 420px; flex: 1;">
        <el-table-column prop="name" label="数据集" align="center"  />
        <el-table-column prop="total" label="总数" align="center"  />
        <el-table-column prop="classNum" label="分类数" align="center" />
        <!-- <el-table-column prop="acc" label="最低 ACC 要求" align="center" width="120" /> -->
      </el-table>
          </div>
    <div class="reward-section">
      <b>奖励可选：</b>
      <ol>
        <li>完成任意数据集测试获得35元报酬（单个数据集耗时：30~45min）</li>
        <li>完成任意8个数据集测试可开人工智能实验室实习证明/项目参加证明</li>
        <li>完成任意10个数据集测试可带（需提前注册 OpenReview）</li>
              </ol>
          </div>
  </div>
</template>

<script>
export default {
  name: "AnnotationInfoIndex",
  data() {
    return {
      tableData: [
        { name: 'Ag_News', total:"350*4" , classNum: 4, acc: '90%' },
        { name: 'Amazon', total: "350*5", classNum: 5, acc: '%' },
        { name: 'Civil_Comment', total: "350*3", classNum: 2, acc: '85%' },
        { name: 'ED', total: "350*4", classNum: 32, acc: '%' },
        { name: 'IMDB', total: "350*5", classNum: 2, acc: '95%' },
        { name: 'Rotten', total: "350*4", classNum: 2, acc: '%' },
        { name: 'Tweet', total: "350*5", classNum: 20, acc: '%' },
        { name: 'Yahoo', total: "350*5", classNum: 10, acc: '72%' },
      ]
    }
  }
}
</script>

<style scoped>
.annotation-info-container {
  max-width: 900px;
  margin: 40px auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 32px 32px 24px 32px;
}
.main-title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 18px;
}
.subtitle {
  color: #1e88e5;
  font-size: 18px;
  text-align: center;
  font-weight: bold;
  margin-bottom: 10px;
}
.desc {
  font-size: 15px;
  color: #222;
  margin-bottom: 18px;
  line-height: 1.7;
}
.dataset-list {
  color: #00bcd4;
  font-weight: bold;
}
.table-image-row {
  display: flex;
  align-items: center;
  gap: 32px;
  margin: 20px 0 10px 0;
  flex-wrap: wrap;
}
.side-image {
  max-width: 260px;
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.10);
  object-fit: cover;
  margin-top: 0;
}
@media (max-width: 900px) {
  .table-image-row {
    flex-direction: column;
    align-items: center;
    gap: 18px;
  }
  .side-image {
    margin: 0 auto;
      display: block;
  }
}
.reward-section {
  margin-top: 30px;
  font-size: 15px;
  color: #222;
}
.reward-section b {
  color: #222;
  font-size: 16px;
}
.reward-section ol {
  margin: 10px 0 0 20px;
  padding-left: 18px;
}
.reward-section li {
  margin-bottom: 6px;
  line-height: 1.7;
}
</style>

