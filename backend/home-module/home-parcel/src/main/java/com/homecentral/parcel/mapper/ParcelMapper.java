package com.homecentral.parcel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homecentral.parcel.entity.Parcel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ParcelMapper extends BaseMapper<Parcel> {

    @Update("UPDATE home_parcel.parcel SET api_tracking_raw = #{raw}::jsonb, updated_at = NOW() WHERE id = #{id} AND deleted = false")
    int updateApiTrackingRaw(@Param("id") Long id, @Param("raw") String raw);
}
