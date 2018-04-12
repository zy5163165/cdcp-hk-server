package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.util.date.DateUtil;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-5-7
 * Time: 上午11:12
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ScheduleService {
    private Log logger = LogFactory.getLog(getClass());
    public static ScheduleService inst = new ScheduleService();
    public static ScheduleService getInstance() {
        return inst;
    }
    private ScheduleService() {

    }
    Scheduler m_scheduler = null;
    public void start() throws Exception {
        m_scheduler = StdSchedulerFactory.getDefaultScheduler();
        m_scheduler.start();
        scanSchedules();
    }

    public void scanSchedules() throws Exception {
        List<Schedule> schedules = JpaServerUtil.getInstance().findObjects("select c from Schedule c where c.status = " + Schedule.STATUS_ACTIVE);
        if (schedules != null && schedules.size() > 0) {
            for (int i = 0; i < schedules.size(); i++) {
                Schedule schedule = schedules.get(i);
                try {
                    logger.info("applySchedule:"+schedule.getDn()+" type="+schedule.getTimeType()+" exp="+schedule.getTimeExpression());
                    applySchedule(schedule);
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
    }

    public void destorySchedule(Schedule schedule) throws SchedulerException {
        m_scheduler.deleteJob(getJobDetailName(schedule),
                Scheduler.DEFAULT_GROUP);
    }
    public void reSchedule(Schedule schedule) throws SchedulerException, ParseException {

        try {
            logger.info("reSchedule : "+schedule.getId()+" -- "+schedule.getTimeExpression());
            destorySchedule(schedule);
            logger.info("schedule : "+schedule.getId()+" destoryed");
        } catch (SchedulerException e) {
            logger.info("schedule : "+schedule.getId()+" destoryed failed ,maybe not actived before.");
        }
        if (schedule.getStatus().intValue() == Schedule.STATUS_ACTIVE) {
            applySchedule(schedule);
            logger.info("schedule : "+schedule.getId()+" is active and applyed ");
        }
    }

    public void applySchedule(Schedule schedule) throws SchedulerException, ParseException {
        Integer timeType = schedule.getTimeType();
        if (timeType != null) {
            switch (timeType.intValue()) {
                case Schedule.TIME_TYPE_FIX : {
                    Date date = DateUtil.parseDate(schedule.getTimeExpression());

                    if (date != null) {
                        if (date.after(new Date())) {
                            SimpleTrigger trigger = new SimpleTrigger("trigger-schedule:"+schedule.getId(),date );
                            m_scheduler.scheduleJob(createJobDetail(schedule),trigger);
                        } else {
                            LogUtil.error(this, "Out of date : " + schedule);
                        }
                    }
                    else {
                        LogUtil.error(this, "Failed to parse schedule time : " + schedule);
                        date = new Date(System.currentTimeMillis()+ 60 * 5000l);
                        SimpleTrigger trigger = new SimpleTrigger("ems-sbi-Trigger-:"+schedule.getId(),date );
                        m_scheduler.scheduleJob(createJobDetail(schedule),trigger);
                    }

                    break;
                }

                case Schedule.TIME_TYPE_CRON : {
                    CronTrigger m_trigger = new CronTrigger("ems-sbi-Trigger-"+schedule.getId(),
                            Scheduler.DEFAULT_GROUP, schedule.getTimeExpression());
                    m_scheduler.scheduleJob(createJobDetail(schedule),m_trigger);
                    break;
                }
                
                case Schedule.TIME_TYPE_MANUAL : {
//                    CronTrigger m_trigger = new CronTrigger("ems-sbi-Trigger-"+schedule.getId(),
//                            Scheduler.DEFAULT_GROUP, schedule.getTimeExpression());
//                    m_scheduler.scheduleJob(createJobDetail(schedule),m_trigger);
                    break;
                }

                case Schedule.TIME_TYPE_CYCLE : {
                    System.out.println("!!!!!! timeType = " + timeType);
                    String timeExpression = schedule.getTimeExpression();
                    String min = timeExpression.substring(timeExpression.lastIndexOf("|")+1);
                    int minute = Integer.parseInt(min);
                    if (schedule.getTaskObjects() != null) {
                        Ems ems = null;
                        try {
                            ems = (Ems) JpaClient.getInstance().findObjectByDN(Ems.class,schedule.getTaskObjects());
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                        if (ems != null) {
                             new CycleJob(schedule,minute * 1000 * 60l,ems).start();
                            System.out.println("CycleJob started");
                        } else {
                            logger.error("无法找到EMS:"+schedule.getTaskObjects());
                        }
                    }
                    break;
                }
                default :
                	break;
            }
        }
    }

    private JobDetail createJobDetail(Schedule schedule) {
        JobDetail jobDetail = new JobDetail(getJobDetailName(schedule),
                Scheduler.DEFAULT_GROUP, EmsSBIJob.class);
        jobDetail.getJobDataMap().put(EmsSBIJob.ATTRIBUTE_SCHEDULE,schedule);
        return jobDetail;
    }

    private String getJobDetailName(Schedule schedule) {
        return "ems-sbi-"+schedule.getId();
    }

    public static void main(String[] args) throws Exception {
        String[] locations = {"appserver-spring.xml"};
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        ScheduleService.getInstance().start();
        Thread.sleep(10000l);
    }
}
