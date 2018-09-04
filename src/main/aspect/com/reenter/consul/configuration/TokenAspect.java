package com.reenter.consul.configuration;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.poc.consul.annotation.DynamicConfig;
import com.poc.consul.configuration.TokenManager;

import atg.nucleus.Nucleus;
import atg.nucleus.logging.ApplicationLogging;

@Aspect
public class TokenAspect {
	static String TOKEN_MANAGER_PATH = "/com/poc/consul/configuration/TokenManager";
	
	/**
	 * Retrieves the consul value for an annotated setter method
	 * @param call
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(DynamicConfig) && execution(* *.get*())")
	public Object getConsulValue(ProceedingJoinPoint call) throws Throwable {
		Object value = null;
		Object o = call.getTarget();
		TokenManager tm = (TokenManager)Nucleus.getGlobalNucleus().resolveName(TOKEN_MANAGER_PATH);
		if (tm == null) {
			if (o instanceof ApplicationLogging && ((ApplicationLogging)o).isLoggingError()) {
				((ApplicationLogging)o).logError("Can not retrieve the token manager service from the Nucleus");
			}
			return value;
		}
		
		MethodSignature signature = (MethodSignature)call.getSignature();
		Method m = signature.getMethod();	
		DynamicConfig a = m.getAnnotation(DynamicConfig.class);
		String tokenName = a.tokenName();

		try {
			value = tm.findValueForToken(tokenName);
		} catch (Exception exc) {
			tm.vlogError(exc, "an error occured");
		}
		
		// --- now let's set the value on the instance.
		if (value != null) {
			return tm.configureValue(call, value);
		}
		return value;
	}
	
}
