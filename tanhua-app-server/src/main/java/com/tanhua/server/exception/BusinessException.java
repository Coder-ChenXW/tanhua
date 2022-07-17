package com.tanhua.server.exception;


import com.tanhua.model.vo.ErrorResult;
import lombok.Data;

/**
 * @Function: 功能描述 自定义异常
 * @Author: ChenXW
 * @Date: 17:24 2022/7/16
 */
@Data
public class BusinessException extends RuntimeException{

    private ErrorResult errorResult;

    public BusinessException(ErrorResult errorResult){
        super(errorResult.getErrMessage());
        this.errorResult=errorResult;
    }

}
