package com.liwh.validate.processor;

import com.liwh.enums.ValidateCodeType;
import com.liwh.validate.exception.ValidateCodeException;
import com.liwh.validate.exception.ValidateException;
import com.liwh.validate.model.ValidateCode;
import com.liwh.validate.generator.ValidateCodeGenerator;
import com.liwh.validate.repository.ValidateCodeRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @author: Liwh
 * @ClassName: AbstractValidateCodeProcessor
 * @Description:
 * @version: 1.0.0
 * @date: 2018-12-17 7:17 PM
 */
public abstract class AbstractValidateCodeProcessor<T extends ValidateCode> implements ValidateCodeProcessor {

    protected final String THIS_SUFFIX = "CodeProcessor";

    //依赖搜索
    @Autowired
    Map<String, ValidateCodeGenerator> codeGenerators = new HashMap();

    @Autowired
    private ValidateCodeRepository validateCodeRepository;

    @Override
    public void create(ServletWebRequest servletWebRequest) throws Exception {
        //生成
        T validateCode = generate(servletWebRequest);
        //存储
        save(servletWebRequest, validateCode);
        //发送
        send(servletWebRequest, validateCode);
    }

    private T generate(ServletWebRequest servletWebRequest) {
        //生成有不同的生成方式。那么就有不同的generator实现
        //这是第二层的接口
        //好，接口有多个实现，都注册在spring中，我们就可以集中起来，按需使用实现类！
        //这叫：依赖搜索
        String uri = servletWebRequest.getRequest().getRequestURI();
        String[] split = uri.split("/");
        String type = split[split.length - 1].toLowerCase();
        String keyName = type + ValidateCodeGenerator.class.getSimpleName();
        ValidateCodeGenerator generator = codeGenerators.get(keyName);
        if (generator == null) {
            throw new RuntimeException("验证码生成器" + keyName + "不存在");
        }
        //自动选择生成器
        T validateCode = (T) generator.generate(servletWebRequest);

        return validateCode;
    }

    //存储:1、不同类型不同的KEY,2、不同模块是不同的Repository实现
    private void save(ServletWebRequest servletWebRequest, T validateCode) throws Exception {
        ValidateCode code = new ValidateCode(validateCode.getCode(), validateCode.getExpireTime());
        validateCodeRepository.save(servletWebRequest, code, getValidateCodeType());
    }

    //由子类实现各自的逻辑
    abstract void send(ServletWebRequest servletWebRequest, T validateCode) throws Exception;

    //校验验证码
    @Override
    public void validate(ServletWebRequest servletWebRequest) throws ServletRequestBindingException {

        //获取操作类型
        ValidateCodeType validateCodeType = getValidateCodeType();

        //获取session_key
//        String sessionKey = getSessionKey(validateCodeType);

        //从session中取出存储的验证码
//        T sessionValidateCode = (T) sessionStrategy.getAttribute(servletWebRequest, sessionKey);

        //从Repository中取出存储的验证码
        T code = (T) validateCodeRepository.get(servletWebRequest, validateCodeType);

        //从请求中拿出用户输入（input框）的code
        String inputCode;
        try {
            inputCode = ServletRequestUtils.getStringParameter(servletWebRequest.getRequest(), validateCodeType.getParamNameOnValidate());

        } catch (Exception e) {
            throw new ValidateCodeException("获取验证码的值失败");
        }
        //各种校验
        if (StringUtils.isEmpty(inputCode)) {
            throw new ValidateException("验证码不能为空");
        }

        if (Objects.isNull(code)) {
            throw new ValidateException("验证码不存在");
        }
        if (code.isExpireTime(code.getExpireTime())) {
            validateCodeRepository.remove(servletWebRequest, validateCodeType);
            throw new ValidateException("验证码已过期");
        }

        if (!StringUtils.equalsIgnoreCase(code.getCode(), inputCode)) {
            throw new ValidateException("验证码不匹配");
        }

        //通过则从Repository中移除验证码
        validateCodeRepository.remove(servletWebRequest, validateCodeType);
    }

    private ValidateCodeType getValidateCodeType() {
        String type = StringUtils.substringBefore(getClass().getSimpleName(), THIS_SUFFIX);
        return ValidateCodeType.valueOf(type.toUpperCase());
    }

}
