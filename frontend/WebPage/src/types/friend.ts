export type GroupType = 'FRIEND' | 'COUPLE' | 'FAMILY' | 'CUSTOM'
export type RelationshipStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'BLOCKED'

export interface FriendGroupVO {
  id: number
  ownerId: number
  name: string
  type: GroupType
  color: string
  memberCount?: number
  createdAt: string
}

export interface FriendRelationshipVO {
  id: number
  ownerId: number
  friendUserId: number
  friendName?: string | null
  groupId?: number | null
  groupName?: string | null
  groupType?: GroupType | null
  groupColor?: string | null
  status: RelationshipStatus
  inviteEmailSent?: boolean
  inviteEmailSentAt?: string | null
  ownerName?: string | null
  createdAt: string
  respondedAt?: string | null
}

export interface FriendGroupRequest {
  name: string
  type: GroupType
  color?: string
}

export interface FriendRelationshipActionRequest {
  friendUserId?: number
  groupId?: number
  relationshipId?: number
}

export const GROUP_TYPE_META: Record<GroupType, { label: string; emoji: string; defaultColor: string }> = {
  FRIEND:  { label: '朋友', emoji: '👋', defaultColor: '#909399' },
  COUPLE:  { label: '情侣', emoji: '💞', defaultColor: '#F56C6C' },
  FAMILY:  { label: '家人', emoji: '🏠', defaultColor: '#E6A23C' },
  CUSTOM:  { label: '自定义', emoji: '⭐', defaultColor: '#67C23A' },
}

export const RELATIONSHIP_STATUS_META: Record<RelationshipStatus, { label: string; type: 'default' | 'info' | 'success' | 'warning' | 'error' }> = {
  PENDING:  { label: '待对方接受', type: 'warning' },
  ACCEPTED: { label: '已接受',     type: 'success' },
  REJECTED: { label: '已拒绝',     type: 'default' },
  BLOCKED:  { label: '已拉黑',     type: 'error' },
}
