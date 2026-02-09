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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.tech.domain.TechProduct;
import com.ruoyi.tech.service.ITechProductService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 产品清单Controller
 * 
 * @author ruoyi
 * @date 2026-02-09
 */
@RestController
@RequestMapping("/tech/product")
public class TechProductController extends BaseController
{
    @Autowired
    private ITechProductService techProductService;

    /**
     * 查询产品清单列表
     */
    @PreAuthorize("@ss.hasPermi('tech:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechProduct techProduct)
    {
        startPage();
        List<TechProduct> list = techProductService.selectTechProductList(techProduct);
        return getDataTable(list);
    }

    /**
     * 导出产品清单
     */
    @PreAuthorize("@ss.hasPermi('tech:product:export')")
    @Log(title = "产品清单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechProduct techProduct)
    {
        List<TechProduct> list = techProductService.selectTechProductList(techProduct);
        ExcelUtil<TechProduct> util = new ExcelUtil<TechProduct>(TechProduct.class);
        util.exportExcel(response, list, "产品清单数据");
    }

    /**
     * 获取产品详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:product:query')")
    @GetMapping(value = "/{productId}")
    public AjaxResult getInfo(@PathVariable("productId") Long productId)
    {
        return success(techProductService.selectTechProductByProductId(productId));
    }

    /**
     * 新增产品
     */
    @PreAuthorize("@ss.hasPermi('tech:product:add')")
    @Log(title = "产品清单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechProduct techProduct)
    {
        techProduct.setCreateBy(getUsername());
        return toAjax(techProductService.insertTechProduct(techProduct));
    }

    /**
     * 修改产品
     */
    @PreAuthorize("@ss.hasPermi('tech:product:edit')")
    @Log(title = "产品清单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechProduct techProduct)
    {
        techProduct.setUpdateBy(getUsername());
        return toAjax(techProductService.updateTechProduct(techProduct));
    }

    /**
     * 删除产品
     */
    @PreAuthorize("@ss.hasPermi('tech:product:remove')")
    @Log(title = "产品清单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds)
    {
        return toAjax(techProductService.deleteTechProductByProductIds(productIds));
    }

    /**
     * 导入产品清单数据
     * 
     * 支持两种方式：
     * 1. 直接导入 DX-产品清单原表（titleNum=8，跳过前8行统计和表头）
     * 2. 导入标准模板（titleNum=0，默认）
     * 
     * @param file Excel文件
     * @param updateSupport 是否更新已有数据
     * @param titleNum 标题行数（原表填8，模板填0或不填）
     */
    @Log(title = "产品清单", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('tech:product:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport,
            @RequestParam(defaultValue = "0") int titleNum) throws Exception
    {
        ExcelUtil<TechProduct> util = new ExcelUtil<TechProduct>(TechProduct.class);
        List<TechProduct> productList = util.importExcel(file.getInputStream(), titleNum);
        String operName = getUsername();
        String message = techProductService.importProducts(productList, updateSupport, operName);
        return success(message);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<TechProduct> util = new ExcelUtil<TechProduct>(TechProduct.class);
        util.importTemplateExcel(response, "产品清单数据");
    }

    /**
     * 批量上传产品正面图
     * 
     * 使用方法：图片文件名用轮型号命名（如 00919F03.jpg）
     * 系统根据文件名自动匹配产品记录，将图片路径写入 front_image 字段
     */
    @Log(title = "产品清单-批量图片", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('tech:product:edit')")
    @PostMapping("/batchUploadImages")
    public AjaxResult batchUploadImages(@RequestParam("files") List<MultipartFile> files) throws Exception
    {
        if (files == null || files.isEmpty())
        {
            return error("请选择图片文件");
        }
        String uploadPath = RuoYiConfig.getUploadPath();
        int matchCount = 0;
        int noMatchCount = 0;
        StringBuilder resultMsg = new StringBuilder();

        for (MultipartFile file : files)
        {
            String originalName = file.getOriginalFilename();
            if (originalName == null) continue;

            // 从文件名提取型号（去掉扩展名）
            String code = originalName.contains(".")
                    ? originalName.substring(0, originalName.lastIndexOf(".")).trim()
                    : originalName.trim();

            if (code.isEmpty()) continue;

            // 上传文件
            String fileName = FileUploadUtils.upload(uploadPath, file);

            // 根据文件名匹配产品（模糊匹配轮型号）
            TechProduct query = new TechProduct();
            query.setWheelCode(code);
            List<TechProduct> matchList = techProductService.selectTechProductList(query);

            if (matchList != null && !matchList.isEmpty())
            {
                // 匹配到了，更新图片
                for (TechProduct product : matchList)
                {
                    product.setFrontImage(fileName);
                    product.setUpdateBy(getUsername());
                    techProductService.updateTechProduct(product);
                }
                matchCount++;
                resultMsg.append("<br/>").append(code).append(" -> 匹配 ").append(matchList.size()).append(" 个产品");
            }
            else
            {
                noMatchCount++;
                resultMsg.append("<br/>").append(code).append(" -> 未找到匹配产品");
            }
        }

        String summary = "批量上传完成！匹配成功: " + matchCount + " 张, 未匹配: " + noMatchCount + " 张" + resultMsg;
        return success(summary);
    }
}
