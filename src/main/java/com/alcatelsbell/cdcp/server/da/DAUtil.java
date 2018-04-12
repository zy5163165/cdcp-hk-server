package com.alcatelsbell.cdcp.server.da;

import com.alcatelsbell.cdcp.nbi.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/1/15.
 */
public class DAUtil {
    public static List<DAEntity> daEntities = new ArrayList<DAEntity>();
    static {
        daEntities.add(new DAEntity(CDevice.class,1,"select c.nativeEmsName from CDevice c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CShelf.class,2,"select c.userLabel from CShelf c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CEquipment.class,3,"select c.userLabel from CEquipment c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CPTP.class,4,"select c.dn from CPTP c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CSection.class,5,"select c.nativeEMSName from CSection c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CTunnel.class,6,"select c.nativeEMSName from CTunnel c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CPWE3.class,7,"select c.nativeEMSName from CPWE3 c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CPW.class,8,"select c.nativeEMSName from CPW c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CSubnetwork.class,9,"select c.nativeemsname from CSubnetwork c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CDevice.class,10,"select c.nativeEmsName from CDevice c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CShelf.class,11,"select c.userLabel from CShelf c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CEquipment.class,12,"select c.userLabel from CEquipment c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CPTP.class,13,"select c.dn from CPTP c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CSection.class,14,"select c.nativeEMSName from CSection c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CPath.class,15,"select c.name from CPath c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CRoute.class,16,"select c.name from CRoute c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CEthTrunk.class,17,"select c.name from CEthTrunk c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CEthRoute.class,18,"select c.name from CEthRoute c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CSubnetwork.class,19,"select c.nativeemsname from CSubnetwork c where c.emsName = ':emsName'"));
        // daEntities.add(new DAEntity(CDevice.class,20,"select c.nativeEmsName from CDevice c where c.emsName = ':emsName'"));

        daEntities.add(new DAEntity(CDevice.class,21,"select c.nativeEmsName from CDevice c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CShelf.class,22,"select c.userLabel from CShelf c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CEquipment.class,23,"select c.userLabel from CEquipment c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CPTP.class,24,"select c.dn from CPTP c where c.emsName = ':emsName'"));
        daEntities.add(new DAEntity(CSection.class,25,"select c.nativeEMSName from CSection c where c.emsName = ':emsName' and c.type = 'OTS'"));
        daEntities.add(new DAEntity(CSection.class,26,"select c.nativeEMSName from CSection c where c.emsName = ':emsName' and c.type = 'OMS'"));
        daEntities.add(new DAEntity(CSection.class,27,"select c.nativeEmsName from CRoute c where c.emsName = ':emsName'  "));
        daEntities.add(new DAEntity(CSection.class,28,"select c.name from CPath c where c.emsName = ':emsName'  "));
    }

    public static DAEntity getDAEntity(Class cls) {
        for (DAEntity daEntity : daEntities) {
            if (cls.equals(daEntity.cls))
                return daEntity;
        }
        return null;
    }
    public static DAEntity getDAEntity(int code) {
        for (DAEntity daEntity : daEntities) {
            if (code == daEntity.code)
                return daEntity;
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("select c.nativeemsname from CSubnetwork c where c.emsName = ':emsName'".replaceAll(":emsName","HZ-U2000-1"));
    }
}
