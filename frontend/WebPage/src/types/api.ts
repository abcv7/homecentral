export interface ApiResult<T> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  phone?: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface LoginResponse {
  userId: number
  username: string
  accessToken: string
  refreshToken: string
}

export interface MemberProfileVO {
  id: number
  username: string
  nickname: string | null
  email: string | null
  phone: string | null
  role: string | null
  enabled: boolean
  createdAt: string
}

export type VerificationPurpose = 'EMAIL_CHANGE' | 'PASSWORD_CHANGE'

export interface SendCodeRequest {
  purpose: VerificationPurpose
  email?: string
}

export interface VerifyEmailChangeRequest {
  purpose: 'EMAIL_CHANGE'
  newEmail: string
  code: string
}

export interface ChangePasswordRequest {
  code: string
  newPassword: string
}

export interface UpdateProfileRequest {
  nickname?: string
  phone?: string
}

export type ParcelStatus = 'PENDING_PICKUP' | 'PICKED_UP' | 'RECEIVED'

export interface PageVO<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ParcelVO {
  id: number
  courierCompany: string
  trackingNumber: string
  pickupCode: string | null
  remark: string | null
  attachmentUrl: string | null
  status: ParcelStatus
  createdBy: number | null
  createdAt: string
  source: string
  ownerName: string | null
  daysAtStation: number
  phoneTail: string | null
  productName: string | null
  sharedCount: number | null
}

export interface SharedParcelUserVO {
  userId: number
  nickname: string | null
  email: string | null
  phone: string | null
  sharedAt: string | null
}

export interface ParcelCreateRequest {
  courierCompany: string
  trackingNumber: string
  pickupCode?: string
  remark?: string
  attachmentUrl?: string
  ownerName?: string
  arrivedDate?: string
  phoneTail?: string
  productName?: string
}

export interface ParcelUpdateRequest {
  courierCompany?: string
  trackingNumber?: string
  pickupCode?: string
  remark?: string
  attachmentUrl?: string
  ownerName?: string
  arrivedDate?: string
  phoneTail?: string
  productName?: string
}

export interface ShoppingMemoVO {
  id: number
  itemName: string
  note: string | null
  purchased: boolean
}

export interface ShoppingMemoCreateRequest {
  itemName: string
  note?: string
}

export interface AnniversaryVO {
  id: number
  title: string
  eventDate: string
  remindBeforeDays: number
}

export interface AnniversaryCreateRequest {
  title: string
  eventDate: string
  remindBeforeDays?: number
}

export interface ReminderRuleVO {
  id: number
  title: string
  content: string | null
  cronExpression: string
  enabled: boolean
}

export interface ReminderRuleCreateRequest {
  title: string
  content?: string
  cronExpression: string
  enabled?: boolean
}

export interface FileUploadVO {
  fileId: string
  objectKey: string
  accessUrl: string
}

export interface TrackingVO {
  trackingNumber: string
  courierCode: string
  courierName: string | null
  state: string
  stateLabel: string
  message: string
  tracks: TrackItem[]
}

export interface TrackItem {
  time: string
  context: string
}

export interface BatchResult<T> {
  totalCount: number
  successCount: number
  failureCount: number
  successItems: T[]
  failureItems: {
    index: number
    item: string
    reason: string
  }[]
}

export interface NotificationVO {
  id: number
  title: string
  content: string | null
  sourceType: string | null
  sourceId: string | null
  read: boolean
  notifyTime: string
  userId: number
  createdAt: string
}
