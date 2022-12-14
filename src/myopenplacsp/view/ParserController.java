/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myopenplacsp.view;

/**
 *
 * @author DSGregorio
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.purl.atompub.tombstones._1.DeletedEntryType;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;
import org.w3._2005.atom.LinkType;

import myopenplacsp.MyOpenPLACSP;
import myopenplacsp.model.DatosResultados;
import myopenplacsp.model.DatosSeleccionables;
import myopenplacsp.model.SpreeadSheetManager;
import ext.place.codice.common.caclib.ContractFolderStatusType;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import myopenplacsp.utils.RegistrarAccion;

public class ParserController implements Initializable {

    private final Logger logger = LogManager.getLogger(ParserController.class.getName());

    @FXML
    private CheckBox checkLink;

    @FXML
    private CheckBox checkNumExpediente;

    @FXML
    private CheckBox checkObjetoContrato;

    @FXML
    private TextField textFieldDirOrigen;

    @FXML
    private TextField textFieldOutputFile;

    @FXML
    private RadioButton rbUnaTabla;

    @FXML
    private RadioButton rbDosTablas;

    @FXML
    private TreeView<String> treeDatos;

    @FXML
    private CheckBoxTreeItem<String> rootItem;

    @FXML
    private Text n_licitaciones;

    @FXML
    private Text n_ficheros;

    @FXML
    private ProgressBar progreso;

    @FXML
    private Button buttonGenerar;

    // Reference to the main application.
    private MyOpenPLACSP mainApp;

    private static Unmarshaller atomUnMarshaller;

    //Alert aError = new Alert(AlertType.NONE);  
    /*
	 * initialize se ejecuta después del constructor y sirve para la carga de datos dinámica, en este caso, del TreeView con las variables de los datos
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        /*
            this.rootItem = new CheckBoxTreeItem<String> ("Datos de la licitación");
		rootItem.setExpanded(true);
		this.treeDatos.setEditable(true);
		this.treeDatos.setCellFactory(CheckBoxTreeCell.<String>forTreeView());

		//primer nodo para datos de licitaciones y segundo para los resultados
		//se cotempla la agrupación de variables en el árbol por las hojas del excel generado
		//el orden de añadido es importante para luego su acceso, concuerda con el parámetro queDato de collectCheckedBoxes
		CheckBoxTreeItem<String> datosLicitaciones = new CheckBoxTreeItem<String> ("Datos Generales");
		rootItem.getChildren().add(datosLicitaciones);

		CheckBoxTreeItem<String> datosResultados = new CheckBoxTreeItem<String> ("Datos de Resultados");
		rootItem.getChildren().add(datosResultados);

		for (DatosSeleccionables dato: DatosSeleccionables.values()) {
			CheckBoxTreeItem<String> item = new CheckBoxTreeItem<String> (dato.getTiulo());
			datosLicitaciones.getChildren().add(item);
		}
		for (DatosResultados dato: DatosResultados.values()) {
			CheckBoxTreeItem<String> item = new CheckBoxTreeItem<String> (dato.getTiulo());     
			datosResultados.getChildren().add(item);
		}

		//treeDatos ya se debería haber creado porque lo tenemos pintado en el parser, cuidado
		//this.treeDatos = new TreeView<String> (rootItem);
		this.treeDatos.setRoot(rootItem);
		this.treeDatos.setShowRoot(true);

		//no debería hacer falta apriori, pero así forzamos y nos aseguramos de que salgan las variables como opciones
		this.treeDatos.refresh();
         */
    }

    /**
     * The constructor. The constructor is called before the initialize()
     * method.
     */
    public ParserController() {

    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MyOpenPLACSP mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void selectDirIn() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ATOM", "*.atom"));
        Stage currentStage = (Stage) textFieldDirOrigen.getScene().getWindow();
        File file = fileChooser.showOpenDialog(currentStage);
        if (file != null) {
            textFieldDirOrigen.setText(file.getAbsolutePath());
        }
    }

    //la direncia principal con el selectDirIn es el showSaveDialog en lugar de showOpenDialog y la restricción de extensión
    @FXML
    private void selectDirOut() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX", "*.xlsx"));
        Stage currentStage = (Stage) textFieldOutputFile.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currentStage);
        if (file != null) {
            textFieldOutputFile.setText(file.getAbsolutePath());
        }
    }

    /*public Task<Boolean> procesarDirectorio() {
        return new Task<Boolean>() {
            @SuppressWarnings("finally")
            @Override
            protected Boolean call() throws Exception {

                // Collección que registra las entries que ya han sido procesadas
                HashSet<String> entriesProcesadas = new HashSet<String>();
                HashMap<String, GregorianCalendar> entriesDeleted = new HashMap<String, GregorianCalendar>();
                int numeroEntries = 0;
                int numeroFicherosProcesados = 0;

                FeedType res = null;
                FileOutputStream output_file = null;
                InputStreamReader inStream = null;

                try {

                    updateProgress(0, 1);

                    //Se crea el Stream de salida en el path indicado
                    output_file = new FileOutputStream(new File(textFieldOutputFile.getText()));

                    // Create the JAXBContext
                    JAXBContext jc = JAXBContext.newInstance(
                            "org.w3._2005.atom:org.dgpe.codice.common.caclib:org.dgpe.codice.common.cbclib:ext.place.codice.common.caclib:ext.place.codice.common.cbclib:org.purl.atompub.tombstones._1");
                    atomUnMarshaller = jc.createUnmarshaller();

                    SpreeadSheetManager spreeadSheetManager = null;
                    if (rbDosTablas.isSelected()) {
                        spreeadSheetManager = new SpreeadSheetManager(true);
                    } else {
                        spreeadSheetManager = new SpreeadSheetManager(false);
                    }
                    ArrayList<SXSSFSheet> hojas = new ArrayList<SXSSFSheet>();
                    SXSSFSheet sheet = (SXSSFSheet) spreeadSheetManager.getWorkbook().getSheet(SpreeadSheetManager.LICITACIONES);
                    SXSSFSheet sheetResultados = null;
                    hojas.add(sheet);
                    if (rbDosTablas.isSelected()) {
                        //Se van a crear dos hojas con los datos
                        sheetResultados = (SXSSFSheet) spreeadSheetManager.getWorkbook().getSheet(SpreeadSheetManager.RESULTADOS);
                        hojas.add(sheetResultados);
                    }

                    logger.info("Comienza el procesamiento de los ficheros");
                    // para recoger las opciones seleccionadas por el usuario
                    // atentos a los castings
                    // también a los índices tanto del método (queDatos) como del get del nodo,
                    // indican la hoja del excel
                    // segun el orden de la disposición gráfica
                    // bastante dependencia si se añaden o quitan más enumerados
                    // repensar si en el futuro se contemplan muchas hojas generadas en el Excel que
                    // deban tener sus agrupaciones respectivas
                    ArrayList<Object> buscadorDatosSeleecionablesObj = collectCheckedBoxes(
                            (CheckBoxTreeItem<String>) rootItem.getChildren().get(0), 0);
                    ArrayList<DatosSeleccionables> buscadorDatosSeleecionables = new ArrayList<DatosSeleccionables>(
                            buscadorDatosSeleecionablesObj.size());
                    for (Object auxD : buscadorDatosSeleecionablesObj) {
                        buscadorDatosSeleecionables.add((DatosSeleccionables) auxD); // casting del Objeto al enumerado
                        // correspondiente
                    }
                    // en este caso el indice es 1 en lugar de 0 por corresponderse al enumerado de
                    // DatosResultados
                    ArrayList<Object> buscadorDatosResultadosObj = collectCheckedBoxes(
                            (CheckBoxTreeItem<String>) rootItem.getChildren().get(1), 1);
                    ArrayList<DatosResultados> buscadorDatosResultados = new ArrayList<DatosResultados>(
                            buscadorDatosResultadosObj.size());
                    for (Object auxD : buscadorDatosResultadosObj) {
                        buscadorDatosResultados.add((DatosResultados) auxD);
                    }

                    logger.debug("Se comienzan a añadir los títulos");
                    //Se añaden los títulos de los elementos que se van a insertar en las hojas
                    for (SXSSFSheet hoja : hojas) {
                        Row row = hoja.createRow(0);
                        int cellnum = 0;
                        Cell cell = row.createCell(cellnum++);
                        cell.setCellValue("Identificador");
                        cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue("Link licitación");
                        cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue("Fecha actualización");
                        cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                        if (hoja.getSheetName().compareTo(SpreeadSheetManager.LICITACIONES) == 0) {
                            cell = row.createCell(cellnum++);
                            cell.setCellValue("Vigente/Anulada/Archivada");
                            cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                            for (DatosSeleccionables dato : buscadorDatosSeleecionables) {
                                cell = row.createCell(cellnum++);
                                cell.setCellValue((String) dato.getTiulo());
                                cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                            }
                            if (rbUnaTabla.isSelected()) {
                                for (DatosResultados dato : buscadorDatosResultados) {
                                    cell = row.createCell(cellnum++);
                                    cell.setCellValue((String) dato.getTiulo());
                                    cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                                }
                            }
                        } else {
                            for (DatosResultados dato : buscadorDatosResultados) {
                                cell = row.createCell(cellnum++);
                                cell.setCellValue((String) dato.getTiulo());
                                cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                            }
                        }
                    }
                    //Se cambian los tamaños de las columnas
                    spreeadSheetManager.updateColumnsSize();
                    logger.info("Títulos añadidos");

                    // Se comprueba que exista el ficheroRISP a procesar
                    File ficheroRISP = new File(textFieldDirOrigen.getText());
                    String directorioPath = ficheroRISP.getParent();
                    boolean existeFicheroRisp = ficheroRISP.exists() && ficheroRISP.isFile();

                    if (existeFicheroRisp) {
                        logger.info("Directorio originen de ficheros RISP-PLACSP: " + directorioPath);
                        logger.info("Fichero ráiz: " + ficheroRISP.getName());
                    } else {
                        logger.error("No se puede acceder al fichero " + textFieldDirOrigen.getText());
                    }

                    File[] lista_ficherosRISP = ficheroRISP.getParentFile().listFiles();
                    logger.info("Número previsto de ficheros a procesar: " + lista_ficherosRISP.length);

                    // calculo de cada salto
                    double saltoBar = 1.00 / lista_ficherosRISP.length;
                    double saltoAcumuladoBar = 0;

                    while (existeFicheroRisp) {
                        logger.info("Procesando fichero: " + ficheroRISP.getName());

                        saltoAcumuladoBar += saltoBar;
                        updateProgress(saltoAcumuladoBar, 1);
                        logger.info("Ratio de archivos procesados: " + saltoAcumuladoBar * 100.00 + " %");

                        res = null;
                        inStream = new InputStreamReader(new FileInputStream(ficheroRISP), StandardCharsets.UTF_8);
                        res = ((JAXBElement<FeedType>) atomUnMarshaller.unmarshal(inStream)).getValue();

                        // Se añaden las licitaciones que han dejado de ser válidas
                        if (res.getAny() != null) {
                            for (int indice = 0; indice < res.getAny().size(); indice++) {
                                DeletedEntryType deletedEntry = ((JAXBElement<DeletedEntryType>) res.getAny()
                                        .get(indice)).getValue();
                                if (!entriesDeleted.containsKey(deletedEntry.getRef())) {
                                    entriesDeleted.put(deletedEntry.getRef(), deletedEntry.getWhen().toGregorianCalendar());
                                }
                            }
                        }

                        // Se recorren las licitaciones (elementos entry)
                        numeroEntries += res.getEntry().size();
                        for (EntryType entry : res.getEntry()) {
                            // Se comprueba si ya se ha procesado una entry con el mismo identoficador y que
                            // es más reciente
                            if (!entriesProcesadas.contains(entry.getId().getValue())) {
                                // Se comprueba si se encuentra en la la lista de licitaciones Deleted
                                GregorianCalendar fechaDeleted = null;
                                if (entriesDeleted.containsKey(entry.getId().getValue())) {
                                    fechaDeleted = entriesDeleted.get(entry.getId().getValue());
                                }

                                if (rbDosTablas.isSelected()) {
                                    //La salida es en dos tablas
                                    procesarEntry(entry, sheet, fechaDeleted, buscadorDatosSeleecionables);
                                    procesarEntryResultados(entry, sheetResultados, fechaDeleted, buscadorDatosResultados);
                                } else {
                                    //La salida es en una tabla
                                    procesarEntryCompleta(entry, sheet, fechaDeleted, buscadorDatosSeleecionables, buscadorDatosResultados);
                                }

                                entriesProcesadas.add(entry.getId().getValue());
                            }
                        }
                        // se comprueba cuál es el siguiente fichero a procesar
                        for (LinkType linkType : res.getLink()) {
                            existeFicheroRisp = false;
                            if (linkType.getRel().toLowerCase().compareTo("next") == 0) {
                                String[] tempArray = linkType.getHref().split("/");
                                String nombreSiguienteRIPS = tempArray[tempArray.length - 1];
                                ficheroRISP = new File(directorioPath + "/" + nombreSiguienteRIPS);
                                existeFicheroRisp = ficheroRISP.exists() && ficheroRISP.isFile();
                            }
                        }
                        inStream.close();
                        numeroFicherosProcesados++;
                    }

                    logger.info("Creando el fichero " + textFieldOutputFile.getText());
                    logger.info("Número de ficheros procesados " + numeroFicherosProcesados);
                    logger.info("Número de elementos entry existentes: " + numeroEntries);
                    logger.info("Licitaciones insertadas en el fichero: " + entriesProcesadas.size());

                    spreeadSheetManager.insertarFiltro(buscadorDatosSeleecionables.size(), buscadorDatosResultadosObj.size());

                    logger.info("Comienzo de escritura del fichero de salida");
                    spreeadSheetManager.getWorkbook().write(output_file); // write excel document to output stream
                    output_file.close(); // close the file
                    spreeadSheetManager.getWorkbook().close();
                    // para mostrar algunos resultados en la interfaz de usuario
                    n_licitaciones.setText(Integer.toString(entriesProcesadas.size()));
                    n_ficheros.setText(Integer.toString(numeroFicherosProcesados));

                    logger.info("Fin del proceso de generación del fichero");

                    Platform.runLater(() -> {
                        aError.setAlertType(AlertType.INFORMATION);
                        aError.setHeaderText(null);
                        aError.setContentText("El proceso de generación del fichero ha terminado con éxito");
                        aError.show();
                    });

                } catch (JAXBException e) {// ventanas de error para las excepciones contempladas
                    String auxError = "Error al procesar el fichero ATOM. No se puede continuar con el proceso.";
                    logger.error(auxError);
                    logger.debug(e.getStackTrace());
                    Platform.runLater(() -> {
                        aError.setAlertType(AlertType.ERROR);
                        aError.setHeaderText(null);
                        aError.setContentText(auxError);
                        aError.show();
                    });
                } catch (FileNotFoundException e) {
                    String auxError = "Error al generar el fichero de salida. No se pudo crear un fichero en la ruta indicada o no pudo ser abierto por alguna otra razón.";
                    logger.error(auxError);
                    logger.debug(e.toString());
                    Platform.runLater(() -> {
                        aError.setAlertType(AlertType.ERROR);
                        aError.setHeaderText(null);
                        aError.setContentText(auxError);
                        aError.show();
                    });
                } catch (Exception e) {
                    // error inesperado
                    String auxError = "Error inesperado, revise la configuración y el log...";
                    e.printStackTrace();
                    logger.error(auxError);
                    logger.debug(e.getStackTrace());
                    logger.debug(e.getMessage());
                    Platform.runLater(() -> {
                        aError.setAlertType(AlertType.ERROR);
                        aError.setHeaderText(null);
                        aError.setContentText(auxError);
                        aError.show();
                    });
                } finally {
                    return true;
                }
            }

        };
    }*/
    /**
     * Función paara procesr una entry y extraer todos sus datos.
     *
     * @param entry
     * @param sheet
     */
    private void procesarEntry(EntryType entry, SXSSFSheet sheet, GregorianCalendar fechaDeleted, ArrayList<DatosSeleccionables> buscadorDatosSeleecionables) {
        Cell cell;
        ContractFolderStatusType contractFolder = ((JAXBElement<ContractFolderStatusType>) entry.getAny().get(0)).getValue();

        Row row = sheet.createRow(sheet.getLastRowNum() + 1);

        //Se obtienen los datos de la licitación
        int cellnum = 0;

        //Se añaden los datos básicos y obligatorios de la entry: id, lin y updated
        cell = row.createCell(cellnum++);
        cell.setCellValue(entry.getId().getValue().substring(entry.getId().getValue().lastIndexOf("/") + 1));
        cell = row.createCell(cellnum++);
        cell.setCellValue(entry.getLink().get(0).getHref());

        //Se obtiene la fechaUpdated de la entry
        GregorianCalendar updated = entry.getUpdated().getValue().toGregorianCalendar();

        //La fecha de actualización será la más recinte comparando la última entry con la fecha deleted si existe.
        if (fechaDeleted == null || fechaDeleted.compareTo(updated) < 0) {
            //La entry es válida, no hay un deleted-entry posterior
            cell = row.createCell(cellnum++);
            cell.setCellValue((LocalDateTime) entry.getUpdated().getValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime());
            cell.setCellStyle(SpreeadSheetManager.getCellStyleFechaLarga());
            cell = row.createCell(cellnum++);
            cell.setCellValue("VIGENTE");
        } else {
            //La entry no es válida, hay un deleted-entry posterior
            cell = row.createCell(cellnum++);
            cell.setCellValue((LocalDateTime) fechaDeleted.toZonedDateTime().toLocalDateTime());
            cell.setCellStyle(SpreeadSheetManager.getCellStyleFechaLarga());
            cell = row.createCell(cellnum++);
            if (((fechaDeleted.getTimeInMillis() - updated.getTimeInMillis()) / 1000 / 3660 / 24 / 365) > 5) {
                cell.setCellValue("ARCHIVADA");
            } else {
                cell.setCellValue("ANULADA");
            }
        }

        for (DatosSeleccionables dato : buscadorDatosSeleecionables) {
            Object datoCodice = dato.valorCodice(contractFolder);
            cell = row.createCell(cellnum++);
            if (datoCodice instanceof BigDecimal) {
                cell.setCellValue((double) ((BigDecimal) datoCodice).doubleValue());
            } else if (datoCodice instanceof String) {
                cell.setCellValue((String) datoCodice);
                //String datoCodiceUTF8 = (String) datoCodice;
                //System.out.println("");
                //cell.setCellValue(new String(datoCodiceUTF8.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            } else if (datoCodice instanceof GregorianCalendar) {
                cell.setCellValue((LocalDateTime) ((GregorianCalendar) datoCodice).toZonedDateTime().toLocalDateTime());
            } else if (datoCodice instanceof Boolean) {
                cell.setCellValue((Boolean) datoCodice);
            }
            cell.setCellStyle(SpreeadSheetManager.getCellStyleFormato(dato.getFormato()));

        }

    }

    private void procesarEntryResultados(EntryType entry, SXSSFSheet sheet, GregorianCalendar fechaDeleted, ArrayList<DatosResultados> buscadorDatosResultados) {
        Cell cell;

        ContractFolderStatusType contractFolder = ((JAXBElement<ContractFolderStatusType>) entry.getAny().get(0)).getValue();

        //Se obtiene el número de elementos tenderResult, en caso de que tenga,
        if (contractFolder.getTenderResult() != null) {
            for (int indice = 0; indice < contractFolder.getTenderResult().size(); indice++) {
                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                int cellnum = 0;

                //Se añaden los datos básicos y obligatorios de la entry: id, link y vigencia
                cell = row.createCell(cellnum++);
                cell.setCellValue(entry.getId().getValue().substring(entry.getId().getValue().lastIndexOf("/") + 1));
                cell = row.createCell(cellnum++);
                cell.setCellValue(entry.getLink().get(0).getHref());

                //Se obtiene la fechaUpdated de la entry
                GregorianCalendar updated = entry.getUpdated().getValue().toGregorianCalendar();

                //La fecha de actualización será la más recinte comparando la última entry con la fecha deleted si existe.
                if (fechaDeleted == null || fechaDeleted.compareTo(updated) < 0) {
                    //La entry es válida, no hay un deleted-entry posterior
                    cell = row.createCell(cellnum++);
                    cell.setCellValue((LocalDateTime) entry.getUpdated().getValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime());
                    cell.setCellStyle(SpreeadSheetManager.getCellStyleFechaLarga());
                } else {
                    //La entry no es válida, hay un deleted-entry posterior
                    cell = row.createCell(cellnum++);
                    cell.setCellValue((LocalDateTime) fechaDeleted.toZonedDateTime().toLocalDateTime());
                    cell.setCellStyle(SpreeadSheetManager.getCellStyleFechaLarga());
                }

                for (DatosResultados dato : buscadorDatosResultados) {
                    Object datoCodice = dato.valorCodice(contractFolder, indice);
                    cell = row.createCell(cellnum++);
                    if (datoCodice instanceof BigDecimal) {
                        cell.setCellValue((double) ((BigDecimal) datoCodice).doubleValue());
                    } else if (datoCodice instanceof String) {
                        cell.setCellValue((String) datoCodice);
                    } else if (datoCodice instanceof GregorianCalendar) {
                        cell.setCellValue((LocalDateTime) ((GregorianCalendar) datoCodice).toZonedDateTime().toLocalDateTime());
                    } else if (datoCodice instanceof Boolean) {
                        cell.setCellValue((Boolean) datoCodice);
                    }
                    cell.setCellStyle(SpreeadSheetManager.getCellStyleFormato(dato.getFormato()));
                }

            }
        }
    }

    /**
     * Se añaden los datos en una única hoja. Los "datos seleccionables" se
     * repiten por cada resultado. Si no hay resultados, se insertan una única
     * vez
     *
     * @param entry
     * @param sheet
     * @param fechaDeleted
     * @param buscadorDatosSeleecionables
     * @param buscadorDatosResultados
     */
    private void procesarEntryCompleta(EntryType entry, SXSSFSheet sheet, GregorianCalendar fechaDeleted,
            ArrayList<DatosSeleccionables> buscadorDatosSeleccionables,
            ArrayList<DatosResultados> buscadorDatosResultados) {

        Cell cell;
        ContractFolderStatusType contractFolder = ((JAXBElement<ContractFolderStatusType>) entry.getAny().get(0)).getValue();

        //Se obtiene el número de elementos tenderResult, en caso de que tenga,
        if (contractFolder.getTenderResult().size() > 0) {
            //hay resultados en esta entry, por lo que se insertan tantas filas como resultados

            for (int indice = 0; indice < contractFolder.getTenderResult().size(); indice++) {
                //Se insertan los datos comunes
                procesarEntry(entry, sheet, fechaDeleted, buscadorDatosSeleccionables);

                //En la misma fila, se completan con los datos del tenderresult
                Row row = sheet.getRow(sheet.getLastRowNum());
                int cellnum = buscadorDatosSeleccionables.size() + 4;

                for (DatosResultados dato : buscadorDatosResultados) {
                    Object datoCodice = dato.valorCodice(contractFolder, indice);
                    cell = row.createCell(cellnum++);
                    if (datoCodice instanceof BigDecimal) {
                        cell.setCellValue((double) ((BigDecimal) datoCodice).doubleValue());
                    } else if (datoCodice instanceof String) {
                        cell.setCellValue((String) datoCodice);
                    } else if (datoCodice instanceof GregorianCalendar) {
                        cell.setCellValue((LocalDateTime) ((GregorianCalendar) datoCodice).toZonedDateTime().toLocalDateTime());
                    } else if (datoCodice instanceof Boolean) {
                        cell.setCellValue((Boolean) datoCodice);
                    }
                    cell.setCellStyle(SpreeadSheetManager.getCellStyleFormato(dato.getFormato()));
                }
            }
        } else {
            //No hay resultados en esta entry, solo se inserta una única vez
            procesarEntry(entry, sheet, fechaDeleted, buscadorDatosSeleccionables);
        }
    }


    /*
	 * inputTree se le pasa el nodo raíz correspondiente a la hoja de calculo del excel de salida (DatosSeleccionables o DatosResultados)
	 * queDatos es el índice de dichonodo respecto a la raíz añadida al treeView, sirve para diferenciar en qué Enumerado estamos
	 * tiene el mismo orden que se ha insertado en la raíz de treeView
	 * se debería generalizar mejor, ¿quizás lanzar una excepción si el entero no tiene los valores adecuados cuando se llama a este método?
     */
    private ArrayList<Object> collectCheckedBoxes(CheckBoxTreeItem<String> inputTree, int queDatos) {

        ArrayList<Object> resultado = new ArrayList<>(inputTree.getChildren().size());

        //tantos arrays intermedios como hojas haya + 1 del CheckBoxTreeItem original
        //esto se hace así porque esta clase no es Iterable y no se puede recorrer directamente, necesitamos un salto intermedio
        ArrayList<DatosSeleccionables> intermedioSeleccionables = new ArrayList<DatosSeleccionables>(inputTree.getChildren().size());
        ArrayList<DatosResultados> intermedioResultados = new ArrayList<DatosResultados>(inputTree.getChildren().size());
        ArrayList<CheckBoxTreeItem<String>> auxItermediateIterator = new ArrayList<CheckBoxTreeItem<String>>();

        //el salto intermedio que se ha mencionado antes
        findCheckedBoxes((CheckBoxTreeItem<String>) inputTree, auxItermediateIterator);

        //quizás se poría obviar el uso de 1 for por cada Enum si echamos directamente los seleccionados sin 
        //comprobar que están dentro del enumerado, pero así nos evitamos errores, probar quizás para versiones futuras
        //en el caso de que tengamos varias hojas de excel y se empiece a volver inmanejable
        for (CheckBoxTreeItem<String> auxBox : auxItermediateIterator) {
            if (queDatos == 0) { //primera plantilla
                for (DatosSeleccionables auxDS : DatosSeleccionables.values()) {
                    if (auxBox.getValue().equals(auxDS.getTiulo())) {
                        intermedioSeleccionables.add(auxDS);
                    }
                }
            } else {
                for (DatosResultados auxDS : DatosResultados.values()) {
                    if (auxBox.getValue().equals(auxDS.getTiulo())) {
                        intermedioResultados.add(auxDS);
                    }
                }
            }
        }
        if (queDatos == 0) {
            resultado.addAll(intermedioSeleccionables);
        } else {
            resultado.addAll(intermedioResultados);
        }
        return resultado;
    }

    //funcion auxiliar para recoger los checkboxes seleccionados
    private void findCheckedBoxes(CheckBoxTreeItem<String> item, ArrayList<CheckBoxTreeItem<String>> checkedItems) {
        /*
        //DSG 
        if (item.isSelected()) {
            checkedItems.add(item);
        }
         */
        checkedItems.add(item);
        for (TreeItem<String> child : item.getChildren()) {
            findCheckedBoxes((CheckBoxTreeItem<String>) child, checkedItems);
        }
    }

    @FXML
    public void generarXLSX(File fileInput, File fileOutput, String rutaPlantillaExcel) throws Exception {
        /*
        progreso.progressProperty().unbind();
	progreso.setProgress(0);
        Task<Boolean> process = procesarDirectorio();
        progreso.progressProperty().unbind();
        progreso.progressProperty().bind(process.progressProperty());
        buttonGenerar.disableProperty().bind(process.runningProperty());
        new Thread(process).start();*/
        File file = new File(MyOpenPLACSP.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath());
        //userDirectory tendrá la dirección desde la que se está ejecutando:
        //  el jar C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\dist
        //  la compilación  C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\build
        String userDirectory = file.getParent();

        // Collección que registra las entries que ya han sido procesadas
        HashSet<String> entriesProcesadas = new HashSet<String>();
        HashMap<String, GregorianCalendar> entriesDeleted = new HashMap<String, GregorianCalendar>();
        int numeroEntries = 0;
        int numeroFicherosProcesados = 0;
        FeedType res = null;
        FileOutputStream output_file = null;
        InputStreamReader inStream = null;
        File messageLog = new File(userDirectory + "/message-MyOpenPLACSP.txt");
        File debugLog = new File(userDirectory + "/debug-MyOpenPLACSP.txt");
        File errorLog = new File(userDirectory + "/error-MyOpenPLACSP.txt");
        RegistrarAccion reg = new RegistrarAccion();

        try {

            //Se crea el Stream de salida en el path indicado                                                    
            output_file = new FileOutputStream(fileOutput);

            // Create the JAXBContext
            JAXBContext jc = JAXBContext.newInstance(
                    "org.w3._2005.atom:org.dgpe.codice.common.caclib:org.dgpe.codice.common.cbclib:ext.place.codice.common.caclib:ext.place.codice.common.cbclib:org.purl.atompub.tombstones._1");
            atomUnMarshaller = jc.createUnmarshaller();

            SpreeadSheetManager spreeadSheetManager = null;
            //por defecto se pone a true para que genere la pestaña Licitaciones y Resultado
            spreeadSheetManager = new SpreeadSheetManager(true, rutaPlantillaExcel);

            ArrayList<SXSSFSheet> hojas = new ArrayList<SXSSFSheet>();
            SXSSFSheet sheet = (SXSSFSheet) spreeadSheetManager.getWorkbook().getSheet(SpreeadSheetManager.LICITACIONES);
            SXSSFSheet sheetResultados = null;
            hojas.add(sheet);

            sheetResultados = (SXSSFSheet) spreeadSheetManager.getWorkbook().getSheet(SpreeadSheetManager.RESULTADOS);
            hojas.add(sheetResultados);

            reg.registrarAccion(messageLog, "Comienza el procesamiento de los ficheros");
            // para recoger las opciones seleccionadas por el usuario
            // atentos a los castings
            // también a los índices tanto del método (queDatos) como del get del nodo,
            // indican la hoja del excel
            // segun el orden de la disposición gráfica
            // bastante dependencia si se añaden o quitan más enumerados
            // repensar si en el futuro se contemplan muchas hojas generadas en el Excel que
            // deban tener sus agrupaciones respectivas
            //GenericodeTypes
            rootItem = new CheckBoxTreeItem<>("");
            rootItem.getChildren().addAll(new CheckBoxTreeItem<>("Datos Generales"), new CheckBoxTreeItem<>("Datos de Resultados"));
            //Elementos / columnas de "Datos Generales"
            rootItem.getChildren().get(0).getChildren().addAll(
                    new CheckBoxTreeItem<>("Primera publicación"),
                    new CheckBoxTreeItem<>("Estado"),
                    new CheckBoxTreeItem<>("Número de expediente"),
                    new CheckBoxTreeItem<>("Objeto del Contrato"),
                    new CheckBoxTreeItem<>("Valor estimado del contrato"),
                    new CheckBoxTreeItem<>("Presupuesto base sin impuestos"),
                    new CheckBoxTreeItem<>("Presupuesto base con impuestos"),
                    new CheckBoxTreeItem<>("CPV"),
                    new CheckBoxTreeItem<>("Tipo de contrato"),
                    new CheckBoxTreeItem<>("Lugar de ejecución"),
                    new CheckBoxTreeItem<>("Órgano de Contratación"),
                    new CheckBoxTreeItem<>("ID OC en PLACSP"),
                    new CheckBoxTreeItem<>("NIF OC"),
                    new CheckBoxTreeItem<>("DIR3"),
                    new CheckBoxTreeItem<>("Enlace al Perfil de Contratante del OC"),
                    new CheckBoxTreeItem<>("Tipo de procedimiento"),
                    new CheckBoxTreeItem<>("Sistema de contratación"),
                    new CheckBoxTreeItem<>("Tramitación"),
                    new CheckBoxTreeItem<>("Forma de presentación de la oferta"),
                    new CheckBoxTreeItem<>("Fecha de presentación de ofertas"),
                    new CheckBoxTreeItem<>("Fecha de presentación de solicitudes de participacion"),
                    new CheckBoxTreeItem<>("Directiva de la aplicación"),
                    new CheckBoxTreeItem<>("Financiación Europea y fuente"),
                    new CheckBoxTreeItem<>("Subcontratación permitida"),
                    new CheckBoxTreeItem<>("Subcontratación permitida porcentaje"));
            //Elementos / columnas de "Datos de Resultados"
            rootItem.getChildren().get(1).getChildren().addAll(
                    new CheckBoxTreeItem<>("Número de expediente"),
                    new CheckBoxTreeItem<>("Lote"),
                    new CheckBoxTreeItem<>("Objeto licitación/lote"),
                    new CheckBoxTreeItem<>("Presupuesto base con impuestos licitación/lote"),
                    new CheckBoxTreeItem<>("Presupuesto base sin impuestos licitación/lote"),
                    new CheckBoxTreeItem<>("CPV licitación/lote"),
                    new CheckBoxTreeItem<>("Lugar ejecución licitación/lote"),
                    new CheckBoxTreeItem<>("Resultado licitación/lote"),
                    new CheckBoxTreeItem<>("Fecha del acuerdo licitación/lote"),
                    new CheckBoxTreeItem<>("Número de ofertas recibidas por licitación/lote"),
                    new CheckBoxTreeItem<>("Precio de la oferta más baja por licitación/lote"),
                    new CheckBoxTreeItem<>("Precio de la oferta más alta por licitación/lote"),
                    new CheckBoxTreeItem<>("Se han excluído ofertas por ser anormalmente bajas por licitación/lote"),
                    new CheckBoxTreeItem<>("Número del contrato licitación/lote"),
                    new CheckBoxTreeItem<>("Fecha formalización del contrato licitación/lote"),
                    new CheckBoxTreeItem<>("Fecha entrada en vigor del contrato de licitación/lote"),
                    new CheckBoxTreeItem<>("Adjudicatario licitación/lote"),
                    new CheckBoxTreeItem<>("Tipo de identificador de adjudicatario por licitación/lote"),
                    new CheckBoxTreeItem<>("Identificador Adjudicatario de la licitación/lote"),
                    new CheckBoxTreeItem<>("El adjudicatario es o no PYME de la licitación/lote"),
                    new CheckBoxTreeItem<>("Importe adjudicación sin impuestos licitación/lote"),
                    new CheckBoxTreeItem<>("Importe adjudicación con impuestos licitación/lote"));

            ArrayList<Object> buscadorDatosSeleecionablesObj = collectCheckedBoxes(
                    (CheckBoxTreeItem<String>) rootItem.getChildren().get(0), 0);
            ArrayList<DatosSeleccionables> buscadorDatosSeleecionables = new ArrayList<DatosSeleccionables>(
                    buscadorDatosSeleecionablesObj.size());
            for (Object auxD : buscadorDatosSeleecionablesObj) {
                buscadorDatosSeleecionables.add((DatosSeleccionables) auxD); // casting del Objeto al enumerado                
                // correspondiente
            }
            // en este caso el indice es 1 en lugar de 0 por corresponderse al enumerado de
            // DatosResultados
            ArrayList<Object> buscadorDatosResultadosObj = collectCheckedBoxes(
                    (CheckBoxTreeItem<String>) rootItem.getChildren().get(1), 1);
            ArrayList<DatosResultados> buscadorDatosResultados = new ArrayList<DatosResultados>(
                    buscadorDatosResultadosObj.size());
            for (Object auxD : buscadorDatosResultadosObj) {
                buscadorDatosResultados.add((DatosResultados) auxD);
            }

            reg.registrarAccion(debugLog, "Se comienzan a añadir los títulos");
            //Se añaden los títulos de los elementos que se van a insertar en las hojas
            for (SXSSFSheet hoja : hojas) {
                Row row = hoja.createRow(0);
                int cellnum = 0;
                Cell cell = row.createCell(cellnum++);
                cell.setCellValue("Identificador");
                cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                cell = row.createCell(cellnum++);
                cell.setCellValue("Link licitación");
                cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                cell = row.createCell(cellnum++);
                cell.setCellValue("Fecha actualización");
                cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                if (hoja.getSheetName().compareTo(SpreeadSheetManager.LICITACIONES) == 0) {
                    cell = row.createCell(cellnum++);
                    cell.setCellValue("Vigente/Anulada/Archivada");
                    cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                    for (DatosSeleccionables dato : buscadorDatosSeleecionables) {
                        cell = row.createCell(cellnum++);
                        cell.setCellValue((String) dato.getTiulo());
                        cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                    }
                } else {
                    for (DatosResultados dato : buscadorDatosResultados) {
                        cell = row.createCell(cellnum++);
                        cell.setCellValue((String) dato.getTiulo());
                        cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                    }
                }
            }
            //Se cambian los tamaños de las columnas
            spreeadSheetManager.updateColumnsSize();
            reg.registrarAccion(messageLog, "Títulos añadidos");

            // Se comprueba que exista el ficheroRISP a procesar
            //DSG File ficheroRISP = new File(textFieldDirOrigen.getText());
            File ficheroRISP = fileInput;

            String directorioPath = ficheroRISP.getParent();
            boolean existeFicheroRisp = ficheroRISP.exists() && ficheroRISP.isFile();

            if (existeFicheroRisp) {
                reg.registrarAccion(messageLog, "Directorio originen de ficheros RISP-PLACSP: " + directorioPath);
                reg.registrarAccion(messageLog, "Fichero ráiz: " + ficheroRISP.getName());
            } else {
                //DSG logger.error("No se puede acceder al fichero " + textFieldDirOrigen.getText());
                reg.registrarAccion(errorLog, "No se puede acceder al fichero " + fileInput.getAbsolutePath());
            }

            File[] lista_ficherosRISP = ficheroRISP.getParentFile().listFiles();
            reg.registrarAccion(messageLog, "Número previsto de ficheros a procesar: " + lista_ficherosRISP.length);

            // calculo de cada salto
            double saltoBar = 1.00 / lista_ficherosRISP.length;
            double saltoAcumuladoBar = 0;

            while (existeFicheroRisp) {
                reg.registrarAccion(messageLog, "Procesando fichero: " + ficheroRISP.getName());

                saltoAcumuladoBar += saltoBar;
                //DSG updateProgress(saltoAcumuladoBar, 1);
                reg.registrarAccion(messageLog, "Ratio de archivos procesados: " + saltoAcumuladoBar * 100.00 + " %");

                res = null;
                inStream = new InputStreamReader(new FileInputStream(ficheroRISP), StandardCharsets.UTF_8);
                res = ((JAXBElement<FeedType>) atomUnMarshaller.unmarshal(inStream)).getValue();

                // Se añaden las licitaciones que han dejado de ser válidas
                if (res.getAny() != null) {
                    for (int indice = 0; indice < res.getAny().size(); indice++) {
                        DeletedEntryType deletedEntry = ((JAXBElement<DeletedEntryType>) res.getAny()
                                .get(indice)).getValue();
                        if (!entriesDeleted.containsKey(deletedEntry.getRef())) {
                            entriesDeleted.put(deletedEntry.getRef(), deletedEntry.getWhen().toGregorianCalendar());
                        }
                    }
                }

                // Se recorren las licitaciones (elementos entry)
                numeroEntries += res.getEntry().size();
                for (EntryType entry : res.getEntry()) {
                    // Se comprueba si ya se ha procesado una entry con el mismo identoficador y que
                    // es más reciente
                    if (!entriesProcesadas.contains(entry.getId().getValue())) {
                        // Se comprueba si se encuentra en la la lista de licitaciones Deleted
                        GregorianCalendar fechaDeleted = null;
                        if (entriesDeleted.containsKey(entry.getId().getValue())) {
                            fechaDeleted = entriesDeleted.get(entry.getId().getValue());
                        }

                        //La salida es en dos tablas
                        procesarEntry(entry, sheet, fechaDeleted, buscadorDatosSeleecionables);
                        procesarEntryResultados(entry, sheetResultados, fechaDeleted, buscadorDatosResultados);

                        entriesProcesadas.add(entry.getId().getValue());
                    }
                }
                // se comprueba cuál es el siguiente fichero a procesar
                for (LinkType linkType : res.getLink()) {
                    existeFicheroRisp = false;
                    if (linkType.getRel().toLowerCase().compareTo("next") == 0) {
                        String[] tempArray = linkType.getHref().split("/");
                        String nombreSiguienteRIPS = tempArray[tempArray.length - 1];
                        ficheroRISP = new File(directorioPath + "/" + nombreSiguienteRIPS);
                        existeFicheroRisp = ficheroRISP.exists() && ficheroRISP.isFile();
                    }
                }
                inStream.close();
                numeroFicherosProcesados++;
            }

            //DSG logger.info("Creando el fichero " + textFieldOutputFile.getText());
            reg.registrarAccion(messageLog, "Creando el fichero " + fileInput.getAbsolutePath());
            reg.registrarAccion(messageLog, "Número de ficheros procesados " + numeroFicherosProcesados);
            reg.registrarAccion(messageLog, "Número de elementos entry existentes: " + numeroEntries);
            reg.registrarAccion(messageLog, "Licitaciones insertadas en el fichero: " + entriesProcesadas.size());

            spreeadSheetManager.insertarFiltro(buscadorDatosSeleecionables.size(), buscadorDatosResultadosObj.size());

            reg.registrarAccion(messageLog, "Comienzo de escritura del fichero de salida");
            spreeadSheetManager.getWorkbook().write(output_file); // write excel document to output stream
            output_file.close(); // close the file
            spreeadSheetManager.getWorkbook().close();

            reg.registrarAccion(messageLog, "Fin del proceso de generación del fichero");

        } catch (JAXBException e) {// ventanas de error para las excepciones contempladas
            String auxError = "Error al procesar el fichero ATOM. No se puede continuar con el proceso.";
            reg.registrarAccion(errorLog, auxError);
            reg.registrarAccion(debugLog, e.getStackTrace().toString());

        } catch (FileNotFoundException e) {
            String auxError = "Error al generar el fichero de salida. No se pudo crear un fichero en la ruta indicada o no pudo ser abierto por alguna otra razón.";
            reg.registrarAccion(errorLog, auxError);
            reg.registrarAccion(debugLog, e.getStackTrace().toString());
        } catch (Exception e) {
            // error inesperado
            String auxError = "Error inesperado, revise la configuración y el log...";
            reg.registrarAccion(errorLog, auxError);
            reg.registrarAccion(debugLog, e.getStackTrace().toString());
            reg.registrarAccion(debugLog, e.getMessage());
        }
    }
}
