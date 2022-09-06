package com.shepherdboy.pdstreamline.beanview;

/**
 * 可以加载商品信息的View统一获取绑定的商品条码
 */
public interface BeanView {
    String getProductCode();
    void bindData(Object o);
}
