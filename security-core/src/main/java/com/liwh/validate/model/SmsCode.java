package com.liwh.validate.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: Liwh
 * @ClassName: SmsCode
 * @Description:
 * @version: 1.0.0
 * @date: 2018-12-17 4:04 PM
 */
@Data
public class SmsCode extends ValidateCode {

    private static final long serialVersionUID = -8104026037194401380L;

    //常规构造方法
    public SmsCode(String code, LocalDateTime expireTime) {
        super(code, expireTime);
    }

    //设置过期时间的构造方法
    public SmsCode(String code, Integer expireTime) {
        super(code, expireTime);
    }
}
