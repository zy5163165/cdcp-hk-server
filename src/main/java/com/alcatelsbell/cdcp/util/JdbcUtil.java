package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2014/12/12
 * Time: 21:16
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class JdbcUtil {
	public static List queryObjects(Connection  connection,Class cls,String sql)  {
		return null;
	}


	public static Object queryObjectById(Connection  connection,Class cls,String tableName,long id) throws Exception {

		Field[] fields = cls.getDeclaredFields();

		StringBuffer sqlb = new StringBuffer();
		sqlb.append("select ");
		for (Field field : fields) {
			sqlb.append(field.getName()+",");

		}
		String sql = sqlb.toString();
		sql = sql.substring(0,sql.length()-1);
		sql += " from "+tableName+" where id = "+id;
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		Object obj = cls.newInstance();
		while (resultSet.next()) {
			int idx = 1;
			for (Field field : fields) {
				Object value = resultSet.getObject(idx++);
				if (value != null && value instanceof BigDecimal) {
					if (field.getType().equals(Long.class) || field.getType().equals(long.class))
						value = ((BigDecimal) value).longValue();
				}
				field.set(obj,value);

			}

		}

		return obj;


	}

	public static long queryId(Connection  connection, String sql) throws SQLException {

		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		try {
			while (resultSet.next()) {
				long id = resultSet.getLong(1);
				return id;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}  finally {

			resultSet.close();
			statement.close();
		}
		return -1;

	}

	public static void executeUpdate(Connection connection,String sql) throws SQLException {
		if (!toDB) {
			System.out.println("executeUpdate "+sql);
			return  ;
		}
		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
		statement.close();

	}
	public static void execute(Connection connection,String sql) throws SQLException {
		if (!toDB) {
			System.out.println("execute "+sql);
			return  ;
		}
		Statement statement = connection.createStatement();
		statement.execute(sql);
		statement.close();

	}

	public static boolean toDB =  true;
	public static Object insertObject(Connection connection,Object obj,String tableName) throws SQLException, IllegalAccessException {
		if (!toDB) {
			System.out.println("insert "+obj);
			return obj;
		}
		Class cls = obj.getClass();
		Field[] fields = cls.getDeclaredFields();

		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (Field field : fields) {
			sb.append(field.getName()+",");
			sb2.append("?,");
		}
		String fs = sb.toString();
		fs = fs.substring(0,fs.length()-1);

		String qs = sb2.toString();
		qs = qs.substring(0,qs.length()-1);

		PreparedStatement prepareStatement =
				connection.prepareStatement("insert into "+(tableName == null ? obj.getClass().getSimpleName() : tableName)
						+"("+fs+") values ("+qs+")");
		 log(prepareStatement);
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Class<?> type = field.getType();

			if (type.equals(Long.class) || type.equals(long.class)) {
				prepareStatement.setLong(i+1, field.get(obj) == null ? 0: (Long) field.get(obj));
			}
			if (type.equals(Integer.class) || type.equals(int.class)) {
				prepareStatement.setInt(i + 1, field.get(obj) == null ? 0: (Integer)field.get(obj));
			}
			if (type.equals(String.class)) {
				prepareStatement.setString(i+1, (String) field.get(obj));
			}

			if (type.equals(Date.class)) {
				Date date = (Date) field.get(obj);
				prepareStatement.setTimestamp(i+1, date == null ? null : new java.sql.Timestamp(date.getTime()));
			}

		}
		prepareStatement.execute();
		prepareStatement.close();
		return obj;

	}


	public static Object updateObjectById(Connection connection,Object obj,String tableName) throws SQLException, IllegalAccessException {
		Class cls = obj.getClass();
		Field[] fields = cls.getDeclaredFields();
		long id = -1;
		try {
			id = (Long)obj.getClass().getDeclaredField("id").get(obj);
		} catch (NoSuchFieldException e) {
			throw new SQLException("obj : "+obj+" has no id property");
		}
		StringBuffer sb = new StringBuffer();

		for (Field field : fields) {
			sb.append(field.getName()+" = ");
			sb.append("?,");
		}
		String fs = sb.toString();
		fs = fs.substring(0,fs.length()-1);



		PreparedStatement prepareStatement =
				connection.prepareStatement("update "+(tableName == null ? obj.getClass().getSimpleName() : tableName)
						+" set "+fs+" where id = "+id);
		// log(prepareStatement);
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Class<?> type = field.getType();

			if (type.equals(Long.class) || type.equals(long.class)) {
				prepareStatement.setLong(i+1, (Long) field.get(obj));
			}
			if (type.equals(Integer.class) || type.equals(int.class)) {
				prepareStatement.setInt(i+1, (Integer) field.get(obj));
			}
			if (type.equals(String.class)) {
				prepareStatement.setString(i+1, (String) field.get(obj));
			}

			if (type.equals(Date.class)) {
				Date date = (Date) field.get(obj);
				prepareStatement.setDate(i+1, date == null ? null : new java.sql.Date(date.getTime()));
			}

		}
		prepareStatement.executeUpdate();
		prepareStatement.close();
		return obj;

	}


	private static void log(PreparedStatement prepareStatement) {
		System.out.println(prepareStatement);
	}

	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Date parse = sdf.parse("2014/9/25");
		int i = 'A';
		System.out.println("i = " + i);
	}
}
