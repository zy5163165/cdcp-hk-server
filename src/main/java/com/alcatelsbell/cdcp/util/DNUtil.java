package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-24
 * Time: 下午12:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DNUtil {
    public static String extractNEDn(String endDn) {
        int idx = endDn.indexOf("@",endDn.indexOf("ManagedElement:"));
        if (idx == -1) return endDn;
        return endDn.substring(0,idx);
    }
    public static String extractShelfDn(String endDn) {
        int idx = endDn.indexOf("/",endDn.indexOf("shelf="));
        if (idx == -1) return endDn;
        String s = endDn.substring(0,idx);
        if (s.contains("PTP:"))
            return s.replace("PTP:", "EquipmentHolder:");
        if (s.contains("FTP:"))
            return s.replace("FTP:","EquipmentHolder:");
        if (s.contains("Equipment:"))
            return s.replace("Equipment:","EquipmentHolder:");
        return s;
    }
    public static String extractRackDn(String endDn) {
        int idx = endDn.indexOf("/",endDn.indexOf("rack="));
        if (idx == -1) return endDn;
        String s = endDn.substring(0,idx);
        if (s.contains("PTP:"))
            return s.replace("PTP:", "EquipmentHolder:");
        if (s.contains("FTP:"))
            return s.replace("FTP:","EquipmentHolder:");
        if (s.contains("Equipment:"))
            return s.replace("Equipment:","EquipmentHolder:");
        return s;
    }

    public static String extractOMSCtp(String ctp) {
        int i = ctp.indexOf("oms=");
        int j = ctp.indexOf("/",i);
        if (i > 0 && j >0)
            return ctp.substring(0,j);
        return null;
    }

    public static String extractSlotDn(String endDn) {
        int idx = endDn.indexOf("/",endDn.indexOf("slot="));
        if (idx == -1) return endDn;
        String s = endDn.substring(0,idx);
        if (s.contains("PTP:"))
            return s.replace("PTP:", "EquipmentHolder:");
        if (s.contains("FTP:"))
            return s.replace("FTP:","EquipmentHolder:");
        if (s.contains("Equipment:"))
            return s.replace("Equipment:","EquipmentHolder:");
        return s;
    }
    public static String extractCardDn(String endDn) {
        return  extractSlotDn(endDn)+"@Equipment:1";
    }
    public static String extractPortDn(String endDn) {
        if (endDn.contains("@CTP"))
            return endDn.substring(0,endDn.indexOf("@CTP"));
        if (endDn.contains("port=")) {
            int end = endDn.indexOf("/"+endDn.indexOf("port="));
            if (end > -1)
                return endDn.substring(0,end);
        }
        return endDn;
    }
    public static String extractPortNumber(String endDn) {
        if (endDn.contains("port=")) {
           return endDn.substring(endDn.lastIndexOf("port=")+5);
        }
        return null;
    }

    public static String getParentCTPdn(String ctpDn) {
        int i = ctpDn.indexOf("CTP:/");
        if (i > 0) {
            int j = ctpDn.lastIndexOf("/");
            if (i+4 < j) {
                return ctpDn.substring(0,ctpDn.lastIndexOf("/"));
            }
        }
        return null;
    }

    public static String extractCTPSimpleName(String ctpDn) throws Exception {
        if (ctpDn.contains("CTP:"))
            return ctpDn.substring(ctpDn.indexOf("CTP:")+4);
        else throw new Exception("无法解析出CTP名称:"+ctpDn);
    }

    public static String compressCCDn(String ccDn) {
        if (true)
            return ccDn;
        int i = ccDn.indexOf("_/rack=");
        if (i > -1) {
            String aend = ccDn.substring(0, i);
            String zend = ccDn.substring(i);


            int ai = aend.indexOf("/rack=");
            String aHead = aend.substring(0, ai);
            String atail = aend.substring(ai);
            String[] split = atail.split("/rack=");
            List<String> tails = merge(split);
            aend = aHead;
            for (String tail : tails) {
                aend +="/rack="+tail;
            }

            int zi = zend.indexOf("/rack=");
            String zHead = zend.substring(0, zi);
            String ztail = zend.substring(zi);
            String[] split2 = ztail.split("/rack=");
            List<String> ztails = merge(split2);
            zend = zHead;
            for (String tail : ztails) {
                zend +="/rack="+tail;
            }

            ccDn = aend + zend;
        }

        return ccDn.replaceAll("ManagedElement:","").replaceAll("CrossConnect:","").replaceAll("rack=","")
                .replaceAll("shelf=","").replaceAll("slot=","").replaceAll("domain=","").replaceAll("port=","");
    }

    public static List<String> merge(String[] s) {
        List l = new ArrayList();
        for (String s1 : s) {
            if (!s1.trim().isEmpty()) {
                if (!l.contains(s1))
                    l.add(s1);
            }
        }
        return l;


    }

    public static String extractValue(String dn ,String key) {
        String k = "/"+key+"=";
        if (dn.contains(k)) {
            int beginIndex = dn.indexOf(k) + k.length();
            int endIndex = dn.indexOf("/", beginIndex);
            if (endIndex > -1)
                return dn.substring(beginIndex, endIndex) ;
            else
                return dn.substring(beginIndex);
        }
        return null;

    }

    public static String extractOCHno(String ctpDn) {
        if (ctpDn.contains("och="))
            return ctpDn.substring(ctpDn.indexOf("och=")+4);
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(DNUtil.extractPortDn("EMS:JXI-OTNM2000-1-P@ManagedElement:134232284;69898@FTP:/rack=603905/shelf=1/slot=17826833/port=36@CTP:/ethernet=139468865"));
    }
}
