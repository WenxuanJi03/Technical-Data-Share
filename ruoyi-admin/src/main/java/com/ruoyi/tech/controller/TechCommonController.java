package com.ruoyi.tech.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.service.ISysUserService;

/**
 * 技术科公共接口 - 提供不需要系统管理员权限的基础查询，
 * 如用户姓名下拉列表（发起人 / 负责人选择），任何已登录用户均可访问。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/tech/common")
public class TechCommonController extends BaseController {

    @Autowired
    private ISysUserService userService;

    /**
     * 获取用户列表（仅返回 userId、userName、nickName，用于下拉选择）。
     * 无 @PreAuthorize，只需登录即可调用，不需要 system:user:list 权限。
     */
    @GetMapping("/users")
    public TableDataInfo getUsers() {
        startPage();
        SysUser query = new SysUser();
        query.setStatus("0");
        List<SysUser> list = userService.selectUserList(query);
        // 只返回前端需要的基础字段，避免暴露密码等敏感信息
        List<SysUser> simplified = list.stream().map(u -> {
            SysUser s = new SysUser();
            s.setUserId(u.getUserId());
            s.setUserName(u.getUserName());
            s.setNickName(u.getNickName());
            return s;
        }).collect(Collectors.toList());
        return getDataTable(simplified);
    }
}
