package com.reenter.consul.configuration;

import java.lang.reflect.Method;
import java.text.Annotation;
import java.util.Enumeration;
import java.util.Properties;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.poc.consul.annotation.DynamicConfig;
import com.poc.consul.configuration.TokenManager;

import atg.core.util.StringUtils;
import atg.nucleus.Configuration;
import atg.nucleus.ConfigurationFinder;
import atg.nucleus.MultipleConfigurationFinder;
import atg.nucleus.Nucleus;
import atg.nucleus.PropertyConfiguration;
import atg.nucleus.TokenAwarePropertyConfigurationFinder;
import atg.nucleus.logging.ApplicationLogging;
import atg.nucleus.logging.ApplicationLoggingImpl;

@Aspect
public class TokenAspect {
	static String TOKEN_MANAGER_PATH = "/com/poc/consul/configuration/TokenManager";
	
//	@Around("execution(* atg.nucleus.NucleusNameResolver.configureAndStartService(atg.nucleus.naming.ComponentName, atg.nucleus.Configuration, atg.naming.NameContext, atg.nucleus.ConfigurationLockRegistry, java.lang.Object, atg.nucleus.logging.ApplicationLogging)) && "
//			+ "execution(!final * atg.nucleus.NucleusNameResolver.configureAndStartService(atg.nucleus.naming.ComponentName, atg.nucleus.Configuration, atg.naming.NameContext, atg.nucleus.ConfigurationLockRegistry, java.lang.Object, atg.nucleus.logging.ApplicationLogging)) && "
//			+ "execution(!private * atg.nucleus.NucleusNameResolver.configureAndStartService(atg.nucleus.naming.ComponentName, atg.nucleus.Configuration, atg.naming.NameContext, atg.nucleus.ConfigurationLockRegistry, java.lang.Object, atg.nucleus.logging.ApplicationLogging))")
//	public Object configure(ProceedingJoinPoint pjp) throws Throwable {
//		Object[] args = pjp.getArgs();
//		Configuration pConfig = (Configuration) args[1];
//		ApplicationLogging pLog = (ApplicationLogging) args[5];
//		if (pConfig == null) {
//			pLog.logError("Configuration object can not be null, we can not search for tokens");
//			return pjp.proceed();
//		}
//		if (pConfig instanceof PropertyConfiguration) {
//			PropertyConfiguration pc = (PropertyConfiguration) pConfig;
//			Properties props = pc.getProperties();
//			Properties absoluteProps = pc.getAbsoluteProperties();
//
//			if (props == null || props.isEmpty()) {
//				return pjp.proceed();
//			}
//
//			Enumeration<Object> propNames = props.keys();
//			while (propNames.hasMoreElements()) {
//				String propName = (String) propNames.nextElement();
//				Object propValue = props.get(propName);
//				pLog.logDebug(pConfig.getServiceName() + "." + propName + " value is of type "
//						+ (propValue == null ? "null" : propValue.getClass().getName()));
//			}
//			return pjp.proceed();
//		} else {
//			pLog.logWarning("Configuration of type " + pConfig.getClass().getName() + " are not supported for now");
//			return pjp.proceed();
//		}
//	}
	
	/**
	 * Retrieves the consul value for an annotated setter method
	 * @param call
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(DynamicConfig) && execution(* *.get*())")
	public Object getConsulValue(ProceedingJoinPoint call) throws Throwable {
		Object value = call.proceed();
		Object o = call.getTarget();
		TokenManager tm = (TokenManager)Nucleus.getGlobalNucleus().resolveName(TOKEN_MANAGER_PATH);
		if (tm == null) {
			if (o instanceof ApplicationLogging && ((ApplicationLogging)o).isLoggingError()) {
				((ApplicationLogging)o).logError("Can not retrieve the token manager service from the Nucleus");
			}
			return value;
		}
		if (value != null)  return value;
		
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
			tm.configureValue(call, value);
		}
		return value;
	}
	
}
