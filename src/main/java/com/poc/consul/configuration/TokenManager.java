package com.poc.consul.configuration;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import atg.beans.DynamicBeans;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.Nucleus;
import atg.nucleus.ServiceException;
import atg.service.cache.Cache;

public class TokenManager extends GenericService {
	Cache mConsulCache;
	
	public void doStartService() throws ServiceException {
		super.doStartService();
	}
	
	/**
	 * Get the value of a given token from the cache
	 * @param pTokenName
	 * @return
	 */
	public Object findValueForToken(String pTokenName) {
		if (StringUtils.isBlank(pTokenName)) {
			vlogError("Can not retrieve the value of a token with an empty name");
			return null;
		}
		Object tokenValue = null;
		try {
			tokenValue = getConsulCache().get(pTokenName);
		} catch (Exception exc) {
			vlogError(exc, "Unable to retrieve the value of the token named {0}", pTokenName);
		}
		
		vlogDebug("Found value {0} from token {1}", tokenValue, pTokenName);
		return tokenValue;
	}
	
	/**
	 * 
	 * @param pCall
	 * @param setterName
	 * @param pValue
	 */
	public Object configureValue(ProceedingJoinPoint pCall, Object pValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		GenericService pConfigurableService = (GenericService)pCall.getTarget();
		
		MethodSignature signature = (MethodSignature)pCall.getSignature();
		Method m = signature.getMethod();
		String setterName = StringUtils.replace(m.getName(), "get", "set");
		String memberName = Introspector.decapitalize(setterName.substring(3));
		
		if (pConfigurableService == null) {
			vlogError("The configurable service can not be null");
			return null;
		}
		if (pValue == null) {
			vlogDebug("There is no need to set a null value on {0}", ((GenericService)pConfigurableService).getAbsoluteName());
			return null;
		}
		Class returnType = m.getReturnType();
		Method setter = pConfigurableService.getClass().getMethod(setterName, returnType);
		if (setter == null) {
			vlogError("Setter method {0} either does not exist or is not accessible on class {1}", setterName, pConfigurableService.getClass().getName());
			return null;
		}
		
		// --- in case the setter is setting a Nucleus object, we need to resolve it
		
		try {
			if (returnType.isAssignableFrom(GenericService.class)) {
				vlogDebug("We need to resolve component with path {0} for setter", pValue);
				setter.invoke(pConfigurableService, Nucleus.getGlobalNucleus().resolveName((String)pValue));
			} else if (returnType.isAssignableFrom(int.class)){
				final int v = Integer.parseInt((String)pValue);
				setter.invoke(pConfigurableService, v);
				return v;
			} else if (returnType.isAssignableFrom(float.class)){
				final float v = Float.parseFloat((String)pValue);
				setter.invoke(pConfigurableService, v);
				return v;
			} else if (returnType.isAssignableFrom(double.class)){
				final double v = Double.parseDouble((String)pValue);
				setter.invoke(pConfigurableService, v);
				return v;
			} else {
				
				DynamicBeans.setPropertyValueFromString(pConfigurableService, memberName, (String)pValue);
			} 
		} catch (Exception exc) {
			vlogError(exc, "Could not set value {0} on {1} using {2}", pValue, pConfigurableService.getAbsoluteName(), setter.getName());
		}
		return pValue;
	}
	
	public Cache getConsulCache() {
		return mConsulCache;
	}
	public void setConsulCache(Cache pConsulCache) {
		mConsulCache = pConsulCache;
	}
}
