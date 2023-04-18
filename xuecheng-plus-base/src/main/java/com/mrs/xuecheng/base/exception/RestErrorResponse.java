package com.mrs.xuecheng.base.exception;

import java.io.Serializable;

/**
 * description: RestErrorResponse
 * date: 2023/4/18 17:09
 * author: MR.孙
 */
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

}
