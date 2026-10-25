package com.lycorder.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lycorder.common.constant.SystemConstants;
import com.lycorder.common.enums.StatusEnum;
import com.lycorder.common.model.KeyValue;
import com.lycorder.common.model.Option;
import com.lycorder.security.util.SecurityUtils;
import com.lycorder.system.converter.SysMenuConverter;
import com.lycorder.system.enums.MenuTypeEnum;
import com.lycorder.system.mapper.SysMenuMapper;
import com.lycorder.system.model.entity.SysMenu;
import com.lycorder.system.model.form.MenuForm;
import com.lycorder.system.model.query.MenuQuery;
import com.lycorder.system.model.vo.MenuVO;
import com.lycorder.system.model.vo.RouteVO;
import com.lycorder.system.service.SysMenuService;
import com.lycorder.system.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* 菜单业务实现，负责菜单树构建、路由生成及菜单维护。
*
* @author LYCorder
* @description 针对表【sys_menu(系统菜单表)】的数据库操作Service实现
* @createDate 2026-01-23 19:26:46
*/
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService {

    private final SysMenuConverter sysMenuConverter;
    private final SysRoleMenuService sysRoleMenuService;
    private final ObjectMapper objectMapper;



    /**
     * 查询菜单树列表。
     *
     * @param queryParams 菜单查询参数
     * @return 菜单树列表
     */
    @Override
    public List<MenuVO> listMenus(MenuQuery queryParams) {
        List<SysMenu> menus = this.list(new LambdaQueryWrapper<SysMenu>()
                .like(StrUtil.isNotBlank(queryParams.getKeywords()), SysMenu::getName, queryParams.getKeywords())
                .orderByAsc(SysMenu::getSort)
        );
        // 获取所有菜单ID
        Set<Long> menuIds = menus.stream()
                .map(SysMenu::getId)
                .collect(Collectors.toSet());

        // 获取所有父级ID
        Set<Long> parentIds = menus.stream()
                .map(SysMenu::getParentId)
                .collect(Collectors.toSet());

        // 获取根节点ID（递归的起点），即父节点ID中不包含在部门ID中的节点，注意这里不能拿顶级菜单 O 作为根节点，因为菜单筛选的时候 O 会被过滤掉
        List<Long> rootIds = parentIds.stream()
                .filter(id -> !menuIds.contains(id))
                .toList();

        // 使用递归函数来构建菜单树
        return rootIds.stream()
                .flatMap(rootId -> buildMenuTree(rootId, menus).stream())
                .collect(Collectors.toList());
    }
    /**
     * 递归生成菜单列表
     *
     * @param parentId 父级ID
     * @param menuList 菜单列表
     * @return 菜单列表
     */
    private List<MenuVO> buildMenuTree(Long parentId, List<SysMenu> menuList) {
        return CollectionUtil.emptyIfNull(menuList)
                .stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .map(entity -> {
                    MenuVO menuVo = sysMenuConverter.toVo(entity);
                    List<MenuVO> children = buildMenuTree(entity.getId(), menuList);
                    menuVo.setChildren(children);
                    return menuVo;
                }).toList();
    }

    /**
     * 获取菜单下拉选项。
     *
     * @param onlyParent 是否只查询父级菜单
     * @return 菜单下拉选项
     */
    @Override
    public List<Option<Long>> listMenuOptions(boolean onlyParent) {
        List<SysMenu> menuList = this.list(new LambdaQueryWrapper<SysMenu>()
                .in(onlyParent, SysMenu::getType, MenuTypeEnum.CATALOG.getValue(), MenuTypeEnum.MENU.getValue())
                .orderByAsc(SysMenu::getSort)
        );
        return buildMenuOptions(SystemConstants.ROOT_NODE_ID, menuList);
    }


    /**
     * 递归生成菜单下拉层级列表
     *
     * @param parentId 父级ID
     * @param menuList 菜单列表
     * @return 菜单下拉列表
     */
    private List<Option<Long>> buildMenuOptions(Long parentId, List<SysMenu> menuList) {
        List<Option<Long>> menuOptions = new ArrayList<>();

        for (SysMenu menu : menuList) {
            if (menu.getParentId().equals(parentId)) {
                Option<Long> option = new Option<>(menu.getId(), menu.getName());
                List<Option<Long>> subMenuOptions = buildMenuOptions(menu.getId(), menuList);
                if (!subMenuOptions.isEmpty()) {
                    option.setChildren(subMenuOptions);
                }
                menuOptions.add(option);
            }
        }

        return menuOptions;
    }

    /**
     * 获取当前用户菜单列表
     *
     * @return 菜单列表
     */
    @Override
    public List<RouteVO> listCurrentUserRoutes() {
        Set<String> roleCodes = SecurityUtils.getRoles();
        if (CollectionUtil.isEmpty(roleCodes)) {
            return Collections.emptyList();
        }
        List<SysMenu> menuList;
        if (SecurityUtils.isRoot()) {
            // 超级管理员获取所有菜单
            menuList = this.list(new LambdaQueryWrapper<SysMenu>()
                    .ne(SysMenu::getType, MenuTypeEnum.BUTTON.getValue())
                    .orderByAsc(SysMenu::getSort)
            );
        } else {
            // 普通用户：通过角色获取菜单（权限控制已过滤）
            menuList = this.baseMapper.getMenusByRoleCodes(roleCodes);

            // 双重保障：动态查询"平台管理"目录，过滤其子菜单
            // 通过路由路径识别平台管理目录，避免硬编码
            SysMenu platformMenu = this.getOne(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getRoutePath, "/platform")
                    .eq(SysMenu::getParentId, SystemConstants.ROOT_NODE_ID)
                    .eq(SysMenu::getType, MenuTypeEnum.CATALOG.getValue())
                    .last("LIMIT 1")
            );
            if (platformMenu != null) {
                final Long platformMenuId = platformMenu.getId();
                menuList = menuList.stream()
                        .filter(menu -> {
                            String treePath = menu.getTreePath();
                            // 排除平台管理目录及其子菜单
                            // treePath 格式：0,1 或 0,1,110 等
                            return treePath == null ||
                                    (!treePath.startsWith("0," + platformMenuId + ",") &&
                                            !treePath.equals("0," + platformMenuId));
                        })
                        .collect(Collectors.toList());
            }
        }
        return buildRoutes(SystemConstants.ROOT_NODE_ID, menuList);
    }


    /**
     * 递归生成菜单路由层级列表
     *
     * @param parentId 父级ID
     * @param menuList 菜单列表
     * @return 路由层级列表
     */
    private List<RouteVO> buildRoutes(Long parentId, List<SysMenu> menuList) {
        List<RouteVO> routeList = new ArrayList<>();

        for (SysMenu menu : menuList) {
            if (menu.getParentId().equals(parentId)) {
                RouteVO routeVo = toRouteVo(menu);
                List<RouteVO> children = buildRoutes(menu.getId(), menuList);
                if (!children.isEmpty()) {
                    routeVo.setChildren(children);
                }
                routeList.add(routeVo);
            }
        }

        return routeList;
    }

    /**
     * 根据RouteBO创建RouteVo
     */
    private RouteVO toRouteVo(SysMenu menu) {
        RouteVO routeVo = new RouteVO();
        String routePath = menu.getRoutePath();
        boolean externalLink = StrUtil.startWithAny(routePath, "http://", "https://");
        // 获取路由名称
        String routeName = menu.getRouteName();
        if (StrUtil.isBlank(routeName)) {
            // 外链不做驼峰转换，使用唯一占位，避免 http:// 被解析异常
            routeName = externalLink
                    ? "ext-" + menu.getId()
                    : StringUtils.capitalize(StrUtil.toCamelCase(routePath, '-'));
        }
        // 根据name路由跳转 this.$router.push({name:xxx})
        routeVo.setName(routeName);
        // 根据path路由跳转 this.$router.push({path:xxx})
        routeVo.setPath(routePath);
        routeVo.setRedirect(menu.getRedirect());
        // 外链无组件
        routeVo.setComponent(externalLink ? null : menu.getComponent());
        RouteVO.Meta meta = new RouteVO.Meta();
        meta.setTitle(menu.getName());
        meta.setIcon(menu.getIcon());
        meta.setHidden(StatusEnum.DISABLE.getValue().equals(menu.getVisible()));
        // 【菜单】是否开启页面缓存
        if (MenuTypeEnum.MENU.getValue().equals(menu.getType())
                && ObjectUtil.equals(menu.getKeepAlive(), 1)) {
            meta.setKeepAlive(true);
        }
        meta.setAlwaysShow(ObjectUtil.equals(menu.getAlwaysShow(), 1));
        String paramsJson = menu.getParams();
        // 将 JSON 字符串转换为 Map<String, String>
        if (StrUtil.isNotBlank(paramsJson)) {
            try {
                Map<String, String> paramMap = objectMapper.readValue(paramsJson, new TypeReference<>() {
                });
                meta.setParams(paramMap);
            } catch (Exception e) {
                throw new RuntimeException("解析参数失败", e);
            }
        }
        routeVo.setMeta(meta);
        return routeVo;
    }

    /**
     * 获取菜单编辑表单数据，并将路由参数JSON转换为键值对列表。
     *
     * @param id 菜单ID
     * @return 菜单表单数据
     */
    @Override
    public MenuForm getMenuForm(Long id) {

        SysMenu entity = this.getById(id);
        Assert.isTrue(entity != null, "菜单不存在");
        MenuForm formData = sysMenuConverter.toForm(entity);
        // 路由参数字符串 {"id":"1","name":"张三"} 转换为 [{key:"id", value:"1"}, {key:"name", value:"张三"}]
        String params = entity.getParams();
        if (StrUtil.isNotBlank(params)) {
            try {
                // 解析 JSON 字符串为 Map<String, String>
                Map<String, String> paramMap = objectMapper.readValue(params, new TypeReference<>() {
                });

                // 转换为 List<KeyValue> 格式 [{key:"id", value:"1"}, {key:"name", value:"张三"}]
                List<KeyValue> transformedList = paramMap.entrySet().stream()
                        .map(entry -> new KeyValue(entry.getKey(), entry.getValue()))
                        .toList();

                // 将转换后的列表存入 MenuForm
                formData.setParams(transformedList);
            } catch (Exception e) {
                throw new RuntimeException("解析参数失败", e);
            }
        }

        return formData;
    }


    /**
     * 保存菜单/修改菜单
     * @param menuForm 菜单表单
     *                 菜单类型为目录时，routePath 必须以 / 开头
     *                 菜单类型为菜单时，component 必须填写
     *                 菜单类型为按钮时，perm 必须填写
     * @return 是否保存成功
     */
    @Override
    public boolean saveMenu(MenuForm menuForm) {

        String menuType = menuForm.getType();
        boolean isExternalLink = MenuTypeEnum.MENU.getValue().equals(menuType)
                && StrUtil.startWithAny(menuForm.getRoutePath(), "http://", "https://");

        if (MenuTypeEnum.CATALOG.getValue().equals(menuType)) {  // 如果是目录
            String path = menuForm.getRoutePath();
            if (menuForm.getParentId() == 0 && !path.startsWith("/")) {
                menuForm.setRoutePath("/" + path); // 一级目录需以 / 开头
            }
            menuForm.setComponent("Layout");
        } else if (isExternalLink) {
            // 外链菜单组件设置为 null，通过 routePath 判断外链
            menuForm.setComponent(null);
        }
        if (Objects.equals(menuForm.getParentId(), menuForm.getId())) {
            throw new RuntimeException("父级菜单不能为当前菜单");
        }
        SysMenu entity = sysMenuConverter.toEntity(menuForm);
        String treePath = generateMenuTreePath(menuForm.getParentId());
        entity.setTreePath(treePath);

        List<KeyValue> params = menuForm.getParams();
        // 路由参数 [{key:"id",value:"1"}，{key:"name",value:"张三"}] 转换为 [{"id":"1"},{"name":"张三"}]
        if (CollectionUtil.isNotEmpty(params)) {
            entity.setParams(JSONUtil.toJsonStr(params.stream()
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue))));
        } else {
            entity.setParams(null);
        }
        // 新增类型为菜单时候 路由名称唯一
        if (MenuTypeEnum.MENU.getValue().equals(menuType) && !isExternalLink) {
            Assert.isFalse(this.exists(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getRouteName, entity.getRouteName())
                    .ne(menuForm.getId() != null, SysMenu::getId, menuForm.getId())
            ), "路由名称已存在");
        } else {
            // 其他类型时 给路由名称赋值为空
            entity.setRouteName(null);
        }

        boolean result = this.saveOrUpdate(entity);
        if (result) {
            // 编辑刷新角色权限缓存
            if (menuForm.getId() != null) {
                sysRoleMenuService.refreshRolePermsCache();
            }
        }
        // 修改菜单如果有子菜单，则更新子菜单的树路径
        updateChildrenTreePath(entity.getId(), treePath);
        return result;
    }

    /**
     * 部门路径生成
     *
     * @param parentId 父ID
     * @return 父节点路径以英文逗号(, )分割，eg: 1,2,3
     */
    private String generateMenuTreePath(Long parentId) {
        if (SystemConstants.ROOT_NODE_ID.equals(parentId)) {
            return String.valueOf(parentId);
        } else {
            SysMenu parent = this.getById(parentId);
            return parent != null ? parent.getTreePath() + "," + parent.getId() : null;
        }
    }

    /**
     * 更新子菜单树路径
     *
     * @param id       当前菜单ID
     * @param treePath 当前菜单树路径
     */
    private void updateChildrenTreePath(Long id, String treePath) {
        List<SysMenu> children = this.list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (CollectionUtil.isNotEmpty(children)) {
            // 子菜单的树路径等于父菜单的树路径加上父菜单ID
            String childTreePath = treePath + "," + id;
            this.update(new LambdaUpdateWrapper<SysMenu>()
                    .eq(SysMenu::getParentId, id)
                    .set(SysMenu::getTreePath, childTreePath)
            );
            for (SysMenu child : children) {
                // 递归更新子菜单
                updateChildrenTreePath(child.getId(), childTreePath);
            }
        }
    }

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteMenu(Long id) {

        // 同时删除当前菜单及所有子孙菜单（通过 tree_path 匹配）
        boolean result = this.remove(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getId, id)
                .or()
                .apply("CONCAT (',',tree_path,',') LIKE CONCAT('%,',{0},',%')", id));
        // 刷新角色权限缓存
        if (result) {
            sysRoleMenuService.refreshRolePermsCache();
        }
        return result;
    }

    /**
     * 更新菜单可见状态
     *
     * @param menuId   菜单ID
     * @param visible  可见状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateMenuVisible(Long menuId, Integer visible) {
        return this.update(new LambdaUpdateWrapper<SysMenu>()
                .eq(SysMenu::getId, menuId)
                .set(SysMenu::getVisible, visible)
        );
    }
}




