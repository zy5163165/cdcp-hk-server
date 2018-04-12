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

import java.io.File;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-23
 * Time: 下午9:55
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class XMLParser {
    private Log logger = LogFactory.getLog(getClass());
    private static Element getElement(Element ele,String tag,int idx) {
        return (Element)ele.getElementsByTagName("tag").item(idx);
    }
    public static void main(String[] args) throws Exception {
        DocumentBuilder dombuilder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = dombuilder.parse(new File("d:/work/soap.xml"));





        Element documentElement = doc.getDocumentElement();


        NodeList summary = documentElement.getElementsByTagName("summary");
        Element node =(Element)summary.item(0);



        NodeList recordInfo = node.getElementsByTagName("recordInfo");
        node = (Element)recordInfo.item(0);

        NodeList fieldInfos = node.getElementsByTagName("fieldInfo");
        node = (Element)recordInfo.item(0);

        NodeList fieldChNames = node.getElementsByTagName("fieldChName");
        node = (Element)fieldChNames.item(0);
        System.out.println(node.getTextContent());




        //     System.out.println("recordInfo = " + recordInfo);

    }
}
