package com.example.ai.auth.service;

import com.example.ai.auth.domain.SysMenuEntity;
import com.example.ai.auth.mapper.SysMenuMapper;
import com.example.ai.common.api.dto.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单树组装。
 *
 * @author 邓鑫鑫
 * @since 0.7.0
 */
@Service
public class SysMenuService {

    private static final Logger log = LoggerFactory.getLogger(SysMenuService.class);

    private static final Comparator<MenuItem> BY_SORT = Comparator
            .comparingInt((MenuItem item) -> item.sortOrder() == null ? 0 : item.sortOrder())
            .thenComparing(MenuItem::id);

    private final SysMenuMapper menuMapper;

    public SysMenuService(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * @param userId 用户主键
     * @return 用户可见菜单树
     */
    public List<MenuItem> menusForUser(long userId) {
        try {
            return build(menuMapper.selectVisibleByUser(userId));
        } catch (DataAccessException ex) {
            log.debug("菜单表不可用，返回空菜单: userId={}", userId, ex);
            return List.of();
        }
    }

    /**
     * @return 全部可见菜单树（管理端）
     */
    public List<MenuItem> allMenus() {
        try {
            return build(menuMapper.selectAll());
        } catch (DataAccessException ex) {
            log.debug("菜单表不可用，返回空菜单", ex);
            return List.of();
        }
    }

    private static List<MenuItem> build(List<SysMenuEntity> rows) {
        Map<Long, MenuItem> nodes = new LinkedHashMap<>();
        for (SysMenuEntity row : rows) {
            nodes.put(row.getId(), new MenuItem(
                    row.getId(),
                    row.getParentId(),
                    row.getName(),
                    row.getPath(),
                    row.getIcon(),
                    row.getPermissionCode(),
                    row.getSortOrder(),
                    row.getVisible(),
                    row.getStatus(),
                    new ArrayList<>()
            ));
        }
        List<MenuItem> roots = new ArrayList<>();
        for (SysMenuEntity row : rows) {
            MenuItem node = nodes.get(row.getId());
            MenuItem parent = nodes.get(row.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        sort(roots);
        return roots;
    }

    private static void sort(List<MenuItem> items) {
        items.sort(BY_SORT);
        for (MenuItem item : items) {
            if (!item.children().isEmpty()) {
                sort(item.children());
            }
        }
    }
}
