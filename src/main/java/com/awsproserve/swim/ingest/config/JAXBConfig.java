/**
 * 
 */
package com.awsproserve.swim.ingest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * @author lembrent
 *
 */
@Configuration
public class JAXBConfig {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
//		marshaller.setContextPath("aero.aixm.schema._5");
//		marshaller.setClassesToBeBound(
////				net.opengis.wfs.v_2_0.GetFeatureType.class,
////				aero.aixm.extensions.faa.fnse.ObjectFactory.class,
//				aero.aixm.schema._5_1.ObjectFactory.class);
		marshaller.setPackagesToScan("com.awsproserve.swim.ingest.entity.*");
//		marshaller.setPackagesToScan("aero.*", "com.awsproserve.swim.ingest.entity.*");
//		marshaller.setPackagesToScan("aero.*", "net.*", "org.*");
//		marshaller.setPackagesToScan("aero.aixm.schema._5.*", "aero.aixm.schema._5_1.message.*", "aero.faa.nas._3");
		return marshaller;
	}

}
