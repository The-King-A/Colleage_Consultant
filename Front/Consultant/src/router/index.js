import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { guest: true },
  },
  {
    path: '/chat',
    name: 'chat',
    component: () => import('../views/ChatView.vue'),
  },
  {
    path: '/recommend',
    name: 'recommend',
    component: () => import('../views/RecommendView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/interest-test',
    name: 'interestTest',
    component: () => import('../views/InterestTestView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/schools',
    name: 'schools',
    component: () => import('../views/SchoolListView.vue'),
  },
  {
    path: '/school-map',
    name: 'schoolMap',
    component: () => import('../views/SchoolMapView.vue'),
  },
  {
    path: '/majors',
    name: 'majors',
    component: () => import('../views/MajorListView.vue'),
  },
  {
    path: '/compare',
    name: 'compare',
    component: () => import('../views/CompareView.vue'),
  },
  {
    path: '/matcher',
    name: 'matcher',
    component: () => import('../views/AdmissionMatcherView.vue'),
  },
  {
    path: '/insights',
    name: 'insights',
    component: () => import('../views/ScoreInsightsView.vue'),
  },
  {
    path: '/rank',
    name: 'rank',
    component: () => import('../views/ScoreRankView.vue'),
  },
  {
    path: '/wizard',
    name: 'wizard',
    component: () => import('../views/VolunteerWizardView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/plans',
    name: 'plans',
    component: () => import('../views/VolunteerPlanView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/DataDashboardView.vue'),
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('../views/FavoritesView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/school/:code',
    name: 'schoolDetail',
    component: () => import('../views/SchoolDetailView.vue'),
  },
  {
    path: '/major/:code',
    name: 'majorDetail',
    component: () => import('../views/MajorDetailView.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('../views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// Auth guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && authStore.isLoggedIn) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
