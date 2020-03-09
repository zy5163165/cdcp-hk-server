package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.irm.IrmCollectReportUtil;
import com.alcatelsbell.cdcp.nbi.irm.TJ_INTERFACE_EMSRESULTS;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.cdcp.nodefx.ObjectInfo;
import com.alcatelsbell.cdcp.server.adapters.DBDataUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;

import javax.persistence.EntityManager;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-11
 * Time: 下午4:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrateManager {
	private Log logger = LogFactory.getLog(getClass());
	public static MigrateManager inst = new MigrateManager();

	public static MigrateManager getInstance() {
		return inst;
	}
	private MigrateRunnable currentJob;
	private MigrateRunnable suspendEmsJob;

	private MigrateManager() {
		CdcpServerUtil.setDbFileExecutor(this.executor);
		try {
			ScheduleService.getInstance().start();
		} catch (Exception e) {
			LogUtil.error(getClass(), e, e);
		}

		Timer timer = new Timer();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY,8);
		calendar.set(Calendar.MINUTE,0);
		if (calendar.getTime().before(new Date())) {
			calendar.add(Calendar.DAY_OF_MONTH,1);
		}
		try {
			timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
					Calendar ca = Calendar.getInstance();
					if (ca.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
							|| ca.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
						return;
					logger.info("[Morning!] 8 am now, clear the migration queue!");
                  //  queue.clear();
					Iterator<MigrateRunnable> iterator = queue.iterator();
					List<MigrateRunnable> toberemoved = new ArrayList<MigrateRunnable>();
					while (iterator.hasNext()) {
						MigrateRunnable migrateRunnable = iterator.next();

						if (migrateRunnable.getSbiResult() instanceof FtpInfo) {
							logger.info(migrateRunnable.getSerial()+" to be removed from queue");
							toberemoved.add(migrateRunnable);
						}
					}

					queue.removeAll(toberemoved);
				}
            }, calendar.getTime(),24 * 3600 * 1000l);
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	private void initJobs() {


	}

	public MigrateRunnable getCurrentJob() {
		return currentJob;
	}

	public MigrateRunnable getSuspendEmsJob() {
		return suspendEmsJob;
	}

	public void setSuspendEmsJob(MigrateRunnable suspendEmsJob) {
		this.suspendEmsJob = suspendEmsJob;
		logger.info("[JOB]suspendEmsJob = "+suspendEmsJob);
	}

	public void setCurrentJob(MigrateRunnable current) {
		this.currentJob = current;
		logger.info("[JOB]currentJob = "+current);
	}

	private ConcurrentHashMap hashMap = new ConcurrentHashMap();

	public void registerEmsJob(Schedule schedule, String emsdn, String serial) {
		Task task = new Task();
		task.setDn(serial);
        if (schedule != null)
		    task.setScheduleId(schedule.getId());
		task.setName(emsdn);
		Ems ems = null;
		try {
			ems = (Ems) JpaClient.getInstance().findObjectByDN(Ems.class, emsdn);
		} catch (Exception e) {
			logger.error(e, e);
		}
		if (ems != null)
			task.setDescription(ems.getName());
		task.setTag1(emsdn);
		if (ems.getProtocalType() != null)
			task.setFromWhere(ems.getProtocalType());
		task.setTaskObject(emsdn);
		task.setStartTime(new Date());
		task.setStatus(Constants.TASK_STATUS_SBI_RUNNING);

		try {
			JpaClient.getInstance().saveObject(-1, task);
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	public synchronized Task registerEmsSingleMEJob(String emsdn, String me) {
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            logger.error(e, e);
        }
        String serial = me + "<>" + emsdn + "@" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		Task task = new Task();
		task.setDn(serial);
		// task.setScheduleId(schedule.getId());
		task.setName("DEVICE:" + me);
		task.setDescription("同步网元:" + me);
		task.setTag1(emsdn);
		// task.setTaskObject(me);
		task.setTaskObject(emsdn);
		task.setStartTime(new Date());
		task.setStatus(Constants.TASK_STATUS_SBI_RUNNING);

		try {
			return (Task) JpaClient.getInstance().saveObject(-1, task);
		} catch (Exception e) {
			logger.error(e, e);
		}
		return null;
	}

	public synchronized void updateTask(String serial, Integer status, String desc, Date endTime) {
		try {
			Task task = (Task) JpaClient.getInstance().findObjectByDN(Task.class, serial);
			if (task != null) {
				task.setStatus(status);
				if (desc != null)
					task.setDescription(desc);
				task.setEndTime(endTime);

				JpaClient.getInstance().saveObject(-1, task);

			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	public void handleSbiFinish(FtpInfo ftpInfo, String serial) {
        synchronized (this) {
            CdcpServerUtil.updateTask(serial, Constants.TASK_STATUS_MIGRATE_WAITING, null);

			Task task = CdcpServerUtil.findTask(serial);

			task.setTag3(System.currentTimeMillis() + "");

			try {
				JpaServerUtil.getInstance().saveObject(-1,task);
			} catch (Exception e) {
				logger.error(e, e);
			}

			HashMap map = new HashMap();
			try {
				if (task != null && task.getScheduleId() != null) {
                    Schedule schedule = CdcpServerUtil.findSchedule(task.getScheduleId());
                    if (schedule != null && schedule.getArguments() != null) {
                        String[] split = schedule.getArguments().split("&");
                        for (String s : split) {
                            String k = s.substring(0,s.indexOf("="));
                            String v = s.substring(s.indexOf("=")+1);
                            map.put(k, v);

                        }
                    }
                }
			} catch (Exception e) {
				logger.error(e, e);
			}
			if (ftpInfo.getAttributes().get("logical") != null)
				map.put("logical",ftpInfo.getAttributes().get("logical"));
			executor.execute(new MigrateRunnable(ftpInfo, serial,map,this));
            logger.info("Execute task " + serial + "," + ftpInfo);
            if (queue.size() > 0) {
				logger.info(queue.size() + " migrate task is waiting .. ");
			}
        }
		// todo
	}



    public void handleSbiFinishASAP(FtpInfo ftpInfo, String serial,HashMap paras) {
        synchronized (this) {
            try {
                CdcpServerUtil.updateTask(serial, Constants.TASK_STATUS_MIGRATE_WAITING, null);
                Iterator iterator = queue.iterator();
                List l = new ArrayList();
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    l.add(next);
                }
                queue.clear();
                executor.execute(new MigrateRunnable(ftpInfo, serial,paras,this));
                logger.info("Execute task " + serial + "," + ftpInfo);
                if (queue.size() > 0)
                    logger.info(queue.size() + " migrate task is waiting .. ");

                queue.addAll(l);
            } catch (Exception e) {
                logger.error(e, e);
                e.printStackTrace();
            }
        }
        // todo
    }

	private LinkedBlockingQueue<MigrateRunnable> deviceQueue = new LinkedBlockingQueue();
	private Runnable deviceQueueConsumer = null;
	public void handleSbiFinish(ObjectInfo objectInfo, String serial) {
		CdcpServerUtil.updateTask(serial, Constants.TASK_STATUS_MIGRATE_WAITING, null);

		deviceQueue.offer(new MigrateRunnable(objectInfo, serial,this));
		logger.info("Execute task " + serial + "," + objectInfo);
		if (deviceQueue.size() > 0)
			logger.info(deviceQueue.size() + " migrate task is waiting .. ");

		if (deviceQueueConsumer == null) {
			synchronized (this) {
				if (deviceQueueConsumer == null) {
					deviceQueueConsumer = new Runnable() {
						@Override
						public void run() {
							while (true) {
								try {
									MigrateRunnable runnable = deviceQueue.take();
									if (currentJob != null && currentJob.getType() == MigrateRunnable.TYPE_EMS) {
										currentJob.requestAndWaitToSuspendOrFinish();
									}
									Thread.currentThread().setName("DeviceConsumer-"+runnable.getSerial());
									runnable.run();
								} catch (Exception e) {
									logger.error(e, e);
								} finally {
									if (deviceQueue.isEmpty() && suspendEmsJob != null)
										suspendEmsJob.resume();

								}
							}
						}
					};

					new Thread(deviceQueueConsumer,"DeviceConsumer").start();
				}
			}
		}
		// todo
	}

	public void migrateLocal(String filePath, String serial) {
		FtpInfo info = new FtpInfo(null, null, null, 1, null, filePath);

		handleSbiFinishASAP(info, serial,null);
	}

	public void migrateLocal(String filePath, String serial,String paras) {
		FtpInfo info = new FtpInfo(null, null, null, 1, null, filePath);

		if (paras != null) {
			String[] split = paras.split("&");
			HashMap map = new HashMap();
			try {
				for (String s : split) {
                    String k = s.substring(0,s.indexOf("="));
                    String v = s.substring(s.indexOf("=")+1);
                    if (k != null && v != null)
                        map.put(k,v);
                }
			} catch (Exception e) {
				logger.error(e, e);
			}

			handleSbiFinishASAP(info, serial,map);
		}
		else
			handleSbiFinishASAP(info, serial,null);
	}

	private LinkedBlockingQueue queue = new LinkedBlockingQueue();

	public LinkedBlockingQueue getQueue() {

		return queue;
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	private ThreadPoolExecutor executor = new ThreadPoolExecutor(SysProperty.getInt("migrateThreadSize", 1), SysProperty.getInt("migrateThreadSize", 1), 10,
			TimeUnit.DAYS, queue);

	public void handleSbiFailed(String info, String serial) {
        logger.info("SBI Failed ,info="+info+"; serial="+serial);
		updateTask(serial, Constants.TASK_STATUS_SBI_EXCEPTION, info, new Date());
		CdcpServerUtil.upateEms(serial,  null, Constants.EMS_ISSYNCOK_NOK);

        Task task = CdcpServerUtil.findTask(serial);
        if (task != null && task.getTag2() != null && task.getTag2().length() > 0) {    //单网元同步任务
            IrmsClientUtil.callBackIRM(task.getTag2(), 1, info);
        }

		insertIrmDB(task,2,"厂商接口采集失败:"+info);

    }

	private void insertIrmDB(Task task,int result,String info) {
		if  (task != null ) {
			if (task.getDn().contains("@ManagedElement")) return;
			try {
				Ems ems = CdcpServerUtil.findEms(task.getTaskObject());
				if (ems != null) {
					TJ_INTERFACE_EMSRESULTS tj_interface_emsresults = new TJ_INTERFACE_EMSRESULTS(ems.getDn(), new Date(), ems.getProtocalType(),
							ems.getVendordn(), result, info, 0);
					if (task.getTag3() != null) {
					//	tj_interface_emsresults.setEndtime(new Date(Long.parseLong(task.getTag3().trim())));
					}
					//tj_interface_emsresults.setEndtime(task.getEndTime());
					IrmCollectReportUtil.emsCollectionResult(
							tj_interface_emsresults);
				}
			} catch (Exception e) {
				logger.error(e, e);
			}
		}

	}

	public void handleMigrateFinish(String serial, boolean callIrm, final boolean migrateLogical) {
		updateTask(serial, Constants.TASK_STATUS_FINISHED, null, new Date());
		CdcpServerUtil.updateTaskPercentage(serial, 100);
		final Ems ems = CdcpServerUtil.upateEms(serial,  new Date(), Constants.EMS_ISSYNCOK_OK);
        final String _serial = serial;
		insertIrmDB(CdcpServerUtil.findTask(serial), 1, null);
		if (callIrm) {
			logger.info("MigrateFinish ,callIrmAutoMigrate:"+serial);
			JPASupport ctx = DBDataUtil.createJPASupport();
			try {
				EntityManager entityManager = ctx.getEntityManager();
				if (entityManager instanceof HibernateEntityManager) {
					Session session = ((HibernateEntityManager) entityManager).getSession();
					session.doWork(new Work() {
						@Override
						public void execute(Connection connection) throws SQLException {
							CallableStatement cstmt = connection.prepareCall("{call callIrmAutoMigrate(?,?)}");
							cstmt.setString(1, ems.getDn());
							if (migrateLogical) {
								cstmt.setInt(2, 3);
								logger.info("callIrmAutoMigrate:"+3);
							}
							else {
								cstmt.setInt(2, 1);
								logger.info("callIrmAutoMigrate:"+1);
							}
							cstmt.execute();
							logger.info("callIrmAutoMigrate sucess:"+_serial);
						}
					});
				} else {
					logger.error("Not Hibernate Entity Manager :" + entityManager);
				}
			} catch (HibernateException e) {
				logger.error(e, e);
			} finally {
				ctx.release();
			}
		}
	}

	public void handleMigrateFailed(String info, String serial) {
		updateTask(serial, Constants.TASK_STATUS_MIGRATING_EXCEPTION, info, new Date());
		CdcpServerUtil.upateEms(serial, null, Constants.EMS_ISSYNCOK_NOK);
		insertIrmDB(CdcpServerUtil.findTask(serial), 4, "接口数据同步入库失败:" + info);
	}

	public void onMRStateChange(MigrateRunnable runnable) {
		synchronized (this) {
			if (runnable.getState() == MigrateRunnable.RUNNING) {
				if (runnable.getType() == MigrateRunnable.TYPE_EMS) {
					setSuspendEmsJob(null);
				}
				setCurrentJob(runnable);
			}

			if (runnable.getState() == MigrateRunnable.SUSPENDED) {
				if (getCurrentJob() == runnable)
					setCurrentJob(null);
				setSuspendEmsJob(runnable);
			}

			if (runnable.getState() == MigrateRunnable.FINISHED) {
				if (getCurrentJob() == runnable)
					setCurrentJob(null);
				if (getSuspendEmsJob() == runnable)
					setSuspendEmsJob(null);
			}
		}
	}

}
