package com.alcatelsbell.cdcp.web.common;

import com.alcatelsbell.cdcp.web.common.annotation.DicItem;
import com.alcatelsbell.cdcp.web.model.D_Dictionary;
import com.alcatelsbell.nms.common.DicEntry;
import com.alcatelsbell.nms.common.XMLLoadDictionary;
import com.alcatelsbell.nms.common.annotation.DicGroup;
import com.alcatelsbell.nms.common.annotation.DicGroupMapping;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;

import com.alcatelsbell.nms.util.SysProperty;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * User: Ronnie
 * Date: 12-5-9
 * Time: 上午10:23
 */
public class DictionaryUtil {
    private static Log log = LogFactory.getLog(DictionaryUtil.class);
//    private static Vector<Class> loadedClasses = new Vector<Class>();
//    public static void load(Class cls) {
//        loadedClasses.add(cls);
//    }

    static {
        try {
            init();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void init() {

        List<String> keys = SysProperty.listKeys("dictionary");
        for (String key : keys) {
            String json = SysProperty.getString(key);

            String groupno = key.substring(key.indexOf(".")+1);
            List<D_Dictionary> dbDictionaries = getDBDictionaries(groupno);
            if (dbDictionaries == null || dbDictionaries.isEmpty()) {
                JSONObject jsonObject = JSONObject.fromObject(json);
                Set<String> set = jsonObject.keySet();
                for (String s : set) {
                    int value = jsonObject.getInt(s);
                    D_Dictionary d = new D_Dictionary(s, value,s,"","");
                    d.setGroupno(Integer.parseInt(groupno));
                    d.setDn(groupno+":"+value);
                    try {
                        JpaServerUtil.getInstance().storeObjectByDn(-1,d);
                        log.info("Init Dictionary : "+groupno+" : "+s+" = "+value);
                    } catch (Exception e) {
                        log.error(e, e);
                    }
                }
            }
        }


        loadDBDictionaries();

    }


    private static HashMap<Integer,List<D_Dictionary>> dbDictionaries = null;


    public static synchronized void loadDBDictionaries() {
        if (dbDictionaries == null) dbDictionaries = new HashMap();
        dbDictionaries.clear();
        List<D_Dictionary> dics = null;
        try {
            dics = JpaServerUtil.getInstance().findAllObjects(D_Dictionary.class);
        } catch (Exception e) {
            log.error(e, e);
        }
        for (D_Dictionary dic : dics) {
            Integer group = dic.getGroupno();
            List<D_Dictionary> d_dictionaries = dbDictionaries.get(group);
            if (d_dictionaries == null) {
                d_dictionaries = new ArrayList();
                dbDictionaries.put(group,d_dictionaries);
            }
            d_dictionaries.add(dic);
        }
    }

    public static D_Dictionary getDBDictionaryEntry(int groupno,String desc) {
        if (desc == null || desc.trim().isEmpty()) return null;
        List<D_Dictionary> d_dictionaries = dbDictionaries.get(groupno);
        for (D_Dictionary d_dictionary : d_dictionaries) {
            if (desc.equals(d_dictionary.getDesc()))
                return d_dictionary;
        }
        return null;
    }

    public static List<D_Dictionary> getDBDictionaries(String group) {
        List objects = null;
        try {
            objects = JpaServerUtil.getInstance().findObjects("SELECT C FROM D_Dictionary C where C.groupno = " + group + "");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return objects;
    }

    public static HashMap getXMLDicEntrys(String groupName) {

            HashMap<Integer,String[]> map = new HashMap<Integer, String[]>();

            List<DicEntry> dicEntries = XMLLoadDictionary.getInstance().getDicEntries(groupName);
            if (dicEntries != null) {
                for (int i = 0; i < dicEntries.size(); i++) {
                    DicEntry dicEntry = dicEntries.get(i);
                    if (dicEntry.value >= 0) {
                        map.put(dicEntry.value,new String[]{dicEntry.desc,dicEntry.color,dicEntry.code});
                    } else {
                        map.put(-1-i,new String[]{dicEntry.desc,dicEntry.color,dicEntry.code});
                    }
                }
            }
            return map;

    }

    @Deprecated
    public static HashMap getDicEntrysOld(Class dicClass, String groupName) {
        Class[] classes = dicClass.getDeclaredClasses();
        HashMap<Integer, String[]> map = new HashMap<Integer, String[]>();
        if (classes != null) {
            for (Class cls1 : classes) {
                DicGroup dg = (DicGroup) cls1.getAnnotation(DicGroup.class);
                if ((dg != null && dg.name().equals(groupName)) || cls1.getSimpleName().equals(groupName)) {
                    Field[] fields = cls1.getFields();
                    for (Field field : fields) {
                        Object v = null;
                        try {
                            v = field.get(dicClass);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        if (v != null && v instanceof DicEntry) {


                            String desc = ((DicEntry) v).desc;
                            String color = ((DicEntry) v).color;
                            String code = ((DicEntry) v).code;
                            int idx = ((DicEntry) v).value;
                            map.put(idx, new String[]{desc, color, code});

                        }

                    }
                }
            }
        }

        return map;
    }

    public static HashMap getDicEntrys(Class dicClass, String groupName) {
        if (dicClass.equals(DicGroupMapping.class))
             return getXMLDicEntrys(groupName);

        if (dicClass.equals(D_Dictionary.class) && groupName != null && groupName.trim().length() > 0) {
            List<D_Dictionary> dbDictionaries = getDBDictionaries(groupName);
            HashMap map = new HashMap();
            for (D_Dictionary dbDictionary : dbDictionaries) {
                map.put(dbDictionary.value, new String[]{dbDictionary.getDesc(), null, dbDictionary.code});
            }
            return map;
        }
        if (groupName != null && groupName.trim().length() > 0) {
            return getDicEntrysOld(dicClass, groupName);
        }

        else  {

            HashMap<Integer, String[]> map = new HashMap<Integer, String[]>();
            if (dicClass != null) {


                    Field[] fields = dicClass.getFields();
                    int i = 0;
                    for (Field field : fields) {
                        Object v = null;
                        try {
                            v = field.get(dicClass);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        DicItem dicItem = field.getAnnotation(DicItem.class);
                        if (dicItem != null)
                        {


                            String desc = (dicItem).desc();
                            String color = (dicItem).color();
                            String code = (dicItem).code();
                            int idx = 0;
                            try {
                                idx = Integer.parseInt(String.valueOf( v));
                                map.put(idx, new String[]{desc, color, code});
                            } catch (NumberFormatException e) {
                                map.put(0-(++i), new String[]{desc, color, String.valueOf( v)});
                            }


                        }

                    }


            }

            return map;
        }
    }



    public static String getDicCode(Class dicGroupClass, int value) {
        D_Dictionary entry = getDicEntry(dicGroupClass,value);
        return entry == null ? null : entry.code ;
    }


    public static D_Dictionary getDicEntry(Class dicGroupCls, int value) {
        Field[] fields = dicGroupCls.getFields();
        for (Field field : fields) {
            Object v = null;
            try {
                v = field.get(dicGroupCls);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            DicItem dicItem = field.getAnnotation(DicItem.class);
            if (value == (dicItem.value())) {
                return new D_Dictionary(dicItem.desc(), Integer.parseInt(String.valueOf(v)),dicItem.code(),dicItem.color(),dicGroupCls.getName());
            }

        }
        return null;
    }

    /**
     *
     * @param dicClass
     * @param
     * @param value
     * @return
     */
    public static String getDicDesc(Class dicClass , int value) {
        D_Dictionary entry = getDicEntry(dicClass,value);
        return entry != null ? entry.desc  : null;
    }

//    /**
//     *
//     * @param cls
//     * @param fieldName      The field must be specified by the annotation @DicGroupMapping, if not will return null.
//     * @param value
//     * @return
//     * @throws NoSuchFieldException
//     */
//    public static String getFieldDesc(Class cls,String fieldName,int value) throws NoSuchFieldException {
//
//        Field field = cls.getDeclaredField(fieldName);
//        if (field != null) {
//            DicGroupMapping dicGroupMapping = field.getAnnotation(DicGroupMapping.class);
//            if (dicGroupMapping != null) {
//                Class dicClass = dicGroupMapping.definitionClass();
//                String groupName = dicGroupMapping.groupName();
//                return getDicDesc(dicClass,groupName,value);
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * 根据描述获取值
//     * @throws NoSuchFieldException
//     * @throws SecurityException
//     * */
//    public static int getFieldValue(Class cls,String fieldName,String desc) throws SecurityException, NoSuchFieldException{
//    	Field field = cls.getDeclaredField(fieldName);
//    	if(field !=null){
//    		DicGroupMapping dicGroupMapping = field.getAnnotation(DicGroupMapping.class);
//    		if (dicGroupMapping != null) {
//                Class dicClass = dicGroupMapping.definitionClass();
//                String groupName = dicGroupMapping.groupName();
//                return getDicValue(dicClass,groupName,desc);
//            }else{
//            	return Integer.MIN_VALUE;
//            }
//    	}
//    	return Integer.MAX_VALUE;
//    }

    /**
	 * @param dicClass
	 * @param
	 * @param desc
	 * @return
	 */
	public static int getDicValue(Class dicClass, String desc) {
        D_Dictionary entry = getDicEntry(dicClass,desc);
        return entry != null ? entry.value  : Integer.MAX_VALUE;
	}
	

	/**
	 * @param
	 * @param desc
	 * @return
	 */
	private static D_Dictionary getDicEntry(Class<?> dicGroupCls, String desc) {


		Field[] fields = dicGroupCls.getFields();
        for (Field field : fields) {
            Object v = null;
            try {
                v = field.get(dicGroupCls);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            DicItem dicItem = field.getAnnotation(DicItem.class);
            if (dicItem != null && desc.equalsIgnoreCase(dicItem.desc())) {
                return new D_Dictionary(dicItem.desc(), Integer.parseInt(String.valueOf(v)),dicItem.code(),dicItem.color(),dicGroupCls.getName());
            }

        }
        return null;
	}

    private static HashMap<Class,HashMap<Object,String>> codeMap = new HashMap();
    public static String getCode(Class cls, Object value) {

        HashMap<Object, String> map = codeMap.get(cls);
        if (map == null) {
            synchronized (codeMap) {
                map = codeMap.get(cls);
                if (map == null) {
                    map = new HashMap();
                    Field[] fields = cls.getDeclaredFields();

                    for (Field field : fields) {
                        DicItem annotation = field.getAnnotation(DicItem.class);
                        if (annotation != null) {
                            String code = annotation.code();
                            try {
                                map.put(field.get(cls),code);
                            } catch (IllegalAccessException e) {
                                //logger.error(e, e);
                            }
                        }
                    }
                    codeMap.put(cls,map);
                }
            }
        }
        return map.get(value);
    }

	public static void main(String[] args) {
        JSONObject jsonObject = JSONObject.fromObject(" {\"ronnnie\":1,\"aaa\":2}");

     //   DictionaryUtil.getDicValue(SmasDictionary.Boolean.class,"是");
    }

}
