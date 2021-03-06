package com.liwh.validate.model;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * @author: Liwh
 * @ClassName: ImageCode
 * @Description:
 * @version: 1.0.0
 * @date: 2018-12-11 5:59 PM
 */
@Data
public class ImageCode extends ValidateCode {

    private static final long serialVersionUID = -7144525720956914912L;
    //图片
    private BufferedImage image;

    //设置过期时间的构造方法
    public ImageCode(BufferedImage image, String code, int expireIn) {
        super(code, expireIn);
        this.image = image;
    }

    public ImageCode(BufferedImage image, String code, LocalDateTime expireTime) {
        super(code, expireTime);
        this.image = image;
    }

}
