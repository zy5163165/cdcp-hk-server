/**
 * Author: Ronnie.Chen
 * Date: 14-4-23
 * Time: 下午9:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
package com.alcatelsbell.cdcp.nbi.ws;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Endpoint;

import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.cdcp.util.MBeanProxy;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-8
 * Time: 下午2:07
 * rongrong.chen@alcatel-sbell.com.cn
 */

@WebService(targetNamespace = "http://www.w3.org/2001/XMLSchema")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class CdcpNBIService2 {
	private Log logger = LogFactory.getLog(getClass());
	boolean debug = false; // 用于纯接口调试，不涉及数据库或接口操作

	@WebMethod
	public String getNeListInEms(String request) {
		logger.info("listEmsDevices<<" + request);

		StrResponse response = null;
		try {
			String emsName = null;// getFieldContent(request.getSummary(), "emsName");
			Document document = XMLUtil.parse(request);
			Element summary = XMLUtil.getElement(document, "summary", 0);
			Element recordInfo = XMLUtil.getElement(summary, "recordInfo", 0);
			NodeList fieldInfos = recordInfo.getElementsByTagName("fieldInfo");
			for (int i = 0; i < fieldInfos.getLength(); i++) {
				Element fieldInfo = (Element) fieldInfos.item(i);
				Element fieldContent = XMLUtil.getElement(fieldInfo, "fieldContent", 0);
				emsName = fieldContent.getTextContent();
			}

			response = new StrResponse();

			try {

				CEMS cems = null;
				if (!debug) {
					try {
						cems = (CEMS) DBDataUtil.findObjectByDn(CEMS.class, emsName);
					} catch (Exception e) {
						logger.error(e, e);
					}
				}
				if (debug || (cems != null && (cems.getStatus().intValue() != Constants.CEMS_STATUS_READY))) {
					response.setResultCode("1");
					response.setResultMsg("EMSBusy");
				} else {

					List<DeviceInfo> cDevices = null;
					if (!debug) {
						MBeanProxy<NodeAdminMBean> nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(emsName);
						try {
							cDevices = nodeAdminProxy.proxy.listDevices(CdcpServerUtil.findEms(emsName));
						} finally {
							nodeAdminProxy.close();
						}

						logger.info("cdevices size = "+ (cDevices == null ? null : cDevices.size()));
					}
					else {
						cDevices = new ArrayList<DeviceInfo>();
						cDevices.add(new DeviceInfo("dn1", "name1", "ems", "productNme1"));
						cDevices.add(new DeviceInfo("dn2", "name2", "ems", "productNme2"));
						cDevices.add(new DeviceInfo("dn3", "name3", "ems", "productNme3"));
					}
					if (cDevices != null && cDevices.size() > 0) {
						if (!debug) {
						//	DBDataUtil.executeQL("delete from DeviceInfo c where c.emsName = '" + emsName + "'");
						//	DBDataUtil.saveObjects(cDevices);
						}
					}
					if (cDevices != null && !cDevices.isEmpty()) {
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
                        long id = SysUtil.nextLongId();
                        logger.error("ERROR id="+id+"");
						response.setResultCode("1");
						response.setResultMsg("接口正在采集EMS全网数据,请稍候...");
					}
				}
			} catch (Exception e) {
                long id = SysUtil.nextLongId();
				logger.error(id+":"+e, e);
				response.setResultCode("1");


                if (e instanceof IOException) {
                    response.setResultMsg("采集平台IO异常;编号" +id);
                }
                else
                    response.setResultMsg("采集平台异常;编号:" + id);
			}
		} catch (Exception e) {
			logger.error(e, e);
		}

		logger.info("listEmsDevices>>" + response == null ? null : response.toXML());
		return response == null ? "" : response.toXML();
	}

	@WebMethod
	public String synchronizeNeInfoInEms(String request) {
		logger.info("syncDevice<<" + request);

		StrResponse response = null;
		try {
			Document document = XMLUtil.parse(request);
			Element summary = XMLUtil.getElement(document, "summary", 0);
			Element fieldInfo0 = XMLUtil.getElement(summary, "fieldInfo", 0);
			String projectName = XMLUtil.getElement(fieldInfo0, "fieldContent", 0).getTextContent();

			Element fieldInfo1 = XMLUtil.getElement(summary, "fieldInfo", 1);
			String emsName = XMLUtil.getElement(fieldInfo1, "fieldContent", 0).getTextContent();

			List<String> neDNs = new ArrayList<String>();
			NodeList recordInfos = document.getElementsByTagName("recordInfo");
			for (int i = 0; i < recordInfos.getLength(); i++) {
				Element recordInfo = (Element) recordInfos.item(i);
				Element fieldInfo = XMLUtil.getElement(recordInfo, "fieldInfo", 0);
				Element fieldContent = XMLUtil.getElement(fieldInfo, "fieldContent", 0);
				neDNs.add(fieldContent.getTextContent());
			}

			response = new StrResponse();

			CEMS cems = null;
			if (!debug) {
				try {
					cems = (CEMS) DBDataUtil.findObjectByDn(CEMS.class, emsName);
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
			if (debug || (cems != null && (cems.getStatus().intValue() != Constants.CEMS_STATUS_READY))) {
				response.setResultCode("1");
				response.setResultMsg("EMSBusy");
			} else {
				if (neDNs != null) {
					projectName = projectName + ">>" + SysUtil.nextLongId() + "##" + neDNs.size();
					for (String neDN : neDNs) {
						try {
							String serial = CdcpServerUtil.syncDevice(emsName, neDN);
							// 把projectname保存下来，返回时还给IRM
							try {
								String ql = " update Task c set c.tag2 = '" + projectName + "' where c.dn = '" + serial + "'";
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

			logger.info("syncDevice>>" + response.toXML());
		} catch (Exception e) {
			logger.error(e, e);
		}
		return response == null ? "" : response.toXML();
	}

	@WebMethod
	public String synchronize_sectionInfoInEms(String request) {
		logger.info("synchronize_sectionInfoInEms<<" + request);

		StrResponse response = null;
		try {
			Document document = XMLUtil.parse(request);
			Element summary = XMLUtil.getElement(document, "summary", 0);
			Element fieldInfo0 = XMLUtil.getElement(summary, "fieldInfo", 0);
			String projectName = XMLUtil.getElement(fieldInfo0, "fieldContent", 0).getTextContent();

			Element fieldInfo1 = XMLUtil.getElement(summary, "fieldInfo", 1);
			final String emsName = XMLUtil.getElement(fieldInfo1, "fieldContent", 0).getTextContent();

			List<String> neDNs = new ArrayList<String>();
			NodeList recordInfos = document.getElementsByTagName("recordInfo");
			for (int i = 0; i < recordInfos.getLength(); i++) {
				Element recordInfo = (Element) recordInfos.item(i);
				Element fieldInfo = XMLUtil.getElement(recordInfo, "fieldInfo", 0);
				Element fieldContent = XMLUtil.getElement(fieldInfo, "fieldContent", 0);
				neDNs.add(fieldContent.getTextContent());
			}

			response = new StrResponse();

			CEMS cems = null;
			if (!debug) {
				try {
					cems = (CEMS) DBDataUtil.findObjectByDn(CEMS.class, emsName);
				} catch (Exception e) {
					logger.error(e, e);
				}
			}
			if (debug || (cems != null && (cems.getStatus().intValue() != Constants.CEMS_STATUS_READY))) {
				response.setResultCode("1");
				response.setResultMsg("EMSBusy");
			} else {
				if (neDNs != null) {
//					projectName = projectName + ">>" + SysUtil.nextLongId() + "##" + neDNs.size();
					final List<String> _neDns = neDNs;
					final String _projectName = projectName;

					Thread t  = new Thread( ) {
						public void run() {
							for (String neDN : _neDns) {
								try {
									CdcpServerUtil.syncDeviceSection(emsName,neDN);
								} catch ( Exception e) {
									logger.error(e, e);
									//response.setResultCode("1");

								}
							}

							IrmsClientUtil.callBackIRMDeviceSectionSync(_projectName,0,"");
						}
					};
					t.start();

				}
			}
			response.setResultCode("0");
			logger.info("syncDevice>>" + response.toXML());
		} catch (Exception e) {
			logger.error(e, e);
		}
		return response == null ? "" : response.toXML();
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
		CdcpNBIService2 cdcpNBIService = new CdcpNBIService2();
		String nbiWsUrl = SysProperty.getString("nbiWsUrl", "http://0.0.0.0:9090/cdcpnbi");

		Endpoint.publish(nbiWsUrl, cdcpNBIService);

		Thread.sleep(1000000l);
	}
}
