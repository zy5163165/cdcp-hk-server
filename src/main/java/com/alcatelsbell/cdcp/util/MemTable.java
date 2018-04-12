package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-25
 * Time: 下午8:51
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MemTable  implements Serializable{
    private Log logger = LogFactory.getLog(getClass());
    protected String[] columns = null;
    protected List<Object[]> data = new ArrayList<Object[]>();
    private ConcurrentHashMap<String,ConcurrentHashMap<Object,Object>> indexs = new ConcurrentHashMap<String, ConcurrentHashMap<Object, Object>>();

    public MemTable(String... columns){
        this.columns = columns;
    }
    public MemTable(){

    }

    public void addIndex(String column) {
        indexs.put(column,new ConcurrentHashMap<Object, Object>());
    }

    public MemRow findRowByIndex(String column,String value) {
        List<MemRow> rowsByIndex = findRowsByIndex(column, value);
        if (!rowsByIndex.isEmpty())
            return rowsByIndex.get(0);
        return null;
    }

    public List<MemRow> findRowsByIndex(String column,String value) {
        if (value == null) value = INDEX_NULL_VALUE;
        Object  objects =   indexs.get(column).get(value);
        List list = new ArrayList();
        if (objects != null) {
        if (objects instanceof List) {
            for (Object o : (List)objects) {
                list.add( (new MemRow(columns, (Object[]) o)));
            }
        } else {
            list.add( (new MemRow(columns, (Object[]) objects)));
        }
        }

        return list;
    }
    public final static String INDEX_NULL_VALUE = "INDEX_NULL_VALUE";
    public void add(Object... row) throws Exception {
        if (row.length != columns.length) throw new Exception("row length = "+row.length+" but column length = "+columns.length);

        for (int i = 0; i < row.length; i++) {
            Object columnValue = row[i];
            String column = columns[i];
            if (indexs.get(column) != null) {
                ConcurrentHashMap<Object, Object> indexValues = indexs.get(column);
                if (columnValue == null)
                    columnValue = INDEX_NULL_VALUE;
                if (indexValues.get(columnValue) != null) {
                    Object set = indexValues.get(columnValue);
                    if (set instanceof List) {
                        ((List) set).add(row);
                    }  else {

                        ArrayList arrayList = new ArrayList();
                        indexValues.put(columnValue, arrayList);
                        arrayList.add(set);
                        arrayList.add(row);
                    }

                } else {
                    indexValues.put(columnValue,row);
                }
            }

        }

        data.add(row);
    }

    public void addAll(List<Object[]> rows) {
        data.addAll(rows);
    }

    public static void main(String[] args) throws Exception {
        MemTable table = new MemTable("id","name","sex");
        table.add(1,"ronnie","female");
        table.add(2,"ronnie2","male");

        table.add(3,"cathy3","male");
        table.add(4,"cathy4","female");

        List<MemRow> l1 = table.findRow(new Condition("id", "=", 1));
        System.out.println("l1 = " + l1);
        List<MemRow> l2 = table.findRow(new Condition("name", "like", "ronnie"));
        System.out.println("l2 = " + l2);
        List<MemRow> l3 = table.findRow(new Condition(
                Condition.AND,new Condition("name", "like", "ronnie"),new Condition("sex","=","male"))
        );

        System.out.println("l3 = " + l3);
        List<MemRow> l4 = table.findRow(new Condition("id", ">=", 2));
        System.out.println("l4 = " + l4);
    }

    public List<MemRow> findRow(Condition condition) throws Exception {
        long t1 = System.currentTimeMillis();
        List result = new ArrayList();
        for (Object[] objects : data) {
            MemRow row = new MemRow(columns,objects);
            if (condition.judge(row)) {
                result.add(row);
            }
        }
        long t = System.currentTimeMillis() - t1;
        if (t > 500) {
            logger.error("spend too much time: condition = "+condition.column+condition.operator+ condition.value);
        }
        return result;
    }





    public static class  MemRow {
        MemRow(String[] columns, Object[] rowData) {
            this.columns = columns;
            this.rowData = rowData;
        }

        String[] columns;
        Object[] rowData;

        public  Object getColumnData(String column) throws Exception {
            int i = indexOf(column);
            if (i >= 0) return rowData[i];
            throw new Exception("Column:"+column +" not exsited");
        }

        public int indexOf(String column) {
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];
                if (col.equals(column))
                    return i;
            }
            return -1;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("[");
            for (int i = 0; i < columns.length; i++) {
                sb.append(columns[i]).append(" : ").append(rowData[i]);
                if (i < columns.length -1)
                    sb.append(" ; ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public static class Condition {
        public static final int  AND = 0;
        public static final int  OR = 1;

        Condition[] subConditions;
        int subConditionsRelation;


        String column;
        Object value;

        // =, !=, like , >, < ,>=, <=
        String operator = "=";

        public Condition(String column, String operator, Object value) {
            this.column = column;
            this.value = value;
            this.operator = operator;
        }

        public Condition(int subConditionsRelation, Condition... subConditions) {
            this.subConditionsRelation = subConditionsRelation;
            this.subConditions = subConditions;
        }

        public Condition or (Condition cond2) {
            return new Condition(OR,this,cond2);
        }

        public Condition and (Condition cond2) {
            return new Condition(AND,this,cond2);
        }


        public boolean judge(MemRow row) throws Exception {
            if (subConditions != null && subConditions.length  > 0) {
                for (Condition subCondition : subConditions) {
                    boolean judge = subCondition.judge(row);
                    if (judge && subConditionsRelation == OR) {
                            return true;
                    } else if (!judge && subConditionsRelation == AND)
                        return false;
                }

                return subConditionsRelation == AND;
            }

            else if (column != null) {
                Object columnData = row.getColumnData(column);
                if (operator.equals("=")) {
                    if (columnData == null ) {
                        if (value == null) return true;
                        return false;
                    } else {
                        return (columnData.equals(value));
                    }

                } else if (operator.equals("!=")) {
                    if (columnData == null ) {
                        if (value == null) return false;
                        return true;
                    } else {
                        return (!columnData.equals(value));
                    }
                }   else if (operator.equals("like")) {
                    if (columnData == null ) {
                        return false;
                    } else {
                        return (columnData.toString().contains(value.toString()));
                    }
                } else if (operator.equals(">") || operator.equals("<")  || operator.equals("<=")  || operator.equals(">=")  ) {
                    if (columnData == null || value == null) return false;
                    if (!(columnData instanceof Number) || !(value instanceof Number))
                        throw new Exception(" column : "+column +" not Number,can not use operator "+operator);

                    if (operator.equals(">"))
                        return ((Number)columnData).longValue() > ((Number)value).longValue();
                    else if (operator.equals("<"))
                        return ((Number)columnData).longValue() < ((Number)value).longValue();
                    else if (operator.equals(">="))
                        return ((Number)columnData).longValue() >= ((Number)value).longValue();
                    else if (operator.equals("<="))
                        return ((Number)columnData).longValue() <= ((Number)value).longValue();
                    else {
                        throw new Exception("should not be here");
                    }
                }

                else {
                    throw new Exception("unknown operator : "+operator);
                }




            } else {
                return true;
            }


        }
    }

    public void removeAll() {
        data.clear();
    }


}
