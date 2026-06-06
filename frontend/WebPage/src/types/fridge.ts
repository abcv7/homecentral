export type FridgeZone = 'REFRIGERATED' | 'FROZEN'

export type FridgeItemStatus = 'PENDING' | 'ACTIVE' | 'CONSUMED' | 'DISCARDED'

export type FridgeItemSource = 'MANUAL' | 'AI'

export type FridgeLayout = 'CLASSIC' | 'BOTTOM_FREEZER' | 'SIDE_BY_SIDE' | 'THREE_DOOR'

export interface FridgeCategoryVO {
  id: number
  name: string
  icon?: string
  color?: string
  system?: boolean
  sortOrder?: number
  createdBy?: number
  createdAt?: string
}

export interface FridgeCategoryRequest {
  name: string
  icon?: string
  color?: string
  sortOrder?: number
}

export interface FridgeItemVO {
  id: number
  name: string
  zone: FridgeZone | null
  subZone?: string | null
  categoryId?: number
  categoryName?: string
  categoryIcon?: string
  categoryColor?: string
  quantity?: number
  unit?: string
  purchaseDate?: string
  expiryDate?: string
  daysToExpiry?: number
  imageUrl?: string
  source: FridgeItemSource
  status: FridgeItemStatus
  notes?: string
  consumedAt?: string
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

export interface FridgeItemRequest {
  name: string
  zone: FridgeZone
  subZone?: string
  categoryId?: number
  categoryName?: string
  quantity?: number
  unit?: string
  purchaseDate?: string
  expiryDate?: string
  imageUrl?: string
  source?: FridgeItemSource
  notes?: string
}

export interface FridgeQuickCreateRequest {
  name: string
  categoryId?: number
  quantity?: number
  unit?: string
  source?: FridgeItemSource
  purchaseDate?: string
  expiryDate?: string
  zone?: FridgeZone | null
  subZone?: string | null
}

export interface FridgeItemMoveRequest {
  zone?: FridgeZone | null
  subZone?: string | null
}

export interface FridgeRecognizeRequest {
  imageBase64: string
  zone?: FridgeZone
}

export interface FridgeRecognizeVO {
  name: string
  suggestedCategoryId?: number
  suggestedCategoryName?: string
  suggestedZone: FridgeZone
  estimatedQuantity?: number
  estimatedUnit?: string
  estimatedExpiryDays?: number
  purchaseDate?: string
  confidence?: number
}

export interface FridgeRecognizeResult {
  items: FridgeRecognizeVO[]
  totalCount: number
}

export interface FridgeExpiringVO {
  total: number
  expired: number
  expiringSoon: number
  fresh: number
  items: FridgeItemVO[]
}

export interface FridgeTemplateVO {
  id: number
  ownerId?: number | null
  name: string
  layout: FridgeLayout
  fridgeLayers: number
  freezerLayers: number
  chillerLayers: number
  doorShelfCount: number
  default: boolean
  system: boolean
  createdAt?: string
  updatedAt?: string
}

export interface FridgeTemplateRequest {
  name: string
  layout: FridgeLayout
  fridgeLayers?: number
  freezerLayers?: number
  chillerLayers?: number
  doorShelfCount?: number
}

export interface FridgeShoppingItem {
  name: string
  categoryId?: number
  quantity?: number
  unit?: string
  source?: string
}

export interface FridgeShoppingConfirmRequest {
  items: FridgeShoppingItem[]
  partnerEmail?: string
  groupId?: number
  basketItemIds?: number[]
}

export interface FridgeShoppingHistoryItemVO {
  id?: number
  name: string
  categoryId?: number
  quantity?: number
  unit?: string
  source?: string
}

export interface FridgeShoppingHistoryVO {
  batchId: string
  purchasedAt: string
  partnerEmail?: string
  emailSent?: boolean
  items: FridgeShoppingHistoryItemVO[]
}
