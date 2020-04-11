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
////		marshaller.setContextPath("net.opengis.wfs._2");
//		marshaller.setClassesToBeBound(
//				net.opengis.wfs.v_2_0.GetFeatureType.class,
////				aero.aixm.extensions.faa.fnse.ObjectFactory.class,
//				aero.aixm.ObjectFactory.class);
		marshaller.setPackagesToScan("aero.*", "net.*", "org");
		return marshaller;
	}

}
