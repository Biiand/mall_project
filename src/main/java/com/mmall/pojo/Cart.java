package com.mmall.pojo;

import lombok.*;

import java.util.Date;

//使用lombok注解简化代码，lombok是在编译.java阶段，
//在生成AST(abstract syntax tree)抽象语法数后对AST操作，添加注解代表的代码。
//要注意lombok主要用于pojo这种单纯的类，对一个类有更多元的操作时lombok有可能会冲突或失效，需要根据实际情况调整
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private Integer id;

    private Integer userId;

    private Integer productId;

    private Integer quantity;

    private Integer checked;

    private Date createTime;

    private Date updateTime;


}