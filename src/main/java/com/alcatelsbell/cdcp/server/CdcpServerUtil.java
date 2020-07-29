package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.nodefx.*;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.alu.ALUDBFMigrator;
import com.alcatelsbell.cdcp.server.adapters.alu.ALU_OTN_Migrator;
import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.FHOTNM2000Migrator;
import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.FHOTNM2000OTN2Migrator;
import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.FHOTNM2000OTN3Migrator;
import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.FHOTNM2000OTNMigrator;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000DBFMigrator;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000DWDMMigrator;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000SDHMigrator;
import com.alcatelsbell.cdcp.server.adapters.zte.ZTE_OTNU31_OTN_Migrator;
import com.alcatelsbell.cdcp.server.message.CdcpServerMessage;
import com.alcatelsbell.cdcp.test.ServerEnv;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.cdcp.util.MigrateThread;
import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.common.SpringContext;
import com.alcatelsbell.nms.common.SysConst;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.interfaces.Constants;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import com.alcatelsbell.nms.valueobject.sys.SysNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.ManagedElement;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.util.FileLogger;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午6:29
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpServerUtil {
    private static Log logger = LogFactory.getLog(CdcpServerUtil.class);

    public static SysNode findNode(String emsDn) {
		SysNode node = null;
		try {
			node = (SysNode) JpaClient.getInstance().findOneObject("select c from SysNode c,Ems e where e.sysNodeDn = c.dn and e.dn = '" + emsDn + "'");
		} catch (Exception e) {
			LogUtil.error(CdcpServerUtil.class, e, e);
		}
		return node;
	}

	public static Ems upateEms(String serial,  Date syncEndTime, int synIsOk) {
		try {
			Task task = CdcpServerUtil.findTask(serial);
			if (task == null) {
				logger.error("Faild to find task : "+serial);
				return null;
			}
			String taskObject = task.getTaskObject();
			Ems ems = CdcpServerUtil.findEms(taskObject);
	//		ems.setStatus(emsStatus);
			if (syncEndTime != null)
				ems.setSynEndTime(syncEndTime);
			ems.setSynIsOk(synIsOk);

			return (Ems)JpaClient.getInstance().saveObject(-1, ems);
		} catch (Exception e) {
			LogUtil.error(CdcpServerUtil.class, e, e);
		}
        return null;
	}

	public static Ems findEms(String emsDn) throws Exception {
		return (Ems) JpaClient.getInstance().findObjectByDN(Ems.class, emsDn);
	}

	public static MBeanProxy<NodeAdminMBean> createNodeAdminProxy(String emsDn) throws Exception {
		SysNode node = findNode(emsDn);
		if (node != null)
			return createNodeAdminProxy(node.getIpaddress(), node.getJmxport());
		throw new Exception("无法找到采集节点：emsdn="+emsDn);
	}

	public static MBeanProxy<NodeAdminMBean> createNodeAdminProxy(String host, int port) throws Exception {

		try {
			return MBeanProxy.create(NodeAdminMBean.class, CDCPConstants.NODE_JMS_DOMAIN + ":name=NodeAdmin", host, port);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			logger.info("IOException : retry start !");
			 return createNodeAdminProxy(host, port,1);
		}
	}

	public static MBeanProxy<NodeAdminMBean> createNodeAdminProxy(String host, int port,int retry) throws Exception {
		logger.info("createNodeAdminProxy="+host+":"+port+": retry = "+retry);
		Thread.sleep(10000);
		try {
			return MBeanProxy.create(NodeAdminMBean.class, CDCPConstants.NODE_JMS_DOMAIN + ":name=NodeAdmin", host, port);
		} catch (IOException e) {
			logger.error(e, e);
			if (retry == 5) return null;
			return createNodeAdminProxy(host, port, retry+1);
		}
	}

	public static MBeanProxy<KeepAliveConnectionDynamicMBean> createNodeEmsProxy(String emsDn) throws Exception {
		SysNode node = findNode(emsDn);
		if (node != null)
			return createNodeEmsProxy(node.getIpaddress(), node.getJmxport(),emsDn);
		throw new Exception("无法找到采集节点：emsdn="+emsDn);
	}

	public static MBeanProxy<KeepAliveConnectionDynamicMBean> createNodeEmsProxy(String host, int port,String emsDn) throws Exception {
		return MBeanProxy.create(KeepAliveConnectionDynamicMBean.class, CDCPConstants.NODE_JMS_DOMAIN + ":name=Connection_"+emsDn, host, port);
	}

	public static Task findTask(String serial) {
		Task task = null;
		try {
			task = (Task) JpaClient.getInstance().findObjectByDN(Task.class, serial);
		} catch (Exception e) {
			LogUtil.error(CdcpServerUtil.class, e, e);
		}
		return task;
	}

	public static Schedule findSchedule(Long scheduleId) {
		try {
			return (Schedule) JpaClient.getInstance().findObjectById(Schedule.class, scheduleId);
		} catch (Exception e) {
			LogUtil.error(CdcpServerUtil.class, e, e);
		}

		return null;
	}
	public static boolean isTaskLogical(String serial) {
		Task task = findTask(serial);
		if (task != null && task.getScheduleId() != null) {
			Schedule schedule = findSchedule(task.getScheduleId());
			HashMap arguments = getScheduleArguments(schedule);
			String logical = (String)arguments.get("logical");
			if (logical != null && logical.equalsIgnoreCase("false")) {
				return false;
			}
		}

		return true;
	}
	public static HashMap getScheduleArguments(Schedule schedule) {
		HashMap map = new HashMap();
		 if (schedule != null && schedule.getArguments() != null) {
			String[] split = schedule.getArguments().split("&");
			for (String s : split) {
				String k = s.substring(0,s.indexOf("="));
				String v = s.substring(s.indexOf("=")+1);
				map.put(k, v);

			}
		}
		return map;
	}
//	public static void updateEmsStatus(String emsDn, Integer status) {
//		try {
//			JpaClient.getInstance().executeUpdateSQL("update Ems s set s.status = " + status + " where s.dn = '" + emsDn + "'");
//		} catch (Exception e) {
//			LogUtil.error(CdcpServerUtil.class, e, e);
//		}
//	}

	public static void createEmsLog(String emsDn, String operation, String txt) {
        logger.info("emsdn="+emsDn+";operation="+operation+";txt="+txt);
        try {
		com.alcatelsbell.nms.valueobject.sys.Log log = new com.alcatelsbell.nms.valueobject.sys.Log();
		log.setTime(new Date());
		log.setDn(SysUtil.nextDN());
		log.setCategory(SysConst.LOG_CATEGORY_INTERFACE);
		log.setOperation(operation);

		log.setComments(txt);
		log.setContent(txt);
		log.setIpaddress("");
		log.setModule("EMS");
		log.setObject(emsDn);
		log.setDn(SysUtil.nextLongId() + "");
		log.setSource("");
		log.setSysname("CDCP");

			JpaClient.getInstance().saveObject(-1, log);
		} catch (Throwable e) {
			LogUtil.error(CdcpServerUtil.class, e, e);
		}
	}

	public static void executeUpdateQl(String ql) throws Exception {
		JpaClient.getInstance().executeUpdateSQL(ql);
	}

	public static void updateTask(String serial, Integer status, String desc) {
		if (desc == null)
			desc = "";
		try {
			String ql = " update Task c set c.status = " + status + " where c.dn = '" + serial + "'";
			if (status != null)
				ql = " update Task c set c.status = " + status + " , c.description = '" + desc + "' where c.dn = '" + serial + "'";

			if (status == com.alcatelsbell.cdcp.common.Constants.TASK_STATUS_MIGRATING) {
				ql = " update Task c set c.tag3 = "+System.currentTimeMillis()+", c.status = " + status + " , c.description = '" + desc + "' where c.dn = '" + serial + "'";
			}
			executeUpdateQl(ql);
		} catch (Exception e) {
			LogUtil.error(CdcpServerUtil.class, e, e);
		}
	}

    public static void updateTaskPercentage(String serial,Integer percentage) {
        try {
            if (percentage != null) {
                String ql = " update Task c set c.percentage = " + percentage + " where c.dn = '" + serial + "'";
                CdcpServerUtil.executeUpdateQl(ql);
            }
        } catch (Throwable e) {
            LogUtil.error(CdcpServerUtil.class, e, e);
        }
    }


	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000));

	public static void sendMigrateLogMessage(final String taskSerial, final String emsdn, final String info, final int percentage) throws Exception {
		Runnable runnable = new Runnable() {
			public void run() {
				CdcpServerMessage message = new CdcpServerMessage();
				message.setAttribute(com.alcatelsbell.cdcp.common.Constants.SERVER_MESSAGE_ATTRIBUTE_EMS_DN, emsdn);
				message.setAttribute(com.alcatelsbell.cdcp.common.Constants.SERVER_MESSAGE_ATTRIBUTE_MIGRATE_PERCENTAGE, percentage);
				message.setAttribute(com.alcatelsbell.cdcp.common.Constants.SERVER_MESSAGE_ATTRIBUTE_MIGRATE_TASK_SERIAL, taskSerial);
				message.setAttribute(com.alcatelsbell.cdcp.common.Constants.SERVER_MESSAGE_ATTRIBUTE_MIGRATE_TXT, info);
				try {
					SpringContext.getInstance().getJMSSupport().sendTopicMessage(com.alcatelsbell.cdcp.common.Constants.TOPIC_SERVER_MIGRATE_LOG, message);
				} catch (Exception e) {
					LogUtil.error(CdcpServerUtil.class, e, e);
				}
			}
		};
		executor.execute(runnable);
	}


	private static ThreadPoolExecutor dbFileExecutor = null;
	public static void  setDbFileExecutor(ThreadPoolExecutor executor) {
		dbFileExecutor = executor;
	}

	public static ThreadPoolExecutor getDbFileExecutor() {
		return dbFileExecutor;
	}


	public static String readEmsDn(String filePath) throws Exception {
		SqliteDelegation sd = new SqliteDelegation(JPASupportFactory.createSqliteJPASupport(filePath));
		try {
			List<ManagedElement> query = sd.query("select c from ManagedElement c ", 0, 1);
			if (query != null && query.size() > 0) {
				ManagedElement em = query.get(0);
				return em.getEmsName();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			sd.release();
		}

		return null;
	}

    public static Serializable obtainByURI(String emsDn,URI uri) {
		MBeanProxy<NodeAdminMBean> nodeAdminProxy = null;
		try {
            Ems ems = findEms(emsDn);
            nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(emsDn);
            return nodeAdminProxy.proxy.obtain(ems,uri);
        } catch (Exception e) {
            logger.error(e, e);
        } finally {
			if (nodeAdminProxy != null)
				try {
					nodeAdminProxy.close();
				} catch (IOException e) {
					logger.error(e, e);
				}
		}

        return null;
    }

	private static FileLogger sectionlogger = new FileLogger("Section.log");
	public static String syncDeviceSection(String emsdn,String device) throws NodeException {
		sectionlogger.info("syncDeviceSection:"+emsdn+":"+device);
		MBeanProxy<NodeAdminMBean> nodeEmsProxy = null;
		try {
		//	Task task = MigrateManager.getInstance().registerEmsSingleMEJob(emsDn,device);

			nodeEmsProxy = CdcpServerUtil.createNodeAdminProxy(emsdn);

//			EmsJob job = new EmsJob();
//			job.setJobType(EmsJob.JOB_TYPE_SYNC_DEVICE);
//			job.getDataMap().put(CDCPConstants.EMSJOB_DATA_KEY_DEVICE_DN,device);
//			job.getDataMap().put(CDCPConstants.EMSJOB_DATA_KEY_SYNC_TYPE,CDCPConstants.EMSJOB_DATA_VALUE_SYNC_TYPE_MAN);
//			job.setEms(CdcpServerUtil.findEms(emsDn));
//			job.setSerial(task.getDn());

			String url = "node://"+emsdn+"/NBIService/retrieveAllSections";
			Ems ems = CdcpServerUtil.findEms(emsdn);
			Serializable obtain = nodeEmsProxy.proxy.obtain(ems, url);
			sectionlogger.info("obtain = "+(obtain == null ? null : (obtain instanceof  Collection ? obtain.getClass().getSimpleName()+" size = "+((Collection) obtain).size() : obtain)));
			ArrayList<Section> sectionList = (ArrayList) obtain;

			sectionlogger.info("section size = "+sectionList == null ? null : sectionList.size());
			ArrayList <Section> neSections = new ArrayList<Section>();
			for (Section section : sectionList) {
				if (section.getaEndTP().contains(device+"@") || section.getzEndTP().contains(device+"@")) {
					neSections.add(section);
				}
			}
			sectionlogger.info("ne section size = "+neSections.size());

			AbstractDBFLoader loader = null;
			String type = ems.getType();

			if (type.equals(CDCPConstants.EMS_TYPE_ALU_PTN)) {
				if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
						|| ems.getTag1().equalsIgnoreCase("DWDM") || ems.getTag1().equalsIgnoreCase("SDH")) )
					loader = new ALU_OTN_Migrator(neSections,emsdn);
				else loader = new ALUDBFMigrator(neSections, emsdn);

			}

			if (type.equals("ZTE")) {
				if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
						|| ems.getTag1().equalsIgnoreCase("DWDM")) )
					loader = new ZTE_OTNU31_OTN_Migrator(neSections,emsdn);


			}

			if (type.equals(CDCPConstants.EMS_TYPE_FENGHUOOTNM2000_PTN)) {
				if (ems.getTag1() != null && ems.getTag1().equals("SDH"))
					loader = new FHOTNM2000OTN3Migrator(neSections,emsdn);
				else if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
						|| ems.getTag1().equalsIgnoreCase("DWDM")) ) {
					if (ems.getDn().equals("JH-OTNM2000-2-OTN") || ems.getDn().equals("JXI-OTNM2000-1-P"))
						loader = new FHOTNM2000OTN2Migrator(neSections,emsdn);
					else
						loader = new FHOTNM2000OTNMigrator(neSections,emsdn);
				}
				else
					loader = new FHOTNM2000Migrator(neSections, emsdn);

			}

			if (type.equals(CDCPConstants.EMS_TYPE_HWU2000_PTN)) {
				if (ems.getTag1() != null && ems.getTag1().equals("SDH"))
					loader = new HWU2000SDHMigrator(neSections,emsdn);
				else if (ems.getTag1() != null && (ems.getTag1().equals("DWDM") ||ems.getTag1().equals("OTN")  ))
					loader = new HWU2000DWDMMigrator(neSections,emsdn);
				else
					loader = new HWU2000DBFMigrator(neSections, emsdn);
			}


			if (loader == null)
				sectionlogger.info("致命异常: 无法为类型为:" + type + "的EMS 找到对应的同步适配程序");

			loader.migrateNESection(neSections,device);


			return "success";
		} catch (NodeException ne) {
			throw ne;
		}
		catch (Exception e) {
			sectionlogger.error(e, e);
			return null;
		} finally {
			if (nodeEmsProxy != null)
				try {
					nodeEmsProxy.close();
				} catch (IOException e) {
					sectionlogger.error(e, e);
				}
		}
	}

	public static String syncDeviceTask(Task task) throws NodeException {
		if (!task.getName().startsWith("DEVICE:")) return "not device task : "+task.getName();
		MBeanProxy<NodeAdminMBean> nodeAdminProxy = null;
		String emsDn = task.getTaskObject();
		String device = task.getName().substring("DEVICE:".length());
		try {

			nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(emsDn);

			EmsJob job = new EmsJob();
			job.setJobType(EmsJob.JOB_TYPE_SYNC_DEVICE);
			job.getDataMap().put(CDCPConstants.EMSJOB_DATA_KEY_DEVICE_DN,device);
			job.getDataMap().put(CDCPConstants.EMSJOB_DATA_KEY_SYNC_TYPE,CDCPConstants.EMSJOB_DATA_VALUE_SYNC_TYPE_MAN);
			job.setEms(CdcpServerUtil.findEms(emsDn));
			job.setSerial(task.getDn());

			nodeAdminProxy.proxy.executeJob(job);

			return job.getSerial();
		} catch (NodeException ne) {
			throw ne;
		}
		catch (Exception e) {
			logger.error(e, e);
			return null;
		} finally {
			if (nodeAdminProxy != null)
				try {
					nodeAdminProxy.close();
				} catch (IOException e) {
					logger.error(e, e);
				}
		}

	}

    public static String syncDevice(String emsDn,String device ) throws NodeException {

		MBeanProxy<NodeAdminMBean> nodeAdminProxy = null;
		try {
            Task task = MigrateManager.getInstance().registerEmsSingleMEJob(emsDn,device);

              nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(emsDn);

            EmsJob job = new EmsJob();
            job.setJobType(EmsJob.JOB_TYPE_SYNC_DEVICE);
            job.getDataMap().put(CDCPConstants.EMSJOB_DATA_KEY_DEVICE_DN,device);
            job.getDataMap().put(CDCPConstants.EMSJOB_DATA_KEY_SYNC_TYPE,CDCPConstants.EMSJOB_DATA_VALUE_SYNC_TYPE_MAN);
            job.setEms(CdcpServerUtil.findEms(emsDn));
            job.setSerial(task.getDn());

            nodeAdminProxy.proxy.executeJob(job);

            return job.getSerial();
        } catch (NodeException ne) {
            throw ne;
        }
        catch (Exception e) {
            logger.error(e, e);
            return null;
        } finally {
			if (nodeAdminProxy != null)
				try {
					nodeAdminProxy.close();
				} catch (IOException e) {
					logger.error(e, e);
				}
		}
	}


//	private static ReentrantLock dbLock = new ReentrantLock();
//	public static void acquireDBLock() {
//		logger.info("Try to accquire dbLock by thread : "+Thread.currentThread().getName());
//		try {
//			if (!dbLock.tryLock(12,TimeUnit.HOURS)) {
//                // 长时间获取不到数据库锁
//				logger.error("DB Lock acquired failed : "+Thread.currentThread().getName());
//            }  else {
//				logger.info("DB Lock acquired by thread : "+Thread.currentThread().getName());
//			}
//		} catch (InterruptedException e) {
//			logger.error(e, e);
//		}
//
//	}
//
//	public static void releaseDBLock() {
//		try {
//			dbLock.unlock();
//		} catch (IllegalMonitorStateException e) {
//		}
//		logger.info("DB Lock released by thread : "+Thread.currentThread().getName());
//
//	}



	public static void main(String[] args) throws Exception {
        MBeanProxy<NodeAdminMBean> nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy("10.212.46.89", 10003);

        System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).format(new Date()));
		ServerEnv.init();

		String emsdn = "abcde";
		String serial = "12134";
		Task task = new Task();
		task.setDn(serial);
		task.setScheduleId(111l);
		task.setName(emsdn);
		task.setDescription(emsdn);
		task.setTag1(emsdn);
		task.setTaskObject(emsdn);
		task.setStartTime(new Date());
		task.setStatus(com.alcatelsbell.cdcp.common.Constants.TASK_STATUS_SBI_RUNNING);

		try {
			JpaServerUtil.getInstance().saveObject(-1, task);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String ql = " update Task c set c.status = " + 1 + " where c.dn = '" + "sdf" + "'";
		JpaServerUtil.getInstance().executeUpdateSQL(ql);
		List<Ems> allObjects = JpaServerUtil.getInstance().findAllObjects(Ems.class);
		for (int i = 0; i < allObjects.size(); i++) {
			Ems ems = allObjects.get(i);
			CorbaEms corbaEms = new CorbaEms(ems);
			System.out.println(corbaEms.getEmsName());
		}

	}

}
