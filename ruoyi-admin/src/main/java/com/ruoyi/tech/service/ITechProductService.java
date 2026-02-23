package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechProduct;

/**
 * 产品清单Service接口
 */
public interface ITechProductService {
    public TechProduct selectTechProductByProductId(Long productId);

    public List<TechProduct> selectTechProductList(TechProduct techProduct);

    public int insertTechProduct(TechProduct techProduct);

    public int updateTechProduct(TechProduct techProduct);

    public int deleteTechProductByProductIds(Long[] productIds);

    public int deleteTechProductByProductId(Long productId);

    /**
     * 导入产品清单数据（从Excel导入）
     */
    public String importProducts(List<TechProduct> productList, boolean isUpdateSupport, String operName);

    /**
     * 按条件清空产品（全选删除）
     */
    public int cleanAllProducts(TechProduct query);
}
