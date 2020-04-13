/**
 * 
 */
package com.awsproserve.swim.ingest.entity;

import java.util.List;

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
@XmlRootElement(name = "asdexMsg")
@XmlAccessorType(XmlAccessType.FIELD)
public class ASDEXMessage {
	
	@XmlElement(name = "airport", required = true)
	private String airport;
	
	@XmlElement(name = "positionReport", required = true)
	private List<PositionReport> positionReports;

}
