import request from './request'
import type { ApiResult, PageVO, ParcelVO, ParcelCreateRequest, ParcelUpdateRequest, SharedParcelUserVO, TrackingVO } from '../types/api'

export function listParcels(params?: { status?: string; trackingNumber?: string; page?: number; size?: number }) {
  return request.get<ApiResult<PageVO<ParcelVO>>>('/parcel', { params })
}

export function getParcel(id: number) {
  return request.get<ApiResult<ParcelVO>>(`/parcel/${id}`)
}

export function createParcel(data: ParcelCreateRequest) {
  return request.post<ApiResult<ParcelVO>>('/parcel', data)
}

export function updateParcel(id: number, data: ParcelUpdateRequest) {
  return request.put<ApiResult<ParcelVO>>(`/parcel/${id}`, data)
}

export function pickUpParcel(id: number) {
  return request.put<ApiResult<ParcelVO>>(`/parcel/${id}/pickup`)
}

export function receiveParcel(id: number) {
  return request.put<ApiResult<ParcelVO>>(`/parcel/${id}/receive`)
}

export function deleteParcel(id: number) {
  return request.delete<ApiResult<null>>(`/parcel/${id}`)
}

export function queryParcelTracking(id: number) {
  return request.get<ApiResult<TrackingVO>>(`/parcel/${id}/tracking`)
}

export function shareParcel(id: number, targetUserId: number) {
  return request.post<ApiResult<null>>(`/parcel/${id}/share`, null, { params: { targetUserId } })
}

export function unshareParcel(id: number, targetUserId: number) {
  return request.delete<ApiResult<null>>(`/parcel/${id}/share`, { params: { targetUserId } })
}

export function listParcelShares(id: number) {
  return request.get<ApiResult<SharedParcelUserVO[]>>(`/parcel/${id}/shares`)
}

export function queryTrackingByNumber(trackingNumber: string, courierCode?: string) {
  const params: Record<string, string> = { trackingNumber }
  if (courierCode) params.courierCode = courierCode
  return request.get<ApiResult<TrackingVO>>('/parcel/tracking/query', { params })
}

export function getPhoneTail() {
  return request.get<ApiResult<string>>('/parcel/phone-tail')
}

export function getPendingRecognitions() {
  return request.get<ApiResult<PendingRecognitionVO[]>>('/parcel/recognition/pending')
}

export function getRecognitionById(id: number) {
  return request.get<ApiResult<PendingRecognitionVO>>(`/parcel/recognition/${id}`)
}

export function claimRecognition(id: number) {
  return request.post<ApiResult<null>>(`/parcel/recognition/${id}/claim`)
}

export function discardRecognition(id: number) {
  return request.post<ApiResult<null>>(`/parcel/recognition/${id}/discard`)
}

export interface PendingRecognitionVO {
  id: number
  status: 'processing' | 'completed' | 'failed' | 'imported' | 'cancelled'
  resultJson: string | null
  failureMessage: string | null
  createdAt: string
  completedAt: string | null
  expiresAt: string | null
}
