package com.homecentral.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.auth.api.feign.MemberEmailClient;
import com.homecentral.auth.api.feign.MemberInfoClient;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.common.model.Result;
import com.homecentral.friend.api.enums.GroupType;
import com.homecentral.friend.api.enums.RelationshipStatus;
import com.homecentral.friend.api.dto.FriendGroupRequest;
import com.homecentral.friend.api.dto.FriendRelationshipActionRequest;
import com.homecentral.friend.api.vo.FriendGroupVO;
import com.homecentral.friend.api.vo.FriendRelationshipVO;
import com.homecentral.friend.entity.FriendGroup;
import com.homecentral.friend.entity.FriendRelationship;
import com.homecentral.friend.mapper.FriendGroupMapper;
import com.homecentral.friend.mapper.FriendRelationshipMapper;
import com.homecentral.notification.api.email.FriendAcceptEmail;
import com.homecentral.notification.api.feign.MailClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendRelationshipServiceImplTest {

    @Mock
    private FriendRelationshipMapper relMapper;

    @Mock
    private FriendGroupMapper groupMapper;

    @Mock
    private MemberEmailClient memberEmailClient;

    @Mock
    private MemberInfoClient memberInfoClient;

    @Mock
    private MailClient mailClient;

    @InjectMocks
    private FriendRelationshipServiceImpl relService;

    @InjectMocks
    private FriendGroupServiceImpl groupService;

    @Test
    void shouldCreateGroup() {
        when(groupMapper.insert(any(FriendGroup.class))).thenAnswer(inv -> {
            FriendGroup g = inv.getArgument(0);
            g.setId(10L);
            return 1;
        });
        FriendGroupRequest req = new FriendGroupRequest();
        req.setName("家人");
        req.setType(GroupType.FAMILY);
        req.setColor("#FF6B6B");
        FriendGroupVO vo = groupService.create(req, 1L);
        assertNotNull(vo);
        assertEquals(10L, vo.getId());
        assertEquals("家人", vo.getName());
        assertEquals(GroupType.FAMILY, vo.getType());
        assertEquals("#FF6B6B", vo.getColor());
        verify(groupMapper).insert(any(FriendGroup.class));
    }

    @Test
    void shouldDeleteGroupAndUnassignRelationships() {
        FriendGroup g = new FriendGroup();
        g.setId(5L);
        g.setOwnerId(1L);
        when(groupMapper.selectById(5L)).thenReturn(g);
        FriendRelationship r = new FriendRelationship();
        r.setId(1L);
        r.setGroupId(5L);
        when(relMapper.selectList(any())).thenReturn(List.of(r));
        groupService.delete(5L, 1L);
        verify(relMapper).updateById(any(FriendRelationship.class));
        verify(groupMapper).deleteById(5L);
    }

    @Test
    void shouldRejectDeletingOthersGroup() {
        FriendGroup g = new FriendGroup();
        g.setId(5L);
        g.setOwnerId(2L);
        when(groupMapper.selectById(5L)).thenReturn(g);
        assertThrows(RuntimeException.class, () -> groupService.delete(5L, 1L));
    }

    @Test
    void shouldInviteFriend() {
        FriendGroup g = new FriendGroup();
        g.setId(2L);
        g.setOwnerId(1L);
        g.setName("朋友");
        when(groupMapper.selectById(2L)).thenReturn(g);
        when(relMapper.selectOne(any())).thenReturn(null);
        when(relMapper.insert(any(FriendRelationship.class))).thenAnswer(inv -> {
            FriendRelationship r = inv.getArgument(0);
            r.setId(100L);
            return 1;
        });
        FriendRelationshipActionRequest req = new FriendRelationshipActionRequest();
        req.setGroupId(2L);
        req.setFriendUserId(2L);
        FriendRelationshipVO vo = relService.invite(1L, 2L, 2L);
        assertEquals(100L, vo.getId());
        assertEquals(RelationshipStatus.PENDING, vo.getStatus());
        assertEquals("朋友", vo.getGroupName());
    }

    @Test
    void shouldNotAllowInvitingSelf() {
        assertThrows(RuntimeException.class, () -> relService.invite(1L, 1L, 2L));
    }

    @Test
    void shouldAcceptRelationship() {
        FriendRelationship r = new FriendRelationship();
        r.setId(50L);
        r.setOwnerId(1L);
        r.setFriendUserId(2L);
        r.setStatus(RelationshipStatus.PENDING);
        when(relMapper.selectById(50L)).thenReturn(r);
        FriendRelationshipVO vo = relService.accept(50L, 2L);
        assertEquals(RelationshipStatus.ACCEPTED, vo.getStatus());
        verify(relMapper).updateById(any(FriendRelationship.class));
    }

    @Test
    void shouldNotAcceptIfNotInvitee() {
        FriendRelationship r = new FriendRelationship();
        r.setId(50L);
        r.setOwnerId(1L);
        r.setFriendUserId(2L);
        when(relMapper.selectById(50L)).thenReturn(r);
        assertThrows(RuntimeException.class, () -> relService.accept(50L, 3L));
    }

    @Test
    void shouldRejectRelationship() {
        FriendRelationship r = new FriendRelationship();
        r.setId(50L);
        r.setOwnerId(1L);
        r.setFriendUserId(2L);
        r.setStatus(RelationshipStatus.PENDING);
        when(relMapper.selectById(50L)).thenReturn(r);
        FriendRelationshipVO vo = relService.reject(50L, 2L);
        assertEquals(RelationshipStatus.REJECTED, vo.getStatus());
    }

    @Test
    void shouldUnbindAsOwner() {
        FriendRelationship r = new FriendRelationship();
        r.setId(50L);
        r.setOwnerId(1L);
        r.setFriendUserId(2L);
        when(relMapper.selectById(50L)).thenReturn(r);
        relService.unbind(50L, 1L);
        verify(relMapper).deleteById(50L);
    }

    @Test
    void shouldUnbindAsFriend() {
        FriendRelationship r = new FriendRelationship();
        r.setId(50L);
        r.setOwnerId(1L);
        r.setFriendUserId(2L);
        when(relMapper.selectById(50L)).thenReturn(r);
        relService.unbind(50L, 2L);
        verify(relMapper).deleteById(50L);
    }

    @Test
    void shouldReturnVisibleUserIds() {
        FriendRelationship r1 = new FriendRelationship();
        r1.setOwnerId(1L);
        r1.setFriendUserId(2L);
        r1.setStatus(RelationshipStatus.ACCEPTED);
        FriendRelationship r2 = new FriendRelationship();
        r2.setOwnerId(1L);
        r2.setFriendUserId(3L);
        r2.setStatus(RelationshipStatus.PENDING);
        when(relMapper.selectList(any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            LambdaQueryWrapper<FriendRelationship> w = inv.getArgument(0);
            return List.of(r1, r2).stream()
                    .filter(r -> r.getStatus() == RelationshipStatus.ACCEPTED)
                    .collect(Collectors.toList());
        });
        List<Long> visible = relService.getVisibleUserIds(1L);
        assertEquals(1, visible.size());
        assertEquals(2L, visible.get(0));
    }

    @Test
    void shouldReturnVisibleUserIdsAsFriendSide() {
        FriendRelationship r1 = new FriendRelationship();
        r1.setOwnerId(1L);
        r1.setFriendUserId(2L);
        r1.setStatus(RelationshipStatus.ACCEPTED);
        FriendRelationship r2 = new FriendRelationship();
        r2.setOwnerId(3L);
        r2.setFriendUserId(2L);
        r2.setStatus(RelationshipStatus.ACCEPTED);
        when(relMapper.selectList(any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            LambdaQueryWrapper<FriendRelationship> w = inv.getArgument(0);
            return List.of(r1, r2).stream()
                    .filter(r -> r.getStatus() == RelationshipStatus.ACCEPTED)
                    .collect(Collectors.toList());
        });
        List<Long> visible = relService.getVisibleUserIds(2L);
        assertEquals(2, visible.size());
        assertTrue(visible.contains(1L));
        assertTrue(visible.contains(3L));
    }

    @Test
    void shouldReturnGroupMemberIds() {
        FriendRelationship r = new FriendRelationship();
        r.setFriendUserId(2L);
        r.setStatus(RelationshipStatus.ACCEPTED);
        when(relMapper.selectList(any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            LambdaQueryWrapper<FriendRelationship> w = inv.getArgument(0);
            return List.of(r).stream()
                    .filter(rel -> rel.getStatus() == RelationshipStatus.ACCEPTED)
                    .collect(Collectors.toList());
        });
        List<Long> members = relService.getGroupMemberIds(7L);
        assertEquals(1, members.size());
        assertEquals(2L, members.get(0));
    }

    @Test
    void shouldSendAcceptNotificationEmailWithNickname() {
        FriendRelationship r = new FriendRelationship();
        r.setId(80L);
        r.setOwnerId(1L);
        r.setFriendUserId(2L);
        r.setStatus(RelationshipStatus.PENDING);
        FriendGroup g = new FriendGroup();
        g.setId(7L);
        g.setName("家人");
        when(relMapper.selectById(80L)).thenReturn(r);
        when(memberEmailClient.getMemberEmail(1L)).thenReturn(Result.ok("owner@qq.com"));
        MemberVO owner = new MemberVO();
        owner.setId(1L);
        owner.setNickname("老王");
        MemberVO accepter = new MemberVO();
        accepter.setId(2L);
        accepter.setNickname("小李");
        accepter.setUsername("xiaoli");
        when(memberInfoClient.getMemberById(2L)).thenReturn(Result.ok(accepter));
        when(memberInfoClient.getMemberById(1L)).thenReturn(Result.ok(owner));
        when(mailClient.sendMessage(any())).thenReturn(Result.ok(null));

        relService.accept(80L, 2L);

        verify(mailClient).sendMessage(argThat(m ->
            m instanceof FriendAcceptEmail
                && "owner@qq.com".equals(m.getTo())
                && m.getDefaultSubject().contains("好友接受")
        ));
    }
}
