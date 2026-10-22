package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.common.model.Option;
import com.lycorder.system.model.entity.SysMenu;
import com.lycorder.system.model.form.MenuForm;
import com.lycorder.system.model.query.MenuQuery;
import com.lycorder.system.model.vo.MenuVO;
import com.lycorder.system.model.vo.RouteVO;

import java.util.List;


/**
* @author LYCorder
* @description 针对表【sys_menu(系统菜单表)】的数据库操作Service
* @createDate 2026-01-23 19:26:46
*/
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 菜单列表
     *
     * @param queryParams 查询参数
     * @return 菜单列表
     */
    List<MenuVO> listMenus(MenuQuery queryParams);

    /**
     * 当前用户菜单路由列表
     *
     * @return 菜单路由列表
     */
    List<RouteVO> listCurrentUserRoutes();

    /**
     * 菜单选项
     *
     * @param onlyParent 是否只获取父级菜单
     * @return 菜单选项
     */
    List<Option<Long>> listMenuOptions(boolean onlyParent);

    /**
     * 获取菜单表单
     *
     * @param id 菜单ID
     * @return 菜单表单
     */
    MenuForm getMenuForm(Long id);

    /**
     * 保存菜单/修改菜单
     *
     * @param menuForm 菜单表单
     * @return 是否保存成功
     */
    boolean saveMenu(MenuForm menuForm);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 是否删除成功
     */
    boolean deleteMenu(Long id);

    /**
     * 修改菜单可见性
     *
     * @param menuId 菜单ID
     * @param visible 可见性
     * @return 是否修改成功
     */
    boolean updateMenuVisible(Long menuId, Integer visible);
}
