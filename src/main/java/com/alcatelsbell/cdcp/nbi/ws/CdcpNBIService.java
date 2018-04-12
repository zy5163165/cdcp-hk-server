package com.alcatelsbell.cdcp.nbi.ws;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Endpoint;

import com.alcatelsbell.nms.common.SysUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.DeviceInfo;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.CEMS;
import com.alcatelsbell.cdcp.nodefx.NodeException;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.server.adapters.DBDataUtil;
import com.alcatelsbell.nms.util.SysProperty;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-8
 * Time: 下午2:07
 * rongrong.chen@alcatel-sbell.com.cn
 */

@WebService(targetNamespace = "http://cdcp.alcatel-sbell.com.cn/nbiservice")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class CdcpNBIService {
	private Log logger = LogFactory.getLog(getClass());

	@WebMethod
	public StrResponse getNeListInEms(
			@WebParam(name = "strRequest", mode = WebParam.Mode.IN, targetNamespace = "http://cdcp.alcatel-sbell.com.cn/nbiservice") StrRequest request) {
		logger.info("listEmsDevices<<" + request);
		StrResponse response = new StrResponse();
		try {
			String emsName = getFieldContent(request.getSummary(), "emsName");
			List<DeviceInfo> cDevices = CdcpServerUtil.createNodeAdminProxy(emsName).proxy.listDevices(CdcpServerUtil.findEms(emsName));
			if (cDevices != null && !cDevices.isEmpty()) {
				DBDataUtil.executeQL("delete from DeviceInfo c where c.emsName = '" + emsName + "'");
				DBDataUtil.saveObjects(cDevices);

				response.setResultCode("0");
				ArrayList<List<FieldInfo>> list = new ArrayList<List<FieldInfo>>();
				response.setRecordInfo(list);
				for (DeviceInfo cDevice : cDevices) {
					ArrayList<FieldInfo> fields = new ArrayList<FieldInfo>();
					list.add(fields);
					fields.add(new FieldInfo("网元名称", "neName", cDevice.getDeviceName()));
					fields.add(new FieldInfo("网元DN", "neDn", cDevice.getDeviceDn()));
					fields.add(new FieldInfo("网元型号", "neModel", cDevice.getProductNme()));
				}
			} else {
				response.setResultCode("1");
				response.setResultMsg("该EMS数据尚未同步过.");
			}
		} catch (Exception e) {
			logger.error(e, e);
			response.setResultCode("1");
			response.setResultMsg("数据库异常:" + e.getMessage());
		}
		logger.info("listEmsDevices>>" + response);
		return response;
	}

	@WebMethod
	public StrResponse synchronizeNeInfoInEms(
			@WebParam(name = "strRequest", mode = WebParam.Mode.IN, targetNamespace = "http://cdcp.alcatel-sbell.com.cn/nbiservice") StrRequest request) {
		logger.info("syncDevice<<" + request);

		StrResponse response = new StrResponse();
		try {
			CEMS cems = null;
			String emsName = getFieldContent(request.getSummary(), "emsName");
			String projectName = getFieldContent(request.getSummary(), "projectName");
            projectName = projectName+">>"+SysUtil.nextLongId();
			List<String> neDNs = getFieldContents(request.getSummary(), "neDn");
			cems = (CEMS) DBDataUtil.findObjectByDn(CEMS.class, emsName);
			if (cems != null && (cems.getStatus().intValue() != Constants.CEMS_STATUS_READY)) {
				response.setResultCode("1");
				response.setResultMsg("EMSBusy");
			} else {
				if (neDNs != null) {
					for (String neDN : neDNs) {
						try {
							String serial = CdcpServerUtil.syncDevice(emsName, neDN);
							// 把projectname保存下来，返回时还给IRM
							try {
								String ql = " update Task c set c.tag2 = " + projectName + " where c.dn = '" + serial + "'";
								CdcpServerUtil.executeUpdateQl(ql);
							} catch (Exception e) {
								logger.error(e, e);
							}
							response.setResultCode("0");
						} catch (NodeException e) {
							logger.error(e, e);
							response.setResultCode("1");
							response.setResultMsg(e.getMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e, e);
			response.setResultCode("1");
			response.setResultMsg(e.getMessage());
		}
		logger.info("syncDevice>>" + response);
		return response;
	}

	private String getFieldContent(List<FieldInfo> fieldInfos, String fieldEnName) {
		for (FieldInfo fieldInfo : fieldInfos) {
			if (fieldInfo.getFieldEnName() != null && fieldInfo.getFieldEnName().equals(fieldEnName))
				return fieldInfo.getFieldContent();
		}
		return null;
	}

	private List<String> getFieldContents(List<FieldInfo> fieldInfos, String fieldEnName) {
		List<String> list = new ArrayList<String>();
		for (FieldInfo fieldInfo : fieldInfos) {
			if (fieldInfo.getFieldEnName() != null && fieldInfo.getFieldEnName().equals(fieldEnName))
				list.add(fieldInfo.getFieldContent());
		}
		return list;
	}

	public static void main(String[] args) throws InterruptedException {
		CdcpNBIService cdcpNBIService = new CdcpNBIService();
		String nbiWsUrl = SysProperty.getString("nbiWsUrl", "http://0.0.0.0:9090/cdcpnbi");

		Endpoint.publish(nbiWsUrl, cdcpNBIService);

		Thread.sleep(1000000l);
	}
}
