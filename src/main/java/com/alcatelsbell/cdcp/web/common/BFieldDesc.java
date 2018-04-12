    package com.alcatelsbell.cdcp.web.common;

import com.alcatelsbell.nms.common.Dic;
import com.alcatelsbell.nms.common.XMLLoadDictionary;
import com.alcatelsbell.nms.common.annotation.DicGroupMapping;
import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.alarm.VendorAlarmLib;

import java.lang.reflect.Field;
import java.util.*;

    /**
     * User: Ronnie
     * Date: 12-2-13
     * Time: 下午3:44
     */
    public class BFieldDesc {
        public static final String FIELD_TYPE_INT = "INT";
        public static final String FIELD_TYPE_LONG = "LONG";
        public static final String FIELD_TYPE_FLOAT = "FLOAT";
        public static final String FIELD_TYPE_STRING = "STRING";
        public static final String FIELD_TYPE_DATE = "DATE";

        private String fieldType = null;

        private String viewType;
        private String createType;
        private String editType;
        private String searchType;
        private String mergeType;

        private String description;
        private List<HashMap> values;

        private String dnField;
        private String dnReferenceEntityName;
        private String dnReferenceEntityField;
        private String dnReferenceTransietField;
        private int sequence;

        private String fieldName;

        public BFieldDesc(Field field) {
            parse(field);
        }

        public BFieldDesc() {

        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        private void parse(Field field) {
            BField bField = field.getAnnotation(BField.class);

            this.fieldName = field.getName();
            viewType = bField.viewType().toString();
            createType = bField.createType().toString();
            editType = bField.editType().toString();
            searchType = bField.searchType().toString();
            mergeType = bField.mergeType().toString();
            sequence = bField.sequence();
            description = bField.description();

            if (bField.values() != null && bField.values().length > 0) {
                values = new ArrayList<HashMap>();
                for (int i = 0; i < bField.values().length; i++) {
                    HashMap pair = new HashMap();
                    String value = bField.values()[i];
                    pair.put("key",i);
                    pair.put("label",value);
                    values.add(pair);
                }
            }

            DicGroupMapping dicGroupMapping = field.getAnnotation(DicGroupMapping.class);


            // dictionary.xml
            if (dicGroupMapping != null  &&  DicGroupMapping.class.equals(dicGroupMapping.definitionClass()) ) {
                Dic dic = XMLLoadDictionary.getInstance().getDic(dicGroupMapping.groupName());
                if (dic != null && dic.getDesc() != null && !dic.getDesc().isEmpty())
                    description = dic.getDesc();
            }

                if (dicGroupMapping != null) {
                Class dicClass = dicGroupMapping.definitionClass();
                String groupName = dicGroupMapping.groupName();
                HashMap<Integer,String[]> map = DictionaryUtil.getDicEntrys(dicClass, groupName);
                values = new ArrayList<HashMap>();
                Iterator<Integer> keys = map.keySet().iterator();
                while (keys.hasNext()) {
                    Integer key = keys.next();
                    HashMap pair = new HashMap();
                    pair.put("key",key);
                    pair.put("label",map.get(key)[0]);
                    pair.put("color", map.get(key)[1]);

                    // 对于那些存的字典值是字符串的情况，index在定义时使用小于0的数值。
                    if (key < 0)
                        pair.put("code", map.get(key)[2]);


                    values.add(pair);
                }
    //
    //            int max = 0;
    //
    //
    //            Iterator<Integer> it = map.keySet().iterator();
    //            while (it.hasNext()) {
    //                int key = it.next();
    //                max = key > max ? key : max;
    //            }
    //
    //            String[] vs = new String[max+1];
    //            for (int i = 0; i <= max ; i++) {
    //                String desc = map.get(i);
    //                vs[i] = (desc == null ? "": desc);
    //            }
    //            values = Arrays.asList(vs);

            }


            dnField=bField.dnField();
            dnReferenceEntityField = bField.dnReferenceEntityField();
            dnReferenceEntityName = bField.dnReferenceEntityName();
            dnReferenceTransietField = bField.dnReferenceTransietField();

            Class cls = field.getType();
            fieldType = FIELD_TYPE_STRING;
            if (cls.equals(Integer.class) || cls.equals(Integer.TYPE)) {
                fieldType = FIELD_TYPE_INT ;
            }
            if (cls.equals(Long.class) || cls.equals(Long.TYPE)) {
                fieldType = FIELD_TYPE_LONG;
            }
            if (cls.equals(Float.class) || cls.equals(Float.TYPE) || cls.equals(Double.class) || cls.equals(Double.TYPE) ) {
                fieldType = FIELD_TYPE_FLOAT ;
            }
            if (cls.equals(Date.class) ) {
                fieldType = FIELD_TYPE_DATE;
            }
        }

        public String getViewType() {
            return viewType;
        }

        public void setViewType(String viewType) {
            this.viewType = viewType;
        }

        public String getCreateType() {
            return createType;
        }

        public void setCreateType(String createType) {
            this.createType = createType;
        }

        public String getEditType() {
            return editType;
        }

        public void setEditType(String editType) {
            this.editType = editType;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }

        public String getSearchType() {
            return searchType;
        }

        public void setSearchType(String searchType) {
            this.searchType = searchType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<HashMap> getValues() {
            return values;
        }

        public void setValues(List<HashMap> values) {
            this.values = values;
        }

        public String getDnReferenceEntityName() {
            return dnReferenceEntityName;
        }

        public void setDnReferenceEntityName(String dnReferenceEntityName) {
            this.dnReferenceEntityName = dnReferenceEntityName;
        }

        public String getDnReferenceEntityField() {
            return dnReferenceEntityField;
        }

        public void setDnReferenceEntityField(String dnReferenceEntityField) {
            this.dnReferenceEntityField = dnReferenceEntityField;
        }

        public String getDnReferenceTransietField() {
            return dnReferenceTransietField;
        }

        public void setDnReferenceTransietField(String dnReferenceTransietField) {
            this.dnReferenceTransietField = dnReferenceTransietField;
        }

        public static void main(String[] args) throws NoSuchFieldException {
            new BFieldDesc(VendorAlarmLib.class.getDeclaredField("severity"));
        }

        public String getMergeType() {
            return mergeType;
        }

        public void setMergeType(String mergeType) {
            this.mergeType = mergeType;
        }

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public String getDnField() {
            return dnField;
        }

        public void setDnField(String dnField) {
            this.dnField = dnField;
        }

        public String getDicValue(int value) {
            List<HashMap> values = this.getValues();
            for (HashMap v : values) {
                if ((v.get("key")+"").equals(value+""))
                    return (String)v.get("label");
            }
            return null;
        }
    }
