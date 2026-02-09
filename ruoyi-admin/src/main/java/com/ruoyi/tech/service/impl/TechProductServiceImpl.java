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
public class TechProductServiceImpl implements ITechProductService
{
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
    public String importProducts(List<TechProduct> productList, boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(productList) || productList.size() == 0) {
            throw new ServiceException("导入数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (TechProduct product : productList)
        {
            try {
                // 跳过轮型号为空的行（可能是表头或空行）
                if (StringUtils.isEmpty(product.getWheelCode())) {
                    continue;
                }
                product.setCreateBy(operName);
                product.setCreateTime(DateUtils.getNowDate());

                // 检查轮型号是否已存在
                TechProduct query = new TechProduct();
                query.setWheelCode(product.getWheelCode());
                List<TechProduct> existList = techProductMapper.selectTechProductList(query);

                if (existList != null && existList.size() > 0) {
                    if (isUpdateSupport) {
                        TechProduct exist = existList.get(0);
                        product.setProductId(exist.getProductId());
                        product.setUpdateBy(operName);
                        product.setUpdateTime(DateUtils.getNowDate());
                        techProductMapper.updateTechProduct(product);
                        successNum++;
                        successMsg.append("<br/>").append(successNum).append("、轮型 ").append(product.getWheelCode()).append(" 更新成功");
                    } else {
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、轮型 ").append(product.getWheelCode()).append(" 已存在");
                    }
                } else {
                    techProductMapper.insertTechProduct(product);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、轮型 ").append(product.getWheelCode()).append(" 导入成功");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、轮型 " + product.getWheelCode() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}
