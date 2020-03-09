package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/16
 * Time: 10:08
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ReflectionUtil {
    private Log logger = LogFactory.getLog(getClass());

    public static List<Field> getAllFields (Class cls) {
        List<Field> fs = new ArrayList<Field>();
        while (true) {
            Field[] declaredFields = cls.getDeclaredFields();

            if (declaredFields != null && declaredFields.length > 0) {
                for (Field declaredField : declaredFields) {
                    if (declaredField.getAnnotation(Transient.class) == null && !Modifier.isFinal(declaredField.getModifiers()) )
                        fs.add(declaredField);

                }

            }

            cls = cls.getSuperclass();
            if (cls.equals(Object.class)) break;
        }
        return fs;
    }
    public static Field getField(Class<?> type, String name) {
        Field result = null;
        Field[] arr$ = type.getDeclaredFields(); int len$ = arr$.length; for (int i$ = 0; i$ < len$; ++i$) { Field field = arr$[i$];
            if (field.getName().equals(name))
                return field;
        }

        if (type.getSuperclass() != null) {
            return getField(type.getSuperclass(), name);
        }

        return result;
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        if(obj != null) {
            Field field =  getField(obj.getClass(), fieldName);
            setFieldValue(obj, field, value);
        }
    }

    public static void setFieldValue(Object obj, Field field, Object value) throws Exception {
        boolean oldAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(oldAccessible);
    }

    public static Object getFieldValue(Object o, Field field ) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(o);
    }



}
