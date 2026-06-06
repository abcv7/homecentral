<script setup lang="ts">
import { NRadioButton, NRadioGroup, NSpace, NText } from 'naive-ui'
import type { WorkshopMode } from '../../../types/workshop'

const props = defineProps<{ modelValue: WorkshopMode }>()
const emit = defineEmits<{ (e: 'update:modelValue', m: WorkshopMode): void }>()
</script>

<template>
  <NSpace align="center" :size="8">
    <NText depth="3" style="font-size: 13px;">匹配模式</NText>
    <NRadioGroup
      :value="props.modelValue"
      @update:value="(v) => emit('update:modelValue', v as WorkshopMode)"
    >
      <NRadioButton value="strict">严格（缺一不可）</NRadioButton>
      <NRadioButton value="main">主料（缺装饰可）</NRadioButton>
    </NRadioGroup>
  </NSpace>
</template>
