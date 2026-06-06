import request from './request'
import type {
  ApiResult, ShoppingMemoVO, ShoppingMemoCreateRequest,
  AnniversaryVO, AnniversaryCreateRequest,
  ReminderRuleVO, ReminderRuleCreateRequest,
} from '../types/api'

// Shopping Memo
export function listShoppingMemos() {
  return request.get<ApiResult<ShoppingMemoVO[]>>('/life/shopping-memos')
}
export function createShoppingMemo(data: ShoppingMemoCreateRequest) {
  return request.post<ApiResult<ShoppingMemoVO>>('/life/shopping-memos', data)
}
export function toggleShoppingMemo(id: number) {
  return request.put<ApiResult<ShoppingMemoVO>>(`/life/shopping-memos/${id}/toggle`)
}

// Anniversary
export function listAnniversaries() {
  return request.get<ApiResult<AnniversaryVO[]>>('/life/anniversaries')
}
export function createAnniversary(data: AnniversaryCreateRequest) {
  return request.post<ApiResult<AnniversaryVO>>('/life/anniversaries', data)
}

// Reminder Rule
export function listReminderRules() {
  return request.get<ApiResult<ReminderRuleVO[]>>('/life/reminder-rules')
}
export function createReminderRule(data: ReminderRuleCreateRequest) {
  return request.post<ApiResult<ReminderRuleVO>>('/life/reminder-rules', data)
}
export function toggleReminderRule(id: number) {
  return request.put<ApiResult<ReminderRuleVO>>(`/life/reminder-rules/${id}/toggle`)
}
