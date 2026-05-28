<template>
  <div class="map-page">
    <aside class="map-sidebar">
      <div class="sidebar-head">
        <h1>地图选校</h1>
        <p>按省份筛选，点击标记查看院校；坐标由城市库推算</p>
      </div>

      <el-form label-position="top" class="filter-form" @submit.prevent="loadMarkers">
        <el-form-item label="院校所在省">
          <el-select v-model="filters.location" clearable filterable placeholder="全国" style="width: 100%">
            <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="录取生源省（看分）">
          <el-select v-model="filters.scoreProvince" filterable style="width: 100%">
            <el-option v-for="p in scoreProvinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="校名关键词">
          <el-input v-model="filters.keyword" clearable placeholder="可选" />
        </el-form-item>
        <div class="tag-row">
          <el-checkbox v-model="filters.is985">985</el-checkbox>
          <el-checkbox v-model="filters.is211">211</el-checkbox>
        </div>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="loadMarkers">
          加载地图
        </el-button>
      </el-form>

      <p v-if="stats" class="stats">
        已标注 {{ stats.mappedCount }} / {{ stats.totalQueried }} 所
        <span v-if="stats.skippedNoCoords">（{{ stats.skippedNoCoords }} 所暂无坐标）</span>
      </p>

      <div class="school-list">
        <div
          v-for="s in markers"
          :key="s.schoolCode"
          class="school-row"
          :class="{ active: selected?.schoolCode === s.schoolCode }"
          @click="focusSchool(s)"
        >
          <strong>{{ s.schoolName }}</strong>
          <span>{{ s.province }} · {{ s.city || s.location }}</span>
          <span v-if="s.latestMinScore" class="score">最低 {{ s.latestMinScore }}</span>
          <div class="tags">
            <span v-if="s.is985" class="t985">985</span>
            <span v-if="s.is211" class="t211">211</span>
          </div>
        </div>
      </div>
    </aside>

    <div ref="mapRef" class="map-container" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { ElMessage } from 'element-plus'
import { fetchSchoolMapMarkers, fetchSchoolLocations, fetchScoreProvinces } from '../api/school'
import { CHINA_PROVINCES } from '../utils/provinces'

const router = useRouter()
const mapRef = ref(null)
const mapInstance = shallowRef(null)
const markerLayer = shallowRef(null)
const loading = ref(false)
const markers = ref([])
const stats = ref(null)
const selected = ref(null)
const provinces = ref(CHINA_PROVINCES)
const scoreProvinces = ref([])

const filters = reactive({
  location: '福建',
  scoreProvince: '北京',
  subjectType: 'science',
  keyword: '',
  is985: false,
  is211: false,
})

onMounted(async () => {
  initMap()
  try {
    const [locRes, provRes] = await Promise.all([
      fetchSchoolLocations(),
      fetchScoreProvinces(),
    ])
    if (locRes.data?.length) provinces.value = locRes.data
    if (provRes.data?.length) scoreProvinces.value = provRes.data
  } catch { /* use defaults */ }
  loadMarkers()
})

onBeforeUnmount(() => {
  mapInstance.value?.remove()
})

function initMap() {
  if (!mapRef.value || mapInstance.value) return
  const map = L.map(mapRef.value, {
    center: [35.0, 105.0],
    zoom: 5,
    minZoom: 4,
    maxZoom: 18,
  })
  L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; OpenStreetMap &copy; CARTO',
    subdomains: 'abcd',
    maxZoom: 19,
  }).addTo(map)
  markerLayer.value = L.layerGroup().addTo(map)
  mapInstance.value = map
}

async function loadMarkers() {
  loading.value = true
  try {
    const res = await fetchSchoolMapMarkers({
      location: filters.location || undefined,
      keyword: filters.keyword || undefined,
      scoreProvince: filters.scoreProvince,
      subjectType: filters.subjectType,
      is985: filters.is985 || undefined,
      is211: filters.is211 || undefined,
      limit: 500,
    })
    const data = res.data || {}
    markers.value = data.markers || []
    stats.value = data
    renderMarkers()
    if (!markers.value.length) {
      ElMessage.info('当前条件下没有可标注的院校，请换省或放宽筛选')
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function renderMarkers() {
  const layer = markerLayer.value
  const map = mapInstance.value
  if (!layer || !map) return
  layer.clearLayers()
  const bounds = []

  markers.value.forEach((s) => {
    if (s.lat == null || s.lng == null) return
    const color = s.is985 ? '#dc2626' : s.is211 ? '#2563eb' : '#6366f1'
    const circle = L.circleMarker([s.lat, s.lng], {
      radius: s.is985 ? 9 : 7,
      color,
      fillColor: color,
      fillOpacity: 0.75,
      weight: 2,
    })
    const scoreText = s.latestMinScore != null ? `<br/>最低分 ${s.latestMinScore}` : ''
    const tags = [
      s.is985 ? '<span style="color:#dc2626">985</span>' : '',
      s.is211 ? '<span style="color:#2563eb">211</span>' : '',
    ].filter(Boolean).join(' ')
    circle.bindPopup(
      `<strong>${s.schoolName}</strong><br/>${s.province} · ${s.city || s.location || ''}${scoreText}${tags ? '<br/>' + tags : ''}`
    )
    circle.on('click', () => {
      selected.value = s
    })
    circle.on('dblclick', () => {
      router.push(`/school/${s.schoolCode}`)
    })
    layer.addLayer(circle)
    bounds.push([s.lat, s.lng])
  })

  if (bounds.length === 1) {
    map.setView(bounds[0], 10)
  } else if (bounds.length > 1) {
    map.fitBounds(bounds, { padding: [48, 48], maxZoom: 12 })
  }
}

function focusSchool(s) {
  selected.value = s
  if (s.lat != null && s.lng != null) {
    mapInstance.value?.setView([s.lat, s.lng], 12, { animate: true })
    markerLayer.value?.eachLayer((layer) => {
      const latlng = layer.getLatLng?.()
      if (latlng && Math.abs(latlng.lat - s.lat) < 1e-6 && Math.abs(latlng.lng - s.lng) < 1e-6) {
        layer.openPopup()
      }
    })
  }
}
</script>

<style scoped>
.map-page {
  display: flex;
  height: calc(100vh - 72px);
  background: #f1f5f9;
}

.map-sidebar {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-head {
  padding: 20px 20px 12px;
  border-bottom: 1px solid #f1f5f9;
}

.sidebar-head h1 {
  font-size: 1.25rem;
  font-weight: 800;
  color: #1e293b;
}

.sidebar-head p {
  font-size: 0.8rem;
  color: #94a3b8;
  margin-top: 6px;
  line-height: 1.5;
}

.filter-form {
  padding: 12px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.tag-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.stats {
  font-size: 0.75rem;
  color: #64748b;
  padding: 8px 20px;
  background: #f8fafc;
}

.school-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px 16px;
}

.school-row {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 6px;
  border: 1px solid transparent;
  transition: background 0.15s;
}

.school-row:hover,
.school-row.active {
  background: #eef2ff;
  border-color: #c7d2fe;
}

.school-row strong {
  display: block;
  font-size: 0.9rem;
  color: #1e293b;
}

.school-row span {
  display: block;
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 4px;
}

.school-row .score {
  color: #6366f1;
}

.tags {
  margin-top: 6px;
}

.t985, .t211 {
  font-size: 0.65rem;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 4px;
}

.t985 { background: #fee2e2; color: #dc2626; }
.t211 { background: #dbeafe; color: #2563eb; }

.map-container {
  flex: 1;
  min-width: 0;
  z-index: 0;
}

:deep(.leaflet-popup-content) {
  font-size: 13px;
  line-height: 1.5;
  margin: 10px 14px;
}

@media (max-width: 900px) {
  .map-page {
    flex-direction: column;
    height: auto;
  }
  .map-sidebar {
    width: 100%;
    max-height: 42vh;
  }
  .map-container {
    height: 50vh;
  }
}
</style>
