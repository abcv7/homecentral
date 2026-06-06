package com.homecentral.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.friend.api.dto.FriendGroupRequest;
import com.homecentral.friend.api.enums.GroupType;
import com.homecentral.friend.api.vo.FriendGroupVO;
import com.homecentral.friend.entity.FriendGroup;
import com.homecentral.friend.entity.FriendRelationship;
import com.homecentral.friend.mapper.FriendGroupMapper;
import com.homecentral.friend.mapper.FriendRelationshipMapper;
import com.homecentral.friend.service.IFriendGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendGroupServiceImpl implements IFriendGroupService {

    private final FriendGroupMapper groupMapper;
    private final FriendRelationshipMapper relMapper;

    public FriendGroupServiceImpl(FriendGroupMapper groupMapper, FriendRelationshipMapper relMapper) {
        this.groupMapper = groupMapper;
        this.relMapper = relMapper;
    }

    @Override
    @Transactional
    public FriendGroupVO create(FriendGroupRequest request, Long ownerId) {
        if (ownerId == null) {
            throw new RuntimeException("需要登录");
        }
        FriendGroup g = new FriendGroup();
        g.setOwnerId(ownerId);
        g.setName(request.getName());
        g.setType(request.getType() == null ? GroupType.FRIEND : request.getType());
        g.setColor(request.getColor());
        g.setCreatedAt(OffsetDateTime.now());
        groupMapper.insert(g);
        return toVO(g, 0);
    }

    @Override
    @Transactional
    public FriendGroupVO update(Long groupId, FriendGroupRequest request, Long ownerId) {
        FriendGroup g = mustOwn(groupId, ownerId);
        g.setName(request.getName());
        if (request.getType() != null) {
            g.setType(request.getType());
        }
        g.setColor(request.getColor());
        groupMapper.updateById(g);
        return toVO(g, countMembers(groupId));
    }

    @Override
    @Transactional
    public void delete(Long groupId, Long ownerId) {
        FriendGroup g = mustOwn(groupId, ownerId);
        LambdaQueryWrapper<FriendRelationship> rel = new LambdaQueryWrapper<>();
        rel.eq(FriendRelationship::getGroupId, groupId);
        List<FriendRelationship> rels = relMapper.selectList(rel);
        for (FriendRelationship r : rels) {
            r.setGroupId(null);
            relMapper.updateById(r);
        }
        groupMapper.deleteById(g.getId());
    }

    @Override
    public List<FriendGroupVO> listMine(Long ownerId) {
        if (ownerId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FriendGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendGroup::getOwnerId, ownerId).orderByAsc(FriendGroup::getId);
        List<FriendGroup> rows = groupMapper.selectList(wrapper);
        List<FriendGroupVO> result = new ArrayList<>();
        for (FriendGroup g : rows) {
            result.add(toVO(g, countMembers(g.getId())));
        }
        return result;
    }

    @Override
    public FriendGroupVO getById(Long groupId, Long ownerId) {
        FriendGroup g = mustOwn(groupId, ownerId);
        return toVO(g, countMembers(groupId));
    }

    private FriendGroup mustOwn(Long groupId, Long ownerId) {
        if (ownerId == null) {
            throw new RuntimeException("需要登录");
        }
        FriendGroup g = groupMapper.selectById(groupId);
        if (g == null) {
            throw new RuntimeException("分组不存在");
        }
        if (!ownerId.equals(g.getOwnerId())) {
            throw new RuntimeException("无权操作他人分组");
        }
        return g;
    }

    private int countMembers(Long groupId) {
        LambdaQueryWrapper<FriendRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRelationship::getGroupId, groupId);
        return Math.toIntExact(relMapper.selectCount(wrapper));
    }

    private FriendGroupVO toVO(FriendGroup g, int memberCount) {
        FriendGroupVO vo = new FriendGroupVO();
        vo.setId(g.getId());
        vo.setOwnerId(g.getOwnerId());
        vo.setName(g.getName());
        vo.setType(g.getType());
        vo.setColor(g.getColor());
        vo.setMemberCount(memberCount);
        vo.setCreatedAt(g.getCreatedAt());
        return vo;
    }
}
