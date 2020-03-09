package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.nodefx.CDCPConstants;
import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.cdcp.nodefx.ObjectInfo;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.alu.ALUDBFMigrator;
import com.alcatelsbell.cdcp.server.adapters.alu.ALU_OTN_Migrator;
import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.*;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000DBFMigrator;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000DWDMMigrator;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000SDHMigrator;
import com.alcatelsbell.cdcp.server.adapters.zte.ZTE_OTNU31_OTN_Migrator;
import com.alcatelsbell.cdcp.server.adapters.zte.ZTE_PTN_U31_Migrator;
import com.alcatelsbell.cdcp.server.v3.FileInfo;
import com.alcatelsbell.cdcp.server.v3.SBIFileLoader;
import com.alcatelsbell.cdcp.util.DataFileUtil;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午3:10
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrateRunnable implements Runnable {
	private Object sbiResult = null;
	private String serial = null;
	private Log logger = LogFactory.getLog(getClass());
    private Date time = new Date();
	private HashMap paras = null;
	private MigrateManager migrateManager = null;
	public static final int WAITING = 0;
	public static final int RUNNING = 1;
	public static final int SUSPENDED = 2;
	public static final int FINISHED = 3;

	public static final int TYPE_EMS = 0;
	public static final int TYPE_DEVICE = 1;

	private int state = WAITING;
	private int type = TYPE_EMS;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		getLogger().info("[STATE]="+state);
		this.state = state;
		if (migrateManager != null)
			migrateManager.onMRStateChange(this);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public MigrateRunnable(Object sbiResult, String serial,MigrateManager migrateManager) {
		this.sbiResult = sbiResult;
		this.serial = serial;
		this.migrateManager = migrateManager;
		if (sbiResult instanceof ObjectInfo)
			type = TYPE_DEVICE;
		if (sbiResult instanceof FtpInfo)
			type = TYPE_EMS;
	}

	public MigrateRunnable(Object sbiResult, String serial,HashMap paras,MigrateManager migrateManager) {
		this.sbiResult = sbiResult;
		this.serial = serial;
		this.paras = paras;
		this.migrateManager = migrateManager;
		if (sbiResult instanceof ObjectInfo)
			type = TYPE_DEVICE;
		if (sbiResult instanceof FtpInfo)
			type = TYPE_EMS;

	}

	public Object getSbiResult() {
		return sbiResult;
	}

	public String getSerial() {
		return serial;
	}
	private AbstractDBFLoader loader = null;






	private volatile boolean needSuspend = false;
	private ReentrantLock lock = new ReentrantLock();
	private Condition suspended = lock.newCondition();

	public void requestAndWaitToSuspendOrFinish() {
		if (getState() == RUNNING) {
			getLogger().info("Thread is requested to suspend by " + Thread.currentThread().getName());
			needSuspend = true;
			lock.lock();
			try {
				suspended.await();
			} catch (InterruptedException e) {
				getLogger().error(e, e);
			} finally {
				lock.unlock();
			}
		}

	}

	public void resume() {
		getLogger().info("Thread resume by "+Thread.currentThread().getName());
		synchronized (this) {
			needSuspend = false;
			this.notifyAll();
		}
	}
	private void notifySuspendWaiters() {
		lock.lock();
		try {
			suspended.signalAll();
		} catch (Exception e) {
			getLogger().error(e, e);
		} finally {
			lock.unlock();
		}
	}
	public void checkSuspend() {
		if (needSuspend) {
			notifySuspendWaiters();


			synchronized (this) {
				try {
					setState(SUSPENDED);
					getLogger().info("[SUSPEND]Suspend ! Waiting ...");
					this.wait();
					getLogger().info("[RESUME]Waiting finished !");
					setState(RUNNING);
				} catch (InterruptedException e) {
					getLogger().error(e, e);
				}
			}
		}
	}

	private Logger getLogger() {
		if (loader != null)
			return loader.getLogger();
		return Logger.getLogger(getClass());
	}



	@Override
	public void run() {
		setState(RUNNING);
		CdcpServerUtil.updateTask(serial, Constants.TASK_STATUS_MIGRATING, null);
		try {
			internalRun();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			setState(FINISHED);
			notifySuspendWaiters();
		}

	}

	private void internalRun() {
		String emsdn = null;
		Ems ems = null;
		String type = null;
		try {
			Task task = (Task) JpaClient.getInstance().findObjectByDN(Task.class, serial);
			if (task == null)
				throw new Exception("无法找到任务:" + serial);
			emsdn = task.getTag1();
			ems=  (Ems) JpaClient.getInstance().findObjectByDN(Ems.class, emsdn);
			if (ems == null)
				throw new Exception("无法找到EMS:" + emsdn);
			type = ems.getType();
			if (type == null)
				throw new Exception("EMS:" + emsdn + " 未设置类型");
		} catch (Exception e) {
			logger.error(e, e);
			MigrateManager.getInstance().handleMigrateFailed("致命异常:" + e.getMessage(), serial);
		}

		if (sbiResult instanceof FtpInfo) {

			File file = null;
			FtpInfo ftpInfo = (FtpInfo) sbiResult;
			CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "开始下载文件:" + ftpInfo.getRemoteFilePath() + ";" + ftpInfo.getFileName());
			try {
				file = DataFileUtil.downloadFile(ftpInfo);

			} catch (Exception e) {
				logger.error(e, e);
				MigrateManager.getInstance().handleMigrateFailed("下载文件失败:" + e.getMessage() + ":" + ftpInfo.getRemoteFilePath() + "/" + ftpInfo.getFileName(),
						serial);
				CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "下载文件异常:" + e.getMessage() + " 文件信息:" + ftpInfo.getRemoteFilePath() + ";"
						+ ftpInfo.getFileName());

			}
			if (file == null || !file.exists()) {
				file = new File("db",ftpInfo.getFileName());
				com.alcatelsbell.nms.util.FileUtil.copyFile(new File(ftpInfo.getRemoteFilePath(),ftpInfo.getFileName()), file);
				file = new File("db",ftpInfo.getFileName());
				if (!file.exists()) {
					logger.error("copy file failed !! :"+ftpInfo);
					file = null;
				}
			}

			if (file != null) {

				CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "下载文件成功:" + ftpInfo.getRemoteFilePath() + ";" + ftpInfo.getFileName());



				if (type.equals(CDCPConstants.EMS_TYPE_ALU_PTN)) {
					if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
							|| ems.getTag1().equalsIgnoreCase("DWDM")) )
						loader = new ALU_OTN_Migrator(file.getAbsolutePath(),emsdn);
					else loader = new ALUDBFMigrator(file.getAbsolutePath(), emsdn);

				}

				if (type.equals("ZTE")) {
					if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
							|| ems.getTag1().equalsIgnoreCase("DWDM")) )
						loader = new ZTE_OTNU31_OTN_Migrator(file.getAbsolutePath(),emsdn);
					else
						loader = new ZTE_PTN_U31_Migrator(file.getAbsolutePath(),emsdn);


				}

				if (type.equals(CDCPConstants.EMS_TYPE_FENGHUOOTNM2000_PTN)) {
					if (ems.getTag1() != null && ems.getTag1().equals("SDH"))
						loader = new FHOTNM2000OTN3Migrator(file.getAbsolutePath(),emsdn);
					else if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
							|| ems.getTag1().equalsIgnoreCase("DWDM")) ) {
						if ( ems.getDn().equals("JXI-OTNM2000-1-P")
							//	||ems.getDn().equals("JH-OTNM2000-2-OTN")
								)
							loader = new FHOTNM2000OTN2Migrator(file.getAbsolutePath(),emsdn);
						else
							loader = new FHOTNM2000OTNMigrator(file.getAbsolutePath(),emsdn);
					}
					else
						loader = new FHOTNM2000Migrator(file.getAbsolutePath(), emsdn);

				}

				if (type.equals(CDCPConstants.EMS_TYPE_HWU2000_PTN)) {
					if (ems.getTag1() != null && ems.getTag1().equals("SDH"))
						loader = new HWU2000SDHMigrator(file.getAbsolutePath(),emsdn);
					else if (ems.getTag1() != null && (ems.getTag1().equals("DWDM") ||ems.getTag1().equals("OTN")  ))
						loader = new HWU2000DWDMMigrator(file.getAbsolutePath(),emsdn);
					else
						loader = new HWU2000DBFMigrator(file.getAbsolutePath(), emsdn);
				}

				if (loader == null) {
					logger.error("致命异常: 无法为类型为:" + type + "的EMS 找到对应的同步适配程序");
					MigrateManager.getInstance().handleMigrateFailed("致命异常: 无法为类型为:" + type + " tag1="+ems.getTag1()+" 的EMS 找到对应的同步适配程序", serial);
				}

				loader.setTaskSerial(serial);

				if (paras != null) {
					loader.getAttributesMap().putAll(paras);
				}
				try {
					logger.info("Start executing task : " + serial+" ,loader="+loader.getClass());
					CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "开始解析文件入库:" + ftpInfo.getFileName());



					loader.setRunnable(this);
					loader.execute();

					logger.info("Finish executing task : " + serial);
					CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "结束解析文件入库:" + serial + ":" + ftpInfo.getFileName());
					MigrateManager.getInstance().handleMigrateFinish(serial, true,loader.isMigrateLogical());
					loader = null;
				} catch (Throwable e) {
					logger.error("Failed executing task : " + serial);
					logger.error(e, e);
					MigrateManager.getInstance().handleMigrateFailed("同步数据失败:" + e.getMessage(), serial);
					CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "解析文件入库失败:" + serial + ";Exception:" + e.getMessage());
				}
			}
		}

		else if (sbiResult instanceof ObjectInfo) {
			MBeanProxy<NodeAdminMBean> nodeAdminProxy = null;
			try {
				nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(emsdn);
				Serializable serializable = nodeAdminProxy.proxy.pullObject(((ObjectInfo) sbiResult).getToken());
				CdcpServerUtil.createEmsLog(emsdn, "同步接口上报数据", "[" + serial + "] ");



				if (type.equals(CDCPConstants.EMS_TYPE_ALU_PTN)) {
					if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
							|| ems.getTag1().equalsIgnoreCase("DWDM")) )
						loader = new ALU_OTN_Migrator(serializable,emsdn);
					else loader = new ALUDBFMigrator(serializable, emsdn);

				}

				if (type.equals("ZTE")) {
					if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
							|| ems.getTag1().equalsIgnoreCase("DWDM")) )
						loader = new ZTE_OTNU31_OTN_Migrator(serializable,emsdn);


				}

				if (type.equals(CDCPConstants.EMS_TYPE_FENGHUOOTNM2000_PTN)) {
					if (ems.getTag1() != null && ems.getTag1().equals("SDH"))
						loader = new FHOTNM2000OTN3Migrator(serializable,emsdn);
					else if (ems.getTag1() != null && (ems.getTag1().equalsIgnoreCase("OTN")
							|| ems.getTag1().equalsIgnoreCase("DWDM")) ) {
						if (ems.getDn().equals("JH-OTNM2000-2-OTN") || ems.getDn().equals("JXI-OTNM2000-1-P"))
							loader = new FHOTNM2000OTN2Migrator(serializable,emsdn);
						else
							loader = new FHOTNM2000OTNMigrator(serializable,emsdn);
					}
					else
						loader = new FHOTNM2000Migrator(serializable, emsdn);

				}

				if (type.equals(CDCPConstants.EMS_TYPE_HWU2000_PTN)) {
					if (ems.getTag1() != null && ems.getTag1().equals("SDH"))
						loader = new HWU2000SDHMigrator(serializable,emsdn);
					else if (ems.getTag1() != null && (ems.getTag1().equals("DWDM") ||ems.getTag1().equals("OTN")  ))
						loader = new HWU2000DWDMMigrator(serializable,emsdn);
					else
						loader = new HWU2000DBFMigrator(serializable, emsdn);
				}


				if (loader == null)
					MigrateManager.getInstance().handleMigrateFailed("致命异常: 无法为类型为:" + type + "的EMS 找到对应的同步适配程序", serial);

				loader.setTaskSerial(serial);
				Task task = CdcpServerUtil.findTask(serial);
				if (task == null) {
					logger.error("Failed to find task : "+serial);
				}
				try {
					logger.info("Start executing objectinfo task : " + serial);
					CdcpServerUtil.createEmsLog(emsdn, "同步接口单个网元数据", "[" + serial + "] ");
					// loader.executeMigrateDevice();
					loader.setRunnable(this);
					loader.migrateSingleDevice();
					logger.info("Finish executing task : " + serial);
					CdcpServerUtil.createEmsLog(emsdn, "同步接口单个网元数据", "[" + serial + "] " + "结束:");
					MigrateManager.getInstance().handleMigrateFinish(serial, false,true);
					try {
						IrmsClientUtil.callBackIRM(task,0,"");
					} catch (Exception e) {
						logger.error(e, e);
					}
				} catch (Throwable e) {
					logger.error("Failed executing task : " + serial);
					logger.error(e, e);
					MigrateManager.getInstance().handleMigrateFailed("同步接口单个网元数据失败:" + e.getMessage(), serial);
					IrmsClientUtil.callBackIRM(task,1,"ServerError");
					CdcpServerUtil.createEmsLog(emsdn, "同步接口单个网元数据", "[" + serial + "] ");
				}

			} catch (Exception e) {
				logger.error(e, e);
			} finally {
				if (nodeAdminProxy != null) {
					try {
						nodeAdminProxy.close();
					} catch (IOException e) {
						logger.error(e, e);
					}
				}


			}
		} else if (sbiResult instanceof File) {
		//	File file = ((FileInfo) sbiResult).getFile();
			File file = (File)sbiResult;
			if (file != null && file.exists()) {
				Task task = CdcpServerUtil.findTask(serial);
				if (task == null) {
					logger.error("Failed to find task : "+serial);
				} else {
					Schedule schedule = CdcpServerUtil.findSchedule(task.getScheduleId());
					if (schedule != null) {
						String loaderName = (String)this.paras.get("loader.class");
						if (loaderName != null) {
							try {
								SBIFileLoader loader = (SBIFileLoader)Class.forName(loaderName).newInstance();
								loader.setTaskSerial(serial);

								if (paras != null) {
									loader.getAttributesMap().putAll(paras);
								}
								logger.info("Start executing task : " + serial+" ,loader="+loader.getClass());
								CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "开始解析文件入库:" + file.getAbsolutePath());
								loader.setRunnable(this);
								loader.execute();
								logger.info("Finish executing task : " + serial);
								CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "结束解析文件入库:" + serial + ":" + file.getAbsolutePath());
								MigrateManager.getInstance().handleMigrateFinish(serial, true,loader.isMigrateLogical());
								loader = null;
							} catch (Throwable e) {
								logger.error("Failed executing task : " + serial);
								logger.error(e, e);
								MigrateManager.getInstance().handleMigrateFailed("同步数据失败:" + e.getMessage(), serial);
								CdcpServerUtil.createEmsLog(emsdn, "同步接口数据", "[" + serial + "] " + "解析文件入库失败:" + serial + ";Exception:" + e.getMessage());
							}
						}

					}
				}
			} else {
				logger.error(sbiResult+" file not existd !");
			}
		}

	}

}
