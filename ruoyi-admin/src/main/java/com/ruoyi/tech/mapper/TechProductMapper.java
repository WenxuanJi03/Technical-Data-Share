package com.ruoyi.tech.mapper;

import java.util.List;
import com.ruoyi.tech.domain.TechProduct;

/**
 * 产品清单Mapper接口
 * 
 * @author ruoyi
 * @date 2026-02-09
 */
public interface TechProductMapper
{
    public TechProduct selectTechProductByProductId(Long productId);

    public List<TechProduct> selectTechProductList(TechProduct techProduct);

    public int insertTechProduct(TechProduct techProduct);

    public int updateTechProduct(TechProduct techProduct);

    public int deleteTechProductByProductId(Long productId);

    public int deleteTechProductByProductIds(Long[] productIds);
}
