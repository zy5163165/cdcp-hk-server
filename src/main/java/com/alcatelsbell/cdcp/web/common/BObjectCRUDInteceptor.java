package com.alcatelsbell.cdcp.web.common;

/**
 * Author: Ronnie.Chen
 * Date: 12-10-12
 * Time: 下午1:53
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface BObjectCRUDInteceptor {
    public void fireOnCRUDEvent(CRUDEvent event) throws Exception;


    public class CRUDEvent {
        public static final int EVENT_TYPE_CREATE = 0;
        public static final int EVENT_TYPE_MODIFY = 1;
        public static final int EVENT_TYPE_DELETE = 2;

        private int eventType;
        private Object oldObject;
        private Object object;

        public CRUDEvent(int eventType, Object object) {
            this.eventType = eventType;
            this.object = object;
        }

        public int getEventType() {
            return eventType;
        }

        public void setEventType(int eventType) {
            this.eventType = eventType;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public Object getOldObject() {
            return oldObject;
        }

        public void setOldObject(Object oldObject) {
            this.oldObject = oldObject;
        }
    }
}


