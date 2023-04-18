package com.mrs.xuecheng.base.exception;

/**
 * description: XueChengPlusException
 * date: 2023/4/18 16:56
 * author: MR.孙
 */
public class XueChengPlusException extends RuntimeException{

    private String errorMessage;

    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static void cast(String errorMessage) {
        throw new XueChengPlusException(errorMessage);
    }

    public static void cast(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrorMessage());

    }


}
