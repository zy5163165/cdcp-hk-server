package com.alcatelsbell.cdcp.nbi.ws.irmclient;

import com.alcatelsbell.cdcp.server.adapters.DBDataUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.modules.task.model.Task;
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
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-22
 * Time: 下午12:52
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class    IrmsClientUtil {
    private static Log logger = LogFactory.getLog(IrmsClientUtil.class);
    public static void callBackIRM(Task task ,int isSuccess,String failReason) {
        if (task != null)
            callBackIRM(task.getTag2(),isSuccess,failReason);
        else
            logger.error("nulltask",new Exception());
    }
    private static HashMap<String,AtomicInteger> taskSizeMap = new HashMap<String, AtomicInteger>();
    private static HashMap<String,String> faultMap = new HashMap<String, String>();
    public static void callBackIRM(String projectName ,int isSuccess,String failReason) {
        logger.info("callBackIRM:projectName="+projectName+";issucess="+isSuccess+";failReason="+failReason);
        //在收到请求时，projectName = projectName+">>"+SysUtil.nextLongId();>>后的内容用于标识一次请求
        // exp : projectName>>12321313##4
        if (projectName.contains(">>") && projectName.contains("##")) {
            String requestKey = projectName.substring(projectName.indexOf(">>")+2);
            projectName = projectName.substring(0,projectName.indexOf(">>"));
            String requestId = requestKey.substring(0,requestKey.indexOf("##"));
            int size = Integer.parseInt(requestKey.substring(requestKey.indexOf("##") + 2));

            logger.info("requestId="+requestId);
            logger.info("projectName="+projectName);
            logger.info("size="+size);
            synchronized (taskSizeMap) {
                AtomicInteger finishSize = taskSizeMap.get(requestId);
                if (finishSize == null) {
                    finishSize = new AtomicInteger(0);
                    taskSizeMap.put(requestId, finishSize);
                }

                if (failReason != null)
                    faultMap.put(requestId,failReason);

                finishSize.incrementAndGet();

                logger.info("The "+finishSize.intValue()+" time response of"+size);
                if (finishSize.intValue() == size) {
                    String fault = faultMap.get(requestId);
                    callBackIRM(projectName, fault == null ? 0 : 1 ,fault);
                }


            }

        } else {
            try {

                String xml  ="<strRequest>\n" +
                        "\t <summary>\n" +
                        "\t\t<fieldInfo>\n" +
                        "\t\t\t<fieldChName>工程名称</fieldChName>\n" +
                        "\t\t\t<fieldEnName>projectName</fieldEnName>\n" +
                        "\t\t\t<fieldContent>"+projectName+"</fieldContent>\n" +
                        "\t\t</fieldInfo>\n" +
                        "        <fieldInfo>\n" +
                        "\t\t\t<fieldChName>是否成功</fieldChName>\n" +
                        "\t\t\t<fieldEnName>isSuccess</fieldEnName>\n" +
                        "\t\t\t<fieldContent>"+isSuccess+"</fieldContent>\n" +
                        "\t\t</fieldInfo>\n" +
                        "        <fieldInfo>\n" +
                        "\t\t\t<fieldChName>失败原因</fieldChName>\n" +
                        "\t\t\t<fieldEnName>failReason</fieldEnName>\n" +
                        "\t\t\t<fieldContent>"+failReason+"</fieldContent>\n" +
                        "\t\t</fieldInfo>\n" +
                        "\t</summary>\n" +
                        "</strRequest>\n";
                logger.info("callBackIRM:"+xml);
                String s = new IrmsDataSyncWebServiceImplService().getIrmsDataSyncWebService().notifyNeInfoSyncResult(xml);
                logger.info("result="+s);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }


    public static void callBackIRMDeviceSectionSync(String projectName ,int isSuccess,String failReason) {
        logger.info("callBackIRMDeviceSectionSync:projectName="+projectName+";issucess="+isSuccess+";failReason="+failReason);

            try {

                String xml  ="<strRequest>\n" +
                        "\t <summary>\n" +
                        "\t\t<fieldInfo>\n" +
                        "\t\t\t<fieldChName>工程名称</fieldChName>\n" +
                        "\t\t\t<fieldEnName>projectName</fieldEnName>\n" +
                        "\t\t\t<fieldContent>"+projectName+"</fieldContent>\n" +
                        "\t\t</fieldInfo>\n" +
                        "        <fieldInfo>\n" +
                        "\t\t\t<fieldChName>是否成功</fieldChName>\n" +
                        "\t\t\t<fieldEnName>result_code</fieldEnName>\n" +
                        "\t\t\t<fieldContent>"+isSuccess+"</fieldContent>\n" +
                        "\t\t</fieldInfo>\n" +
                        "        <fieldInfo>\n" +
                        "\t\t\t<fieldChName>失败原因</fieldChName>\n" +
                        "\t\t\t<fieldEnName>result_msg</fieldEnName>\n" +
                        "\t\t\t<fieldContent>"+failReason+"</fieldContent>\n" +
                        "\t\t</fieldInfo>\n" +
                        "\t</summary>\n" +
                        "</strRequest>\n";
                logger.info("callBackIRM:"+xml);
                String s = new IrmsDataSyncWebServiceImplService().getIrmsDataSyncWebService().notify_sectionInfoSyncResult(xml);
                logger.info("result="+s);
            } catch (Exception e) {
                logger.error(e, e);
            }

    }



    public static void callIRMEmsMigrationFinished(final String emsDn) {
        JPASupport ctx = DBDataUtil.createJPASupport();
        try {
            EntityManager entityManager = ctx.getEntityManager();
            if (entityManager instanceof HibernateEntityManager) {
                Session session = ((HibernateEntityManager) entityManager).getSession();
                session.doWork(new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        CallableStatement cstmt = connection.prepareCall("{call callIrmAutoMigrate(?)}");
                        cstmt.setString(1, emsDn);
                        cstmt.execute();
                        //  logger.info("callIrmAutoMigrate sucess");
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
