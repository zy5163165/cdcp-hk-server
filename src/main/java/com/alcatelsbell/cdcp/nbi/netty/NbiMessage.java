package com.alcatelsbell.cdcp.nbi.netty;

import io.netty.handler.codec.json.JsonObjectDecoder;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2016/7/26
 * Time: 11:48
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class NbiMessage{
    private static  ObjectMapper objectMapper = new ObjectMapper();
    private HashMap<String,?> dataMap = null;

    public static final String HEADER  = "message_header";
    public static final String HEADER_MESSAGE_TYPE = "message_type";
    public static final String HEADER_MESSAGE_ID = "message_id";
    public static final String HEADER_CLIENT_ID = "client_id";

    public static final String BODY = "message_body";

    public NbiMessage() {

    }

    public String getMessageType() {
        return (String)dataMap.get(HEADER);
    }

    public byte[] toJsonBytes() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        objectMapper.writeValue(bao,dataMap);
        byte[] bs =  bao.toByteArray();
        bao.close();
        return bs;
    }

    public static NbiMessage parse(byte[] json) throws IOException {
        List list = objectMapper.readValue(new String(json,"utf-8"),List.class);
        NbiMessage message = new NbiMessage();
        message.dataMap = (HashMap)list.get(0);
        return message;
    }

    public HashMap getDataMap() {
        return dataMap;
    }

    public void setDataMap(HashMap dataMap) {
        this.dataMap = dataMap;
    }

    public static void main(String[] args) throws IOException {
        String json = "[{\n" +
                "   \"message_header\": {\n" +
                "   \"message_id\": \"request123456\",\n" +
                "   \"client_id\":\"client123\",\n" +
                "   \"message_type\":\"alarm\"      \n" +
                "   },\n" +
                "   \"message_body\": {\n" +
                "              \"key\":\"StartAlarm\"\n" +
                "   }\n" +
                "}]";
        System.out.println("json = " + json);
        List list = objectMapper.readValue(json, List.class);
        System.out.println("list = " + list);
    }
}
