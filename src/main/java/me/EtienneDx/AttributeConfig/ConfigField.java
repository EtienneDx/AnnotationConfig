package me.EtienneDx.AttributeConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@interface ConfigField
{
	String name() default "";
}
