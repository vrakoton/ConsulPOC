package com.poc.consul.configuration;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import atg.core.util.StringUtils;
import atg.nucleus.Configuration;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;
import atg.nucleus.StartupComponentProcessor;
import atg.nucleus.logging.ApplicationLogging;

public class TokenComponentProcessor extends GenericService implements StartupComponentProcessor {
	boolean mEnabled = true;
	String mTokenPattern;
	Pattern mCompiledPattern;
	
	public void doStartService() throws ServiceException {
		super.doStartService();
		if (!StringUtils.isBlank(getTokenPattern())) {
			mCompiledPattern = Pattern.compile(getTokenPattern());
		} else {
			vlogError("Can not parse the token detection pattern, disabling component");
			setEnabled(false);
		}
	}
	
	
	/**
	 * processing the components in order to replace the dynamic properties
	 */
	@Override
	public void processComponent(Object pObject, Configuration pConfiguration,
			ApplicationLogging pApplicationLogging) throws ServiceException {
		if (!isEnabled())
			return;
		
		if (pObject == null) {
			vlogError("Can not process a null object");
		}
		
		Class c = pObject.getClass();
		if (pConfiguration == null) {
			vlogError("Configuration for this component is null, can not introspect tokens");
			return;
		}
		
		Field [] fields = c.getDeclaredFields();
		String fieldName = null;
		Method getter = null;
		Method setter = null; 
		
		for (Field f : fields) {
			// --- ignore static fields
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			
			fieldName = stripPrefix(f.getName(), c);
			
			try {
				vlogDebug("Searching for public getter for field {0} in class {1}", fieldName, c.getName());
				
				PropertyDescriptor pd = new PropertyDescriptor(fieldName, c);
				getter = pd.getReadMethod();
				setter = pd.getWriteMethod(); 
				
				if (getter == null || !Modifier.isPublic(getter.getModifiers())) {
					vlogError("Could not find public getter {0} on class {1}", getter.getName(), c.getName());
					continue;
				}
				
				vlogInfo("invoking {0} on {1}", getter.getName(), pObject.getClass().getName());
				Object value = getter.invoke(pObject);
				if (value == null || !mCompiledPattern.matcher(value.toString()).matches()) {
					vlogDebug("Value of getter {0} on class {1} is not tokenized: {2}", getter.getName(), c.getName(), value);
					continue;
				}
				vlogDebug("Resolving value for token {0} on component {1}", value, pConfiguration.getServiceName());
			} catch (InvocationTargetException | IllegalAccessException exc) {
				vlogError(exc, "There was an error invoking the getter method {0} on class {1}", getter.getName(), c.getName());
			} catch (IntrospectionException ie) {
				// --- when no getter or no setter is found, it throws an introspection exception that we do not want to log
			}
		}
	}
	
	/**
	 * removes classic ATG prefix (ex: 'm') for members 
	 * @param pMemberName
	 * @param pClass
	 * @return
	 */
	public String stripPrefix(String pMemberName, Class pClass) {
		if (pMemberName == null) {
			return pMemberName;
		}
		if (pMemberName != null && pMemberName.startsWith("m")) {
			vlogDebug("Stripping the classic m ATG prefix for class member on field {0} in class {1}", pMemberName, pClass.getName());
			return pMemberName.substring(1);
		}
		return pMemberName;
	}
	
	public String getGetterName(String pMemberName, Class pClass) {
		
		StringBuffer b = new StringBuffer("get");
		b.append(pMemberName.substring(0,1).toUpperCase());
		b.append(pMemberName.substring(1));
		return b.toString();
	}
	
	public boolean isEnabled() {
		return mEnabled;
	}
	public void setEnabled(boolean pEnabled) {
		mEnabled = pEnabled;
	}


	public String getTokenPattern() {
		return mTokenPattern;
	}
	public void setTokenPattern(String pTokenPattern) {
		mTokenPattern = pTokenPattern;
	}
}
