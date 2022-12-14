/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.utils.genericode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author DSGregorio
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="Value")
public class Value {
	
	@XmlElement(name = "SimpleValue")
	private String simpleValue;
	
	@XmlAttribute(name="ColumnRef")
	protected String columnRef;
	
	public String getSimpleValue() {
		return simpleValue;
	}
	
	public void setSimpleValue(String simpleValue) {
		this.simpleValue = simpleValue;
	}
	
	public String getColumnRef() {
		return columnRef;
	}
	
	public void setColumnRef(String columnRef) {
		this.columnRef = columnRef;
	}
}
