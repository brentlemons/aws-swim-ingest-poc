/**
 * 
 */
package com.awsproserve.swim.ingest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * @author lembrent
 *
 */
@Data
@XmlRootElement(name = "positionReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class PositionReport {
	
	@XmlAttribute
	private Boolean full;
	
	@XmlElement
	private Integer seqNum;

	@XmlElement
	private Integer track;

	@XmlElement
	private Integer stid;

}
