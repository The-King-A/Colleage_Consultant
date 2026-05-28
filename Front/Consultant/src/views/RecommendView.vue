<template>
  <div class="recommend-page">
    <!-- Page Header -->
    <section class="page-hero">
      <div class="hero-inner">
        <div class="hero-badge">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#a5b4fc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 8l10 6 10-6-10-6z"/>
            <path d="M2 16l10 6 10-6"/>
            <path d="M2 12l10 6 10-6"/>
          </svg>
          <span>志愿智能推荐</span>
        </div>
        <h1 class="hero-title">填写信息，获取个性化方案</h1>
        <p class="hero-desc">基于历年录取数据与 AI 深度分析，按冲、稳、保三级梯度为您推荐最佳院校与专业组合</p>
      </div>
    </section>

    <!-- Content -->
    <section class="page-content">
      <div class="content-grid">
        <!-- Form -->
        <div class="form-panel">
          <div class="panel-card">
            <div class="panel-header">
              <h3><el-icon><Edit /></el-icon> 考生信息</h3>
              <span class="step-badge">Step 1</span>
            </div>

            <el-form ref="formRef" :model="formData" :rules="rules" label-position="top" size="large" class="rec-form">
              <div class="form-row">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="formData.name" placeholder="考生姓名" />
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="formData.gender" class="gender-group">
                    <el-radio-button value="male">男</el-radio-button>
                    <el-radio-button value="female">女</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </div>

              <div class="form-row">
                <el-form-item label="所在省份" prop="province">
                  <el-select v-model="formData.province" placeholder="选择省份" filterable>
                    <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
                  </el-select>
                </el-form-item>
                <el-form-item label="高考分数" prop="score">
                  <el-input-number v-model="formData.score" :min="0" :max="750" controls-position="right" placeholder="0-750" style="width:100%" />
                </el-form-item>
              </div>

              <el-form-item label="科目类型" prop="subject">
                <el-radio-group v-model="formData.subject" class="gender-group">
                  <el-radio-button value="science">理科</el-radio-button>
                  <el-radio-button value="liberal">文科</el-radio-button>
                  <el-radio-button value="comprehensive">综合</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="意向专业方向" prop="interestedMajors">
                <el-select v-model="formData.interestedMajors" multiple placeholder="可多选" filterable collapse-tags collapse-tags-tooltip>
                  <el-option v-for="m in majorCategories" :key="m" :label="m" :value="m" />
                </el-select>
              </el-form-item>

              <el-form-item label="意向地区" prop="preferredRegions">
                <el-select v-model="formData.preferredRegions" multiple placeholder="可多选" filterable collapse-tags collapse-tags-tooltip>
                  <el-option v-for="r in regions" :key="r" :label="r" :value="r" />
                </el-select>
              </el-form-item>

              <el-form-item label="其他要求">
                <el-input v-model="formData.otherRequirements" type="textarea" :rows="3" placeholder="学校类型、专业特点等特殊要求" />
              </el-form-item>

              <el-button type="primary" size="large" class="submit-btn" @click="handleSubmit" :loading="isLoading" round>
                <el-icon><Search /></el-icon>
                获取推荐方案
              </el-button>
            </el-form>
          </div>
        </div>

        <!-- Result -->
        <div class="result-panel">
          <div class="panel-card result-card">
            <div class="panel-header">
              <h3><el-icon><DataAnalysis /></el-icon> 推荐结果</h3>
              <span class="step-badge">Step 2</span>
            </div>

            <div v-if="!recommendResult" class="empty-state">
              <div class="empty-icon">
                <el-icon :size="48"><Document /></el-icon>
              </div>
              <p class="empty-title">暂无推荐结果</p>
              <p class="empty-desc">填写左侧信息后点击「获取推荐方案」<br/>AI 将为您生成个性化志愿方案</p>
            </div>

            <div v-else class="result-body">
              <div class="result-content" v-html="formatResult(recommendResult)"></div>
            </div>
          </div>

          <div class="panel-card tips-card">
            <div class="panel-header">
              <h3><el-icon><InfoFilled /></el-icon> 填报建议</h3>
            </div>
            <ul class="tips-list">
              <li>推荐结果基于历年录取数据和 AI 智能分析</li>
              <li>建议结合「冲、稳、保」策略合理填报志愿</li>
              <li>专业选择应综合考虑个人兴趣与未来发展</li>
              <li>如需更深入指导，请使用智能问答功能</li>
            </ul>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Edit, Search, DataAnalysis, Document, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { chatStream } from '../api/chat'
import { useChatStore } from '../stores/chat'

const route = useRoute()
const chatStore = useChatStore()
const formRef = ref(null)
const isLoading = ref(false)
const recommendResult = ref('')

const formData = reactive({
  name: '',
  gender: 'male',
  province: '',
  score: null,
  subject: 'science',
  interestedMajors: [],
  preferredRegions: [],
  otherRequirements: ''
})

const rules = {
  name: [{ required: true, message: '请输入考生姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  province: [{ required: true, message: '请选择所在省份', trigger: 'change' }],
  score: [
    { required: true, message: '请输入高考分数', trigger: 'blur' },
    { type: 'number', min: 0, max: 750, message: '分数范围0-750', trigger: 'blur' }
  ],
  subject: [{ required: true, message: '请选择科目类型', trigger: 'change' }]
}

const provinces = [
  '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
  '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南',
  '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州',
  '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆'
]

const majorCategories = [
  '计算机类', '电子信息类', '自动化类', '机械类', '土木类', '建筑类',
  '经济学类', '金融学类', '管理学类', '法学类', '医学类', '生物科学类',
  '化学类', '物理学类', '数学类', '文学类', '外语类', '新闻传播类',
  '艺术类', '教育学类'
]

const regions = ['华北地区', '东北地区', '华东地区', '华中地区', '华南地区', '西南地区', '西北地区']

const formatResult = (content) => content.replace(/\n/g, '<br/>')

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    isLoading.value = true
    recommendResult.value = ''

    const message = `
我是一名来自${formData.province}的考生，性别${formData.gender === 'male' ? '男' : '女'}，
高考成绩为${formData.score}分，科目类型为${formData.subject === 'science' ? '理科' : formData.subject === 'liberal' ? '文科' : '综合'}。
${formData.interestedMajors.length > 0 ? `我对以下专业方向比较感兴趣：${formData.interestedMajors.join('、')}。` : ''}
${formData.preferredRegions.length > 0 ? `我希望在以下地区就读：${formData.preferredRegions.join('、')}。` : ''}
${formData.otherRequirements ? `其他要求：${formData.otherRequirements}` : ''}

请根据我的情况，按照"冲、稳、保"的策略，推荐合适的院校和专业，并包含历年录取分数和专业热度信息。`.trim()

    await chatStream(chatStore.generateMemoryId(), message, (chunk) => {
      recommendResult.value += chunk
    })

    ElMessage.success('推荐方案已生成')
  } catch (error) {
    if (error !== false) ElMessage.error('获取推荐失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  const fromTest = route.query.majors
  if (fromTest && typeof fromTest === 'string') {
    formData.interestedMajors = fromTest.split(',').map((s) => s.trim()).filter(Boolean)
  }
})
</script>

<style scoped>
.recommend-page {
  min-height: 100vh;
  background: var(--bg-body);
}

/* ---- Hero ---- */
.page-hero {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 64px 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.page-hero::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  background: #6366f1;
  border-radius: 50%;
  filter: blur(150px);
  opacity: 0.1;
  top: -200px;
  right: -100px;
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 640px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 18px;
  border-radius: 20px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.1);
  color: #a5b4fc;
  font-size: 0.8125rem;
  font-weight: 500;
  margin-bottom: 20px;
}

.hero-title {
  font-size: clamp(1.75rem, 3.5vw, 2.25rem);
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.hero-desc {
  color: #94a3b8;
  font-size: 1rem;
  line-height: 1.7;
}

/* ---- Content ---- */
.page-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
  align-items: start;
}

/* ---- Panels ---- */
.panel-card {
  background: var(--bg-surface);
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.panel-card:hover {
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

.result-card {
  margin-bottom: 0;
}

.tips-card {
  margin-top: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 28px;
  border-bottom: 1px solid #f1f5f9;
  background: #fafbfc;
}

.panel-header h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.0625rem;
  font-weight: 700;
  color: #0f172a;
}

.step-badge {
  font-size: 0.75rem;
  font-weight: 600;
  color: #6366f1;
  background: #eef2ff;
  padding: 4px 12px;
  border-radius: 10px;
}

/* ---- Form ---- */
.rec-form {
  padding: 24px 28px 28px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.gender-group {
  width: 100%;
}

.submit-btn {
  width: 100%;
  padding: 14px 0;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.25);
  margin-top: 8px;
}

.submit-btn:hover {
  box-shadow: 0 4px 18px rgba(99, 102, 241, 0.35);
}

/* ---- Empty ---- */
.empty-state {
  padding: 72px 28px;
  text-align: center;
}

.empty-icon {
  color: #cbd5e1;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 0.9rem;
  color: #94a3b8;
  line-height: 1.7;
}

/* ---- Result ---- */
.result-body {
  padding: 24px 28px;
}

.result-content {
  font-size: 0.925rem;
  line-height: 1.8;
  color: #334155;
}

/* ---- Tips ---- */
.tips-list {
  list-style: none;
  padding: 20px 28px;
  margin: 0;
}

.tips-list li {
  padding: 8px 0 8px 20px;
  position: relative;
  color: #64748b;
  font-size: 0.875rem;
  line-height: 1.6;
}

.tips-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 14px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6366f1;
}

/* ---- Input overrides ---- */
:deep(.el-input__wrapper) {
  border-radius: 10px;
  border-color: #e2e8f0;
  box-shadow: none;
}

:deep(.el-input__wrapper:hover) { border-color: #cbd5e1; }
:deep(.el-input__wrapper.is-focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.08);
}

:deep(.el-select .el-input__wrapper) { border-radius: 10px; }
:deep(.el-textarea__inner) { border-radius: 10px; border-color: #e2e8f0; }
:deep(.el-textarea__inner:focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.08);
}

:deep(.el-radio-button__inner) { border-radius: 8px; }

/* ---- Responsive ---- */
@media (max-width: 992px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
