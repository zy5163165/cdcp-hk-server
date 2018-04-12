package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.model.relationship.RCDeviceVDevice;
import com.alcatelsbell.cdcp.nbi.model.virtualentity.VDevice;
import com.alcatelsbell.nms.common.CommonUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-3-10
 * Time: 下午8:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
// public class VDeviceMergeRunnable implements Runnable {
public class VDeviceMergeRunnable {
    private Logger logger = Logger.getLogger(getClass());

	public VDeviceMergeRunnable(Logger logger) {
		this.logger = logger;
	}

	public void run(String emsName) {
		try {
			logger.info("VDeviceMergeRunnable...");
			JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
		//	List<VDevice> vDevices = JPAUtil.getInstance().findObjects(jpaSupport, "select c from VDevice c where c.emsName = '" + emsName + "'");
			List<VDevice> vDevices = JPAUtil.getInstance().findObjects(jpaSupport, "select c from RCDeviceVDevice r, VDevice c where  r.vDeviceDn = c.dn and r.cDeviceDn like 'EMS:" + emsName + "%'");
			// List<VDevice> vDevices = JPAUtil.getInstance().findAllObjects(jpaSupport, VDevice.class);
			if (vDevices != null && vDevices.size() > 0) {
				logger.info("vDevices: " + vDevices.size());
				for (VDevice vDevice : vDevices) {
					try {
						logger.info("processVDevice: " + vDevice.getDn());
						List<RCDeviceVDevice> rdvs = JPAUtil.getInstance().findObjects(jpaSupport,
								"select c from RCDeviceVDevice c where c.vDeviceDn = '" + vDevice.getDn() + "'");
						if (rdvs == null || rdvs.isEmpty()) {
							logger.error("VDevice:" + vDevice.getDn() + " has no devices !");
							continue;
						}
						String[] strs = getRealDeviceDnAndEmsDn(rdvs);
						String realDeviceDn = strs[0];
						String realEmsDn = strs[1];
						CDevice realDevice = (CDevice) DBDataUtil.findObjectByDn(CDevice.class, realDeviceDn);
						if (realDevice == null) {
							logger.info("Real Device Not Existed,May be not migrated yet, dn=" + realDeviceDn);
							continue;
						}

						for (RCDeviceVDevice rdv : rdvs) {
							// 如果是虚拟网元
							if (!rdv.getcDeviceDn().equals(realDeviceDn)) {
								String emsdn = rdv.getcDeviceEMSName();
								// if (emsdn.equals(emsName)) {
								processEmsData(emsdn, realEmsDn, rdv.getcDeviceDn(), realDeviceDn);
								// }
							}
						}
					} catch (Exception e) {
						logger.error(e, e);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e, e);
		}

	}

	private void processEmsData(String emsdn, String realEmsDn, String deviceDn, String realDeviceDn) throws Exception {

		boolean isSameVendor = (isU2000EMS(emsdn) && isU2000EMS(realEmsDn)) || (isOTNMEMS(emsdn) && isOTNMEMS(realEmsDn))
				|| (isALUEMS(emsdn) && isALUEMS(realEmsDn));
		logger.info("emsdn="+emsdn+"; realemsdn = "+realEmsDn+"; devicedn="+deviceDn+"; realdevicedn = "+realDeviceDn);
		logger.info("isSameVendor : "+isSameVendor);
	//	processLineData(emsdn, realEmsDn, deviceDn, realDeviceDn, CSection.class, "aendTp", "zendTp", "aptpId", "zptpId", isSameVendor);
		processSectionData(emsdn,realEmsDn,deviceDn,realDeviceDn,isSameVendor);
		processLineData(emsdn, realEmsDn, deviceDn, realDeviceDn, CTunnel.class, "aptp", "zptp", "aptpId", "zptpId", isSameVendor);
		processLineData(emsdn, realEmsDn, deviceDn, realDeviceDn, CPW.class, "aptp", "zptp", "aptpId", "zptpId", isSameVendor);
		processLineData(emsdn, realEmsDn, deviceDn, realDeviceDn, CPWE3.class, "aptp", "zptp", "aptpId", "zptpId", isSameVendor);
//		processLineData(emsdn, realEmsDn, deviceDn, realDeviceDn, CIPRoute.class, "aptp", "zptp", "aptpId", "zptpId", isSameVendor);

		long t1 = System.currentTimeMillis();
			DBDataUtil.executeQL("delete   from CPTP c where c.deviceDn = '" + deviceDn + "'");
			DBDataUtil.executeQL("delete   from CEquipment c where c.dn  like '" + deviceDn + "@%'");
			DBDataUtil.executeQL("delete   from CSlot c where c.parentDn = '" + deviceDn + "'");
			DBDataUtil.executeQL("delete   from CRack c where c.parentDn = '" + deviceDn + "'");
			DBDataUtil.executeQL("delete   from CShelf c where c.parentDn = '" + deviceDn + "'");
			DBDataUtil.executeQL("delete   from CDevice c where c.dn = '" + deviceDn + "'");

//
//			DBDataUtil.executeQL("delete   from CPTP c where c.dn like '" + deviceDn + "@%'");
//			DBDataUtil.executeQL("delete   from CEquipment c where c.dn  like '" + deviceDn + "@%'");
//			DBDataUtil.executeQL("delete   from CSlot c where c.dn like '" + deviceDn + "@%'");
//			DBDataUtil.executeQL("delete   from CRack c where c.dn like '" + deviceDn + "@%'");
//			DBDataUtil.executeQL("delete   from CShelf c where c.dn like '" + deviceDn + "@%'");
//			DBDataUtil.executeQL("delete   from CDevice c where c.dn = '" + deviceDn + "'");
		long t2 = System.currentTimeMillis();
		logger.info("process : "+deviceDn+" spend : "+((t2-t1)/1000)+"s");
	}

	// @Deprecated
	// private void processLineDataInSameVendor(String emsdn, String deviceDn, String realDeviceDn) throws Exception {
	// List<CSection> sections = DBDataUtil.findObjects("select c from CSection c where c.emsName = '" + emsdn
	// + "' and ( (c.aendTp like '" + deviceDn + "@%') or (c.zendTp like '" + deviceDn + "@%') )");
	// for (CSection section : sections) {
	// if (section.getAendTp().startsWith(deviceDn+"@")) {
	// String newAendTP = realDeviceDn + section.getAendTp().substring(deviceDn.length());
	// section.setAendTp(newAendTP);
	// section.setAptpId(DBDataUtil.readSID(CPTP.class,newAendTP));
	// }
	//
	// if (section.getZendTp().startsWith(deviceDn+"@")) {
	// String newZendTP = realDeviceDn + section.getZendTp().substring(deviceDn.length());
	// section.setZendTp(newZendTP);
	// section.setZptpId(DBDataUtil.readSID(CPTP.class, newZendTP));
	// }
	//
	// DBDataUtil.storeObject(section);
	//
	// }
	//
	// }

	private String convertPtpDnWithSameVendor(String ptpDn, String deviceDn, String realDeviceDn) {
		return realDeviceDn + ptpDn.substring(deviceDn.length());
	}

	/**
	 * 不同EMS
	 * 
	 * @param ptpDn
	 * @param deviceDn
	 * @param realDeviceDn
	 * @return
	 */
	private String convertPtpDnWithDiffVendor(String emsDn, String realEmsDn, String ptpDn, String deviceDn, String realDeviceDn) throws Exception {
		if (isU2000EMS(emsDn) && isOTNMEMS(realEmsDn)) {
			return PTPNamingUtil.u2000_2_fenghuo(deviceDn, realDeviceDn, ptpDn, realEmsDn);
		}

		if (isU2000EMS(emsDn) && isALUEMS(realEmsDn)) {
			return PTPNamingUtil.u2000_2_alu(deviceDn, realDeviceDn, ptpDn);
		}

		if (isOTNMEMS(emsDn) && isU2000EMS(realEmsDn)) {
			return PTPNamingUtil.fenghuo_2_u2000(realDeviceDn, ptpDn);
		}

		if (isOTNMEMS(emsDn) && isALUEMS(realEmsDn)) {
			return PTPNamingUtil.fenghuo_2_alu(deviceDn, realDeviceDn, ptpDn);
		}

		if (isALUEMS(emsDn) && isOTNMEMS(realEmsDn)) {
			return PTPNamingUtil.alu_2_fenghuo(deviceDn, realEmsDn, realDeviceDn, ptpDn);
		}

		if (isALUEMS(emsDn) && isU2000EMS(realEmsDn)) {
			return PTPNamingUtil.alu_2_u2000(deviceDn, realDeviceDn, ptpDn);
		}

		throw new Exception("convertPtpDnWithDiffVendor strange ems:" + emsDn + "," + realEmsDn);
	}

	private boolean isU2000EMS(String emsDn) {
		return emsDn.contains("-U2000-") || emsDn.contains("-T2000-");
	}

	private boolean isOTNMEMS(String emsDn) {
		return emsDn.contains("-OTNM2000-");
	}

	private boolean isALUEMS(String emsDn) {
		return emsDn.contains("ALU/");
	}

	private void processSectionData(String emsdn, String realEmsDn, String deviceDn, String realDeviceDn,boolean sameVendor) throws Exception {
		List<CSection> sections = DBDataUtil.findObjects("select c from  CSection c where c.emsName = '" + emsdn +
				"' and ( (c.aendTp like '" + deviceDn + "@%') or (c.zendTp like '" + deviceDn + "@%') )");

		logger.info("Find "+(sections == null ? 0:sections.size())+" section ");
		for (CSection section : sections) {

			String oldATp = section.getAendTp();
			String oldZTp = section.getZendTp();
			if (oldATp.startsWith(deviceDn + "@")) {
				String newAendTP = sameVendor ? convertPtpDnWithSameVendor(oldATp, deviceDn, realDeviceDn) : convertPtpDnWithDiffVendor(emsdn, realEmsDn,
						oldATp, deviceDn, realDeviceDn);
				section.setAendTp(newAendTP);

				section.setAptpId(DBDataUtil.readSID(CPTP.class, newAendTP));
			}

			if (oldZTp.startsWith(deviceDn + "@")) {
				String newZendTP = sameVendor ? convertPtpDnWithSameVendor(oldZTp, deviceDn, realDeviceDn) : convertPtpDnWithDiffVendor(emsdn, realEmsDn,
						oldATp, deviceDn, realDeviceDn);
				section.setZendTp(newZendTP);
				section.setZptpId(DBDataUtil.readSID(CPTP.class, newZendTP));
			}



			CSection sameSection = (CSection)DBDataUtil.findOneObject("select c from CSection c where (c.aendTp = '" + section.getAendTp() + "' and c.zendTp = '" + section.getZendTp() + "') or (c.zendTp = '" + section.getAendTp() + "' and c.aendTp = '" + section.getZendTp() + "')");
			if (sameSection != null) {
				logger.info("find same section : "+sameSection.getDn());
				logger.info("remove section : "+section.getDn());

				DBDataUtil.removeObject(section);

				List<CChannel> channelsToBeDelete = DBDataUtil.findObjects("select c from CChannel c where c.sectionOrHigherOrderDn = '" + section.getDn() + "'");
				List<CChannel> realChannels = DBDataUtil.findObjects("select c from CChannel c where c.sectionOrHigherOrderDn = '" + sameSection.getDn() + "'");
				if (channelsToBeDelete == null) channelsToBeDelete = new ArrayList<CChannel>();
				if (realChannels == null) realChannels = new ArrayList<CChannel>();
				logger.info("channelsToBeDelete size = "+channelsToBeDelete.size());
				logger.info("realChannels size = "+realChannels.size());
				HashMap<String,CChannel> noChannelMap = new HashMap<String, CChannel>();
				for (CChannel realChannel : realChannels) {
					noChannelMap.put(realChannel.getNo(),realChannel);
				}

				for (CChannel cChannel : channelsToBeDelete) {
					CChannel realChannel = noChannelMap.get(cChannel.getNo());
					if (realChannel != null) {
						DBDataUtil.removeObject(cChannel);
						String sql1 = " update CPath_Channel c set c.channelDn = '" + realChannel.getDn() + "' where c.channelDn = '" + cChannel.getDn() + "'";
						logger.info("sql1="+sql1);
						DBDataUtil.executeQL(sql1);
						String sql2 = " update CRoute_Channel c set c.channelDn = '" + realChannel.getDn() + "' where c.channelDn = '" + cChannel.getDn() + "'";
						logger.info("sql2="+sql2);
						DBDataUtil.executeQL(sql2);
					} else {
						logger.error("Failed to find same no channel : "+cChannel.getDn()+" no = "+cChannel.getNo());
					}
				}



			} else {
				logger.info("try to update : "+section.getDn());
				DBDataUtil.storeObject(section);
			}

		}


		logger.info(" processSection   finished");

	}
	private void processLineData(String emsdn, String realEmsDn, String deviceDn, String realDeviceDn, Class cls, String aPortField, String zPortField,
			String aPortIdField, String zPortIdField, boolean sameVendor) throws Exception {
		List<BObject> lines = DBDataUtil.findObjects("select c from " + cls.getName() + " c where c.emsName = '" + emsdn + "' and ( (c." + aPortField
				+ " like '" + deviceDn + "@%') or (c." + zPortField + " like '" + deviceDn + "@%') )");
		logger.info("Find "+(lines == null ? 0:lines.size())+" "+cls.getSimpleName());
		for (BObject line : lines) {

			String oldATp = (String) CommonUtil.getObjectFieldValue(cls.getDeclaredField(aPortField), line);
			String oldZTp = (String) CommonUtil.getObjectFieldValue(cls.getDeclaredField(zPortField), line);
			if (oldATp.startsWith(deviceDn + "@")) {
				String newAendTP = sameVendor ? convertPtpDnWithSameVendor(oldATp, deviceDn, realDeviceDn) : convertPtpDnWithDiffVendor(emsdn, realEmsDn,
						oldATp, deviceDn, realDeviceDn);
				CommonUtil.getInstance().setFiledValue(line, aPortField, newAendTP);
				if (aPortIdField != null)
					CommonUtil.getInstance().setFiledValue(line, aPortIdField, DBDataUtil.readSID(CPTP.class, newAendTP));
			}

			if (oldZTp.startsWith(deviceDn + "@")) {
				String newZendTP = sameVendor ? convertPtpDnWithSameVendor(oldZTp, deviceDn, realDeviceDn) : convertPtpDnWithDiffVendor(emsdn, realEmsDn,
						oldATp, deviceDn, realDeviceDn);
				CommonUtil.getInstance().setFiledValue(line, zPortField, newZendTP);
				if (zPortIdField != null)
					CommonUtil.getInstance().setFiledValue(line, zPortIdField, DBDataUtil.readSID(CPTP.class, newZendTP));
			}
			logger.info("try to update : "+line.getDn());
			DBDataUtil.storeObject(line);

		}
		logger.info(" processLine "+cls.getSimpleName()+" finished");
	}

	private static String[] getRealDeviceDnAndEmsDn(List<RCDeviceVDevice> rcDeviceVDevices) {
		if (rcDeviceVDevices != null) {
			for (RCDeviceVDevice rcDeviceVDevice : rcDeviceVDevices) {
				if (rcDeviceVDevice.getcDevicePrimaryType() != null && rcDeviceVDevice.getcDevicePrimaryType().trim().equals("2")) {
					return new String[] { rcDeviceVDevice.getcDeviceDn(), rcDeviceVDevice.getcDeviceEMSName() };
				}
			}
		}
		return null;
	}

    public static void main(String[] args) throws Exception {
        VDeviceMergeRunnable runnable = new VDeviceMergeRunnable(Logger.getLogger(VDeviceMergeRunnable.class));
        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        System.out.println("VDeviceMergeRunnable...");
        JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
        List<VDevice> vDevices = JPAUtil.getInstance().findObjects
                (jpaSupport, "select c from VDevice c where c.dn = '" + args[0] + "'");
        // List<VDevice> vDevices = JPAUtil.getInstance().findAllObjects(jpaSupport, VDevice.class);
        if (vDevices != null && vDevices.size() > 0) {
            System.out.println("vDevices: " + vDevices.size());
            for (VDevice vDevice : vDevices) {
                try {
                    System.out.println("processVDevice: " + vDevice.getDn());
                    List<RCDeviceVDevice> rdvs = JPAUtil.getInstance().findObjects(jpaSupport,
                            "select c from RCDeviceVDevice c where c.vDeviceDn = '" + vDevice.getDn() + "'");
                    if (rdvs == null || rdvs.isEmpty()) {
                        System.out.println("VDevice:" + vDevice.getDn() + " has no devices !");
                        continue;
                    }
                    String[] strs = getRealDeviceDnAndEmsDn(rdvs);
                    String realDeviceDn = strs[0];
                    String realEmsDn = strs[1];
                    CDevice realDevice = (CDevice) DBDataUtil.findObjectByDn(CDevice.class, realDeviceDn);
                    if (realDevice == null) {
                        System.out.println("Real Device Not Existed,May be not migrated yet, dn=" + realDeviceDn);
                        continue;
                    }

                    for (RCDeviceVDevice rdv : rdvs) {
                        // 如果是虚拟网元
                        if (!rdv.getcDeviceDn().equals(realDeviceDn)) {
                            String emsdn = rdv.getcDeviceEMSName();
                            // if (emsdn.equals(emsName)) {
                            runnable.processEmsData(emsdn, realEmsDn, rdv.getcDeviceDn(), realDeviceDn);
                            // }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
