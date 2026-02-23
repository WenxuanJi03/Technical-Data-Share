package com.ruoyi.tech.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
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
public class TechProductController extends BaseController {
    @Autowired
    private ITechProductService techProductService;

    /**
     * 查询产品清单列表
     */
    @PreAuthorize("@ss.hasPermi('tech:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechProduct techProduct) {
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
    public void export(HttpServletResponse response, TechProduct techProduct) {
        List<TechProduct> list = techProductService.selectTechProductList(techProduct);
        ExcelUtil<TechProduct> util = new ExcelUtil<TechProduct>(TechProduct.class);
        util.exportExcel(response, list, "产品清单数据");
    }

    /**
     * 获取产品详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:product:query')")
    @GetMapping(value = "/{productId}")
    public AjaxResult getInfo(@PathVariable("productId") Long productId) {
        return success(techProductService.selectTechProductByProductId(productId));
    }

    /**
     * 新增产品
     */
    @PreAuthorize("@ss.hasPermi('tech:product:add')")
    @Log(title = "产品清单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechProduct techProduct) {
        techProduct.setCreateBy(getUsername());
        return toAjax(techProductService.insertTechProduct(techProduct));
    }

    /**
     * 修改产品
     */
    @PreAuthorize("@ss.hasPermi('tech:product:edit')")
    @Log(title = "产品清单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechProduct techProduct) {
        techProduct.setUpdateBy(getUsername());
        return toAjax(techProductService.updateTechProduct(techProduct));
    }

    /**
     * 删除产品
     */
    @PreAuthorize("@ss.hasPermi('tech:product:remove')")
    @Log(title = "产品清单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds) {
        return toAjax(techProductService.deleteTechProductByProductIds(productIds));
    }

    /**
     * 导入产品清单数据（图文一体化导入）
     * 
     * 核心逻辑：
     * 1. 用 POI 打开工作簿，提取 Sheet 中所有嵌入图片及其单元格位置
     * 2. 用 ExcelUtil 按常规方式导入文字数据
     * 3. 按行号将图片与产品记录对应，保存图片文件，设置 frontImage 路径
     * 
     * @param file          Excel文件
     * @param updateSupport 是否更新已有数据
     * @param titleNum      标题行数（原表填8，模板填0或不填）
     */
    @Log(title = "产品清单", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('tech:product:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport,
            @RequestParam(defaultValue = "0") int titleNum) throws Exception {

        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TechProductController.class);

        // ① 将文件读到字节数组，以便多次读取
        byte[] fileBytes = file.getBytes();
        log.info("【产品导入】文件大小: {} bytes, titleNum={}", fileBytes.length, titleNum);

        // ② 提取 WPS 嵌入式图片
        // WPS 将图片存入 wb.getAllPictures()，并使用名为 "WpsReserved_CellImgList" 的隐藏表映射 ID
        Map<String, byte[]> wheelCodePicMap = new HashMap<>();

        try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(fileBytes);
                Workbook wb = WorkbookFactory.create(bis)) {

            List<? extends PictureData> allPictures = wb.getAllPictures();
            Sheet wpsSheet = wb.getSheet("WpsReserved_CellImgList");

            if (wpsSheet != null && !allPictures.isEmpty()) {
                log.info("【产品导入】检测到 WPS 嵌入图片表，总图片数: {}", allPictures.size());
                // ─── 核心：由于 MD5 匹配失败，我们需要在 OPC 包中寻找真正的映射文件 ───
                // 通常 WPS 或新版 Excel 会将该映射存于 xl/cellimages.xml 或类似位置
                Map<String, byte[]> wpsIdBytesMap = new HashMap<>();
                try {
                    org.apache.poi.openxml4j.opc.OPCPackage opc = ((XSSFWorkbook) wb).getPackage();

                    // 1. 尝试直接定位关键映射文件
                    String[] possiblePaths = { "/xl/cellimages.xml", "/xl/cellimage.xml", "/xl/cellImgList.xml" };
                    for (String path : possiblePaths) {
                        try {
                            org.apache.poi.openxml4j.opc.PackagePart part = opc
                                    .getPart(org.apache.poi.openxml4j.opc.PackagingURIHelper.createPartName(path));
                            if (part != null) {
                                log.info("【WPS调试】找到关键映射文件: {}", path);
                                String xmlContent = new String(
                                        org.apache.poi.util.IOUtils.toByteArray(part.getInputStream()), "UTF-8");
                                // 解析映射: <cellImage><pic><nvPicPr><cNvPr id="1" name="ID_BC91..."/> ... <blip
                                // r:embed="rIdN"/>
                                // 或者其它格式，只要包含 ID_ 和 rId
                                java.util.regex.Matcher m = java.util.regex.Pattern
                                        .compile("name=\"(ID_[A-F0-9]+)\".*?r:embed=\"([^\"]+)\"",
                                                java.util.regex.Pattern.DOTALL)
                                        .matcher(xmlContent);
                                Map<String, String> tempIdToRId = new HashMap<>();
                                while (m.find())
                                    tempIdToRId.put(m.group(1), m.group(2));

                                log.info("【WPS调试】从 {} 解析到映射数量: {}", path, tempIdToRId.size());

                                // 根据 rId 找到图片字节 (从该 Part 的 Relationships 中找)
                                for (Map.Entry<String, String> entry : tempIdToRId.entrySet()) {
                                    try {
                                        org.apache.poi.openxml4j.opc.PackageRelationship rel = part
                                                .getRelationship(entry.getValue());
                                        org.apache.poi.openxml4j.opc.PackagePart imgPart = part.getRelatedPart(rel);
                                        wpsIdBytesMap.put(entry.getKey(),
                                                org.apache.poi.util.IOUtils.toByteArray(imgPart.getInputStream()));
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }

                    // 2. 如果还没找到，全量扫描所有 XML Part，寻找 ID_ 字符串
                    if (wpsIdBytesMap.isEmpty()) {
                        log.info("【WPS调试】开始全量扫描 OPC Parts...");
                        for (org.apache.poi.openxml4j.opc.PackagePart part : opc.getParts()) {
                            if (part.getPartName().getName().endsWith(".xml")) {
                                String content = new String(
                                        org.apache.poi.util.IOUtils.toByteArray(part.getInputStream()), "UTF-8");
                                if (content.contains("ID_BC91B1BFEDF04A9C8CFE88AB0E191957")) { // 使用日志中确认存在的 ID 做路标
                                    log.info("【WPS调试】在 Part {} 中找到了目标 ID 字符串！内容片段: {}",
                                            part.getPartName().getName(),
                                            content.substring(content.indexOf("ID_BC91B1BFEDF04A9C8CFE88AB0E191957"),
                                                    Math.min(content.length(),
                                                            content.indexOf("ID_BC91B1BFEDF04A9C8CFE88AB0E191957")
                                                                    + 200)));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("【WPS调试】OPC 扫描出错: {}", e.getMessage());
                }

                log.info("【WPS】最终还原 ID→图像 映射数量: {}", wpsIdBytesMap.size());

                // Step D: 扫描主表 C 列 DISPIMG 公式，提取 ID，关联轮型号
                Sheet mainSheet = wb.getSheetAt(0);
                int wheelCodeCol = 1; // B列
                int imgCol = 2; // C列
                java.util.regex.Pattern dispimgPat = java.util.regex.Pattern.compile("ID_[A-F0-9]+",
                        java.util.regex.Pattern.CASE_INSENSITIVE);

                log.info("【WPS调试】开始扫描主表，总行数: {}, titleNum: {}", mainSheet.getLastRowNum(), titleNum);

                for (int r = 0; r <= mainSheet.getLastRowNum(); r++) {
                    Row row = mainSheet.getRow(r);
                    if (row == null)
                        continue;

                    Cell imgCell = row.getCell(imgCol);
                    if (imgCell == null)
                        continue;

                    if (imgCell.getCellType() != CellType.FORMULA)
                        continue;

                    String formula = imgCell.getCellFormula();
                    if (!formula.contains("ID_"))
                        continue;

                    java.util.regex.Matcher m = dispimgPat.matcher(formula);
                    if (!m.find())
                        continue;

                    String wpsId = m.group().toUpperCase();
                    byte[] imgBytes = wpsIdBytesMap.get(wpsId);

                    // 调试匹配过程
                    if (r < titleNum + 15) {
                        log.info("【WPS调试】Row {}: 提取ID={}, 库中是否存在={}", r, wpsId, (imgBytes != null));
                        Cell wheelCell = row.getCell(wheelCodeCol);
                        log.info("【WPS调试】Row {}: 轮型号B列真实值={}", r, wheelCell == null ? "NULL" : wheelCell.toString());
                    }

                    if (imgBytes == null)
                        continue;

                    Cell wheelCell = row.getCell(wheelCodeCol);
                    if (wheelCell == null)
                        continue;

                    String wheelCode = wheelCell.toString().trim();
                    if (wheelCode.isEmpty())
                        continue;

                    wheelCodePicMap.put(wheelCode, imgBytes);
                }
                log.info("【WPS】最终 wheelCode→图片 映射数量: {}", wheelCodePicMap.size());

            } else {
                log.info("【产品导入】未检测到 WPS 嵌入图片表，尝试使用标准 Drawing 方式提取");
                // 退回到标准 Drawing 提取（用于非 WPS 嵌入模式）
                Map<String, List<PictureData>> stdPics = null;
                Sheet sheet = wb.getSheetAt(0);
                if (wb instanceof XSSFWorkbook) {
                    stdPics = ExcelUtil.getSheetPictures07((XSSFSheet) sheet, (XSSFWorkbook) wb);
                } else if (wb instanceof HSSFWorkbook) {
                    stdPics = ExcelUtil.getSheetPictures03((HSSFSheet) sheet, (HSSFWorkbook) wb);
                }

                if (stdPics != null && !stdPics.isEmpty()) {
                    int wheelCodeCol = 1;
                    for (Map.Entry<String, List<PictureData>> entry : stdPics.entrySet()) {
                        String[] parts = entry.getKey().split("_");
                        int rowIdx = Integer.parseInt(parts[0]);
                        Row row = sheet.getRow(rowIdx);
                        if (row != null && !entry.getValue().isEmpty()) {
                            Cell cell = row.getCell(wheelCodeCol);
                            if (cell != null) {
                                String wheelCode = cell.toString().trim();
                                if (!wheelCode.isEmpty()) {
                                    PictureData pd = entry.getValue().get(0);
                                    wheelCodePicMap.put(wheelCode, pd.getData());
                                }
                            }
                        }
                    }
                }
            }
        }

        log.info("【产品导入】最终建立的 轮型号 -> 图片 映射数量: {}", wheelCodePicMap.size());

        // ③ 常规导入文字数据
        ExcelUtil<TechProduct> util = new ExcelUtil<TechProduct>(TechProduct.class);
        List<TechProduct> productList;
        try (java.io.ByteArrayInputStream bis2 = new java.io.ByteArrayInputStream(fileBytes)) {
            productList = util.importExcel(bis2, titleNum);
        }
        log.info("【产品导入】文字数据导入行数: {}", productList.size());

        // ④ 按轮型号匹配图片并保存
        int matchImgCount = 0;
        for (TechProduct product : productList) {
            if (product.getWheelCode() != null) {
                byte[] imgData = wheelCodePicMap.get(product.getWheelCode().trim());
                if (imgData != null) {
                    String fileName = FileUtils.writeImportBytes(imgData);
                    product.setFrontImage(fileName);
                    matchImgCount++;
                }
            }
        }
        log.info("【产品导入】成功匹配并保存图片（含WPS嵌入）: {} 张", matchImgCount);

        String operName = getUsername();
        String message = techProductService.importProducts(productList, updateSupport, operName);
        if (matchImgCount > 0) {
            message = message + "<br/>同时成功导入 " + matchImgCount + " 张产品图片。";
        }
        return success(message);
    }

    /**
     * 清空产品（按搜索条件全选删除）
     */
    @PreAuthorize("@ss.hasPermi('tech:product:remove')")
    @Log(title = "产品清单", businessType = BusinessType.DELETE)
    @DeleteMapping("/cleanAll")
    public AjaxResult cleanAll(TechProduct techProduct) {
        int count = techProductService.cleanAllProducts(techProduct);
        return success("成功删除 " + count + " 条产品记录");
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
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
    public AjaxResult batchUploadImages(@RequestParam("files") List<MultipartFile> files) throws Exception {
        if (files == null || files.isEmpty()) {
            return error("请选择图片文件");
        }
        String uploadPath = RuoYiConfig.getUploadPath();
        int matchCount = 0;
        int noMatchCount = 0;
        StringBuilder resultMsg = new StringBuilder();

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null)
                continue;

            // 从文件名提取型号（去掉扩展名）
            String code = originalName.contains(".")
                    ? originalName.substring(0, originalName.lastIndexOf(".")).trim()
                    : originalName.trim();

            if (code.isEmpty())
                continue;

            // 上传文件
            String fileName = FileUploadUtils.upload(uploadPath, file);

            // 根据文件名匹配产品（模糊匹配轮型号）
            TechProduct query = new TechProduct();
            query.setWheelCode(code);
            List<TechProduct> matchList = techProductService.selectTechProductList(query);

            if (matchList != null && !matchList.isEmpty()) {
                // 匹配到了，更新图片
                for (TechProduct product : matchList) {
                    product.setFrontImage(fileName);
                    product.setUpdateBy(getUsername());
                    techProductService.updateTechProduct(product);
                }
                matchCount++;
                resultMsg.append("<br/>").append(code).append(" -> 匹配 ").append(matchList.size()).append(" 个产品");
            } else {
                noMatchCount++;
                resultMsg.append("<br/>").append(code).append(" -> 未找到匹配产品");
            }
        }

        String summary = "批量上传完成！匹配成功: " + matchCount + " 张, 未匹配: " + noMatchCount + " 张" + resultMsg;
        return success(summary);
    }
}
