import request from './request'
import type { ApiResult } from '../types/api'
import type {
  ShoppingMemoVO,
  ShoppingMemoCreateRequest,
  AnniversaryVO,
  AnniversaryCreateRequest,
  ReminderRuleVO,
  ReminderRuleCreateRequest,
} from '../types/api'

export function listShoppingMemos() {
  return request.get<ApiResult<ShoppingMemoVO[]>>('/life/shopping')
}

export function createShoppingMemo(data: ShoppingMemoCreateRequest) {
  return request.post<ApiResult<ShoppingMemoVO>>('/life/shopping', data)
}

export function toggleShoppingMemo(id: number) {
  return request.put<ApiResult<ShoppingMemoVO>>(`/life/shopping/${id}/toggle`)
}

export function listAnniversaries() {
  return request.get<ApiResult<AnniversaryVO[]>>('/life/anniversaries')
}

export function createAnniversary(data: AnniversaryCreateRequest) {
  return request.post<ApiResult<AnniversaryVO>>('/life/anniversaries', data)
}

export function listReminderRules() {
  return request.get<ApiResult<ReminderRuleVO[]>>('/life/reminders')
}

export function createReminderRule(data: ReminderRuleCreateRequest) {
  return request.post<ApiResult<ReminderRuleVO>>('/life/reminders', data)
}

export function toggleReminderRule(id: number) {
  return request.put<ApiResult<ReminderRuleVO>>(`/life/reminders/${id}/toggle`)
}
