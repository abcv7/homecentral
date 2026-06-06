<template>
  <n-space vertical :size="16">
    <n-h2>系统设置</n-h2>

    <n-spin :show="loading">
      <n-card
        title="阿里云快递查询 API"
        size="medium"
        :bordered="true"
      >
        <template #header-extra>
          <n-tag v-if="account?.enabled" type="success" size="small">已启用</n-tag>
          <n-tag v-else-if="account" type="warning" size="small">已禁用</n-tag>
          <n-tag v-else type="default" size="small">未配置</n-tag>
        </template>

        <n-space vertical :size="12">
          <n-text depth="3">
            配置阿里云云市场「快递查询Ⅰ」API 的 AppCode。
            前往
            <a href="https://market.aliyun.com/detail/cmapi00050778" target="_blank" rel="noopener">
              阿里云云市场
            </a>
            购买后获取 AppCode。优先级：用户配置 → 全局 yml 回退。
          </n-text>

          <n-form
            :model="form"
            label-placement="top"
          >
            <n-grid :cols="isMobile ? 1 : 2" :x-gap="16">
              <n-gi :span="isMobile ? 1 : 2">
                <n-form-item label="AppCode">
                  <n-input
                    v-model:value="form.apiKey"
                    type="password"
                    show-password-on="click"
                    :placeholder="account ? `当前：${account.apiKeyMasked}` : '请输入阿里云 AppCode'"
                    clearable
                  />
                </n-form-item>
              </n-gi>
              <n-gi>
                <n-form-item label="Customer（可选）">
                  <n-input
                    v-model:value="form.customer"
                    placeholder="客户标识（可选）"
                    clearable
                  />
                </n-form-item>
              </n-gi>
              <n-gi>
                <n-form-item label="启用状态">
                  <n-switch
                    v-model:value="form.enabled"
                    :checked-value="true"
                    :unchecked-value="false"
                  >
                    <template #checked>启用</template>
                    <template #unchecked>禁用（回退到全局 yml）</template>
                  </n-switch>
                </n-form-item>
              </n-gi>
            </n-grid>
          </n-form>

          <n-space justify="end">
            <n-button
              v-if="account"
              type="error"
              quaternary
              :loading="deleting"
              @click="handleDelete"
            >
              删除我的配置
            </n-button>
            <n-button
              type="primary"
              :loading="saving"
              :disabled="!form.apiKey"
              @click="handleSave"
            >
              {{ account ? '更新' : '保存' }}
            </n-button>
          </n-space>

          <n-alert v-if="testResult" :type="testResult.type" :title="testResult.title">
            {{ testResult.message }}
          </n-alert>
        </n-space>
      </n-card>

      <n-card title="使用说明" size="small" :bordered="true">
        <n-ul>
          <n-li>在「驿站管理」中，点击「物流」按钮可实时查询快递轨迹。</n-li>
          <n-li>每用户独立的 AppCode：购买后可多人共享或各自独立申请。</n-li>
          <n-li>留空 = 不保存 AppCode，物流查询将使用系统全局 yml 中的兜底值。</n-li>
          <n-li>禁用状态下将回退到全局配置；删除用户配置同样回退。</n-li>
        </n-ul>
      </n-card>
    </n-spin>
  </n-space>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  NSpace, NH2, NCard, NSpin, NTag, NText, NForm, NFormItem, NInput, NGrid, NGi, NSwitch,
  NButton, NAlert, NUl, NLi, useMessage,
} from 'naive-ui'
import type { ApiAccountVO } from '../../types/api'
import { getAliyunAppCode, saveAliyunAppCode, deleteAliyunAppCode } from '../../api/settings'
import { useBreakpoint } from '../../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
const message = useMessage()

const account = ref<ApiAccountVO | null>(null)
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const testResult = ref<{ type: 'success' | 'error' | 'info'; title: string; message: string } | null>(null)

const form = reactive({
  apiKey: '',
  customer: '',
  enabled: true,
})

async function fetchAccount() {
  loading.value = true
  try {
    const res = await getAliyunAppCode()
    account.value = res.data.data
    if (account.value) {
      form.customer = account.value.customer ?? ''
      form.enabled = account.value.enabled ?? true
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '加载配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.apiKey) {
    message.warning('请输入 AppCode')
    return
  }
  saving.value = true
  try {
    const res = await saveAliyunAppCode({
      apiKey: form.apiKey,
      customer: form.customer || undefined,
      enabled: form.enabled,
    })
    account.value = res.data.data
    form.apiKey = ''
    message.success('保存成功')
    testResult.value = {
      type: 'success',
      title: '已保存',
      message: `AppCode 末尾：${res.data.data.apiKeyMasked}，已${res.data.data.enabled ? '启用' : '禁用'}`,
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  deleting.value = true
  try {
    await deleteAliyunAppCode()
    account.value = null
    form.apiKey = ''
    form.customer = ''
    form.enabled = true
    message.success('已删除用户配置，将回退到全局 yml')
    testResult.value = {
      type: 'info',
      title: '已删除',
      message: '后续物流查询将使用系统全局配置。',
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '删除失败')
  } finally {
    deleting.value = false
  }
}

onMounted(fetchAccount)
</script>
