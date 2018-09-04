package com.poc.consul.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author vrakoton
 * 
 * Description: This is the dynamic config annotation you should use on
 * getter methods to indicate that the attribute should be gretrieved dynamically 
 * on Consul 
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DynamicConfig {
	public String tokenName();
	public int timeout() default 5;
}
