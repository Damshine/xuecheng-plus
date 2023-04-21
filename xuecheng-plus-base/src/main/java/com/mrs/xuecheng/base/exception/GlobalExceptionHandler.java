package com.mrs.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * description: GlobalExceptionHandler
 * date: 2023/4/18 17:23
 * author: MR.孙
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e) {
        log.error("【系统异常】{}", e.getErrorMessage(), e);
        return new RestErrorResponse(e.getErrorMessage());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {
        log.error("【系统异常】{}", e.getMessage(), e);
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrorMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doValidException(MethodArgumentNotValidException argumentNotValidException) {
        BindingResult bindingResult = argumentNotValidException.getBindingResult();

        StringBuffer errMsg = new StringBuffer();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error->{
            errMsg.append(error.getDefaultMessage()).append(",");
        });
        log.error(errMsg.toString());
        return new RestErrorResponse(errMsg.toString());
    }


}
