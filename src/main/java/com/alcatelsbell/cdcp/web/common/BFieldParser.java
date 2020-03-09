package com.alcatelsbell.cdcp.web.common;


import com.alcatelsbell.cdcp.util.ReflectionUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.valueobject.BObject;


import java.lang.reflect.Field;
import java.util.HashMap;

//import org.junit.Test;

/**
 * @author Aaron
 * Date 2012-06-26
 * Description:This class is used for parsing BField of the Object
 * */
public class BFieldParser {

	private static BObjectHelper bObjectHelper = new BObjectHelper();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void parseBObject(BObject obj) throws ClassNotFoundException, Exception {

		HashMap<String, BFieldDesc> map = bObjectHelper.getBObjectFieldDescs(obj.getClass());
		if (map != null && map.size() > 0) {
			for (String key : map.keySet()) {
				parseBField(obj, ReflectionUtil.getField(obj.getClass(),key), map.get(key));
			}
			//return;
		}


		// to be igroned
//
//		try {
//		Class cls = obj.getClass();
//		Field[] all_fields=cls.getDeclaredFields();
//		Map field_bFieldDesc_Map = new HashMap();
//		for(Field field : all_fields){
//			if(field.getAnnotation(BField.class)==null)
//				continue;
//			field_bFieldDesc_Map.put(field,new BFieldDesc(field));
//		}
//		Set<Map.Entry<Field, BFieldDesc>> fieldEntrySet=field_bFieldDesc_Map.entrySet();
//		for(Map.Entry<Field, BFieldDesc> entry : fieldEntrySet){
//			Field field = entry.getKey();
//			BFieldDesc bfdesc = entry.getValue();
//			parseBField(obj,field,bfdesc);
//		}
//		} catch (Exception e) {
//    		e.printStackTrace();
//            throw e;
//    	}
	}

	@SuppressWarnings("rawtypes")
	private static void parseBField(BObject obj, Field field, BFieldDesc bfdesc) throws ClassNotFoundException, Exception {
		boolean oldAccessible= field.isAccessible();
		field.setAccessible(true);
		Object dn= field.get(obj);
		field.setAccessible(oldAccessible);
		
		String dnReferenceEntityName = bfdesc.getDnReferenceEntityName();
		String dnReferenceEntityField = bfdesc.getDnReferenceEntityField();
		String dnReferenceTransietField = bfdesc.getDnReferenceTransietField();
		if (bfdesc.getDnField() == null) bfdesc.setDnField("dn");
		
		if(Utils.notEmpty(dnReferenceEntityName)&&dn!=null){
            String sqlstr="select c from "+dnReferenceEntityName+" as c where c."+bfdesc.getDnField()+" = '"+((dn instanceof Long)? Long.toString((Long)dn):dn.toString())+"'";
			BObject refEntity = (BObject) JpaClient.getInstance().findOneObject(sqlstr);
//			BObject refEntity = (BObject)JpaClient.getInstance().findObjectByDN(Class.forName(dnReferenceEntityName), (String)dn);
			if(refEntity!=null){
				if(Utils.notEmpty(dnReferenceEntityField)){
					Field refField=null;
					Object refField_Value=null;
					Class temp_target_obj=refEntity.getClass();
					
					while(true){
						try {
							refField = temp_target_obj.getDeclaredField(dnReferenceEntityField);
						} catch (SecurityException e) {
								throw e;
						} catch (NoSuchFieldException e) {
								temp_target_obj=temp_target_obj.getSuperclass();
								if(temp_target_obj == Object.class)
									break;
								continue;
						}
						break;
					}	
					
					if(refField!=null){
						oldAccessible= refField.isAccessible();
						refField.setAccessible(true);
						refField_Value= refField.get(refEntity);
						refField.setAccessible(oldAccessible);
					}
									
					if(Utils.notEmpty(dnReferenceTransietField)||refField_Value!=null){
						Field transientField = ReflectionUtil.getField(obj.getClass(),dnReferenceTransietField);
						oldAccessible= transientField.isAccessible();
						transientField.setAccessible(true);
						transientField.set(obj, refField_Value);
						refField.setAccessible(oldAccessible);
					}	
				}
			}
		}
	}



}
