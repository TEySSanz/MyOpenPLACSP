/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.utils.genericode;

/**
 *
 * @author DSGregorio
 */
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import myopenplacsp.view.ParserController;


public class GenericodeManager {
	
	private static final Logger logger = LogManager.getLogger(ParserController.class.getName());
	
	/**
	 * Lee un InputStream (xml con CodeList) a un objeto Java
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static CodeList read(InputStream xml) throws Exception {
		JAXBContext jc;
		CodeList codeList = null;
		try {
                        /*System.out.println(CodeList.class);
                        System.out.println(CodeList.class.getPackage().getName());*/
			//jc = JAXBContext.newInstance(CodeList.class.getPackage().getName() );                        
                        jc = JAXBContext.newInstance(new Class [] {CodeList.class});                        
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			codeList = (CodeList) unmarshaller.unmarshal(xml);
	        xml.close();
		} catch (Exception e) {
			//logger.error("Error leyendo CodeList",e);
                        System.out.println(">>Error leyendo CodeList "+e.getMessage());
			throw e;
		}	
		return codeList;
	}
	
	
	public static HashMap<String, String> generateMap(InputStream xmlGenericCode) throws Exception{
		CodeList codeList = read(xmlGenericCode);
		HashMap<String, String> mapGenericode = new HashMap<String, String>();
		
		//Se recorre la lista y se inserta en el map
		if (codeList != null && codeList.getSimpleCodeList() != null && codeList.getSimpleCodeList().getRows() != null) {
			for (Row row : codeList.getSimpleCodeList().getRows()) {
				String clave = null;
				String valor = null;
				if (row.getValues() != null) {
					for (Value value : row.getValues()) {
						if (value.getColumnRef().equals("code")) {
							clave = value.getSimpleValue();
						}
						else if (value.getColumnRef().equals("nombre")) {
							valor = value.getSimpleValue();
						}
					}
				}
				mapGenericode.put(clave, valor);
			}
		}
		return mapGenericode;
	}

}
