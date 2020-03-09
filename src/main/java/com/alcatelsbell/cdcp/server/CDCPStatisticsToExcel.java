package com.alcatelsbell.cdcp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.Region;
import org.asb.mule.probe.framework.entity.EDS_PTNVO;

import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.valueobject.domain.Report;

public class CDCPStatisticsToExcel {
	private List<EDS_PTNVO> edsHW = new ArrayList<EDS_PTNVO>();
	private List<EDS_PTNVO> edsFH = new ArrayList<EDS_PTNVO>();
	private List<EDS_PTNVO> edsALU = new ArrayList<EDS_PTNVO>();
	private List<EDS_PTNVO> edsZTE = new ArrayList<EDS_PTNVO>();
	List<EDS_PTNVO> collectionsHW = new ArrayList<EDS_PTNVO>();
	List<EDS_PTNVO> collectionsFH = new ArrayList<EDS_PTNVO>();
	List<EDS_PTNVO> collectionsZTE = new ArrayList<EDS_PTNVO>();
	List<EDS_PTNVO> collectionsALU = new ArrayList<EDS_PTNVO>();

	private int sdhCount;
	private int otnCount;

	String[] hwSDHType = { "HZ-T2000-1-P", "HZ-T2000-2-P", "NBO-T2000-10-P",
			"WZH-T2000-8-P", "TZH-T2000-1-P", "TZH-T2000-2-HT",
			"QUZ-T2000-3-P", "LSH-T2000-5-P", "LSH-T2000-HT", "ZSH-T2000-5-P",
			"JIH-T2000-1-P", "ZJ-T2000-2-P", "JX-T2000-HT", "HUZ-T2000-HT" };
	String[] hwOTN = { "HZ-U2000-3-P", "WZH-T2000-6-P", "NBO-T2000-7-P",
			"TZH-T2000-3-P", "QUZ-U2000-1-OTN", "LSH-T2000-3-P",
			"ZSH-U2000-1-P", "ZJ-U2000-1-OTN", "JIH-U2000-1-OTN",
			"HUZ-U2000-1-OTN" };

	String[] fhSDH = { "SHX-OTNM2000-8-P", "JIH-OTNM2000-6-P",
			"JXI-OTNM2000-1-P", "HUZ-OTNM2000-7-P" };
	String[] fhOTN = { "ZJ-OTNM2000-1-P", "SHX-OTNM2000-1-OTN",
			"HUZ-OTNM2000-1-OTN", "HZ-OTNM2000-1-F", "NB-OTNM2000-1-OTN",
			"JH-OTNM2000-2-OTN", "JXI-OTNM2000-1-OTN" };

	String[] zx_alu_OTN = { "ZJ-ALU-1-OTN" };

	String[] zte_OTN = { "TZ-OTNU31-1-P", "WZ-OTNU31-1-P", "ZJ-ZTE-1-P" };

	/**
	 * Create EMS Statictics report
	 * 
	 * @throws RemoteException
	 */
	public void workExcel() throws RemoteException {
		System.out
				.println("Spring timer starts, obtain EDS_PTN data. Create Excel statistical form........");
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyle style = wb.createCellStyle(); // 样式对象
		style.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框

		HSSFCellStyle style2 = wb.createCellStyle(); // 样式对象
		style2.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
		style2.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
		style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平
		style2.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style2.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		style2.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框

		HSSFCellStyle style3 = wb.createCellStyle(); // 样式对象
		style3.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style3.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style3.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平
		style3.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
		style3.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		style3.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style3.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		style3.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		String sql1 = "select * from eds_ptn e where e.emsname is not null and e.createdate > (sysdate-30) order by e.createdate desc";
		String sql2 = "select e.emsname , count(id) from eds_ptn e where e.emsname is not null and e.createdate > (sysdate-30) group by e.emsname";
		List list = queryEDSPTNData(sql1);
		List emsNames = queryEDSPTNData(sql2); 
		System.out.println("LIST_SIZE" + list.size()); 
		List<EDS_PTNVO> edsPTNs = parseDataTypeForList(list);//解析出查询出来的所有数据。
		for (int i = 0; i < emsNames.size(); i++) {
			Object obj = emsNames.get(i);
			Object[] objects = (Object[]) obj;
			if (null != objects[0] && !objects[0].toString().equals("")) {
				String emsName = objects[0].toString();
				int rowCount = new Integer(objects[1].toString());
				EDS_PTNVO eds = new EDS_PTNVO();
				eds.setEmsname(emsName);
				eds.setGroupCount(rowCount);
				if (emsName.toUpperCase().contains("T2000")
						|| emsName.toUpperCase().contains("U2000")) {
					edsHW.add(eds);
				} else if (emsName.toUpperCase().contains("OTNM")) {
					edsFH.add(eds);
				} else if (emsName.toUpperCase().contains("ALU")) {
					edsALU.add(eds);
				} else if (emsName.toUpperCase().contains("OTNU")
						|| emsName.toUpperCase().contains("ZTE")) {
					edsZTE.add(eds);
				}
			}
		}
		collectionsHW = parseManufacturerEMS(edsHW, edsPTNs);
		collectionsFH = parseManufacturerEMS(edsFH, edsPTNs);
		collectionsALU = parseManufacturerEMS(edsALU, edsPTNs);
		collectionsZTE = parseManufacturerEMS(edsZTE, edsPTNs);
		HSSFSheet sheetHW = wb.createSheet("华为");
		createSheet(matchEMSType(collectionsHW), edsHW, sheetHW, style, style2,
				style3);
		HSSFSheet sheetFH = wb.createSheet("烽火");
		createSheet(matchEMSType(collectionsFH), edsFH, sheetFH, style, style2,
				style3);
		HSSFSheet sheetALU = wb.createSheet("阿朗");
		createSheet(matchEMSType(collectionsALU), edsALU, sheetALU, style,
				style2, style3);
		HSSFSheet sheetZTE = wb.createSheet("中兴");
		createSheet(matchEMSType(collectionsZTE), edsZTE, sheetZTE, style,
				style2, style3);
		Report rep = new Report();
		Date date = new Date();
		rep.setCreateDate(date);
		String format = new SimpleDateFormat("yyyyMMdd").format(rep
				.getCreateDate());
		rep.setStarttime(date);
		rep.setEndtime(date);
		rep.setReportName("EMS-STATISTICS-REPORT" + format);
		rep.setCreateTime(date);
		rep.setProductType("采集统计报表");
		String property = System.getProperty("user.dir");
		String replace = property.replace("bin", "webapps");
		String saveUrl = replace + "\\CDCP_WEB\\excel\\";
		String fileName = "EMS_Data_Documents_" + format + ".xls";
		FileOutputStream o;
		try {
			o = new FileOutputStream(new File(saveUrl + fileName));
			wb.write(o);
			o.close();
			System.out
					.println("Spring timer task obtain EDS_PTN data , has been completed.");
			System.out.println("newly increased record to table report");
			insertReportRecord(rep);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			edsHW.clear();
			edsFH.clear();
			edsALU.clear();
			edsZTE.clear();
			collectionsHW.clear();
			collectionsALU.clear();
			collectionsFH.clear();
			collectionsZTE.clear();
		}
	}

	/**
	 * 解析各厂商的EMS，根据EMS名称
	 * 
	 * @param emsParams
	 *            EMS名称
	 * @return listCollection
	 */
	public List<EDS_PTNVO> parseManufacturerEMS(List<EDS_PTNVO> emsParams,
			List<EDS_PTNVO> edsPTNs) {
		List<EDS_PTNVO> listCollection = new ArrayList<EDS_PTNVO>();
		int s = 0;
		for (int i = 0; i < emsParams.size(); i++) {
			EDS_PTNVO vo = new EDS_PTNVO();
			String emsName = emsParams.get(i).getEmsname();
			for (int j = 0; j < edsPTNs.size(); j++) {
				if (emsName.equalsIgnoreCase(edsPTNs.get(j).getEmsname())) {
					s++;
					vo.setEmsname(emsName);
					vo.setGroupCount(s);
					vo.getEdsPTNVOs().add(edsPTNs.get(j));
					edsPTNs.remove(j);
					j--;
				}
			}
			s = 0; 
			listCollection.add(vo);
		}

		return listCollection;
	}

	/**
	 * Create Excel Sheets
	 * 
	 * @param collections
	 *            Excel show data
	 * @param sheet
	 *            sheets
	 * @param style
	 * @param style2
	 * @param style3
	 * @throws Exception
	 */
	public void createSheet(List<EDS_PTNVO> collections,
			List<EDS_PTNVO> rowParam, HSSFSheet sheet, HSSFCellStyle style,
			HSSFCellStyle style2, HSSFCellStyle style3) {
		HSSFRow row = sheet.createRow((short) 0);
		HSSFRow headerTopic = createHeaderTopic(row, style3);

		int exlRow = 1; // Exl Current rows
		// colSpan:
		int startRow = 1;
		// closed 表示一个状态，当j(行)对象的子集循环结束之后 close = true;
		boolean column1 = true;
		boolean column2 = true;
		for (int i = 0; i < collections.size(); i++) { // SDH OTN group
			int row2 = collections.get(i).getEdsPTNVOs().size();
			for (int j = 0; j < row2; j++) { // EMS object group
				List<EDS_PTNVO> rowObj = collections.get(i).getEdsPTNVOs().get(
						j).getEdsPTNVOs();
				for (int j2 = 0; j2 < rowObj.size(); j2++) {
					row = sheet.createRow(exlRow);
					startRow = exlRow;
					HSSFCell cell = null;
					for (int c = 0; c < 13; c++) {
						cell = row.createCell(c);
						cell.getSheet().setColumnWidth(c, 3000);
						// 分组背景颜色
						if (i % 2 == 0) {
							cell.setCellStyle(style2); // 样式，
						} else {
							cell.setCellStyle(style); // 样式，
						}
						switch (c) {
						case 0:
							// String cName =
							// parseEMSObtainManufacturer(rowParam.get(i).getEmsname());
							cell.setCellValue(collections.get(i).getEmsType());
							// 四个参数分别是：起始行，起始列，结束行，结束列
							if (column1) {
								cell.getSheet().addMergedRegion(
										new Region(startRow, (short) 0,
												(startRow + collections.get(i)
														.getGroupCount()) - 1,
												(short) 0));
								column1 = false;
							}
							break;
						case 1:
							cell.setCellValue(collections.get(i).getEdsPTNVOs()
									.get(j).getEmsname());
							cell.getSheet().setColumnWidth(c, 5000);
							// 四个参数分别是：起始行，起始列，结束行，结束列
							if (column2) {
								cell.getSheet().addMergedRegion(
										new Region(startRow, (short) 1,
												(startRow + collections.get(i)
														.getEdsPTNVOs().get(j)
														.getGroupCount()) - 1,
												(short) 1));
								column2 = false;
							}
							break;
						case 2:
							String cName = parseEMSObtainManufacturer(rowObj
									.get(j2).getEmsname());
							cell.setCellValue(cName);
							break;
						case 3:
							cell.setCellValue(rowObj.get(j2).getCreateDate()
									.toString());
							break;
						case 4:
							cell.setCellValue(rowObj.get(j2).getNeCount());
							break;
						case 5:
							cell.setCellValue(rowObj.get(j2).getSlotCount());
							break;
						case 6:
							cell.setCellValue(rowObj.get(j2)
									.getEquipmentCount());
							break;
						case 7:
							cell.setCellValue(rowObj.get(j2).getPtpCount());
							break;
						case 8:
							cell.setCellValue(rowObj.get(j2).getSectionCount());
							break;
						case 9:
							cell.setCellValue(rowObj.get(j2).getRouteCount());
							break;
						case 10:
							cell.setCellValue(rowObj.get(j2).getCcCount());
							break;
						case 11:
							cell.setCellValue(rowObj.get(j2).getCtpCount());
							break;
						case 12:
							cell.setCellValue(rowObj.get(j2).getSncCount());
							break;
						}
					}
					exlRow++;
				}
				column2 = true;
			}
			column1 = true;
		}
	}

	/**
	 * ZSH-T2000-5-P, HZ-T2000-2-P, HUZ-U2000-1-OTN, NBO-T2000-7-P,
	 * HZ-T2000-1-P, ZJ-T2000-2-P, JIH-U2000-1-OTN]
	 * 
	 * @param emsName
	 * @return
	 */
	public String parseEMSObtainManufacturer(String emsNames) {
		String emsName = emsNames.toUpperCase();
		if (emsName.contains("U2000") || emsName.contains("T2000")) {
			if (emsName.contains("LSH") || emsName.contains("LS")) {
				return "丽水";
			}
			if (emsName.contains("TZH") || emsName.contains("TZ")) {
				return "台州";
			}
			if (emsName.contains("WZH") || emsName.contains("WZ")) {
				return "温州";
			}
			if (emsName.contains("QUZ") || emsName.contains("QZ")) {
				return "衢州";
			}
			if (emsName.contains("HZ") || emsName.contains("HZH")) {
				return "杭州";
			}
			if (emsName.contains("ZSH") || emsName.contains("ZS")) {
				return "舟山";
			}
			if (emsName.contains("ZJ") || emsName.contains("ZHJ")) {
				return "浙江";
			}
			if (emsName.contains("JIH") || emsName.contains("JH")) {
				return "金华";
			}
			if (emsName.contains("NBO") || emsName.contains("NB")) {
				return "宁波";
			}
			if (emsName.contains("HUZ") || emsName.contains("HZ")) {
				return "湖州";
			}
		} else if (emsName.contains("OTNM")) {
			if (emsName.contains("JH") || emsName.contains("JIH")) {
				return "金华";
			}
			if (emsName.contains("HZ") || emsName.contains("HUZ")) {
				return "湖州";
			}
			if (emsName.contains("LS") || emsName.contains("LIS")) {
				return "丽水";
			}
			if (emsName.contains("JX") || emsName.contains("JIAX")) {
				return "嘉兴";
			}
			if (emsName.contains("SX") || emsName.contains("SHX")) {
				return "绍兴";
			}
			if (emsName.contains("ZJ") || emsName.contains("ZHJ")) {
				return "省干";
			}
		} else if (emsName.contains("ALU")) {
			if (emsName.contains("ZS") || emsName.contains("ZHS")) {
				return "舟山";
			}
		}
		return "";
	}

	/**
	 * Create EXCEL hearder topic
	 * 
	 * @param writableSheet
	 * @return EXCEL row
	 * @throws Exception
	 */
	public HSSFRow createHeaderTopic(HSSFRow row, HSSFCellStyle style3) {
		row.createCell(0).setCellValue("EmsType");
		row.createCell(1).setCellValue("EMS");
		row.createCell(2).setCellValue("地市");
		row.createCell(3).setCellValue("Time");
		row.createCell(4).setCellValue("NE");
		row.createCell(5).setCellValue("Slot");
		// row.createCell(6).setCellValue("SubSlot");
		row.createCell(6).setCellValue("Equipment");
		row.createCell(7).setCellValue("PTP/FTP");
		// row.createCell(9).setCellValue("FTP");
		row.createCell(8).setCellValue("Section");
		// row.createCell(11).setCellValue("Tunnel");
		// row.createCell(12).setCellValue("PW");
		// row.createCell(13).setCellValue("PWE3");
		row.createCell(9).setCellValue("Route");
		// row.createCell(15).setCellValue("TunnelPG");
		row.createCell(10).setCellValue("CcCount");
		row.createCell(11).setCellValue("CtpCount");
		row.createCell(12).setCellValue("SncCount");
		for (int i = 0; i < 13; i++) {
			row.getCell(i).setCellStyle(style3);
		}
		return row;
	}

	/**
	 * Parse EDS meta data
	 * 
	 * @param list
	 * @return
	 */
	public List<EDS_PTNVO> parseDataTypeForList(List list) {
		List<EDS_PTNVO> edsPTNs = new ArrayList<EDS_PTNVO>();
		for (Object obj : list) {
			Object[] objects = (Object[]) obj;
			if (objects != null) {
				EDS_PTNVO edsPTN = new EDS_PTNVO();
				edsPTN.setId(new Long(objects[0].toString()));
				edsPTN.setCreateDate((Date) objects[1]);
				edsPTN.setDn(objects[2] == null ? "" : objects[2].toString());
				edsPTN.setFromWhere(new Integer(objects[3] == null ? "0"
						: objects[3].toString()));
				edsPTN.setUpdateDate((Date) objects[8] == null ? null
						: (Date) objects[8]);
				edsPTN.setCollectTime((Date) objects[10] == null ? null
						: (Date) objects[10]);
				edsPTN.setEmsname(objects[11] == null ? "Null" : objects[11]
						.toString());
				edsPTN.setEquipmentCount(new Integer(objects[12] == null ? "0"
						: objects[12].toString()));
				edsPTN.setFtpCount(new Integer(objects[13] == null ? "0"
						: objects[13].toString()));
				edsPTN.setNeCount(new Integer(objects[14] == null ? "0"
						: objects[14].toString()));
				edsPTN.setPtpCount(new Integer(objects[15] == null ? "0"
						: objects[15].toString()));
				edsPTN.setPwCount(new Integer(objects[16] == null ? "0"
						: objects[16].toString()));
				edsPTN.setPwe3Count(new Integer(objects[17] == null ? "0"
						: objects[17].toString()));
				edsPTN.setRouteCount(new Integer(objects[18] == null ? "0"
						: objects[18].toString()));
				edsPTN.setSectionCount(new Integer(objects[19] == null ? "0"
						: objects[19].toString()));
				edsPTN.setSlotCount(new Integer(objects[20] == null ? "0"
						: objects[20].toString()));
				edsPTN.setSubSlotCount(new Integer(objects[21] == null ? "0"
						: objects[21].toString()));
				edsPTN.setTaskSerial(objects[22] == null ? "Null" : objects[14]
						.toString());
				edsPTN.setTunnelCount(new Integer(objects[23] == null ? "0"
						: objects[23].toString()));
				edsPTN.setTunnelPG(new Integer(objects[24] == null ? "0"
						: objects[24].toString()));
				edsPTN.setCcCount(new Integer(objects[26] == null ? "0"
						: objects[26].toString()));
				edsPTN.setCtpCount(new Integer(objects[27] == null ? "0"
						: objects[27].toString()));
				edsPTN.setSncCount(new Integer(objects[29] == null ? "0"
						: objects[29].toString()));
				edsPTNs.add(edsPTN);
			}
		}
		return edsPTNs;
	}

	/**
	 * Match EMS type
	 * 
	 * @param edsPtn
	 * @return Manufacturer's all EMS
	 */
	private List<EDS_PTNVO> matchEMSType(List<EDS_PTNVO> edsPtn) {
		List<EDS_PTNVO> vo = new ArrayList<EDS_PTNVO>();
		EDS_PTNVO sdh = new EDS_PTNVO();
		EDS_PTNVO otn = new EDS_PTNVO();
		int sdhSize = 0;
		int otnSize = 0;

		for (int i = 0; i < edsPtn.size(); i++) {
			if (null == edsPtn.get(i).getEmsname()) {
				continue;
			}

			String emsName = edsPtn.get(i).getEmsname().trim().toUpperCase();

			if (emsName.contains("U2000") || emsName.contains("T2000")) {

				for (int j = 0; j < hwSDHType.length; j++) {
					if (emsName.equals(hwSDHType[j])) {
						sdhSize++;
						sdh.setEmsType("SDH");
						sdh.setGroupCount(sdhSize);
						sdh.getEdsPTNVOs().add(edsPtn.get(i));
						break;
					}
				}

				for (int j = 0; j < hwOTN.length; j++) {
					if (emsName.equals(hwOTN[j])) {
						otnSize++;
						otn.setEmsType("OTN");
						otn.setGroupCount(otnSize);
						otn.getEdsPTNVOs().add(edsPtn.get(i));
						break;
					}
				}
			}

			if (emsName.contains("OTNM")) {

				for (int j = 0; j < fhSDH.length; j++) {
					if (emsName.equals(fhSDH[j])) {
						sdhSize++;
						sdh.setEmsType("SDH");
						sdh.setGroupCount(sdhSize);
						sdh.getEdsPTNVOs().add(edsPtn.get(i));
						break;
					}
				}

				for (int j = 0; j < fhOTN.length; j++) {
					if (emsName.equals(fhOTN[j])) {
						otnSize++;
						otn.setEmsType("OTN");
						otn.setGroupCount(otnSize);
						otn.getEdsPTNVOs().add(edsPtn.get(i));
						break;
					}
				}
			}

			if (emsName.contains("ALU") || emsName.contains("ZTE")
					|| emsName.contains("OTNU")) {

				for (int j = 0; j < zx_alu_OTN.length; j++) {
					if (emsName.equals(zx_alu_OTN[j])) {
						otnSize++;
						otn.setEmsType("OTN");
						otn.setGroupCount(otnSize);
						otn.getEdsPTNVOs().add(edsPtn.get(i));
						break;
					}
				}
			}
		}
		vo.add(sdh);
		vo.add(otn);
		for (int i = 0; i < vo.size(); i++) {
			for (int j = 0; j < vo.get(i).getEdsPTNVOs().size(); j++) {
				if (vo.get(i).getEmsType().equals("SDH")) {
					sdhCount += vo.get(i).getEdsPTNVOs().get(j).getGroupCount();
				}
				if (vo.get(i).getEmsType().equals("OTN")) {
					otnCount += vo.get(i).getEdsPTNVOs().get(j).getGroupCount();
				}
			}
		}
		vo.get(0).setGroupCount(sdhCount);
		vo.get(1).setGroupCount(otnCount);
		sdhCount = 0;
		otnCount = 0;
		return vo;
	}

	/**
	 * Search EMS
	 * 
	 * @param sql
	 * @return (Return the current report on all of the record)
	 */
	private List queryEDSPTNData(String sql) {
		JpaClient services = JpaClient.getInstance();
		List list = null;
		try {
			list = services.querySql(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Add report record Table:Report
	 * 
	 * @param rep
	 */
	private void insertReportRecord(Report rep) {
		JpaClient instance = JpaClient.getInstance();
		try {
			instance.saveObject(-1, rep);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
