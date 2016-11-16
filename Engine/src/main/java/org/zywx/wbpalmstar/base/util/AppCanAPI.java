package org.zywx.wbpalmstar.base.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ylt on 16/5/12.
 *
 * 目前只是用于排除混淆
 */

@Retention(RetentionPolicy.CLASS)//编译时处理
@Target({ElementType.METHOD}) //注解方法
public @interface AppCanAPI {
}