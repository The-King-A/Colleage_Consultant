<template>
  <div class="not-found-page">
    <div class="nf-content">
      <h1>404</h1>
      <p>页面不存在</p>
      <router-link to="/" class="back-link">返回首页</router-link>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0b0f1a;
  color: #f1f5f9;
}

.nf-content {
  text-align: center;
}

.nf-content h1 {
  font-size: 6rem;
  font-weight: 800;
  background: linear-gradient(135deg, #818cf8, #c084fc, #22d3ee);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -3px;
}

.nf-content p {
  font-size: 1.25rem;
  color: #94a3b8;
  margin: 16px 0 32px;
}

.back-link {
  display: inline-block;
  padding: 12px 28px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
}

.back-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
}
</style>
