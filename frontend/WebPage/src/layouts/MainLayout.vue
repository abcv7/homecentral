<template>
  <!-- 桌面：固定侧栏 + 内容 -->
  <n-layout v-if="isDesktop || isTablet" has-sider position="absolute">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="isTablet ? 64 : 64"
      :width="220"
      :collapsed="siderCollapsed"
      show-trigger="bar"
      @collapse="siderCollapsed = true"
      @expand="siderCollapsed = false"
      content-style="display:flex;flex-direction:column;"
    >
      <div class="sider-header">
        <span v-if="!siderCollapsed" class="sider-title">HomeCentral</span>
        <span v-else class="sider-title--mini">HC</span>
      </div>
      <n-menu
        :collapsed="siderCollapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuSelect"
      />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="layout-header">
        <n-space align="center" justify="space-between" style="height:100%;padding:0 20px;">
          <span class="header-title">{{ currentTitle }}</span>
          <n-dropdown trigger="hover" :options="userMenuOptions" @select="handleUserMenu">
            <n-button quaternary class="user-button">
              <template #icon><n-icon><person-outline /></n-icon></template>
              <span v-if="isDesktop" class="user-name">{{ authStore.username }}</span>
              <span v-else>{{ authStore.username?.charAt(0) ?? 'U' }}</span>
            </n-button>
          </n-dropdown>
        </n-space>
      </n-layout-header>
      <n-layout-content :class="isTablet ? 'layout-content layout-content--tablet' : 'layout-content'">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>

  <!-- 移动端：顶部 header + 抽屉式侧栏 + 内容 -->
  <div v-else class="mobile-shell">
    <header class="mobile-header">
      <n-button quaternary size="large" class="hamburger" @click="drawerShow = true" aria-label="菜单">
        <n-icon size="22"><menu-outline /></n-icon>
      </n-button>
      <span class="mobile-title">{{ currentTitle }}</span>
      <n-dropdown trigger="click" :options="userMenuOptions" @select="handleUserMenu">
        <n-button quaternary size="large" class="mobile-user" aria-label="用户">
          <n-icon size="20"><person-outline /></n-icon>
        </n-button>
      </n-dropdown>
    </header>

    <main class="mobile-content">
      <router-view />
    </main>

    <n-drawer v-model:show="drawerShow" :width="280" placement="left">
      <n-drawer-content :show-icon="false" :native-scrollbar="false" body-content-style="padding:0;">
        <div class="drawer-header">
          <span class="sider-title">HomeCentral</span>
        </div>
        <n-menu
          :options="menuOptions"
          :value="activeKey"
          :indent="18"
          @update:value="handleMenuSelect"
        />
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, type Component } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon, NButton, NDropdown, NDrawer, NDrawerContent } from 'naive-ui'
import {
  PersonOutline, GridOutline, CubeOutline, HeartOutline,
  NotificationsOutline, SnowOutline, PeopleOutline, SettingsOutline,
  MenuOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '../stores/auth'
import { useBreakpoint } from '../composables/useBreakpoint'
import type { MenuOption } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { isTablet, isDesktop } = useBreakpoint()

const siderCollapsed = ref(false)
const drawerShow = ref(false)

const currentTitle = computed(() => route.meta?.title ?? '')

const activeKey = computed(() => route.path)

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  { label: '仪表盘', key: '/dashboard', icon: renderIcon(GridOutline) },
  { label: '驿站管理', key: '/parcel', icon: renderIcon(CubeOutline) },
  { label: '生活备忘', key: '/life', icon: renderIcon(HeartOutline) },
  { label: '通知中心', key: '/notification', icon: renderIcon(NotificationsOutline) },
  { label: '好友与分组', key: '/friend', icon: renderIcon(PeopleOutline) },
  { label: '冰箱食材', key: '/fridge', icon: renderIcon(SnowOutline) },
  { label: '个人资料', key: '/profile', icon: renderIcon(SettingsOutline) },
]

const userMenuOptions = [
  { label: '个人资料', key: 'profile', icon: renderIcon(PersonOutline) },
  { label: '退出登录', key: 'logout' },
]

function handleMenuSelect(key: string) {
  drawerShow.value = false
  router.push(key)
}

function handleUserMenu(key: string) {
  if (key === 'logout') {
    authStore.logout()
    router.push('/login')
  } else if (key === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.sider-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--n-border-color);
}
.sider-title {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}
.sider-title--mini {
  font-size: 16px;
  font-weight: 700;
  color: #667eea;
}
.layout-header {
  height: 56px;
  background: #fff;
}
.header-title {
  font-size: 16px;
  font-weight: 500;
}
.user-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.user-name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.layout-content {
  padding: var(--h5-content-pad);
  min-height: calc(100vh - 56px);
  background: #f8fafc;
}
.layout-content--tablet {
  padding: 16px;
}

/* ============ 移动端 ============ */
.mobile-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 100vh;
  background: #f8fafc;
}
.mobile-header {
  position: sticky;
  top: 0;
  z-index: 50;
  height: calc(48px + var(--safe-top));
  padding-top: var(--safe-top);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.04);
}
.hamburger,
.mobile-user {
  width: 48px;
  height: 48px;
}
.mobile-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  padding-right: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mobile-content {
  flex: 1;
  padding: var(--h5-content-pad);
  padding-bottom: var(--h5-bottom-bar-h);
}
.drawer-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #fff, #f8fafc);
}
</style>
