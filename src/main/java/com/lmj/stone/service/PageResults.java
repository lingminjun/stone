package com.lmj.stone.service;

import com.lmj.stone.idl.annotation.IDLDesc;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-16
 * Time: 下午11:16
 */
@IDLDesc("一页结果集,请子类化")
public abstract class PageResults<T extends Serializable> {
    @IDLDesc("总数")
    public long total;//总数

    @IDLDesc("当前页数，从1开始")
    public int index;//

    @IDLDesc("一页显示条数")
    public int size;//

    @IDLDesc("数据集")
    public List<T> results;//
}
