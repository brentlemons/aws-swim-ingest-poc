/**
 * 
 */
package com.awsproserve.swim.ingest.entity;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author lembrent
 *
 */
@XmlRegistry
public class ObjectFactory {
	
	private final static QName _asdexMsg_QNAME = new QName("urn:us:gov:dot:faa:atm:terminal:entities:v4-0:smes:surfacemovementevent", "ASDEXMessage");
	
	public ObjectFactory() {
	}
	
	public ASDEXMessage createASDEXMessage() {
		return new ASDEXMessage();
	}

    @XmlElementDecl(namespace = "urn:us:gov:dot:faa:atm:terminal:entities:v4-0:smes:surfacemovementevent", name = "ASDEXMessage")
    public JAXBElement<ASDEXMessage> createASDEXMessage(ASDEXMessage value) {
        return new JAXBElement<ASDEXMessage>(_asdexMsg_QNAME, ASDEXMessage.class, null, value);
    }

}
