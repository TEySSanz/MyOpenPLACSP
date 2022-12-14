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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="CodeList",namespace="http://docs.oasis-open.org/codelist/ns/genericode/1.0/")
public class CodeList {
	@XmlElement(name="SimpleCodeList")
	protected SimpleCodeList simpleCodeList;
	
	public SimpleCodeList getSimpleCodeList() {
		return simpleCodeList;
	}
	
	public void setSimpleCodeList(SimpleCodeList simpleCodeList) {
		this.simpleCodeList = simpleCodeList;
	}
	
}
