/**
 * 冰箱 api
 * ========
 * types 从 ../types/fridge 共享 (FE 契约),后端 VO 名跟 FridgeXxxVO 对齐。
 */

import request from './request'
import type { PageVO } from '../types/api'
import type {
  FridgeItemVO,
  FridgeExpiringVO,
  FridgeCategoryVO,
  FridgeTemplateVO,
  FridgeTemplateRequest,
  FridgeShoppingConfirmRequest,
  FridgeShoppingHistoryVO,
  FridgeShoppingItem,
  FridgeItemRequest,
} from '../types/fridge'

export function listItems(params?: { status?: string; page?: number; size?: number }) {
  return request.get<PageVO<FridgeItemVO>>('/fridge/items', { params })
}

export function createItem(data: FridgeItemRequest) {
  return request.post<FridgeItemVO>('/fridge/items', data)
}

export function consumeItem(id: number) {
  return request.post<null>(`/fridge/items/${id}/consume`)
}

export function discardItem(id: number) {
  return request.post<null>(`/fridge/items/${id}/discard`)
}

export function getExpiringStats() {
  return request.get<FridgeExpiringVO>('/fridge/items/expiring')
}

export function listCategories() {
  return request.get<FridgeCategoryVO[]>('/fridge/categories')
}

export function listTemplates() {
  return request.get<FridgeTemplateVO[]>('/fridge/templates')
}

export function createTemplate(data: FridgeTemplateRequest) {
  return request.post<FridgeTemplateVO>('/fridge/templates', data)
}

export function deleteTemplate(id: number) {
  return request.delete<null>(`/fridge/templates/${id}`)
}

export function confirmPurchase(items: FridgeShoppingItem[]) {
  return request.post<FridgeShoppingHistoryVO>('/fridge/purchase/confirm', {
    items,
  } as FridgeShoppingConfirmRequest)
}
