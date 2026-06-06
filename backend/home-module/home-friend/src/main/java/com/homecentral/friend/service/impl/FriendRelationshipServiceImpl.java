package com.homecentral.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.auth.api.feign.MemberEmailClient;
import com.homecentral.common.model.Result;
import com.homecentral.friend.api.enums.RelationshipStatus;
import com.homecentral.friend.api.vo.FriendGroupVO;
import com.homecentral.friend.api.vo.FriendRelationshipVO;
import com.homecentral.friend.entity.FriendGroup;
import com.homecentral.friend.entity.FriendRelationship;
import com.homecentral.friend.mapper.FriendGroupMapper;
import com.homecentral.friend.mapper.FriendRelationshipMapper;
import com.homecentral.auth.api.feign.MemberInfoClient;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.friend.service.IFriendRelationshipService;
import com.homecentral.notification.api.email.FriendAcceptEmail;
import com.homecentral.notification.api.feign.MailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendRelationshipServiceImpl implements IFriendRelationshipService {

    private static final Logger log = LoggerFactory.getLogger(FriendRelationshipServiceImpl.class);

    private final FriendRelationshipMapper relMapper;
    private final FriendGroupMapper groupMapper;
    private final MemberEmailClient memberEmailClient;
    private final MemberInfoClient memberInfoClient;
    private final MailClient mailClient;

    public FriendRelationshipServiceImpl(FriendRelationshipMapper relMapper,
                                          FriendGroupMapper groupMapper,
                                          MemberEmailClient memberEmailClient,
                                          MemberInfoClient memberInfoClient,
                                          MailClient mailClient) {
        this.relMapper = relMapper;
        this.groupMapper = groupMapper;
        this.memberEmailClient = memberEmailClient;
        this.memberInfoClient = memberInfoClient;
        this.mailClient = mailClient;
    }

    @Override
    @Transactional
    public FriendRelationshipVO invite(Long ownerId, Long friendUserId, Long groupId) {
        if (ownerId == null || friendUserId == null) {
            throw new RuntimeException("参数不完整");
        }
        if (ownerId.equals(friendUserId)) {
            throw new RuntimeException("不能邀请自己");
        }
        if (groupId == null) {
            throw new RuntimeException("分组 ID 必填");
        }
        FriendGroup group = groupMapper.selectById(groupId);
        if (group == null || !ownerId.equals(group.getOwnerId())) {
            throw new RuntimeException("分组不存在或无权使用");
        }

        LambdaQueryWrapper<FriendRelationship> existing = new LambdaQueryWrapper<>();
        existing.eq(FriendRelationship::getOwnerId, ownerId)
                .eq(FriendRelationship::getFriendUserId, friendUserId);
        FriendRelationship row = relMapper.selectOne(existing);
        if (row == null) {
            row = new FriendRelationship();
            row.setOwnerId(ownerId);
            row.setFriendUserId(friendUserId);
            row.setGroupId(groupId);
            row.setStatus(RelationshipStatus.PENDING);
            row.setInviteEmailSent(Boolean.FALSE);
            row.setCreatedAt(OffsetDateTime.now());
            relMapper.insert(row);
        } else {
            row.setGroupId(groupId);
            row.setStatus(RelationshipStatus.PENDING);
            row.setRespondedAt(null);
            relMapper.updateById(row);
        }
        log.info("[friend-invite] owner={} friend={} group={} relId={}", ownerId, friendUserId, groupId, row.getId());
        return toVO(row, group);
    }

    @Override
    @Transactional
    public FriendRelationshipVO accept(Long relationshipId, Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("需要登录");
        }
        FriendRelationship row = relMapper.selectById(relationshipId);
        if (row == null) {
            throw new RuntimeException("邀请不存在");
        }
        if (!currentUserId.equals(row.getFriendUserId())) {
            throw new RuntimeException("无权接受此邀请");
        }
        if (row.getStatus() == RelationshipStatus.ACCEPTED) {
            return toVO(row, lookupGroup(row.getGroupId()));
        }
        row.setStatus(RelationshipStatus.ACCEPTED);
        row.setRespondedAt(OffsetDateTime.now());
        relMapper.updateById(row);
        log.info("[friend-accept] relId={} owner={} friend={}", relationshipId, row.getOwnerId(), row.getFriendUserId());
        sendAcceptNotification(row, lookupGroup(row.getGroupId()));
        return toVO(row, lookupGroup(row.getGroupId()));
    }

    private void sendAcceptNotification(FriendRelationship row, FriendGroup group) {
        try {
            String ownerEmail = fetchEmail(row.getOwnerId());
            if (ownerEmail == null || ownerEmail.isBlank()) {
                log.info("[friend-accept] owner={} 邮箱为空，跳过通知", row.getOwnerId());
                return;
            }
            String groupName = group != null ? group.getName() : "默认分组";
            MemberVO accepter = fetchMember(row.getFriendUserId());
            String accepterNickname = accepter == null || accepter.getNickname() == null || accepter.getNickname().isBlank()
                ? "用户 #" + row.getFriendUserId() : accepter.getNickname();
            String accepterUsername = accepter == null || accepter.getUsername() == null
                ? String.valueOf(row.getFriendUserId()) : accepter.getUsername();
            String ownerNickname = fetchMemberNickname(row.getOwnerId());

            FriendAcceptEmail email = new FriendAcceptEmail(
                ownerEmail, groupName, ownerNickname, accepterNickname, accepterUsername);
            mailClient.sendMessage(email);
            markInviteEmailSent(row.getId());
        } catch (Exception e) {
            log.warn("[friend-accept] 通知发送失败 relId={} ({})", row.getId(), e.getMessage());
        }
    }

    private MemberVO fetchMember(Long userId) {
        try {
            Result<MemberVO> r = memberInfoClient.getMemberById(userId);
            if (r != null && r.isSuccess()) {
                return r.getData();
            }
        } catch (Exception e) {
            log.warn("[friend-accept] 查询用户信息失败 userId={} ({})", userId, e.getMessage());
        }
        return null;
    }

    private String fetchMemberNickname(Long userId) {
        MemberVO m = fetchMember(userId);
        if (m == null) {
            return "您";
        }
        if (m.getNickname() != null && !m.getNickname().isBlank()) {
            return m.getNickname();
        }
        return m.getUsername() == null ? "您" : m.getUsername();
    }

    private String fetchEmail(Long userId) {
        try {
            Result<String> r = memberEmailClient.getMemberEmail(userId);
            if (r != null && r.isSuccess() && r.getData() != null) {
                return r.getData();
            }
        } catch (Exception e) {
            log.warn("[friend-accept] 查询邮箱失败 userId={} ({})", userId, e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public FriendRelationshipVO reject(Long relationshipId, Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("需要登录");
        }
        FriendRelationship row = relMapper.selectById(relationshipId);
        if (row == null) {
            throw new RuntimeException("邀请不存在");
        }
        if (!currentUserId.equals(row.getFriendUserId())) {
            throw new RuntimeException("无权拒绝此邀请");
        }
        row.setStatus(RelationshipStatus.REJECTED);
        row.setRespondedAt(OffsetDateTime.now());
        relMapper.updateById(row);
        return toVO(row, lookupGroup(row.getGroupId()));
    }

    @Override
    @Transactional
    public void unbind(Long relationshipId, Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("需要登录");
        }
        FriendRelationship row = relMapper.selectById(relationshipId);
        if (row == null) {
            return;
        }
        if (!currentUserId.equals(row.getOwnerId()) && !currentUserId.equals(row.getFriendUserId())) {
            throw new RuntimeException("无权解除此关系");
        }
        relMapper.deleteById(relationshipId);
    }

    @Override
    @Transactional
    public void block(Long relationshipId, Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("需要登录");
        }
        FriendRelationship row = relMapper.selectById(relationshipId);
        if (row == null) {
            throw new RuntimeException("关系不存在");
        }
        if (!currentUserId.equals(row.getOwnerId())) {
            throw new RuntimeException("只有 owner 可 block");
        }
        row.setStatus(RelationshipStatus.BLOCKED);
        row.setRespondedAt(OffsetDateTime.now());
        relMapper.updateById(row);
    }

    @Override
    public List<FriendRelationshipVO> listMine(Long ownerId, RelationshipStatus status) {
        if (ownerId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FriendRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelationship::getOwnerId, ownerId)
                .orderByDesc(FriendRelationship::getCreatedAt);
        if (status != null) {
            wrapper.eq(FriendRelationship::getStatus, status);
        }
        List<FriendRelationship> rows = relMapper.selectList(wrapper);
        return mapAndHydrate(rows);
    }

    @Override
    public List<FriendRelationshipVO> listIncoming(Long currentUserId) {
        if (currentUserId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FriendRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelationship::getFriendUserId, currentUserId)
                .eq(FriendRelationship::getStatus, RelationshipStatus.PENDING)
                .orderByDesc(FriendRelationship::getCreatedAt);
        List<FriendRelationship> rows = relMapper.selectList(wrapper);
        return mapAndHydrate(rows);
    }

    @Override
    public FriendRelationshipVO getById(Long relationshipId) {
        FriendRelationship row = relMapper.selectById(relationshipId);
        if (row == null) {
            return null;
        }
        return toVO(row, lookupGroup(row.getGroupId()));
    }

    public List<Long> getVisibleUserIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FriendRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(FriendRelationship::getOwnerId, userId)
                .or().eq(FriendRelationship::getFriendUserId, userId))
                .eq(FriendRelationship::getStatus, RelationshipStatus.ACCEPTED);
        List<FriendRelationship> rows = relMapper.selectList(wrapper);
        List<Long> result = new ArrayList<>();
        for (FriendRelationship r : rows) {
            Long other = r.getOwnerId().equals(userId) ? r.getFriendUserId() : r.getOwnerId();
            if (other != null) {
                result.add(other);
            }
        }
        return result;
    }

    public List<Long> getGroupMemberIds(Long groupId) {
        if (groupId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FriendRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelationship::getGroupId, groupId)
                .eq(FriendRelationship::getStatus, RelationshipStatus.ACCEPTED);
        List<FriendRelationship> rows = relMapper.selectList(wrapper);
        List<Long> result = new ArrayList<>();
        for (FriendRelationship r : rows) {
            if (r.getFriendUserId() != null) {
                result.add(r.getFriendUserId());
            }
        }
        return result;
    }

    public List<Long> getUserGroupIds(Long ownerId) {
        if (ownerId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FriendGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendGroup::getOwnerId, ownerId);
        List<FriendGroup> rows = groupMapper.selectList(wrapper);
        List<Long> result = new ArrayList<>();
        for (FriendGroup g : rows) {
            result.add(g.getId());
        }
        return result;
    }

    public boolean hasAcceptedRelationship(Long userId, Long otherUserId) {
        if (userId == null || otherUserId == null) {
            return false;
        }
        LambdaQueryWrapper<FriendRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelationship::getOwnerId, userId)
                .eq(FriendRelationship::getFriendUserId, otherUserId)
                .eq(FriendRelationship::getStatus, RelationshipStatus.ACCEPTED);
        return relMapper.selectCount(wrapper) > 0;
    }

    public void markInviteEmailSent(Long relationshipId) {
        FriendRelationship row = relMapper.selectById(relationshipId);
        if (row == null) {
            return;
        }
        row.setInviteEmailSent(Boolean.TRUE);
        row.setInviteEmailSentAt(OffsetDateTime.now());
        relMapper.updateById(row);
    }

    private FriendGroup lookupGroup(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return groupMapper.selectById(groupId);
    }

    private List<FriendRelationshipVO> mapAndHydrate(List<FriendRelationship> rows) {
        List<FriendRelationshipVO> result = new ArrayList<>();
        for (FriendRelationship r : rows) {
            result.add(toVO(r, lookupGroup(r.getGroupId())));
        }
        return result;
    }

    private FriendRelationshipVO toVO(FriendRelationship r, FriendGroup g) {
        FriendRelationshipVO vo = new FriendRelationshipVO();
        vo.setId(r.getId());
        vo.setOwnerId(r.getOwnerId());
        vo.setFriendUserId(r.getFriendUserId());
        vo.setStatus(r.getStatus());
        vo.setInviteEmailSent(r.getInviteEmailSent());
        vo.setInviteEmailSentAt(r.getInviteEmailSentAt());
        vo.setCreatedAt(r.getCreatedAt());
        vo.setRespondedAt(r.getRespondedAt());
        if (g != null) {
            vo.setGroupId(g.getId());
            vo.setGroupName(g.getName());
            vo.setGroupType(g.getType());
            vo.setGroupColor(g.getColor());
        }
        return vo;
    }
}
