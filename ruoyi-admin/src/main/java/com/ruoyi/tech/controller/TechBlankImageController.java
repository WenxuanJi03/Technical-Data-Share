package com.ruoyi.tech.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
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
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.tech.domain.TechBlankImage;
import com.ruoyi.tech.service.ITechBlankImageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 毛胚图Controller
 *
 * @author ruoyi
 * @date 2026-03-12
 */
@RestController
@RequestMapping("/tech/blankImage")
public class TechBlankImageController extends BaseController {
    @Autowired
    private ITechBlankImageService techBlankImageService;

    /**
     * 查询毛胚图列表
     */
    @PreAuthorize("@ss.hasPermi('tech:blankImage:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechBlankImage techBlankImage) {
        startPage();
        List<TechBlankImage> list = techBlankImageService.selectTechBlankImageList(techBlankImage);
        return getDataTable(list);
    }

    /**
     * 导出毛胚图
     */
    @PreAuthorize("@ss.hasPermi('tech:blankImage:export')")
    @Log(title = "毛胚图", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechBlankImage techBlankImage) {
        List<TechBlankImage> list = techBlankImageService.selectTechBlankImageList(techBlankImage);
        ExcelUtil<TechBlankImage> util = new ExcelUtil<TechBlankImage>(TechBlankImage.class);
        util.exportExcel(response, list, "毛胚图数据");
    }

    /**
     * 获取毛胚图详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:blankImage:query')")
    @GetMapping(value = "/{blankId}")
    public AjaxResult getInfo(@PathVariable("blankId") Long blankId) {
        return success(techBlankImageService.selectTechBlankImageByBlankId(blankId));
    }

    /**
     * 新增毛胚图
     */
    @PreAuthorize("@ss.hasPermi('tech:blankImage:add')")
    @Log(title = "毛胚图", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechBlankImage techBlankImage) {
        techBlankImage.setCreateBy(getUsername());
        return toAjax(techBlankImageService.insertTechBlankImage(techBlankImage));
    }

    /**
     * 修改毛胚图
     */
    @PreAuthorize("@ss.hasPermi('tech:blankImage:edit')")
    @Log(title = "毛胚图", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechBlankImage techBlankImage) {
        techBlankImage.setUpdateBy(getUsername());
        return toAjax(techBlankImageService.updateTechBlankImage(techBlankImage));
    }

    /**
     * 删除毛胚图
     */
    @PreAuthorize("@ss.hasPermi('tech:blankImage:remove')")
    @Log(title = "毛胚图", businessType = BusinessType.DELETE)
    @DeleteMapping("/{blankIds}")
    public AjaxResult remove(@PathVariable Long[] blankIds) {
        return toAjax(techBlankImageService.deleteTechBlankImageByBlankIds(blankIds));
    }

    /**
     * 导入毛胚图数据（自定义解析：支持卡片式布局的 XLS/XLSX 表格）
     *
     * 表格布局（每4行一组，横向铺开）：
     *   Row N  :  [图片]    img1    img2    img3   ...
     *   Row N+1:  [下发时间] 2025-9-20 ...
     *   Row N+2:  [版本]    C       B       A      ...
     *   Row N+3:  [型号]    00919F03 00919F05 ...
     * 模号(如 009) 出现在 A 列，跨多组
     */
    @Log(title = "毛胚图", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('tech:blankImage:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport,
            @RequestParam(defaultValue = "0") int titleNum) throws Exception {

        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TechBlankImageController.class);

        byte[] fileBytes = file.getBytes();
        String originalName = file.getOriginalFilename();
        log.info("【毛胚图导入】文件: {}, 大小: {} bytes", originalName, fileBytes.length);

        // 解析 Excel
        Workbook wb = WorkbookFactory.create(new java.io.ByteArrayInputStream(fileBytes));
        boolean isXSSF = wb instanceof XSSFWorkbook;
        log.info("【毛胚图导入】格式: {}, Sheet数: {}", isXSSF ? "XLSX" : "XLS", wb.getNumberOfSheets());

        // 找到毛胚图数据所在的 Sheet（扫描所有 Sheet，找包含"型号"标签的那个）
        Sheet dataSheet = null;
        for (int si = 0; si < wb.getNumberOfSheets(); si++) {
            Sheet s = wb.getSheetAt(si);
            String sName = s.getSheetName();
            // 跳过 WPS 内部 sheet
            if (sName != null && sName.startsWith("WpsReserved")) continue;
            // 扫描前 20 行的 B 列，看有没有"型号"
            for (int r = 0; r <= Math.min(20, s.getLastRowNum()); r++) {
                Row row = s.getRow(r);
                if (row == null) continue;
                Cell cell = row.getCell(1); // B列
                if (cell != null && "型号".equals(getCellText(cell).trim())) {
                    dataSheet = s;
                    break;
                }
            }
            if (dataSheet != null) break;
        }
        if (dataSheet == null) {
            // 回退：用第一个非 WPS 内部 sheet
            for (int si = 0; si < wb.getNumberOfSheets(); si++) {
                Sheet s = wb.getSheetAt(si);
                if (s.getSheetName() != null && !s.getSheetName().startsWith("WpsReserved")) {
                    dataSheet = s;
                    break;
                }
            }
        }
        if (dataSheet == null) {
            wb.close();
            return error("未找到有效的毛胚图数据 Sheet");
        }
        log.info("【毛胚图导入】使用 Sheet: {}", dataSheet.getSheetName());

        // ---- 提取浮动图片位置映射（.xls 格式） ----
        // key = "row_col", value = picture bytes
        Map<String, byte[]> floatingPicMap = new HashMap<>();
        if (!isXSSF) {
            try {
                org.apache.poi.hssf.usermodel.HSSFSheet hSheet = (org.apache.poi.hssf.usermodel.HSSFSheet) dataSheet;
                org.apache.poi.hssf.usermodel.HSSFPatriarch patriarch = hSheet.getDrawingPatriarch();
                if (patriarch != null) {
                    for (org.apache.poi.hssf.usermodel.HSSFShape shape : patriarch.getChildren()) {
                        if (shape instanceof org.apache.poi.hssf.usermodel.HSSFPicture) {
                            org.apache.poi.hssf.usermodel.HSSFPicture pic = (org.apache.poi.hssf.usermodel.HSSFPicture) shape;
                            org.apache.poi.hssf.usermodel.HSSFClientAnchor anchor = (org.apache.poi.hssf.usermodel.HSSFClientAnchor) pic.getAnchor();
                            int picRow = anchor.getRow1();
                            int picCol = anchor.getCol1();
                            byte[] picData = pic.getPictureData().getData();
                            floatingPicMap.put(picRow + "_" + picCol, picData);
                        }
                    }
                }
                log.info("【毛胚图导入】.xls 浮动图片数量: {}", floatingPicMap.size());
            } catch (Exception e) {
                log.warn("【毛胚图导入】提取 .xls 浮动图片失败: {}", e.getMessage());
            }
        }

        // ---- 解析卡片式布局 ----
        List<TechBlankImage> parsedList = new java.util.ArrayList<>();
        String currentMoldNo = "";

        for (int r = 0; r <= dataSheet.getLastRowNum(); r++) {
            Row row = dataSheet.getRow(r);
            if (row == null) continue;

            // 检测 A 列的模号（如 "009"）
            String cellA = getCellText(row.getCell(0)).trim();
            if (!cellA.isEmpty() && cellA.matches("\\d{2,4}")) {
                currentMoldNo = cellA;
            }

            // 检测 B 列标签
            String labelB = getCellText(row.getCell(1)).trim();

            // 当 B 列 = "型号" 时，这一行的 C 列开始是型号数据
            if ("型号".equals(labelB)) {
                // 往上找对应的 下发时间(r-2)、版本(r-1)、图片(r-3)
                Row dateRow = dataSheet.getRow(r - 2);
                Row versionRow = dataSheet.getRow(r - 1);
                int imageRowIdx = r - 3;

                for (int c = 2; c <= row.getLastCellNum(); c++) {
                    String modelCode = getCellText(row.getCell(c)).trim();
                    if (modelCode.isEmpty()) continue;

                    TechBlankImage item = new TechBlankImage();
                    item.setModelCode(modelCode);
                    item.setMoldNo(currentMoldNo);

                    // 下发时间
                    if (dateRow != null) {
                        item.setReleaseDate(getCellText(dateRow.getCell(c)).trim());
                    }
                    // 版本
                    if (versionRow != null) {
                        item.setVersion(getCellText(versionRow.getCell(c)).trim());
                    }

                    // 浮动图片匹配
                    String picKey = imageRowIdx + "_" + c;
                    byte[] imgData = floatingPicMap.get(picKey);
                    if (imgData != null) {
                        try {
                            String imgPath = FileUtils.writeImportBytes(imgData);
                            item.setBlankImage(imgPath);
                        } catch (Exception e) {
                            log.warn("【毛胚图导入】保存图片失败: {}", modelCode);
                        }
                    }

                    parsedList.add(item);
                }
            }
        }
        wb.close();

        log.info("【毛胚图导入】解析到 {} 条毛胚图数据", parsedList.size());

        if (parsedList.isEmpty()) {
            return error("未解析到毛胚图数据，请确认表格格式正确（B列应有 图片/下发时间/版本/型号 标签）");
        }

        // 调用 service 导入
        String operName = getUsername();
        String message = techBlankImageService.importBlankImages(parsedList, updateSupport, operName);

        // 统计图片
        long imgCount = parsedList.stream().filter(i -> i.getBlankImage() != null).count();
        if (imgCount > 0) {
            message += "<br/>同时导入了 " + imgCount + " 张毛胚图图片。";
        }
        long noImgCount = parsedList.size() - imgCount;
        if (noImgCount > 0) {
            message += "<br/>有 " + noImgCount + " 条记录无图片，可在详情中手动上传或使用【批量上传图片】补充。";
        }
        return success(message);
    }

    /** 安全读取单元格文本 */
    private String getCellText(Cell cell) {
        if (cell == null) return "";
        try {
            switch (cell.getCellType()) {
                case STRING: return cell.getStringCellValue();
                case NUMERIC:
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-M-d");
                        return sdf.format(cell.getDateCellValue());
                    }
                    double num = cell.getNumericCellValue();
                    if (num == Math.floor(num) && !Double.isInfinite(num)) {
                        return String.valueOf((long) num);
                    }
                    return String.valueOf(num);
                case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try { return cell.getStringCellValue(); }
                    catch (Exception e) { return cell.getCellFormula(); }
                default: return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * AirScript 批量同步入口
     * 接收 JSON 数组，按 modelCode 判重同步
     */
    @PreAuthorize("@ss.hasPermi('tech:blankImage:add')")
    @Log(title = "毛胚图", businessType = BusinessType.INSERT)
    @PostMapping("/syncBatch")
    public AjaxResult syncBatch(@RequestBody List<TechBlankImage> list) {
        String operName = getUsername();
        String message = techBlankImageService.syncBatch(list, operName);
        return success(message);
    }

    /**
     * 批量上传毛胚图图片（智能模式）
     * 图片文件名用型号命名（如 00919F03.jpg）
     * - 已有记录：更新图片
     * - 无记录：自动创建新记录（从文件名解析型号，前3位作为模号）
     */
    @Log(title = "毛胚图-批量图片", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('tech:blankImage:edit')")
    @PostMapping("/batchUploadImages")
    public AjaxResult batchUploadImages(@RequestParam("files") List<MultipartFile> files) throws Exception {
        if (files == null || files.isEmpty()) {
            return error("请选择图片文件");
        }
        String uploadPath = RuoYiConfig.getUploadPath();
        int matchCount = 0;
        int createCount = 0;
        StringBuilder resultMsg = new StringBuilder();

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null) continue;

            String code = originalName.contains(".")
                    ? originalName.substring(0, originalName.lastIndexOf(".")).trim()
                    : originalName.trim();

            if (code.isEmpty()) continue;

            String fileName = FileUploadUtils.upload(uploadPath, file);

            TechBlankImage query = new TechBlankImage();
            query.setModelCode(code);
            List<TechBlankImage> matchList = techBlankImageService.selectTechBlankImageList(query);

            if (matchList != null && !matchList.isEmpty()) {
                // 已有记录 → 更新图片
                for (TechBlankImage item : matchList) {
                    item.setBlankImage(fileName);
                    item.setUpdateBy(getUsername());
                    techBlankImageService.updateTechBlankImage(item);
                }
                matchCount++;
                resultMsg.append("<br/>").append(code).append(" -> 更新图片");
            } else {
                // 无记录 → 自动新建
                TechBlankImage newItem = new TechBlankImage();
                newItem.setModelCode(code);
                // 从型号前3位提取模号（如 00919F03 → 009）
                if (code.length() >= 3) {
                    newItem.setMoldNo(code.substring(0, 3));
                }
                newItem.setBlankImage(fileName);
                newItem.setCreateBy(getUsername());
                techBlankImageService.insertTechBlankImage(newItem);
                createCount++;
                resultMsg.append("<br/>").append(code).append(" -> 新建记录");
            }
        }

        String summary = "批量上传完成！更新已有: " + matchCount + " 条, 自动新建: " + createCount + " 条" + resultMsg;
        return success(summary);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<TechBlankImage> util = new ExcelUtil<TechBlankImage>(TechBlankImage.class);
        util.importTemplateExcel(response, "毛胚图数据");
    }
}
