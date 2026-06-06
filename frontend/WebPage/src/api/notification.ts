import request from './request'
import type { ApiResult, NotificationVO } from '../types/api'

export function listNotifications() {
  return request.get<ApiResult<NotificationVO[]>>('/notification')
}

export function readNotification(id: number) {
  return request.put<ApiResult<NotificationVO>>(`/notification/${id}/read`)
}

export function readAllNotifications() {
  return request.put<ApiResult<null>>('/notification/read-all')
}
