package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.server.adapters.DBDataUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.DeviceInfo;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-8
 * Time: 下午4:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DeviceInfoManager {
	private Log logger = LogFactory.getLog(getClass());
	private static DeviceInfoManager ourInstance = new DeviceInfoManager();

	public static DeviceInfoManager getInstance() {
		return ourInstance;
	}

	private DeviceInfoManager() {
	}

	public void updateCache(String emsName, List<DeviceInfo> deviceInfos) {
		DBDataUtil.executeQL("delete from DeviceInfo c where c.emsName = '" + emsName + "'");
		for (DeviceInfo deviceInfo : deviceInfos) {
			if (deviceInfo.getDn() == null)
				deviceInfo.setDn(deviceInfo.getDeviceDn());
		}
		DBDataUtil.saveObjects(deviceInfos);
	}

	public String getDeviceDn(String deviceName) {
		DeviceInfo oneObject = null;
		try {
			oneObject = (DeviceInfo) DBDataUtil.findOneObject("select c from DeviceInfo c where c.deviceName = '" + deviceName + "'");
		} catch (Exception e) {
			logger.error(e, e);
		}
		if (oneObject != null)
			return oneObject.getDeviceDn();

		return null;
	}
}
