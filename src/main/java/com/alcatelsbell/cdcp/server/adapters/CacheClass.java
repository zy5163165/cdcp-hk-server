package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.CCTP;
import com.alcatelsbell.cdcp.nbi.model.CCrossConnect;
import com.alcatelsbell.cdcp.nbi.model.CRoute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-11
 * Time: 下午4:16
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CacheClass {
    //T开头的类,用来做缓存
    public static class T_CRoute {
        private String aend;
        private String zend;
        private String dn;
        private Long sid;
        private String aends;
        private String zends;

        public T_CRoute() {
        }

        public T_CRoute(CRoute route) {
            aend = route.getAend();
            zend = route.getZend();
            aends = route.getAends();
            zends = route.getZends();
            dn = route.getDn();
            sid = route.getSid();
        }
        public Long getSid() {
            return sid;
        }

        public void setSid(Long sid) {
            this.sid = sid;
        }

        public String getAend() {
            return aend;
        }
        public void setAend(String aend) {
            this.aend = aend;
        }
        public String getZend() {
            return zend;
        }
        public void setZend(String zend) {
            this.zend = zend;
        }
        public String getDn() {
            return dn;
        }
        public void setDn(String dn) {
            this.dn = dn;
        }

        public String getAends() {
            return aends;
        }

        public void setAends(String aends) {
            this.aends = aends;
        }

        public String getZends() {
            return zends;
        }

        public void setZends(String zends) {
            this.zends = zends;
        }
    }
    public static class T_CCrossConnect{
        public T_CCrossConnect() {
        }

        public T_CCrossConnect(CCrossConnect cc) {
            this.aend = cc.getAend();
            this.zend = cc.getZend();
            this.dn = cc.getDn();
        }

        private String aend;
        private String zend;
        private String dn;

        public String getAend() {
            return aend;
        }

        public void setAend(String aend) {
            this.aend = aend;
        }

        public String getZend() {
            return zend;
        }

        public void setZend(String zend) {
            this.zend = zend;
        }

        public String getDn() {
            return dn;
        }

        public void setDn(String dn) {
            this.dn = dn;
        }
    }

    public static class T_CTP {
        public T_CTP() {
        }

        public T_CTP(CCTP ctp) {
            this.dn = ctp.getDn();
            this.rate = ctp.getRate();
            this.nativeEMSName = ctp.getNativeEMSName();
            this.direction = ctp.getDirection();
            this.portdn = ctp.getPortdn();
            this.parentCtp = ctp.getParentCtpdn();
        }
        private String rate;
        private String nativeEMSName;
        private String dn;
        private Integer direction;
        private String portdn;
        private String parentCtp;

        public String getParentCtp() {
            return parentCtp;
        }

        public void setParentCtp(String parentCtp) {
            this.parentCtp = parentCtp;
        }

        public String getPortdn() {
            return portdn;
        }

        public void setPortdn(String portdn) {
            this.portdn = portdn;
        }

        public Integer getDirection() {
            return direction;
        }

        public void setDirection(Integer direction) {
            this.direction = direction;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public String getNativeEMSName() {
            return nativeEMSName;
        }

        public void setNativeEMSName(String nativeEMSName) {
            this.nativeEMSName = nativeEMSName;
        }

        public String getDn() {
            return dn;
        }

        public void setDn(String dn) {
            this.dn = dn;
        }
    }

}
