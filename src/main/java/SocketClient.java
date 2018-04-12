import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-2-13
 * Time: 下午2:37
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SocketClient {
    private Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) throws Exception {
        List allObjects = JpaClient.getInstance().findAllObjects(Ems.class);
        System.out.println("allObjects = " + allObjects.size());




    }
}
