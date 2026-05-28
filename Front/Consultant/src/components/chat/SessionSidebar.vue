<template>
  <aside class="sidebar">
    <!-- Head -->
    <div class="s-head">
      <div class="s-logo">
        <div class="s-logo-icon">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 8l10 6 10-6-10-6z"/><path d="M2 16l10 6 10-6"/><path d="M2 12l10 6 10-6"/>
          </svg>
        </div>
        <span>智选未来</span>
      </div>
      <button class="s-new" @click="$emit('create')">
        <el-icon :size="18"><Plus /></el-icon>
        <span>新对话</span>
      </button>
    </div>

    <!-- Session list -->
    <div class="s-body">
      <div v-if="sortedSessions.length === 0" class="s-empty">
        <p>暂无对话</p>
      </div>

      <template v-else>
        <div v-if="todaySessions.length" class="s-group">
          <span class="s-label">今天</span>
          <div
            v-for="s in todaySessions" :key="s.id"
            class="s-item" :class="{ active: s.id === currentSessionId }"
            @click="$emit('switch', s.id)"
          >
            <el-icon :size="15" class="s-icon"><ChatDotRound /></el-icon>
            <div class="s-info">
              <span class="s-title">{{ s.title || '新对话' }}</span>
              <span class="s-meta">{{ s.messageCount }} 条消息</span>
            </div>
            <el-dropdown trigger="click" @command="(c) => c === 'delete' && $emit('delete', s.id)">
              <button class="s-more" @click.stop><el-icon :size="14"><MoreFilled /></el-icon></button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="delete"><el-icon><Delete /></el-icon><span style="color:#ef4444">删除</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div v-if="weekSessions.length" class="s-group">
          <span class="s-label">近7天</span>
          <div
            v-for="s in weekSessions" :key="s.id"
            class="s-item" :class="{ active: s.id === currentSessionId }"
            @click="$emit('switch', s.id)"
          >
            <el-icon :size="15" class="s-icon"><ChatDotRound /></el-icon>
            <div class="s-info">
              <span class="s-title">{{ s.title || '新对话' }}</span>
              <span class="s-meta">{{ s.messageCount }} 条消息</span>
            </div>
            <el-dropdown trigger="click" @command="(c) => c === 'delete' && $emit('delete', s.id)">
              <button class="s-more" @click.stop><el-icon :size="14"><MoreFilled /></el-icon></button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="delete"><el-icon><Delete /></el-icon><span style="color:#ef4444">删除</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div v-if="olderSessions.length" class="s-group">
          <span class="s-label">更早</span>
          <div
            v-for="s in olderSessions" :key="s.id"
            class="s-item" :class="{ active: s.id === currentSessionId }"
            @click="$emit('switch', s.id)"
          >
            <el-icon :size="15" class="s-icon"><ChatDotRound /></el-icon>
            <div class="s-info">
              <span class="s-title">{{ s.title || '新对话' }}</span>
              <span class="s-meta">{{ s.messageCount }} 条消息</span>
            </div>
            <el-dropdown trigger="click" @command="(c) => c === 'delete' && $emit('delete', s.id)">
              <button class="s-more" @click.stop><el-icon :size="14"><MoreFilled /></el-icon></button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="delete"><el-icon><Delete /></el-icon><span style="color:#ef4444">删除</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
    </div>

    <!-- Foot -->
    <div class="s-foot">
      <router-link to="/" class="s-home">
        <el-icon :size="16"><HomeFilled /></el-icon><span>返回首页</span>
      </router-link>
    </div>
  </aside>
</template>

<script setup>
import { Plus, ChatDotRound, MoreFilled, Delete, HomeFilled } from '@element-plus/icons-vue'

defineProps({
  currentSessionId: { type: String, default: null },
  sortedSessions: { type: Array, default: () => [] },
  todaySessions: { type: Array, default: () => [] },
  weekSessions: { type: Array, default: () => [] },
  olderSessions: { type: Array, default: () => [] },
})

defineEmits(['create', 'switch', 'delete'])
</script>

<style scoped>
.sidebar {
  width: 280px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #e8ecf1;
}

/* Head */
.s-head {
  padding: 18px 16px 12px;
  border-bottom: 1px solid #f1f5f9;
}

.s-logo {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 14px;
}

.s-logo-icon {
  width: 28px; height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #7c3aed, #6366f1);
  display: flex; align-items: center; justify-content: center;
}

.s-logo span { font-size: 0.9rem; font-weight: 700; color: #0f172a; }

.s-new {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  width: 100%;
  padding: 10px 0;
  border-radius: 12px;
  border: 1px solid #e8ecf1;
  background: #fafbfc;
  color: #334155;
  font-size: 0.84rem; font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.s-new:hover { background: #f1f5f9; border-color: #cbd5e1; }

/* Body */
.s-body {
  flex: 1; overflow-y: auto; padding: 6px 8px;
}

.s-empty {
  text-align: center; padding: 40px 16px;
  font-size: 0.83rem; color: #94a3b8;
}

.s-group { margin-bottom: 6px; }

.s-label {
  display: block;
  font-size: 0.68rem; font-weight: 600;
  color: #94a3b8; text-transform: uppercase;
  letter-spacing: 1px; padding: 8px 12px 4px;
}

.s-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
}

.s-item:hover { background: #f8fafc; }

.s-item.active {
  background: #eef2ff;
}

.s-icon { color: #94a3b8; flex-shrink: 0; }
.s-item.active .s-icon { color: #6366f1; }

.s-info { flex: 1; min-width: 0; }

.s-title {
  display: block;
  font-size: 0.82rem; font-weight: 500;
  color: #334155;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.s-item.active .s-title { color: #0f172a; }

.s-meta { font-size: 0.68rem; color: #94a3b8; }

.s-more {
  opacity: 0;
  width: 24px; height: 24px;
  border-radius: 6px; border: none; background: none;
  color: #94a3b8; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}

.s-item:hover .s-more { opacity: 1; }
.s-more:hover { background: #e8ecf1; color: #475569; }

/* Foot */
.s-foot { padding: 10px 16px; border-top: 1px solid #f1f5f9; }

.s-home {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  text-decoration: none;
  font-size: 0.82rem; color: #64748b;
  transition: all 0.15s;
}

.s-home:hover { background: #f8fafc; color: #334155; }
</style>
