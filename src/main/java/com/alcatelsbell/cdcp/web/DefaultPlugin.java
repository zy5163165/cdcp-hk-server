package com.alcatelsbell.cdcp.web;

import com.alcatelsbell.cdcp.server.ScheduleService;
import com.alcatelsbell.cdcp.web.common.*;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.valueobject.domain.Operator;
import com.alcatelsbell.nms.valueobject.domain.Role;
import com.alcatelsbell.nms.valueobject.domain.RoleAssign;

import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/11/3
 * Time: 10:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DefaultPlugin extends BObjectPlugin {
    private Logger logger = LoggerFactory.getLogger(DefaultPlugin.class);

    @Override
    public Class getJavaClass() {
        return null;
    }

    @Override
    public void onEvent(RequestContext context,BObjectEvent event) {
        if (event.getName().equals(BObjectEvent.DELETE) || event.getName().equals(BObjectEvent.UPDATE)) {
            try {
                if (event.getObject() instanceof Role || event.getObject() instanceof RoleAssign) {
                    refreshOps();
                }


            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (event.getName().equals(BObjectEvent.ADD)) {
            if (event.getObject() instanceof Ems) {
                initEmsSchedule(((Ems) event.getObject()) ,null);
            }
        }

        if (event.getName().equals(BObjectEvent.UPDATE)) {
            if (event.getObject() instanceof Schedule) {
                Schedule n = (Schedule)event.getObject();
                Schedule old = (Schedule)event.getObject2();
                if (!n.getTimeExpression().equals(old.getTimeExpression())) {
                    try {
                        ScheduleService.getInstance().reSchedule(n);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }


//        if (event.getName().equals(BObjectEvent.UPDATE)) {
//            if (event.getObject() instanceof Ems) {
//                Ems newEms = (Ems)event.getObject();
//                Ems oldEms = (Ems)event.getObject2();
//
//            }
//        }


    }

    public String initEmsSchedule(Ems ems,String cron) {
        if (cron == null || cron.trim().isEmpty())
            cron = "0 10 17 * * ?";
        Schedule schedule = new Schedule();
        schedule.setTaskObjects(ems.getDn());
        schedule.setJobName("默认采集计划-"+ems.getName());
        schedule.setTimeType(Schedule.TIME_TYPE_CRON);
        schedule.setTimeExpression(cron);
        schedule.setStatus(Schedule.STATUS_ACTIVE);
        schedule.setJobType("MIGRATE-RESOURCE");
        schedule.setDn("DEFAULT_" + ems.getDn());

        try {
            Object objectByDN = JpaClient.getInstance().findObjectByDN(Schedule.class, schedule.getDn());
            if (objectByDN == null) {
                schedule = (Schedule)JpaClient.getInstance().saveObject(-1, schedule);
            }  else {
                return "Schedule : "+schedule.getDn()+" already  existed !";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            ScheduleService.getInstance().reSchedule(schedule);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "ERROR  : "+e.toString();
        }

        return "Success , schedule cron = "+schedule.getTimeExpression();
    }



    private synchronized void refreshOps() {
        try {
            List<Operator> ops = JpaServerUtil.getInstance().findAllObjects(Operator.class);
            List<RoleAssign> assigns = JpaServerUtil.getInstance().findAllObjects(RoleAssign.class);
            List<Role> allroles = JpaServerUtil.getInstance().findAllObjects(Role.class);
            HashMap<Long,List<Role>> opRoles = new HashMap();
            HashMap<Long,Role> roleMap = new HashMap();
            for (Role role : allroles) {
                roleMap.put(role.getId(),role);
            }


            for (RoleAssign assign : assigns) {
                long operatorid = assign.getOperatorid();
                List<Role> roles = opRoles.get(operatorid);
                if (roles == null) {
                    roles = new ArrayList();
                    opRoles.put(operatorid,roles);
                }

                Role r = roleMap.get(assign.getRoleid());
                if (r != null)
                    roles.add(r);
                else {
                    JpaServerUtil.getInstance().deleteObject(assign);
                    logger.info("delete role assign : "+assign.getDn());
                }

            }
            for (Operator op : ops) {
                String old = op.getDescription();
                List<Role> _roles = opRoles.get(op.getId());
                String desc = "";
                if (_roles != null) {
                    for (Role role : _roles) {
                        desc += role.getName() + ",";
                    }
                    desc = desc.substring(0, desc.length() - 1);
                }



                op.setDescription(desc);
                if (!op.getDescription().equals(old)) {

                        JpaServerUtil.getInstance().saveObject(-1, op);

                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
