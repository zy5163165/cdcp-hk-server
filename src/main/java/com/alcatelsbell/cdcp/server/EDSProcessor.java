package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.common.model.EmsBenchmark;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkItem;

import com.alcatelsbell.cdcp.nodefx.CDCPConstants;
import com.alcatelsbell.cdcp.nodefx.CdcpMessage;

import com.alcatelsbell.nms.common.CommonUtil;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.EDS_PTN;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen Date: 14-10-30 Time: 下午4:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class EDSProcessor {
	private static EDSProcessor ourInstance = new EDSProcessor();
	private Log logger = LogFactory.getLog(getClass());

	public static EDSProcessor getInstance() {
		return ourInstance;
	}

	private EDSProcessor() {
	}



	public boolean processEDS(CdcpMessage cdcpMessage, EDS_PTN eds) {
		EDS_PTN _eds = eds; // 只是为了外面能把不符合的理由打印出来。
		Object serial = cdcpMessage
				.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_TASK_SERIAL);

		boolean logical = serial == null ? true : CdcpServerUtil.isTaskLogical((String)serial);


		eds.setTaskSerial(serial + "");
		eds.setTag1(cdcpMessage.getNodeDName());
		eds.setTag2(cdcpMessage.getWorkPath());
		Date startTime = eds.getStartTime();
		Date collectTime = eds.getCollectTime();

		if (startTime != null) {
			if (collectTime == null)
				collectTime = new Date();
			long mis = collectTime.getTime() - startTime.getTime();
			long min = mis / (1000 * 60l);
			if (min > 60) {
				long m = min % 60;
				long hour = min / 60;
				eds.setTimeCost(hour + ":" + m);
			} else {
				eds.setTimeCost("00:" + min);
			}

		}
		try {
			eds = (EDS_PTN) JpaServerUtil.getInstance().saveObject(-1, eds);
		} catch (Exception e) {
			logger.error(e, e);
		}

		boolean valid = true;
		String emsname = eds.getEmsname();
		EmsBenchmark benchmark = null;
		try {
			benchmark = (EmsBenchmark) JpaServerUtil.getInstance()
					.findOneObject(
							"select c from EmsBenchmark c where c.emsname = '"
									+ emsname + "' and c.status = " + 1);

		} catch (Exception e) {
			logger.error(e, e);
		}

		List<EmsBenchmarkItem> items = null;
		if (benchmark != null) {

			try {
				items = JpaServerUtil.getInstance().findObjects(
						"select c from EmsBenchmarkItem c where c.benchmarkDn = '"
								+ benchmark.getDn() + "'");
			} catch (Exception e) {
				logger.error(e, e);
			}
			HashMap<String, EmsBenchmarkItem> itemMap = new HashMap<String, EmsBenchmarkItem>();
			for (EmsBenchmarkItem item : items) {
				itemMap.put(item.getTableName(), item);
			}
			Field[] declaredFields = EDS_PTN.class.getDeclaredFields();
			for (Field declaredField : declaredFields) {
				if (!declaredField.getType().equals(Integer.class))
					continue;
				String fieldName = declaredField.getName();
				if (!logical && !(fieldName.equals("neCount")
						|| fieldName.equals("slotCount")
						|| fieldName.equals("equipmentCount")
						|| fieldName.equals("ptpCount"))
						||fieldName.equals("sectionCount"))
					continue;

				EmsBenchmarkItem emsBenchmarkItem = itemMap.get(declaredField
						.getName());

				Object newValue = CommonUtil.getObjectFieldValue(declaredField,
						eds);

				if (emsBenchmarkItem != null && newValue != null) {
					if (!isValidCount(emsBenchmarkItem, (Integer) newValue)) {
						valid = false;
						eds.setAdditinalInfo(eds.getAdditinalInfo() + ";"
								+ declaredField.getName() + ":" + newValue
								+ "/" + emsBenchmarkItem.getCount());
						_eds.setAdditinalInfo(eds.getAdditinalInfo());
						break;
					}
				}

				if (emsBenchmarkItem == null) {
					emsBenchmarkItem = new EmsBenchmarkItem();
					emsBenchmarkItem.setBenchmarkDn(emsname);
					emsBenchmarkItem.setCount(newValue == null ? 0
							: ((Integer) newValue).intValue());
					emsBenchmarkItem.setTableName(declaredField.getName());
					emsBenchmarkItem.setDvpercentage(80);
					emsBenchmarkItem.setOid(1);
					items.add(emsBenchmarkItem);
				} else {
					emsBenchmarkItem.setCount(newValue == null ? 0
							: ((Integer) newValue).intValue());
					emsBenchmarkItem.setOid(1);
				}
			}

			if (valid) {
				JPASupport jpaSupport = JPASupportFactory.createJPASupport();
				try {
					jpaSupport.begin();
					for (EmsBenchmarkItem item : items) {
						if (item.getOid() == 1) { // 表示有更改或新增 oid不持久化
							JPAUtil.getInstance().saveObject(jpaSupport, -1,
									item);
						}
					}
					jpaSupport.end();
				} catch (Exception e) {
					logger.error(e, e);
					try {
						jpaSupport.rollback();
					} catch (Exception e1) {
						logger.error(e1, e1);
					}
				} finally {
					jpaSupport.release();
				}
			} else {
				eds.setFromWhere(100);
				try {
					JpaServerUtil.getInstance().saveObject(-1, eds);
				} catch (Exception e) {
					logger.error(e, e);
				}
			}

		} else {

			benchmark = new EmsBenchmark();
			benchmark.setDn(emsname);
			benchmark.setEmsname(emsname);
			benchmark.setAdditinalInfo("AUTO");
			JPASupport jpaSupport = JPASupportFactory.createJPASupport();
			try {
				jpaSupport.begin();

				JPAUtil.getInstance().saveObject(jpaSupport, -1, benchmark);

				Field[] declaredFields = EDS_PTN.class.getDeclaredFields();
				for (Field declaredField : declaredFields) {
					if (!declaredField.getType().equals(Integer.class))
						continue;
					Object newValue = CommonUtil.getObjectFieldValue(
							declaredField, eds);

					EmsBenchmarkItem item = new EmsBenchmarkItem();
					item.setBenchmarkDn(emsname);
					item.setCount(newValue == null ? 0 : ((Integer) newValue)
							.intValue());
					item.setTableName(declaredField.getName());
					item.setDvpercentage(80);

					JPAUtil.getInstance().saveObject(jpaSupport, -1, item);

				}

				jpaSupport.end();
			} catch (Exception e) {

				logger.error(e, e);
				try {
					jpaSupport.rollback();
				} catch (Exception e1) {
					logger.error(e1, e1);
				}
			} finally {
				jpaSupport.release();
			}

		}
		return valid;
	}

	private boolean isValidCount(EmsBenchmarkItem item, int count) {
		//关闭检查
		if (true) return true;


		if (item == null || item.getCount() == null || item.getCount() == 0)
			return true;
		Integer bm = item.getCount();
		if (bm > 0 && count == 0 && item.getDvpercentage() > 0 )  //如果偏移为0，则表示可以容忍所有数据r
			return false;

		int low = (int) ((bm * item.getDvpercentage()) / 100);

		if (count < low)
			return false;

		return true;
	}

}
