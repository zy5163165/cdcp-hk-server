package com.alcatelsbell.cdcp.nbi.ws;

import java.io.Serializable;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-8
 * Time: 下午2:31
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class StrResponse implements Serializable {
	private String resultCode;
	private String resultMsg = "";
	private List<List<FieldInfo>> recordInfo;

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public List<List<FieldInfo>> getRecordInfo() {
		return recordInfo;
	}

	public void setRecordInfo(List<List<FieldInfo>> recordInfo) {
		this.recordInfo = recordInfo;
	}

	@Override
	public String toString() {
		return "StrResponse{" + "resultCode='" + resultCode + '\'' + ", resultMsg='" + resultMsg + '\'' + ", recordInfo=" + recordInfo + '}';
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<strResponse>\n" + "    <resultCode>"
				+ resultCode + "</resultCode>\n" + "    <resultMsg>" + resultMsg + "</resultMsg>\n");
		if (recordInfo != null) {

			for (List<FieldInfo> record : recordInfo) {
				sb.append("<recordInfo>\n");
				if (record != null) {
					for (FieldInfo fieldInfo : record) {
						sb.append(fieldInfo.toXML());
					}
				}

				sb.append("</recordInfo>\n");
			}

		}

		sb.append("</strResponse>");
		return sb.toString();
	}
}
