package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.util.CodeTool;

/**
 * Author: Ronnie.Chen
 * Date: 14-3-11
 * Time: 上午10:02
 * rongrong.chen@alcatel-sbell.com.cn
 *
 *
 */
public class PTPNamingUtil {


    /**
     * 保存标准层次结构
     */
    static class PtpIndex {
        PtpIndex(String rackNo, String shelfNo, String slotNo, String portNo) {
            this.rackNo = rackNo;
            this.shelfNo = shelfNo;
            this.slotNo = slotNo;
            this.portNo = portNo;
        }

        public String rackNo;
        public String shelfNo;
        public String slotNo;
        public String portNo;

        @Override
        public String toString() {
            return "PtpIndex{" +
                    "rackNo='" + rackNo + '\'' +
                    ", shelfNo='" + shelfNo + '\'' +
                    ", slotNo='" + slotNo + '\'' +
                    ", portNo='" + portNo + '\'' +
                    '}';
        }
    }



    /**
     *
     * @param u2000DeviceDn
     * @param aluDeviceDn
     * @param u2000PtpDn
     * eg:EMS:TZ-U2000-1-P@ManagedElement:1441903@PTP:/rack=1/shelf=1/slot=2/domain=ptn/type=physical/port=3
     * @return
     */
    public static String u2000_2_alu(String u2000DeviceDn,String aluDeviceDn,String u2000PtpDn) throws Exception {
        PtpIndex u2000PtpIndex = parseU2000Ptp(u2000PtpDn);

        //EMS:ALU/zshptn02@ManagedElement:117/2053@EquipmentHolder:/rack=1/shelf=1/slot=2@Equipment:1
        String aluPtpParentDN =  aluDeviceDn+
                "@EquipmentHolder:/rack="+u2000PtpIndex.rackNo+"/shelf="+u2000PtpIndex.shelfNo+"/slot="+u2000PtpIndex.slotNo+"@Equipment:1";
        CPTP aluPort = (CPTP)DBDataUtil.findOneObject
                ("select c from CPTP c where c.parentDn = '"+aluPtpParentDN+"' and c.no = '"+u2000PtpIndex.portNo+"'");
        return aluPort.getDn();
    }

    public static String u2000_2_fenghuo(String u2000DeviceDn,String fhDeviceDn,String u2000PtpDn,String fhEmsDn) throws Exception {
        PtpIndex u2000PtpIndex = parseU2000Ptp(u2000PtpDn);
        CPTP fhPtp = queryFHPort(fhEmsDn,fhDeviceDn,u2000PtpIndex);
        return fhPtp.getDn();
    }



    public static String fenghuo_2_alu(String fhDeviceDn,String aluDeviceDn,String fhPtpDn) throws Exception {
        PtpIndex ptpIndex = parseFHPtp(fhPtpDn);

        //EMS:ALU/zshptn02@ManagedElement:117/2053@EquipmentHolder:/rack=1/shelf=1/slot=2@Equipment:1
        String aluPtpParentDN =  aluDeviceDn+"@EquipmentHolder:/rack="+ptpIndex.rackNo+"/shelf="+ptpIndex.shelfNo+"/slot="+ptpIndex.slotNo+"@Equipment:1";
        CPTP aluPort = (CPTP)DBDataUtil.findOneObject
                ("select c from CPTP c where c.parentDn = '"+aluPtpParentDN+"' and c.no = '"+ptpIndex.portNo+"'");
        return aluPort.getDn();


    }



    /**
    * 
     * @param
     * @param
     * @param fhPtpDn   //EMS:SHX-OTNM2000-1-PTN@ManagedElement:134218163;2171910@PTP:/rack=95489/shelf=1/slot=10486809/port=3
     * @return
     */
    public static String fenghuo_2_u2000(String u2000DeviceDn,String fhPtpDn) throws Exception {
        PtpIndex ptpIndex = parseFHPtp(fhPtpDn);

        //EMS:TZ-U2000-1-P@ManagedElement:1507490@PTP:/rack=1/shelf=1/slot=3/domain=ptn/type=physical/port=8

        CPTP u2000Ptp = (CPTP)DBDataUtil.findOneObject
                ("select c from CPTP c where c.dn like '" + u2000DeviceDn + "@%rack='"
                        + ptpIndex.rackNo + "/shelf=" + ptpIndex.shelfNo + "/slot=" + ptpIndex.slotNo + "/%/port=" + ptpIndex.portNo);

        return u2000Ptp.getDn();
    }


    public static String alu_2_u2000(String aluDeviceDn,String u2000DeviceDn,String aluPtpDn) throws Exception {
        PtpIndex aluPtpIndex = parseALUPtp(aluPtpDn);
        CPTP u2000Ptp = (CPTP)DBDataUtil.findOneObject
                ("select c from CPTP c where c.dn like '" + u2000DeviceDn + "@%rack='"
                        + aluPtpIndex.rackNo + "/shelf=" + aluPtpIndex.shelfNo + "/slot=" + aluPtpIndex.slotNo + "/%/port=" + aluPtpIndex.portNo);
        return u2000Ptp.getDn();

    }

    public static String alu_2_fenghuo(String aluDeviceDn,String fhEmsDn,String fhDeviceDn,String aluPtpDn) throws Exception {
        PtpIndex aluPtpIndex = parseALUPtp(aluPtpDn);
        CPTP fhPtp = queryFHPort(fhEmsDn,fhDeviceDn,aluPtpIndex);
        return fhPtp.getDn();
    }


    private static PtpIndex parseU2000Ptp(String u2000PtpDn)  throws Exception{
        return new PtpIndex(extractValue(u2000PtpDn,"rack"),extractValue(u2000PtpDn,"shelf"),
                extractValue(u2000PtpDn,"slot"),extractValue(u2000PtpDn,"port"));
    }

    private static PtpIndex parseALUPtp(String aluPtpDn)  throws Exception{
        CPTP aluPtp =  (CPTP) DBDataUtil.findObjectByDn(CPTP.class, aluPtpDn);
//        //EMS:ALU/zshptn02@ManagedElement:117/2053@EquipmentHolder:/rack=1/shelf=1/slot=2@Equipment:1
        String parentdn = aluPtp.getParentDn();
        String aluPortNo = aluPtp.getNo();
        String aluRackNo = extractValue(parentdn,"rack");
        String aluShelfNo = extractValue(parentdn, "shelf");
        String aluSlotNo = extractValue(parentdn,"slot");
        return new PtpIndex(aluRackNo,aluShelfNo,aluSlotNo,aluPortNo);
    }

    private static PtpIndex parseFHPtp(String fhPtpDn)  throws Exception{
        String fhRackNo = extractValue(fhPtpDn,"rack");
        String fhShelfNo = extractValue(fhPtpDn,"shelf");
        String fhSlotNo = extractValue(fhPtpDn,"slot");
        String fhPortNo = extractValue(fhPtpDn,"port");

        CPTP fhPTP = (CPTP) DBDataUtil.findObjectByDn(CPTP.class, fhPtpDn);
        String fhSlotDn = fhPTP.getDn().substring(0,fhPTP.getDn().lastIndexOf("/")).replace("PTP","EquipmentHolder");
        CSlot fhSlot = (CSlot) DBDataUtil.findObjectByDn(CSlot.class, fhSlotDn);
        CShelf fhShelf = (CShelf) DBDataUtil.findObjectByDn(CShelf.class, fhSlot.getShelfDn());
        CRack fhRack = (CRack) DBDataUtil.findObjectByDn(CRack.class, fhShelf.getRackDn());
        return new PtpIndex(fhRack.getNo(),fhShelfNo,Integer.parseInt(fhSlot.getNo(), 16)+"",fhPortNo);
    }


    private static CPTP queryFHPort(String fhEmsDn,String fhDeviceDn,PtpIndex ptpIndex) throws Exception {
        //EMS:LS-OTNM2000-1-PTN@ManagedElement:134218712;71182@EquipmentHolder:/rack=224001
        CRack fhRack = (CRack)DBDataUtil.findOneObject
                ("select c from CRack c where c.emsName = '"+fhEmsDn+"' and c.no = '"+ptpIndex.rackNo+"' and c.parentDn = '"+fhDeviceDn+"'");

        //EMS:SHX-OTNM2000-1-PTN@ManagedElement:134219168;101382@EquipmentHolder:/rack=487937/shelf=1
        CShelf fhShelf = (CShelf)DBDataUtil.findObjectByDn(CShelf.class, fhRack.getDn() + "/shelf=" + ptpIndex.shelfNo);

        //EMS:HZ-OTNM2000-1-PTN@ManagedElement:134217731;65795@EquipmentHolder:/rack=769/shelf=1/slot=22021140
        CSlot fhSlot = (CSlot)DBDataUtil.findOneObject("select c from CSlot c where c.emsName = '"
                + fhEmsDn + "' and c.shelfId = " + fhShelf.getSid() + " and c.no = '" + Integer.toHexString(Integer.parseInt(ptpIndex.slotNo)) + "'");

//EMS:SHX-OTNM2000-1-PTN@ManagedElement:134218163;2171910@PTP:/rack=95489/shelf=1/slot=10486809/port=3
        CPTP fhPtp = (CPTP)DBDataUtil.findObjectByDn(CPTP.class, fhSlot.getDn().replace("EquipmentHolder", "PTP") + "/port=" + ptpIndex.portNo);
        return fhPtp;
    }




    /**
     * HZ-U2000-2-P@720897@/rack=1/shelf=1/slot=3
     *
     * key = rack, return 1
     * @param str
     * @param key
     * @return
     */
    public static String extractValue(String str,String key) {
        int i = str.indexOf(key + "=");
        if (i >= 0) {
            int j = str.indexOf("/",i);
            if (j >= 0 && j > i) {
                return str.substring(i+key.length()+1,j);
            } else
                return str.substring(i+key.length()+1);
        }
        return null;
    }




}
