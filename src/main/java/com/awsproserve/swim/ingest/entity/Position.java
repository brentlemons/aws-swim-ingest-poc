/**
 * 
 */
package com.awsproserve.swim.ingest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * @author lembrent
 *
 */
@Data
@XmlRootElement(name = "position")
@XmlAccessorType(XmlAccessType.FIELD)
public class Position {
	
	@XmlElement
	private Float latitude;

	@XmlElement
	private Float longitude;

}
