package com.example.ai.knowledge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 只读校验 sys_app（与 auth 共用库）。
 *
 * @author 邓鑫鑫
 * @since 0.8.0
 */
@Mapper
public interface SysAppReadMapper {

    /**
     * @param code 应用标识
     * @return 存在且启用时为 1
     */
    @Select("""
            SELECT 1 FROM sys_app
            WHERE code = #{code} AND status = 1
            LIMIT 1
            """)
    Integer selectEnabledFlag(@Param("code") String code);
}
