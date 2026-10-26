package com.example.ai.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai.auth.domain.SysAppEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 平台应用 Mapper。
 *
 * @author 邓鑫鑫
 * @since 0.8.0
 */
@Mapper
public interface SysAppMapper extends BaseMapper<SysAppEntity> {

    /**
     * @return 已启用应用，按排序
     */
    @Select("""
            SELECT * FROM sys_app
            WHERE status = 1
            ORDER BY sort_order ASC, id ASC
            """)
    List<SysAppEntity> selectEnabled();

    /**
     * @param code 应用标识
     * @return 已启用应用
     */
    @Select("""
            SELECT * FROM sys_app
            WHERE code = #{code} AND status = 1
            LIMIT 1
            """)
    SysAppEntity selectEnabledByCode(@Param("code") String code);
}
