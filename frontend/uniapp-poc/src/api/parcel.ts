/**
 * 包裹 api
 * 简化版:仅 listParcels / createParcel / pickUpParcel / receiveParcel
 * 业务逻辑(快递100/拍照识别等)留待 H5 版完整页面
 */

import request from './request'
import type { PageVO, ParcelVO, ParcelCreateRequest } from '../types/api'

export function listParcels(params?: { status?: string; trackingNumber?: string; page?: number; size?: number }) {
  return request.get<PageVO<ParcelVO>>('/parcel', { params })
}

export function createParcel(data: ParcelCreateRequest) {
  return request.post<ParcelVO>('/parcel', data)
}

export function pickUpParcel(id: number) {
  return request.put<ParcelVO>(`/parcel/${id}/pickup`)
}

export function receiveParcel(id: number) {
  return request.put<ParcelVO>(`/parcel/${id}/receive`)
}
