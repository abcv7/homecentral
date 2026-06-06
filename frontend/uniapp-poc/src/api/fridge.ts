/**
 * 冰箱 api
 * ========
 * types 走 ../types/fridge 转发 (FE 约定),后端 VO 经 FridgeXxxVO 导入。
 * 所有端点统一用 ApiResult<T> 包装,匹配后端 Result<T>。
 */

import request from './request'
import type { ApiResult } from '../types/api'
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

export function listItems(params?: { status?: string; zone?: string; categoryId?: number; includePending?: boolean }) {
  return request.get<ApiResult<FridgeItemVO[]>>('/fridge/items', { params })
}

export function createItem(data: FridgeItemRequest) {
  return request.post<ApiResult<FridgeItemVO>>('/fridge/items', data)
}

export function consumeItem(id: number) {
  return request.post<ApiResult<null>>(`/fridge/items/${id}/consume`)
}

export function discardItem(id: number) {
  return request.post<ApiResult<null>>(`/fridge/items/${id}/discard`)
}

export function getExpiringStats() {
  return request.get<ApiResult<FridgeExpiringVO>>('/fridge/items/expiring')
}

export function listCategories() {
  return request.get<ApiResult<FridgeCategoryVO[]>>('/fridge/categories')
}

export function listTemplates() {
  return request.get<ApiResult<FridgeTemplateVO[]>>('/fridge/templates')
}

export function createTemplate(data: FridgeTemplateRequest) {
  return request.post<ApiResult<FridgeTemplateVO>>('/fridge/templates', data)
}

export function deleteTemplate(id: number) {
  return request.delete<ApiResult<null>>(`/fridge/templates/${id}`)
}

export function confirmPurchase(items: FridgeShoppingItem[]) {
  return request.post<ApiResult<FridgeShoppingHistoryVO>>('/fridge/purchase/confirm', {
    items,
  } as FridgeShoppingConfirmRequest)
}
