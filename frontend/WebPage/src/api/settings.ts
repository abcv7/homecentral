import request from './request'
import type { ApiResult, ApiAccountVO } from '../types/api'

const BASE = '/parcel/api-account'

export function getAliyunAppCode() {
  return request.get<ApiResult<ApiAccountVO | null>>(`${BASE}/ALIYUN_EXPRESS`)
}

export function saveAliyunAppCode(req: { apiKey: string; customer?: string; enabled?: boolean }) {
  return request.put<ApiResult<ApiAccountVO>>(`${BASE}/ALIYUN_EXPRESS`, req)
}

export function deleteAliyunAppCode() {
  return request.delete<ApiResult<null>>(`${BASE}/ALIYUN_EXPRESS`)
}
