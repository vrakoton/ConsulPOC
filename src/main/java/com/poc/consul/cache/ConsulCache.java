package com.poc.consul.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import atg.core.util.StringUtils;
import atg.nucleus.ServiceException;
import atg.service.cache.Cache;
import atg.service.perfmonitor.PerformanceMonitor;
import atg.service.scheduler.Schedulable;
import atg.service.scheduler.Schedule;
import atg.service.scheduler.ScheduledJob;
import atg.service.scheduler.Scheduler;

/**
 * 
 * @author vrakoton
 * 
 * Description:
 * 
 * Custom implementation of a Cache to enable persistence to a file and
 * cache reloading at startup. 
 *
 */
public class ConsulCache extends Cache implements Schedulable {
	private static final long serialVersionUID = -2875690723007346817L;
	Scheduler mScheduler;
	Schedule mSchedule;
	int mJobId;
	
	String mJobName;
	String mJobDescription;
	String mCacheFilePath = "/tmp/cache.json";
	boolean mPersistCache = true;
	boolean mReloadCacheOnStartup = true;
	
	/**
	 * Registers the service to the scheduler when it is started. This component can also
	 * reload the latest version of the cache which has been persisted to improve startup speed
	 * or to mask Consul disruption
	 */
	public void doStartService() throws ServiceException {
		super.doStartService();
		
		if ((isPersistCache() || isReloadCacheOnStartup()) && StringUtils.isBlank(getCacheFilePath())) {
			throw new ServiceException("The cache file path must be specified for this service to be able to save the cache content");
		}
		
		if (isReloadCacheOnStartup()) {
			initCacheFromFile();
		}
		
		
		ScheduledJob job = new ScheduledJob (getJobName(),
                getJobDescription(),
                getAbsoluteName(),
                getSchedule(),
                this,
                ScheduledJob.SEPARATE_THREAD);
		mJobId = getScheduler().addScheduledJob(job);
	}
	
	
	/**
	 * This method regularly saves the content of the cache for future start (this is useful if Consul is not up and running)
	 */
	@Override
	public void performScheduledTask(Scheduler pScheduler, ScheduledJob pScheduledJob) {
		saveCache();
	}
	
	/**
	 * reads the cache from the latest saved Json file
	 */
	public void initCacheFromFile() {
		PerformanceMonitor.startOperation("ConsulCache", "initCacheFromFile");
		if (StringUtils.isBlank(getCacheFilePath())) {
			vlogError("The cache file path is not specified, aborting...");
			return;
		}
		File f = new File(getCacheFilePath());
		if (!f.exists() || !f.canWrite()) {
			vlogError("File {0} does not exists or is not writable", f.getAbsolutePath());
			return;
		}
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(getCacheFilePath()));
	
	        Gson gson = new Gson();
	        Map<String, String> cacheMap = gson.fromJson(bufferedReader, HashMap.class);
	        if (cacheMap == null || cacheMap.isEmpty()) return;
	        
	        for(Map.Entry<String, String> entry : cacheMap.entrySet()) {
	        	put(entry.getKey(), entry.getValue());
	        }
	        
		} catch (FileNotFoundException fnfe) {
			vlogError(fnfe, "File {0} could not be found", f.getAbsolutePath());
		} finally {
			PerformanceMonitor.endOperation("ConsulCache", "initCacheFromFile");
		}
	}
	
	/**
	 * This method writes the cache content to a file that can be reused to reload
	 * cache at startup.
	 */
	public void saveCache() {
		PerformanceMonitor.startOperation("ConsulCache", "saveCache");
		vlogDebug("Staring token cache serialization");
		File f = new File(getCacheFilePath());
		Iterator<String> it = getAllKeys();
		
		if (it == null) {
			vlogInfo("Cache is empty, we will not write anything");
			return;
		}
		
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(f);
			Map<String, String> cache = new HashMap<String, String>();
			while (it.hasNext()) {
				final String key = it.next();
				cache.put(key, (String)get(key));
			}
			Gson gson = new GsonBuilder().create();
			String json = gson.toJson(cache);
			fw.write(json);
		} catch (IOException ioe) {
			vlogError(ioe, "Can not greate file {0}", getCacheFilePath());
		} catch (Exception exc) {
			vlogError(exc, "Could not persist cache to {0} because of an error", getCacheFilePath());
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException ioe) {
					vlogError(ioe, "Can not close {0} file successfully", getCacheFilePath());
				}
			}
			PerformanceMonitor.endOperation("ConsulCache", "saveCache");
		}
	}

	public Scheduler getScheduler() {
		return mScheduler;
	}

	public void setScheduler(Scheduler pScheduler) {
		mScheduler = pScheduler;
	}

	public Schedule getSchedule() {
		return mSchedule;
	}

	public void setSchedule(Schedule pSchedule) {
		mSchedule = pSchedule;
	}

	public int getJobId() {
		return mJobId;
	}

	public String getJobName() {
		return mJobName;
	}

	public void setJobName(String pJobName) {
		mJobName = pJobName;
	}

	public String getJobDescription() {
		return mJobDescription;
	}

	public void setJobDescription(String pJobDescription) {
		mJobDescription = pJobDescription;
	}


	public String getCacheFilePath() {
		return mCacheFilePath;
	}


	public void setCacheFilePath(String pCacheFilePath) {
		mCacheFilePath = pCacheFilePath;
	}


	public boolean isPersistCache() {
		return mPersistCache;
	}


	public void setPersistCache(boolean pPersistCache) {
		mPersistCache = pPersistCache;
	}


	public boolean isReloadCacheOnStartup() {
		return mReloadCacheOnStartup;
	}


	public void setReloadCacheOnStartup(boolean pReloadCacheOnStartup) {
		mReloadCacheOnStartup = pReloadCacheOnStartup;
	}
	
}
