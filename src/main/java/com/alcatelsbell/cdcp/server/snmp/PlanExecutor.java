package com.alcatelsbell.cdcp.server.snmp;

import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.cdcp.nbi.model.CPTP;
import com.alcatelsbell.cdcp.util.DataInserter;
//import com.alcatelsbell.cdcp.util.DatabaseUtil;
//import com.alcatelsbell.itmanager2.common.ds.HostInfo;
//import com.alcatelsbell.itmanager2.common.ds.IFInfo;
//import com.alcatelsbell.itmanager2.server.protocal.snmp.DeviceSnmpCollector;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.config.SnmpParameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-8-25
 * Time: 下午7:24
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PlanExecutor   {
    private Log logger = LogFactory.getLog(getClass());
    private String path;

    public PlanExecutor(String path) {
        this.path = path;
    }


    public String run() {
//        int count = 0;
//        int ptpcount = 0;
//        try {
//            DocumentBuilder dombuilder = DocumentBuilderFactory.newInstance()
//                    .newDocumentBuilder();
//            InputStream is = null;
//            try {
//                is = new FileInputStream(path);
//            } catch (FileNotFoundException e) {
//
//            }
//            if (is == null)
//                is = PlanExecutor.class.getClassLoader().getResourceAsStream(path);
//            Document doc = dombuilder.parse(is);
//            Element deviceList = getElement(doc, "DeviceList", 0);
//            NodeList nodeList = deviceList.getElementsByTagName("Device");
//            JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
//            jpaSupport.begin();
//            for (int i = 0; i < nodeList.getLength(); i++) {
//
//                    Node device = nodeList.item(i);
//                    String ip = device.getAttributes().getNamedItem("ip").getNodeValue();
//                try {
//                    String rcommunity = device.getAttributes().getNamedItem("rcommunity").getNodeValue();
//                    String mib = device.getAttributes().getNamedItem("mib").getNodeValue();
//                    System.out.println("采集设备 : ip = " + ip);
//                    DeviceSnmpCollector collector = new DeviceSnmpCollector();
//                    SnmpParameter sp = new SnmpParameter();
//                    sp.setIpAddress(ip);
//                    sp.setReadCommunity(rcommunity);
//                    HostInfo hostInfo = collector.collectHostInfo(sp);
//                    CDevice cDevice = new CDevice();
//                    cDevice.setIpAddress(ip);
//                    cDevice.setNativeEmsName(hostInfo.getName());
//                    cDevice.setAdditionalInfo(hostInfo.getDescription());
//                    cDevice.setNeVersion(hostInfo.getOsname());
//                    cDevice.setUserLabel(hostInfo.getName());
//                    cDevice.setDn(ip);
//                    cDevice.setType(hostInfo.getDescription());
//                    cDevice.setSid(DatabaseUtil.nextSID(CDevice.class));
//                    JPAUtil.getInstance().storeObjectByDn(jpaSupport,-1,cDevice);
//                    //JpaClient.getInstance("jpa.data").storeObjectByDn(-1,cDevice);
//                    count ++;
//                    List<IFInfo> ifInfos = hostInfo.getIfInfos();
//                    if (ifInfos != null) {
//                        for (IFInfo ifInfo : ifInfos) {
//                            ptpcount ++;
//                            CPTP ptp = new CPTP();
//                            ptp.setDn(cDevice.getDn()+"@"+ifInfo.getIndex());
//                            ptp.setNo(ifInfo.getIndex() + "");
//                            ptp.setNativeEMSName(ifInfo.getName());
//                            ptp.setUserLabel(ifInfo.getName());
//                            ptp.setDeviceDn(cDevice.getDn());
//                            ptp.setIpAddress(ip);
//                            ptp.setMacAddress(ifInfo.getPhysicalAddress());
//                            ptp.setSid(DatabaseUtil.nextSID(CPTP.class));
//                            if (ifInfo.getSpeed() != null)
//                                ptp.setSpeed((ifInfo.getSpeed() / (1000 * 1000))+"M");
//                            JPAUtil.getInstance().storeObjectByDn(jpaSupport,-1,ptp);
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error(e, e);
//                    e.printStackTrace();
//                    System.out.println("采集失败 = " + ip);
//                }
//
//
//            }
//
//            jpaSupport.end();
//        } catch (Exception e) {
//            logger.error(e, e);
//            e.printStackTrace();
//        }
//
  //      return "采集成功,\n设备数="+count+"; \n端口数="+ptpcount+"\n性能数据="+(count * 3 + ptpcount * 6+"\n 元数据="+(count * 47));
        return "";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static Document parse(String xml) throws Exception {
        DocumentBuilder dombuilder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = dombuilder.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
        return doc;
    }

    public static Element getElement(Element ele,String tag,int idx) {
        return (Element)ele.getElementsByTagName(tag).item(idx);
    }

    public static Element getElement(Document ele,String tag,int idx) {
        return (Element)ele.getElementsByTagName(tag).item(idx);
    }

    public static void main(String[] args) {
        new PlanExecutor("snmp-device-schedule1.xml").run();
    }
}
