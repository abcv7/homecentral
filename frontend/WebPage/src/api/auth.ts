import request from './request'
import type {
  ApiResult,
  LoginRequest,
  RegisterRequest,
  LoginResponse,
  MemberProfileVO,
  SendCodeRequest,
  VerifyEmailChangeRequest,
  ChangePasswordRequest,
  UpdateProfileRequest,
} from '../types/api'

export function login(data: LoginRequest) {
  return request.post<ApiResult<LoginResponse>>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<ApiResult<LoginResponse>>('/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return request.post<ApiResult<LoginResponse>>('/auth/refresh', { refreshToken })
}

export function getMe() {
  return request.get<ApiResult<MemberProfileVO>>('/auth/me')
}

export function updateProfile(data: UpdateProfileRequest) {
  return request.put<ApiResult<MemberProfileVO>>('/auth/me/profile', data)
}

export function sendEmailCode(newEmail: string) {
  const payload: SendCodeRequest = { purpose: 'EMAIL_CHANGE', email: newEmail }
  return request.post<ApiResult<void>>('/auth/me/email/code', payload)
}

export function confirmEmailChange(data: VerifyEmailChangeRequest) {
  return request.put<ApiResult<MemberProfileVO>>('/auth/me/email', {
    purpose: 'EMAIL_CHANGE',
    email: data.newEmail,
    code: data.code,
  })
}

export function sendPasswordCode() {
  const payload: SendCodeRequest = { purpose: 'PASSWORD_CHANGE' }
  return request.post<ApiResult<void>>('/auth/me/password/code', payload)
}

export function changePassword(data: ChangePasswordRequest) {
  return request.put<ApiResult<void>>('/auth/me/password', data)
}
