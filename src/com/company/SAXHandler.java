package com.company;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

/**
 * Created by vlad on 23.02.2017.
 */
public class SAXHandler extends DefaultHandler {

    private String curTag;
    private HashMap<String, String> hm;
    private String type;
    public SAXHandler() {

        curTag = new String();
        hm = new HashMap<>();
        type= new String();
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        curTag = qName;
        if (qName.equals("message")) {
            type = atts.getValue("type");
        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = "";
        for (int i = 0; i < length; i++){
            value += ch[i + start];
        }
        hm.put(curTag, value);
    }

    public String getType() {
        return type;
    }

    String getValue(String key) {
        return hm.get(key);
    }
 
}
