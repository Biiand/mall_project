package com.mmall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by hasee on 2018/5/2.
 */
@Setter
@Getter
@NoArgsConstructor
public class CartVo {

    private List<CartProductVo> cartProductVoList;

    private BigDecimal cartTotalPrice;

    private boolean allChecked;

    private String imageHost;


}
