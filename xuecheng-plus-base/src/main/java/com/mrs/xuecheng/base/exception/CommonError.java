package com.mrs.xuecheng.base.exception;

public enum CommonError {

    UNKOWN_ERROR("执行过程异常，请重试。"),
    PARAMS_ERROR("非法参数"),
    OBJECT_NULL("对象为空"),
    QUERY_NULL("查询结果为空"),
    REQUEST_NULL("请求参数为空");

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    CommonError(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
