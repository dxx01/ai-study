package com.example.ai.common.dict;

/**
 * 通用启用/停用字典（与库表 {@code TINYINT} 取值一致）。
 * <p>
 * 列注释写法：{@code 启用状态：0=禁用，1=启用}。多值字典从 0 递增，见 docs/standards/dictionaries.md。
 */
public final class EnableStatus {

    public static final int DISABLED = 0;
    public static final int ENABLED = 1;

    private EnableStatus() {
    }

    public static boolean isEnabled(Integer status) {
        return status != null && status == ENABLED;
    }
}
