package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechProductMapper;
import com.ruoyi.tech.domain.TechProduct;
import com.ruoyi.tech.service.ITechProductService;

/**
 * 产品清单Service业务层处理
 */
@Service
public class TechProductServiceImpl implements ITechProductService {
    private static final Logger log = LoggerFactory.getLogger(TechProductServiceImpl.class);

    @Autowired
    private TechProductMapper techProductMapper;

    @Override
    public TechProduct selectTechProductByProductId(Long productId) {
        return techProductMapper.selectTechProductByProductId(productId);
    }

    @Override
    public List<TechProduct> selectTechProductList(TechProduct techProduct) {
        return techProductMapper.selectTechProductList(techProduct);
    }

    @Override
    public int insertTechProduct(TechProduct techProduct) {
        techProduct.setCreateTime(DateUtils.getNowDate());
        return techProductMapper.insertTechProduct(techProduct);
    }

    @Override
    public int updateTechProduct(TechProduct techProduct) {
        techProduct.setUpdateTime(DateUtils.getNowDate());
        return techProductMapper.updateTechProduct(techProduct);
    }

    @Override
    public int deleteTechProductByProductIds(Long[] productIds) {
        return techProductMapper.deleteTechProductByProductIds(productIds);
    }

    @Override
    public int deleteTechProductByProductId(Long productId) {
        return techProductMapper.deleteTechProductByProductId(productId);
    }

    /**
     * 导入产品清单数据
     * 核心逻辑：以轮型号(wheelCode)作为唯一标识判断是否重复
     */
    @Override
    public String importProducts(List<TechProduct> productList, boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(productList) || productList.size() == 0) {
            throw new ServiceException("导入数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        log.info("开始导入新产品数据，总行数：{}", productList.size());

        // 1. 预先加载所有现有产品，用于快速匹配（按轮型号）
        List<TechProduct> allExistProducts = techProductMapper.selectTechProductList(new TechProduct());
        java.util.Map<String, TechProduct> existMap = new java.util.HashMap<>();
        for (TechProduct p : allExistProducts) {
            if (StringUtils.isNotEmpty(p.getWheelCode())) {
                existMap.put(p.getWheelCode().trim(), p);
            }
        }

        for (TechProduct product : productList) {
            try {
                String wheelCode = product.getWheelCode();
                if (StringUtils.isEmpty(wheelCode)) {
                    continue;
                }
                wheelCode = wheelCode.trim();

                // 处理特殊字段
                if (StringUtils.isNotEmpty(product.getFeaResult())) {
                    product.setFeaResult(remove4ByteChars(product.getFeaResult()));
                }

                // 检查是否存在
                TechProduct exist = existMap.get(wheelCode);

                if (exist != null) {
                    if (isUpdateSupport) {
                        product.setProductId(exist.getProductId());
                        product.setUpdateBy(operName);
                        product.setUpdateTime(DateUtils.getNowDate());
                        techProductMapper.updateTechProduct(product);
                        successNum++;
                        // 减少消息拼接量，避免前端显示内容过多导致性能问题
                        if (successNum <= 50) {
                            successMsg.append("<br/>").append(successNum).append("、轮型 ").append(wheelCode)
                                    .append(" 更新成功");
                        }
                    } else {
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、轮型 ").append(wheelCode).append(" 已存在");
                    }
                } else {
                    product.setCreateBy(operName);
                    product.setCreateTime(DateUtils.getNowDate());
                    techProductMapper.insertTechProduct(product);
                    successNum++;
                    if (successNum <= 50) {
                        successMsg.append("<br/>").append(successNum).append("、轮型 ").append(wheelCode).append(" 导入成功");
                    }
                }

                // 每处理100行打一次日志
                if ((successNum + failureNum) % 100 == 0) {
                    log.info("已处理 {} 行数据...", (successNum + failureNum));
                }

            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、轮型 " + product.getWheelCode() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }

        if (successNum > 50) {
            successMsg.append("<br/>...等共 ").append(successNum).append(" 条数据。");
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，部分数据导入失败！共 " + failureNum + " 条错误：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条：");
        }
        return successMsg.toString();
    }

    /**
     * 按条件清空产品（全选删除）
     */
    @Override
    public int cleanAllProducts(TechProduct query) {
        List<TechProduct> list = techProductMapper.selectTechProductList(query);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        Long[] ids = list.stream().map(TechProduct::getProductId).toArray(Long[]::new);
        return techProductMapper.deleteTechProductByProductIds(ids);
    }

    /**
     * 去掉字符串中的 4 字节 UTF-8 字符（如 emoji），避免 MySQL utf8 字符集写入报错。
     * 保留 BMP 范围内字符（code point <= 0xFFFF）。
     */
    private static String remove4ByteChars(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            int cp = text.codePointAt(i);
            if (cp <= 0xFFFF) {
                sb.append((char) cp);
            } else {
                i += Character.charCount(cp) - 1; // 跳过 surrogate 第二半
            }
        }
        return sb.toString();
    }
}
