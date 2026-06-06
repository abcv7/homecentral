import request from './request'
import type { ApiResult, FileUploadVO } from '../types/api'

export function uploadFile(file: File, businessType?: string, businessId?: string) {
  const form = new FormData()
  form.append('file', file)
  if (businessType) form.append('businessType', businessType)
  if (businessId) form.append('businessId', businessId)
  return request.post<ApiResult<FileUploadVO>>('/file/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
