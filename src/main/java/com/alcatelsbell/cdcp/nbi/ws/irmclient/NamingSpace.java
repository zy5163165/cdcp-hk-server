package com.alcatelsbell.cdcp.nbi.ws.irmclient;

import com.alcatelsbell.nms.util.SysProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-24
 * Time: 下午11:28
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class NamingSpace {
	// public static final String ns = "http://10.212.40.12:6120/irm/services/irmsDataSyncWebService";
	public static final String ns_default = "http://10.211.106.219:6020/irm/services/irmsDataSyncWebService";
	public static final String ns = SysProperty.getString("cdcp.nbi.irm.ns", ns_default);

}
