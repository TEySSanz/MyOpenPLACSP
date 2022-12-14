/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.model;

/**
 *
 * @author DSGregorio
 */
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import myopenplacsp.utils.Config;
import myopenplacsp.view.ParserController;
 
public class SpreeadSheetManager {
	
	private final Logger logger = LogManager.getLogger(ParserController.class.getName());
	
	private SXSSFWorkbook workbook = new SXSSFWorkbook(); 
	private static CellStyle cellStyleTexto;
	private static CellStyle cellStyleNumeroEntero;
	private static CellStyle cellStyleFechaLarga;
	private static CellStyle cellStyleFechaDia;
	private static CellStyle cellStyleMoneda;
	private static CellStyle cellStyleTitulo;

	public static final String LICITACIONES = "Licitaciones";
	public static final String RESULTADOS = "Resultados";                
	
	
	/**
	 * @return the workbook
	 */
	public SXSSFWorkbook getWorkbook() {
		return workbook;
	}


	/**
	 * @param workbook the workbook to set
	 */
	public void setWorkbook(SXSSFWorkbook workbo) {
		workbook = workbo;
	}



	/**
	 * @return the cellStyleFechaLarga
	 */
	public static CellStyle getCellStyleFechaLarga() {
		return cellStyleFechaLarga;
	}
	
	/**
	 * @return the cellStyleTitulo
	 */
	public static CellStyle getCellStyleTitulo() {
		return cellStyleTitulo;
	}  

	/**
	 * Creación de objeto
	 */
	public SpreeadSheetManager(boolean dosHojas, String rutaPlantillaExcel) throws Exception{
		
		
		// Se crea a partir de una plantilla y se añaden dos hojas
		try {
			//DSG String rutaPlantilla = Config.getProperty("open-placsp.template.xlsx");
                        //String rutaPlantilla = "C:\\Users\\DSGregorio\\Documents\\NetBeansProjects\\MyOpenPLACSP\\src\\myopenplacsp\\resources\\templates\\datosAbiertos-PLACSP.xlsx";
			
			//DSG workbook = new SXSSFWorkbook(new XSSFWorkbook( OPCPackage.open(SpreeadSheetManager.class.getResourceAsStream(rutaPlantilla))), 5);
                        workbook = new SXSSFWorkbook(new XSSFWorkbook( OPCPackage.open(rutaPlantillaExcel)), 5);
                        if (workbook.getSheet(LICITACIONES) != null){
                            workbook.removeSheetAt(1);
                        }
                        if (workbook.getSheet(RESULTADOS)!=null){
                            workbook.removeSheetAt(1);
                        }
			workbook.createSheet(LICITACIONES);
			if(dosHojas) {
				//Se añaden dos hojas al woorkbook
				workbook.createSheet(RESULTADOS); 
			}
			

			// Se definen los estilos que se van a utilizar
			
			// Texto
			cellStyleTexto = workbook.createCellStyle();
			CreationHelper createHelperTexto = workbook.getCreationHelper();
			cellStyleTexto.setDataFormat(createHelperTexto.createDataFormat().getFormat("@"));
						
			// Numero
			cellStyleNumeroEntero = workbook.createCellStyle();
			CreationHelper createHelperNumero = workbook.getCreationHelper();
			cellStyleNumeroEntero.setDataFormat(createHelperNumero.createDataFormat().getFormat("#,##0"));

			// Fecha larga
			cellStyleFechaLarga = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
			cellStyleFechaLarga.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

			// Fecha corta
			cellStyleFechaDia = workbook.createCellStyle();
			CreationHelper createHelperFechaCorta = workbook.getCreationHelper();
			cellStyleFechaDia.setDataFormat(createHelperFechaCorta.createDataFormat().getFormat("dd/mm/yyyy"));

			// Moneda
			cellStyleMoneda = workbook.createCellStyle();
			CreationHelper createHelperMoneda = workbook.getCreationHelper();
			cellStyleMoneda.setDataFormat(createHelperMoneda.createDataFormat().getFormat("#,##0.00 "));
			
			//Título
			cellStyleTitulo = workbook.createCellStyle();
			Font font= workbook.createFont();
		    font.setFontHeightInPoints((short)12);
		    font.setBold(true);
			cellStyleTitulo.setFont(font);
			cellStyleTitulo.setBorderBottom(BorderStyle.THIN);  
			cellStyleTitulo.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			cellStyleTitulo.setWrapText(true);
			

		}catch (InvalidFormatException | IOException e) {
			logger.error("Se produjo un error al cargar la plantilla");
			e.printStackTrace();
			throw e;
		}catch (Exception e) {
			logger.error("Se produjo un error al cargar la plantilla");
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Función que devuleve el estilo de XLSX asociado al tipo formato
	 * @param 
	 * @return
	 */
	public static CellStyle getCellStyleFormato(EnumFormatos formato) {
		switch(formato)
		{
		   case MONEDA:
		      return cellStyleMoneda;   
		   case FECHA_CORTA:
		      return cellStyleFechaDia;
		   case FECHA_LARGA:
			   return cellStyleFechaLarga;
		   case NUMERO:
			   return cellStyleNumeroEntero;
		   default : 
		      return cellStyleTexto;
		}
	}
	
	public void updateColumnsSize() {
		for (int i=0; i < workbook.getSheet(LICITACIONES).getRow(0).getLastCellNum(); i++){
			workbook.getSheet(LICITACIONES).setColumnWidth(i,Integer.valueOf(Config.getProperty("open-placsp.template.columnsize"))*256);
		}
		if (workbook.getSheet(RESULTADOS) != null) {
			for (int i=0; i < workbook.getSheet(RESULTADOS).getRow(0).getLastCellNum(); i++){
				workbook.getSheet(RESULTADOS).setColumnWidth(i,Integer.valueOf(Config.getProperty("open-placsp.template.columnsize"))*256);
			}
		}
	}
	
	
	public void insertarFiltro(int datosSeleccionables, int datosResultados){
		if (workbook.getSheet(RESULTADOS) != null) {
			//Hay dos tablas
			workbook.getSheet(LICITACIONES).setAutoFilter(new CellRangeAddress(0, 0, 0, datosSeleccionables + 3));
			workbook.getSheet(LICITACIONES).createFreezePane(0, 1);
			workbook.getSheet(RESULTADOS).setAutoFilter(new CellRangeAddress(0, 0, 0, datosResultados + 2));
			workbook.getSheet(RESULTADOS).createFreezePane(0, 1);
			
		}else {
			//hay una tabla
			workbook.getSheet(LICITACIONES).setAutoFilter(new CellRangeAddress(0, 0, 0, datosSeleccionables + datosResultados + 3));
			workbook.getSheet(LICITACIONES).createFreezePane(0, 1);
		}
		
	}
	

}
