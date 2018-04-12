package com.alcatelsbell.cdcp.nbi.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-23
 * Time: 下午10:43
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class XMLUtil {
    public static Document parse(String xml) throws Exception {
        DocumentBuilder dombuilder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = dombuilder.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
        return doc;
    }

    public static Element getElement(Element ele,String tag,int idx) {
        return (Element)ele.getElementsByTagName(tag).item(idx);
    }

    public static Element getElement(Document ele,String tag,int idx) {
        return (Element)ele.getElementsByTagName(tag).item(idx);
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("d:\\docs\\4Backup\\abc.xml");
        int available = fis.available();
        byte[] bs = new byte[available];
        fis.read(bs);
        Document doc = XMLUtil.parse(new String(bs));
        int length = doc.getChildNodes().getLength();
        System.out.println("length = " + length);
        FileWriter fw = new FileWriter("d:\\msn.log");
        int i = 0;
        while (true) {
            Element message = XMLUtil.getElement(doc, "Message", i);
            if (message == null)break;
            String date = message.getAttribute("Date");
            String time = message.getAttribute("Time");
            String txt = message.getTextContent();
            System.out.println("txt = " + txt);
            System.out.println("date = " + date);
            NodeList childNodes = message.getChildNodes();

            Node from = childNodes.item(0);
            Node user = from.getChildNodes().item(0);
            Node friendlyName = user.getAttributes().getNamedItem("FriendlyName");
            System.out.println("" + friendlyName);


            fw.write(date+" "+time+" "+friendlyName.getTextContent()+" : " + txt+"\r\n");
            fw.flush();
            i++;
        }







    }
}
