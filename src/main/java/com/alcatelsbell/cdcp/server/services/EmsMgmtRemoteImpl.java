package com.alcatelsbell.cdcp.server.services;

import com.alcatelsbell.cdcp.api.EmsMgmtIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nodefx.EmsJob;
import com.alcatelsbell.cdcp.nodefx.NodeAdmin;
import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.cdcp.nodefx.NodeException;
import com.alcatelsbell.cdcp.server.*;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.cdcp.web.JettyMain;
import com.alcatelsbell.cdcp.web.httpd.CdcpAjaxHandler;
import com.alcatelsbell.cdcp.web.httpd.HttpRequestHandler;
import com.alcatelsbell.cdcp.web.httpd.NanoHTTPd;
import com.alcatelsbell.hippo.framework.service.DefaultServiceImpl;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.ThreadDumper;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import com.alcatelsbell.nms.valueobject.sys.Log;
import com.alcatelsbell.nms.valueobject.sys.SysNode;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午4:50
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class EmsMgmtRemoteImpl extends DefaultServiceImpl implements EmsMgmtIFC {
	CdcpNodeMaster cdcpNodeMaster = null;

	public EmsMgmtRemoteImpl() throws RemoteException {

	}

	public java.lang.String getJndiNamePrefix() {
		return Constants.SERVICE_NAME_CDCP_EMS;
	}

	private CdcpOverview cdcpOverview = null;
	@Override
	public void start() {
		logger.info("starting");
		cdcpNodeMaster = new CdcpNodeMaster();

        if (SysProperty.getString("cdcp.ems.polling","on").equalsIgnoreCase("on")) {
            logger.info("start ems polling");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(60 * 1000l);
                    } catch (InterruptedException e) {
                        logger.error(e, e);
                    }
                    pollEms();
                    try {
                        Thread.sleep(60 * 1000l * 60l);
                    } catch (InterruptedException e) {
                        logger.error(e, e);
                    }
                }
            };
            new Thread(r).start();
        }
		initSchedules();



		cdcpOverview = CdcpOverview.bind("s1");

		try {
			//startHttpd();
			JettyMain.main(null);
		} catch (Throwable e) {
			logger.error(e, e);
		}




	}

	private void startHttpd() throws IOException {
	//	int port = XProperties.get().getInt("nbi.httpd.port", 8888);
		int port = SysProperty.getInt("cdcp.httpd.port",8888);
		NanoHTTPd nanoHTTPd = new NanoHTTPd(port);
		CdcpAjaxHandler handler = new CdcpAjaxHandler();
		nanoHTTPd.setHttpRequestHandler(handler);
		logger.info("Httpd started ,listen on port : "+port);

	}

	private void initSchedules() {
		List<Ems> emses = null;
		try {
			emses = JpaClient.getInstance().findAllObjects(Ems.class);
		} catch (Exception e) {
			logger.error(e, e);
		}
		for (int i = 0; i < emses.size(); i++) {
			Ems ems = emses.get(i);
			checkInitSchedule(ems);
		}
	}

	private void checkInitSchedule(Ems ems) {

		Schedule schedule = new Schedule();
		schedule.setTaskObjects(ems.getDn());
		schedule.setTimeType(Schedule.TIME_TYPE_CRON);
		schedule.setTimeExpression("0 10 17 * * ?");
		schedule.setStatus(Schedule.STATUS_ACTIVE);
		schedule.setJobType("MIGRATE-RESOURCE");
		schedule.setDn("DEFAULT_" + ems.getDn());

		try {
			Object objectByDN = JpaClient.getInstance().findObjectByDN(Schedule.class, schedule.getDn());
			if (objectByDN == null) {
				JpaClient.getInstance().saveObject(-1, schedule);
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	private void pollEms() {
		logger.info("Start Polling EMS");
		List<Ems> emses = null;
		try {
			emses = JpaClient.getInstance().findAllObjects(Ems.class);
		} catch (Exception e) {
			logger.error(e, e);
		}
		for (int i = 0; i < emses.size(); i++) {
			Ems ems = emses.get(i);
			try {
                if (ems.getMonitored() == null || ems.getMonitored().intValue() == 1) {
                    logger.info("check ems :" + ems.getDn());
                    logger.info(" result = " + testEms(ems));
                }
			} catch (RemoteException e) {
				logger.error(e, e);
			}
		}
	}

	@Override
	public Ems createEms(Ems ems) throws RemoteException {
		try {
            ems.setId(null);
			ems = (Ems) JpaClient.getInstance().saveObject(-1, ems);
		} catch (Exception e) {
			logger.error(e, e);
			throw new RemoteException(e.getMessage(), e);
		} finally {
			CdcpServerUtil.createEmsLog(ems.getDn(), "EMS操作", "新增EMS:" + ems);
		}

		MBeanProxy<NodeAdminMBean> nodeAdminProxy = null;
		try {
			nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(ems.getDn());
			nodeAdminProxy.proxy.newEms(ems);
		} catch (Exception e) {
			logger.error(e, e);
			throw new RemoteException("EMS创建成功，但下发EMS任务失败:"+e.getMessage(), e);
		}

		cdcpOverview.scheduleEmsWithCronExp(ems.getDn(),null);
		return ems;
	}

	@Override
	public void deleteEms(String emsdn) throws RemoteException {
		try {
			Ems ems = (Ems) JpaClient.getInstance().findObjectByDN(Ems.class, emsdn);
			if (ems != null)
				JpaClient.getInstance().removeObject(ems);
		} catch (Exception e) {
			logger.error(e, e);
			throw new RemoteException(e.getMessage(), e);
		} finally {
			CdcpServerUtil.createEmsLog(emsdn, "EMS操作", "删除EMS:" + emsdn);
		}
	}

	@Override
	public Ems modifyEms(Ems ems) throws RemoteException {
		try {
			ems = (Ems) JpaClient.getInstance().storeObjectByDn(-1, ems);
		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			CdcpServerUtil.createEmsLog(ems.getDn(), "EMS操作", "修改EMS:" + ems);
		}

		return ems;
	}

	@Override
	public List<Log> readEmsLogs(String emsDn, int limit) throws RemoteException {
		List objects = null;
		try {
			objects = JpaClient.getInstance().findObjects("select c from Log c where c.object = '" + emsDn + "' order by c.time desc", null, null, 0, limit);
		} catch (Exception e) {
			logger.error(e, e);
		}
		return objects;
	}

	@Override
	public boolean testEms(Ems ems) throws RemoteException {
		if (ems.getEmsversion() == null || ems.getEmsversion().isEmpty() || ems.getEmsversion().equals("Corba")) {
			boolean b = false;
			String cause = "";
			MBeanProxy<NodeAdminMBean> nodeAdminProxy = null;
			try {
				nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(ems.getDn());
				b = nodeAdminProxy.proxy.testEms(ems);
				//	CdcpServerUtil.updateEmsStatus(ems.getDn(), b ? Constants.EMS_STATUS_NORMAL : Constants.EMS_STATUS_EXCEPTION);
				return b;
			} catch (Exception e) {
				logger.error(e, e);
				cause = e.getMessage();
//			CdcpServerUtil.updateEmsStatus(ems.getDn(), Constants.EMS_STATUS_EXCEPTION);
				throw new RemoteException(e.getMessage(), e);
			} finally {
				try {
					if (nodeAdminProxy != null)
						nodeAdminProxy.close();
				} catch (IOException e) {
					logger.error(e, e);
				}
				CdcpServerUtil.createEmsLog(ems.getDn(), "EMS轮询测试", "EMS轮询测试 - " + (b ? "成功" : ("失败 " + cause)));
			}
		}
		return true;
	}

	@Override
	public String migrateEmsDBFile(String localFileUrl) throws RemoteException {
		String paras = null;
		if (localFileUrl.contains("?")) {
			paras = localFileUrl.substring(localFileUrl.indexOf("?") + 1);
			localFileUrl = localFileUrl.substring(0,localFileUrl.indexOf("?"));
		}

		if (!new File(localFileUrl).exists())
			throw new RemoteException("FileNotFound : " + localFileUrl);
		String emsdn = null;
		try {
			emsdn = CdcpServerUtil.readEmsDn(localFileUrl);
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
		final String serial = emsdn + "@" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "@Local";
		System.out.println("serial: " + serial);
		Task task = new Task();
		task.setDn(serial);
		// task.setScheduleId(schedule.getId());
		task.setName(emsdn);
		task.setDescription(emsdn);
		task.setTag1(emsdn);
		task.setTaskObject(emsdn);
		task.setStartTime(new Date());
		task.setStatus(Constants.TASK_STATUS_MIGRATE_WAITING);

		try {
			System.out.println("saveObject: task");
			JpaClient.getInstance().saveObject(-1, task);
		} catch (Exception e) {
			logger.error(e, e);
		}
		System.out.println("s1......");
		MigrateManager.getInstance().migrateLocal(localFileUrl, serial,paras);
		System.out.println("s2......");
		int size = MigrateManager.getInstance().getQueue().size();
		if (size > 0) {
			final String info = size + " tasks is waiting ...  ";
			final String _emsdn = emsdn;
			Runnable runnable = new Runnable() {
				public void run() {

					try {
						Thread.sleep(5000l);
						CdcpServerUtil.sendMigrateLogMessage(serial, _emsdn, info, 0);
					} catch (Exception e) {
						logger.error(e, e);
					}
				}
			};
			new Thread(runnable).start();

		}
		// return "Running ";

		return serial;
	}

    @Override
    public String manualSyncEms(String emsdn) throws RemoteException {
		String paras = null;
		if (emsdn.contains("?")) {
			 paras = emsdn.substring(emsdn.indexOf("?") + 1);
			emsdn = emsdn.substring(0,emsdn.indexOf("?"));
		}
        synchronized (this) {
            String timeStr = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String serial = "MANU_"+emsdn+"_"+timeStr;
            MigrateManager.getInstance().registerEmsJob(null,emsdn,serial);
            MBeanProxy<NodeAdminMBean> jmxmpProxy = null;
            try {
                Ems ems = (Ems)JpaClient.getInstance().findObjectByDN(Ems.class,emsdn);
                if (ems == null) throw new Exception("无法找到EMS："+emsdn);
                String sysNodeDn = ems.getSysNodeDn();
                SysNode sysNode = (SysNode)JpaClient.getInstance().findObjectByDN(SysNode.class,sysNodeDn);
                if (sysNode == null) throw new Exception("无法找到EMS："+emsdn+" 对应的采集节点:"+sysNodeDn);
                logger.info("createJmxmpProxy-> "+sysNode.getIpaddress()+":"+sysNode.getJmxport()+"\\"+ems.getDn());
                jmxmpProxy =   CdcpServerUtil.createNodeAdminProxy(sysNode.getIpaddress(), sysNode.getJmxport());
                EmsJob job = new EmsJob();
                job.setSerial(serial);
                job.setEms(ems);
                job.setJobType(EmsJob.JOB_TYPE_SYNC_EMS);

				if (paras != null) {

					String[] split = paras.split("&");
					HashMap map = new HashMap();
					for (String s : split) {
						String k = s.substring(0,s.indexOf("="));
						String v = s.substring(s.indexOf("=")+1);
						map.put(k, v);

					}

					if (ems.getUserObject() != null && ems.getUserObject() instanceof Map)
						((Map) ems.getUserObject()).putAll(map);
					else
						ems.setUserObject(map);


				}

                jmxmpProxy.proxy.executeJob(job);
                return "Task Delivered Success !";
            }  catch (Exception e) {
                logger.error(e, e);
                MigrateManager.getInstance().handleSbiFailed(e.getMessage(), serial);
                return "Task Delivered Failed !";
            }  finally {
                if (jmxmpProxy != null)
                    try {
                        jmxmpProxy.close();
                    } catch (IOException e) {
                        logger.error(e, e);
                    }
            }
        }

    }

    @Override
    public String emsNotify(Serializable notification) throws RemoteException {
        return null;
    }

	@Override
	public String manualSyncDevice(String deviceDn) throws RemoteException {
		try {
			String serial = CdcpServerUtil.syncDevice(deviceDn.substring(deviceDn.indexOf(":")+1,deviceDn.indexOf("@")), deviceDn);
			return serial;
		} catch (NodeException e) {
			logger.error(e, e);
			throw new RemoteException(e.getExceptionCode(),e);
		}

	}
}
