import { createRouter, createWebHistory } from 'vue-router'
import { getAccessToken } from '../utils/token'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/index.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/register/index.vue'),
    },
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/dashboard/index.vue'),
          meta: { title: '仪表盘' },
        },
        {
          path: 'parcel',
          name: 'Parcel',
          component: () => import('../views/parcel/index.vue'),
          meta: { title: '驿站管理' },
        },
        {
          path: 'life',
          name: 'Life',
          component: () => import('../views/life/index.vue'),
          meta: { title: '生活备忘' },
        },
        {
          path: 'notification',
          name: 'Notification',
          component: () => import('../views/notification/index.vue'),
          meta: { title: '通知中心' },
        },
        {
          path: 'friend',
          name: 'Friend',
          component: () => import('../views/friend/index.vue'),
          meta: { title: '好友与分组' },
        },
        {
          path: 'fridge',
          name: 'Fridge',
          component: () => import('../views/fridge/index.vue'),
          meta: { title: '冰箱食材' },
        },
        {
          path: 'workshop',
          name: 'Workshop',
          component: () => import('../views/workshop/index.vue'),
          meta: { title: '调酒台' },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/profile/index.vue'),
          meta: { title: '个人资料' },
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('../views/settings/index.vue'),
          meta: { title: '系统设置' },
        },
      ],
    },
  ],
})

router.beforeEach((to, _from) => {
  const token = getAccessToken()
  if (to.name !== 'Login' && to.name !== 'Register' && !token) {
    return { name: 'Login' }
  }
  if ((to.name === 'Login' || to.name === 'Register') && token) {
    return { name: 'Dashboard' }
  }
})

export default router
