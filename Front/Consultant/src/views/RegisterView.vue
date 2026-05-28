<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="auth-orb orb-1"></div>
      <div class="auth-orb orb-2"></div>
    </div>

    <div class="auth-card-wrapper">
      <div class="auth-card">
        <!-- Left: Brand -->
        <div class="auth-brand">
          <div class="brand-content">
            <div class="brand-logo">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2L2 8l10 6 10-6-10-6z"/>
                <path d="M2 16l10 6 10-6"/>
                <path d="M2 12l10 6 10-6"/>
              </svg>
            </div>
            <h2>加入智选未来</h2>
            <p>开启你的智能志愿填报之旅</p>
            <div class="brand-features">
              <span>免费注册</span>
              <span>即开即用</span>
              <span>数据安全</span>
              <span>AI 驱动</span>
            </div>
          </div>
        </div>

        <!-- Right: Form -->
        <div class="auth-form">
          <div class="form-header">
            <h3>创建账户</h3>
            <p>填写信息完成注册</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @keyup.enter="handleRegister">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="3-50个字符" :prefix-icon="User" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="至少6位密码" show-password :prefix-icon="Lock" />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password :prefix-icon="Lock" />
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="选填，默认为用户名" />
            </el-form-item>

            <el-form-item label="所在省份">
              <el-select v-model="form.province" placeholder="选择省份（选填）" filterable clearable style="width:100%">
                <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" class="auth-btn" :loading="loading" @click="handleRegister" round>
                注册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>已有账户？</span>
            <router-link to="/login">立即登录</router-link>
          </div>

          <button class="back-home" @click="$router.push('/')">
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  province: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const provinces = [
  '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
  '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南',
  '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州',
  '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆',
]

async function handleRegister() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    await authStore.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username,
      province: form.province,
    })
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0b0f1a;
  position: relative;
  overflow: hidden;
}

.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auth-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
}

.orb-1 {
  width: 500px; height: 500px;
  background: rgba(6, 182, 212, 0.15);
  top: -200px; right: -100px;
}

.orb-2 {
  width: 400px; height: 400px;
  background: rgba(139, 92, 246, 0.12);
  bottom: -150px; left: -80px;
}

.auth-card-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 900px;
  padding: 24px;
}

.auth-card {
  display: flex;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  overflow: hidden;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.auth-brand {
  flex: 1;
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.2), rgba(139, 92, 246, 0.15));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 40px;
}

.brand-content {
  text-align: center;
}

.brand-logo {
  width: 72px; height: 72px;
  border-radius: 18px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
}

.brand-content h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
}

.brand-content p {
  font-size: 0.875rem;
  color: #94a3b8;
  margin-bottom: 24px;
}

.brand-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.brand-features span {
  font-size: 0.75rem;
  padding: 5px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: #c7d2fe;
}

.auth-form {
  flex: 1;
  padding: 56px 40px;
  background: rgba(11, 15, 26, 0.8);
}

.form-header {
  margin-bottom: 32px;
}

.form-header h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #f1f5f9;
  margin-bottom: 6px;
}

.form-header p {
  font-size: 0.875rem;
  color: #64748b;
}

.auth-btn {
  width: 100%;
  padding: 14px 0;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border: none;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.auth-btn:hover {
  box-shadow: 0 6px 24px rgba(99, 102, 241, 0.45);
  transform: translateY(-1px);
}

.form-footer {
  text-align: center;
  font-size: 0.875rem;
  color: #64748b;
  margin-top: 8px;
}

.form-footer a {
  color: #818cf8;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}

.form-footer a:hover {
  color: #a5b4fc;
}

.back-home {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 20px auto 0;
  background: none;
  border: none;
  color: #64748b;
  font-size: 0.825rem;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;
}

.back-home:hover {
  color: #94a3b8;
  background: rgba(255, 255, 255, 0.05);
}

@media (max-width: 768px) {
  .auth-card {
    flex-direction: column;
  }
  .auth-brand {
    padding: 40px 24px;
  }
  .auth-form {
    padding: 40px 24px;
  }
}
</style>
