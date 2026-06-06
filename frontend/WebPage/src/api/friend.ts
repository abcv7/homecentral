import request from './request'
import type { ApiResult } from '../types/api'
import type {
  FriendGroupVO,
  FriendRelationshipVO,
  FriendGroupRequest,
  FriendRelationshipActionRequest,
  RelationshipStatus,
} from '../types/friend'

export function listMyGroups() {
  return request.get<ApiResult<FriendGroupVO[]>>('/friend/groups')
}

export function createGroup(req: FriendGroupRequest) {
  return request.post<ApiResult<FriendGroupVO>>('/friend/groups', req)
}

export function updateGroup(id: number, req: FriendGroupRequest) {
  return request.put<ApiResult<FriendGroupVO>>(`/friend/groups/${id}`, req)
}

export function deleteGroup(id: number) {
  return request.delete<ApiResult<null>>(`/friend/groups/${id}`)
}

export function inviteFriend(req: FriendRelationshipActionRequest) {
  return request.post<ApiResult<FriendRelationshipVO>>('/friend/relationships/invite', req)
}

export function acceptRelationship(req: FriendRelationshipActionRequest) {
  return request.post<ApiResult<FriendRelationshipVO>>('/friend/relationships/accept', req)
}

export function rejectRelationship(req: FriendRelationshipActionRequest) {
  return request.post<ApiResult<FriendRelationshipVO>>('/friend/relationships/reject', req)
}

export function unbindRelationship(id: number) {
  return request.delete<ApiResult<null>>(`/friend/relationships/${id}`)
}

export function blockRelationship(id: number) {
  return request.post<ApiResult<null>>(`/friend/relationships/${id}/block`)
}

export function listMyRelationships(status?: RelationshipStatus) {
  return request.get<ApiResult<FriendRelationshipVO[]>>('/friend/relationships', { params: { status } })
}

export function listIncomingRelationships() {
  return request.get<ApiResult<FriendRelationshipVO[]>>('/friend/relationships/incoming')
}
