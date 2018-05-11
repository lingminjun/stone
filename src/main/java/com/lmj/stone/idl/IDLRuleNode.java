package com.lmj.stone.idl;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/13.
 * 规则映射节点
 */
public final class IDLRuleNode implements Serializable {
    private static final long serialVersionUID = -2502233352465511396L;

//    public String path;        //返回值的部分path, 为空是表示 invocations
    public String invocation; //
    public final String rule;
    public final int level;//规则顺序

    public IDLRuleNode(String rule) throws IDLException {
        this(rule,null);
    }

    public IDLRuleNode(String rule, String invokeMd5) throws IDLException {
        if (rule == null || rule.length() == 0) {
            throw IDLExceptions.PARAMETER_ERROR("请填写正确的规则,否则无法构造RuleNode");
        }
        this.level = IDLRule.judgeSortValue(rule);
        this.rule = rule;
        this.invocation = invokeMd5;
    }
}
