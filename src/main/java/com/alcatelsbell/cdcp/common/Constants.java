package com.alcatelsbell.cdcp.common;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-2
 * Time: 上午10:36
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Constants {
	public static final String SYSPRO_FTP_HOST = "ftp.host";
	public static final String SYSPRO_FTP_USER = "ftp.user";
	public static final String SYSPRO_FTP_PASSWORD = "ftp.password";
	public static final String SYSPRO_FTP_REMOTE_PATH = "ftp.remotepath";

	public static final String SERVICE_NAME_CDCP_DATAJPA = "cdcp.datajpa";
	public static final String SERVICE_NAME_CDCP_EMS = "cdcp.ems";
	public static final String SERVICE_NAME_CDCP_TASK = "cdcp.task";
	public static final String SERVICE_NAME_CDCP_EMSSTATISTICS = "cdcp.emsstatistics";
	public static final String SERVICE_NAME_CDCP_REPORT = "cdcp.report";
	public static final String SERVICE_NAME_CDCP_EMSALARM = "cdcp.emsalarm";

	public static final Integer TASK_STATUS_SBI_RUNNING = 0;
	public static final Integer TASK_STATUS_SBI_EXCEPTION = 1;
	public static final Integer TASK_STATUS_MIGRATING = 2;
	public static final Integer TASK_STATUS_MIGRATE_WAITING = 7;
	public static final Integer TASK_STATUS_MIGRATING_EXCEPTION = 3;
	public static final Integer TASK_STATUS_ERROR = 5;

	public static final Integer TASK_STATUS_FINISHED = 6;

	public static final Integer EMS_STATUS_NORMAL = 0;
	public static final Integer EMS_STATUS_EXCEPTION = 1;

	public static final String SERVER_MESSAGE_ATTRIBUTE_EMS_DN = "emsdn";
	public static final String SERVER_MESSAGE_ATTRIBUTE_MIGRATE_TASK_SERIAL = "migrate_task_serial";
	public static final String SERVER_MESSAGE_ATTRIBUTE_MIGRATE_PERCENTAGE = "migrate_percentage";
	public static final String SERVER_MESSAGE_ATTRIBUTE_MIGRATE_TXT = "migrate_txt";

	public static final String TOPIC_SERVER_MIGRATE_LOG = "topic_server_migrate_log";

	// c_ems.status字段
	// 0：综资可以迁移（陈荣荣从ems迁移成功后，才可设置该值）
	// 1：综资迁移中（这时陈荣荣不能再东改ems的数据）
	// 2：EMS迁移中（这时综资不能迁移该EMS）
	public static final int CEMS_STATUS_READY = 0;
	public static final int CEMS_STATUS_UPPER_LEVEL_LOCK = 1;
	public static final int CEMS_STATUS_MIGRATING = 2;

	public static final int EMS_ISSYNCOK_OK = 1;
	public static final int EMS_ISSYNCOK_NOK = 0;

	public static final String EMS_TYPE_SDH = "SDH";
	public static final String EMS_TYPE_PTN = "PTN";
	public static final String EMS_TYPE_OTN = "OTN";
	public static final String EMS_TYPE_DWDM = "DWDM";

    public static final String[] PRE_LOAD_SQLS = new String[]{
            "alter table C_SLOT modify (acceptableEquipmentTypeList varchar(2048))",
            "alter table C_EQUIPMENT modify (ADDITIONALINFO varchar(2048))",
            "alter table C_CHANNEL modify (dn varchar(512))",
            "alter table C_SHELF modify (ADDITIONALINFO varchar(1024))",
            "alter table C_PATH_CHANNEL modify (channelDn varchar(512))",
            "alter table C_ROUTE_CHANNEL modify (channelDn varchar(512))",
            "alter table C_DEVICE modify (additionalInfo varchar(4000))",
            "alter table C_CROSSCONNECT modify (dn varchar(1024))" ,
            "alter table C_PATH_CC modify (ccDn varchar(1024))",
            "alter table C_ROUTE_CC modify (ccDn varchar(1024))",
            "alter table C_ROUTE modify (dn varchar(512))",
            "alter table C_PATH modify (dn varchar(512))",
            "alter table C_ROUTE_CHANNEL modify (routedn varchar(512))",
			"alter table C_ROUTE_CHANNEL modify (channeldn varchar(512))",
            "alter table C_ROUTE_CC modify (routedn varchar(512))"
    };

	public static final String[] ptn_sqls = new String[] {"create index idx_csubnetwork2ems on c_subnetwork(emsname)",

			"create index idx_csubnetworkdeviceshelf2ems on c_subnetworkdevice(emsname)",
			"create unique index uk_csubnetworkdevice on c_subnetworkdevice(devicedn,subnetworkdn)",

			"create index idx_cdevice2ems on c_device(emsname)",

			"create index idx_cshelf2parentdn on c_shelf(parentdn)",
			"create index idx_cslot2shelf on c_slot(shelfdn)",
			"create index idx_cslot2parent on c_slot(parentdn)",
			"create index idx_crack2parent on c_rack(parentdn)",

			"create index idx_cslot2parentslot on c_slot(parentslotdn)",

			"create index idx_cequipment2ems on c_equipment(emsname)",

			"create index idx_cptp2ems on c_ptp(emsname)",
			"create index idx_cptp2device on c_ptp(devicedn)",

			"create index idx_cctp2ems on c_ctp(emsname)",

			//     "create index idx_cftpptp2ptpdn on c_ftp_ptp(ptpdn)",

			"create unique index uk_cftpptp on c_ftp_ptp(ptpdn,ftpdn)",
			"create index idx_cftpptp2ems on c_ftp_ptp(emsname)",

			"create unique index uk_cmpctp on c_mp_ctp(ptpdn,ctpdn)",
			"create index idx_cmpctp2ems on c_mp_ctp(emsname)",

			"create index idx_csection2ems on c_section(emsname)",

			"create index idx_ctunnel2ems on c_tunnel(emsname)",
			"create index idx_croute2ems on c_route(emsname)",
			"create index idx_croute2ems on C_IPRoute(emsname)",

			"create index idx_ctunnelsection2ems on c_tunnel_section(emsname)",
			//     --需要看为什么unique index创建失败，不合理
			"create unique index idx_ctunnelsection on c_tunnel_section(sectiondn,tunneldn)",

			"create index idx_cprotectiongroup2ems on c_protectiongroup(emsname)",
			"create index idx_cprotectiongrouptunnel2ems on c_protectiongroup_tunnel(emsname)",
			"create unique index uk_cprotectiongrouptunnel on c_protectiongroup_tunnel(protectgroupdn,tunneldn)",

			"create index idx_cpwe32ems on c_pwe3(emsname)",
			"create index idx_cpw2ems on c_pw(emsname)",
			"create index idx_cpwe3pw2ems on c_pwe3_pw(emsname)",
			"create index idx_cpwtunnel2ems on c_pw_tunnel(emsname)",
			"create unique index uk_cpwe3pw on c_pwe3_pw(pwdn,pwe3dn)",
			"create unique index uk_cpwtunnel on c_pw_tunnel(tunneldn,pwdn)",

			"create index  idx_cslot2cardn on  c_slot(carddn)",
			"create index  idx_cequipment2parent on  c_equipment(parentdn)",

			"alter table C_TUNNEL_SECTION modify (dn varchar(512))",
			"alter table C_PWE3_TUNNEL modify (dn varchar(512))" ,
			"alter table C_CHANNEL modify (dn varchar(512))",
			"alter table C_PATH_CHANNEL modify (channelDn varchar(512))" ,
			"alter table C_ROUTE_CHANNEL modify (channelDn varchar(512))"



	};

}
