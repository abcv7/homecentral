import request from './request'
import type { ApiResult } from '../types/api'
import type {
  FridgeCategoryRequest,
  FridgeCategoryVO,
  FridgeExpiringVO,
  FridgeItemMoveRequest,
  FridgeItemRequest,
  FridgeItemVO,
  FridgeQuickCreateRequest,
  FridgeRecognizeRequest,
  FridgeRecognizeResult,
  FridgeShoppingConfirmRequest,
  FridgeShoppingHistoryVO,
  FridgeShoppingItem,
  FridgeTemplateRequest,
  FridgeTemplateVO,
  FridgeZone,
  FridgeItemStatus,
} from '../types/fridge'

export function listCategories() {
  return request.get<ApiResult<FridgeCategoryVO[]>>('/fridge/categories')
}

export function createCategory(data: FridgeCategoryRequest) {
  return request.post<ApiResult<FridgeCategoryVO>>('/fridge/categories', data)
}

export function updateCategory(id: number, data: FridgeCategoryRequest) {
  return request.put<ApiResult<FridgeCategoryVO>>(`/fridge/categories/${id}`, data)
}

export function deleteCategory(id: number) {
  return request.delete<ApiResult<null>>(`/fridge/categories/${id}`)
}

export function listItems(params?: {
  zone?: FridgeZone
  categoryId?: number
  status?: FridgeItemStatus
  keyword?: string
  includePending?: boolean
}) {
  return request.get<ApiResult<FridgeItemVO[]>>(
    '/fridge/items',
    { params },
  )
}

export function getItem(id: number) {
  return request.get<ApiResult<FridgeItemVO>>(`/fridge/items/${id}`)
}

export function createItem(data: FridgeItemRequest) {
  return request.post<ApiResult<FridgeItemVO>>('/fridge/items', data)
}

export function createItemsBatch(data: FridgeItemRequest[]) {
  return request.post<ApiResult<FridgeItemVO[]>>('/fridge/items/batch', data)
}

export function updateItem(id: number, data: FridgeItemRequest) {
  return request.put<ApiResult<FridgeItemVO>>(`/fridge/items/${id}`, data)
}

export function consumeItem(id: number) {
  return request.post<ApiResult<FridgeItemVO>>(`/fridge/items/${id}/consume`)
}

export function consumeOneItem(id: number) {
  return request.post<ApiResult<FridgeItemVO>>(`/fridge/items/${id}/consume-one`)
}

export function discardItem(id: number) {
  return request.post<ApiResult<FridgeItemVO>>(`/fridge/items/${id}/discard`)
}

export function deleteItem(id: number) {
  return request.delete<ApiResult<null>>(`/fridge/items/${id}`)
}

export function moveItem(id: number, data: FridgeItemMoveRequest) {
  return request.post<ApiResult<FridgeItemVO>>(`/fridge/items/${id}/move`, data)
}

export function quickCreateItem(data: FridgeQuickCreateRequest) {
  return request.post<ApiResult<FridgeItemVO>>('/fridge/items/quick', data)
}

export function getExpiringStats(daysAhead?: number) {
  return request.get<ApiResult<FridgeExpiringVO>>('/fridge/items/expiring', {
    params: { daysAhead },
  })
}

export function recognizeImage(data: FridgeRecognizeRequest) {
  return request.post<ApiResult<FridgeRecognizeResult>>('/fridge/recognize', data)
}

export function listTemplates() {
  return request.get<ApiResult<FridgeTemplateVO[]>>('/fridge/templates')
}

export function getDefaultTemplate() {
  return request.get<ApiResult<FridgeTemplateVO>>('/fridge/templates/default')
}

export function createTemplate(data: FridgeTemplateRequest) {
  return request.post<ApiResult<FridgeTemplateVO>>('/fridge/templates', data)
}

export function updateTemplate(id: number, data: FridgeTemplateRequest) {
  return request.put<ApiResult<FridgeTemplateVO>>(`/fridge/templates/${id}`, data)
}

export function deleteTemplate(id: number) {
  return request.delete<ApiResult<null>>(`/fridge/templates/${id}`)
}

export function activateTemplate(id: number) {
  return request.post<ApiResult<FridgeTemplateVO>>(`/fridge/templates/${id}/activate`)
}

export function confirmShopping(data: FridgeShoppingConfirmRequest) {
  return request.post<ApiResult<FridgeShoppingHistoryVO>>('/fridge/shopping/confirm', data)
}

export function listShoppingHistory(limit = 50) {
  return request.get<ApiResult<FridgeShoppingHistoryVO[]>>('/fridge/shopping/history', {
    params: { limit },
  })
}

export function getShoppingBatch(batchId: string) {
  return request.get<ApiResult<FridgeShoppingHistoryVO>>(
    `/fridge/shopping/history/${batchId}`,
  )
}

export function reuseShoppingBatch(batchId: string) {
  return request.post<ApiResult<FridgeShoppingItem[]>>(
    `/fridge/shopping/history/${batchId}/reuse`,
  )
}

export function reuseShoppingOne(historyId: number) {
  return request.post<ApiResult<FridgeShoppingItem>>(
    `/fridge/shopping/history/item/${historyId}/reuse`,
  )
}
