/**
 * 驿站 api
 * ========
 * 后端返回 Result<Page<ParcelSummaryVO>> (MyBatis-Plus Page shape with `records`).
 * 简化:listParcels 数据用 unknown,调用方按运行时检查 (Array.isArray or .records) 自行提取。
 */

import request from './request'
import type { ApiResult } from '../types/api'

export function listParcels(params?: { status?: string; trackingNumber?: string; page?: number; size?: number }) {
  return request.get<ApiResult<unknown>>('/parcel', { params })
}

export function createParcel(data: unknown) {
  return request.post<ApiResult<unknown>>('/parcel', data)
}

export function pickUpParcel(id: number) {
  return request.put<ApiResult<unknown>>(`/parcel/${id}/pickup`)
}

export function receiveParcel(id: number) {
  return request.put<ApiResult<unknown>>(`/parcel/${id}/receive`)
}
