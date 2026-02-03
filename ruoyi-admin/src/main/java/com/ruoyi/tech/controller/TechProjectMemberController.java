package com.ruoyi.tech.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tech.domain.TechProjectMember;
import com.ruoyi.tech.service.ITechProjectMemberService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目成员Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/member")
public class TechProjectMemberController extends BaseController
{
    @Autowired
    private ITechProjectMemberService techProjectMemberService;

    /**
     * 查询项目成员列表
     */
    @PreAuthorize("@ss.hasPermi('tech:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechProjectMember techProjectMember)
    {
        startPage();
        List<TechProjectMember> list = techProjectMemberService.selectTechProjectMemberList(techProjectMember);
        return getDataTable(list);
    }

    /**
     * 导出项目成员列表
     */
    @PreAuthorize("@ss.hasPermi('tech:member:export')")
    @Log(title = "项目成员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechProjectMember techProjectMember)
    {
        List<TechProjectMember> list = techProjectMemberService.selectTechProjectMemberList(techProjectMember);
        ExcelUtil<TechProjectMember> util = new ExcelUtil<TechProjectMember>(TechProjectMember.class);
        util.exportExcel(response, list, "项目成员数据");
    }

    /**
     * 获取项目成员详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:member:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(techProjectMemberService.selectTechProjectMemberById(id));
    }

    /**
     * 新增项目成员
     */
    @PreAuthorize("@ss.hasPermi('tech:member:add')")
    @Log(title = "项目成员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechProjectMember techProjectMember)
    {
        return toAjax(techProjectMemberService.insertTechProjectMember(techProjectMember));
    }

    /**
     * 修改项目成员
     */
    @PreAuthorize("@ss.hasPermi('tech:member:edit')")
    @Log(title = "项目成员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechProjectMember techProjectMember)
    {
        return toAjax(techProjectMemberService.updateTechProjectMember(techProjectMember));
    }

    /**
     * 删除项目成员
     */
    @PreAuthorize("@ss.hasPermi('tech:member:remove')")
    @Log(title = "项目成员", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(techProjectMemberService.deleteTechProjectMemberByIds(ids));
    }
}
