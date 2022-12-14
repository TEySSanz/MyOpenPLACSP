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
	 * initialize se ejecuta despu??s del constructor y sirve para la carga de datos din??mica, en este caso, del TreeView con las variables de los datos
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        /*
            this.rootItem = new CheckBoxTreeItem<String> ("Datos de la licitaci??n");
		rootItem.setExpanded(true);
		this.treeDatos.setEditable(true);
		this.treeDatos.setCellFactory(CheckBoxTreeCell.<String>forTreeView());

		//primer nodo para datos de licitaciones y segundo para los resultados
		//se cotempla la agrupaci??n de variables en el ??rbol por las hojas del excel generado
		//el orden de a??adido es importante para luego su acceso, concuerda con el par??metro queDato de collectCheckedBoxes
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

		//treeDatos ya se deber??a haber creado porque lo tenemos pintado en el parser, cuidado
		//this.treeDatos = new TreeView<String> (rootItem);
		this.treeDatos.setRoot(rootItem);
		this.treeDatos.setShowRoot(true);

		//no deber??a hacer falta apriori, pero as?? forzamos y nos aseguramos de que salgan las variables como opciones
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

    //la direncia principal con el selectDirIn es el showSaveDialog en lugar de showOpenDialog y la restricci??n de extensi??n
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

                // Collecci??n que registra las entries que ya han sido procesadas
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
                    // tambi??n a los ??ndices tanto del m??todo (queDatos) como del get del nodo,
                    // indican la hoja del excel
                    // segun el orden de la disposici??n gr??fica
                    // bastante dependencia si se a??aden o quitan m??s enumerados
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

                    logger.debug("Se comienzan a a??adir los t??tulos");
                    //Se a??aden los t??tulos de los elementos que se van a insertar en las hojas
                    for (SXSSFSheet hoja : hojas) {
                        Row row = hoja.createRow(0);
                        int cellnum = 0;
                        Cell cell = row.createCell(cellnum++);
                        cell.setCellValue("Identificador");
                        cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue("Link licitaci??n");
                        cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue("Fecha actualizaci??n");
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
                    //Se cambian los tama??os de las columnas
                    spreeadSheetManager.updateColumnsSize();
                    logger.info("T??tulos a??adidos");

                    // Se comprueba que exista el ficheroRISP a procesar
                    File ficheroRISP = new File(textFieldDirOrigen.getText());
                    String directorioPath = ficheroRISP.getParent();
                    boolean existeFicheroRisp = ficheroRISP.exists() && ficheroRISP.isFile();

                    if (existeFicheroRisp) {
                        logger.info("Directorio originen de ficheros RISP-PLACSP: " + directorioPath);
                        logger.info("Fichero r??iz: " + ficheroRISP.getName());
                    } else {
                        logger.error("No se puede acceder al fichero " + textFieldDirOrigen.getText());
                    }

                    File[] lista_ficherosRISP = ficheroRISP.getParentFile().listFiles();
                    logger.info("N??mero previsto de ficheros a procesar: " + lista_ficherosRISP.length);

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

                        // Se a??aden las licitaciones que han dejado de ser v??lidas
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
                            // es m??s reciente
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
                        // se comprueba cu??l es el siguiente fichero a procesar
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
                    logger.info("N??mero de ficheros procesados " + numeroFicherosProcesados);
                    logger.info("N??mero de elementos entry existentes: " + numeroEntries);
                    logger.info("Licitaciones insertadas en el fichero: " + entriesProcesadas.size());

                    spreeadSheetManager.insertarFiltro(buscadorDatosSeleecionables.size(), buscadorDatosResultadosObj.size());

                    logger.info("Comienzo de escritura del fichero de salida");
                    spreeadSheetManager.getWorkbook().write(output_file); // write excel document to output stream
                    output_file.close(); // close the file
                    spreeadSheetManager.getWorkbook().close();
                    // para mostrar algunos resultados en la interfaz de usuario
                    n_licitaciones.setText(Integer.toString(entriesProcesadas.size()));
                    n_ficheros.setText(Integer.toString(numeroFicherosProcesados));

                    logger.info("Fin del proceso de generaci??n del fichero");

                    Platform.runLater(() -> {
                        aError.setAlertType(AlertType.INFORMATION);
                        aError.setHeaderText(null);
                        aError.setContentText("El proceso de generaci??n del fichero ha terminado con ??xito");
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
                    String auxError = "Error al generar el fichero de salida. No se pudo crear un fichero en la ruta indicada o no pudo ser abierto por alguna otra raz??n.";
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
                    String auxError = "Error inesperado, revise la configuraci??n y el log...";
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
     * Funci??n paara procesr una entry y extraer todos sus datos.
     *
     * @param entry
     * @param sheet
     */
    private void procesarEntry(EntryType entry, SXSSFSheet sheet, GregorianCalendar fechaDeleted, ArrayList<DatosSeleccionables> buscadorDatosSeleecionables) {
        Cell cell;
        ContractFolderStatusType contractFolder = ((JAXBElement<ContractFolderStatusType>) entry.getAny().get(0)).getValue();

        Row row = sheet.createRow(sheet.getLastRowNum() + 1);

        //Se obtienen los datos de la licitaci??n
        int cellnum = 0;

        //Se a??aden los datos b??sicos y obligatorios de la entry: id, lin y updated
        cell = row.createCell(cellnum++);
        cell.setCellValue(entry.getId().getValue().substring(entry.getId().getValue().lastIndexOf("/") + 1));
        cell = row.createCell(cellnum++);
        cell.setCellValue(entry.getLink().get(0).getHref());

        //Se obtiene la fechaUpdated de la entry
        GregorianCalendar updated = entry.getUpdated().getValue().toGregorianCalendar();

        //La fecha de actualizaci??n ser?? la m??s recinte comparando la ??ltima entry con la fecha deleted si existe.
        if (fechaDeleted == null || fechaDeleted.compareTo(updated) < 0) {
            //La entry es v??lida, no hay un deleted-entry posterior
            cell = row.createCell(cellnum++);
            cell.setCellValue((LocalDateTime) entry.getUpdated().getValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime());
            cell.setCellStyle(SpreeadSheetManager.getCellStyleFechaLarga());
            cell = row.createCell(cellnum++);
            cell.setCellValue("VIGENTE");
        } else {
            //La entry no es v??lida, hay un deleted-entry posterior
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

        //Se obtiene el n??mero de elementos tenderResult, en caso de que tenga,
        if (contractFolder.getTenderResult() != null) {
            for (int indice = 0; indice < contractFolder.getTenderResult().size(); indice++) {
                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                int cellnum = 0;

                //Se a??aden los datos b??sicos y obligatorios de la entry: id, link y vigencia
                cell = row.createCell(cellnum++);
                cell.setCellValue(entry.getId().getValue().substring(entry.getId().getValue().lastIndexOf("/") + 1));
                cell = row.createCell(cellnum++);
                cell.setCellValue(entry.getLink().get(0).getHref());

                //Se obtiene la fechaUpdated de la entry
                GregorianCalendar updated = entry.getUpdated().getValue().toGregorianCalendar();

                //La fecha de actualizaci??n ser?? la m??s recinte comparando la ??ltima entry con la fecha deleted si existe.
                if (fechaDeleted == null || fechaDeleted.compareTo(updated) < 0) {
                    //La entry es v??lida, no hay un deleted-entry posterior
                    cell = row.createCell(cellnum++);
                    cell.setCellValue((LocalDateTime) entry.getUpdated().getValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime());
                    cell.setCellStyle(SpreeadSheetManager.getCellStyleFechaLarga());
                } else {
                    //La entry no es v??lida, hay un deleted-entry posterior
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
     * Se a??aden los datos en una ??nica hoja. Los "datos seleccionables" se
     * repiten por cada resultado. Si no hay resultados, se insertan una ??nica
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

        //Se obtiene el n??mero de elementos tenderResult, en caso de que tenga,
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
            //No hay resultados en esta entry, solo se inserta una ??nica vez
            procesarEntry(entry, sheet, fechaDeleted, buscadorDatosSeleccionables);
        }
    }


    /*
	 * inputTree se le pasa el nodo ra??z correspondiente a la hoja de calculo del excel de salida (DatosSeleccionables o DatosResultados)
	 * queDatos es el ??ndice de dichonodo respecto a la ra??z a??adida al treeView, sirve para diferenciar en qu?? Enumerado estamos
	 * tiene el mismo orden que se ha insertado en la ra??z de treeView
	 * se deber??a generalizar mejor, ??quiz??s lanzar una excepci??n si el entero no tiene los valores adecuados cuando se llama a este m??todo?
     */
    private ArrayList<Object> collectCheckedBoxes(CheckBoxTreeItem<String> inputTree, int queDatos) {

        ArrayList<Object> resultado = new ArrayList<>(inputTree.getChildren().size());

        //tantos arrays intermedios como hojas haya + 1 del CheckBoxTreeItem original
        //esto se hace as?? porque esta clase no es Iterable y no se puede recorrer directamente, necesitamos un salto intermedio
        ArrayList<DatosSeleccionables> intermedioSeleccionables = new ArrayList<DatosSeleccionables>(inputTree.getChildren().size());
        ArrayList<DatosResultados> intermedioResultados = new ArrayList<DatosResultados>(inputTree.getChildren().size());
        ArrayList<CheckBoxTreeItem<String>> auxItermediateIterator = new ArrayList<CheckBoxTreeItem<String>>();

        //el salto intermedio que se ha mencionado antes
        findCheckedBoxes((CheckBoxTreeItem<String>) inputTree, auxItermediateIterator);

        //quiz??s se por??a obviar el uso de 1 for por cada Enum si echamos directamente los seleccionados sin 
        //comprobar que est??n dentro del enumerado, pero as?? nos evitamos errores, probar quiz??s para versiones futuras
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
        //userDirectory tendr?? la direcci??n desde la que se est?? ejecutando:
        //  el jar C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\dist
        //  la compilaci??n  C:\Users\DSGregorio\Documents\NetBeansProjects\MyOpenPLACSP\build
        String userDirectory = file.getParent();

        // Collecci??n que registra las entries que ya han sido procesadas
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
            //por defecto se pone a true para que genere la pesta??a Licitaciones y Resultado
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
            // tambi??n a los ??ndices tanto del m??todo (queDatos) como del get del nodo,
            // indican la hoja del excel
            // segun el orden de la disposici??n gr??fica
            // bastante dependencia si se a??aden o quitan m??s enumerados
            // repensar si en el futuro se contemplan muchas hojas generadas en el Excel que
            // deban tener sus agrupaciones respectivas
            //GenericodeTypes
            rootItem = new CheckBoxTreeItem<>("");
            rootItem.getChildren().addAll(new CheckBoxTreeItem<>("Datos Generales"), new CheckBoxTreeItem<>("Datos de Resultados"));
            //Elementos / columnas de "Datos Generales"
            rootItem.getChildren().get(0).getChildren().addAll(
                    new CheckBoxTreeItem<>("Primera publicaci??n"),
                    new CheckBoxTreeItem<>("Estado"),
                    new CheckBoxTreeItem<>("N??mero de expediente"),
                    new CheckBoxTreeItem<>("Objeto del Contrato"),
                    new CheckBoxTreeItem<>("Valor estimado del contrato"),
                    new CheckBoxTreeItem<>("Presupuesto base sin impuestos"),
                    new CheckBoxTreeItem<>("Presupuesto base con impuestos"),
                    new CheckBoxTreeItem<>("CPV"),
                    new CheckBoxTreeItem<>("Tipo de contrato"),
                    new CheckBoxTreeItem<>("Lugar de ejecuci??n"),
                    new CheckBoxTreeItem<>("??rgano de Contrataci??n"),
                    new CheckBoxTreeItem<>("ID OC en PLACSP"),
                    new CheckBoxTreeItem<>("NIF OC"),
                    new CheckBoxTreeItem<>("DIR3"),
                    new CheckBoxTreeItem<>("Enlace al Perfil de Contratante del OC"),
                    new CheckBoxTreeItem<>("Tipo de Administraci??n"),
                    new CheckBoxTreeItem<>("C??digo Postal"),
                    new CheckBoxTreeItem<>("Tipo de procedimiento"),
                    new CheckBoxTreeItem<>("Sistema de contrataci??n"),
                    new CheckBoxTreeItem<>("Tramitaci??n"),
                    new CheckBoxTreeItem<>("Forma de presentaci??n de la oferta"),
                    new CheckBoxTreeItem<>("Fecha de presentaci??n de ofertas"),
                    new CheckBoxTreeItem<>("Fecha de presentaci??n de solicitudes de participacion"),
                    new CheckBoxTreeItem<>("Directiva de aplicaci??n"),
                    new CheckBoxTreeItem<>("Financiaci??n Europea y fuente"),
                    new CheckBoxTreeItem<>("Descripci??n de la financiaci??n europea"),
                    new CheckBoxTreeItem<>("Subcontrataci??n permitida"),
                    new CheckBoxTreeItem<>("Subcontrataci??n permitida porcentaje"));
            //Elementos / columnas de "Datos de Resultados"
            rootItem.getChildren().get(1).getChildren().addAll(
                    new CheckBoxTreeItem<>("N??mero de expediente"),
                    new CheckBoxTreeItem<>("Lote"),
                    new CheckBoxTreeItem<>("Objeto licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Presupuesto base con impuestos licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Presupuesto base sin impuestos licitaci??n/lote"),
                    new CheckBoxTreeItem<>("CPV licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Lugar ejecuci??n licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Resultado licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Fecha del acuerdo licitaci??n/lote"),
                    new CheckBoxTreeItem<>("N??mero de ofertas recibidas por licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Precio de la oferta m??s baja por licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Precio de la oferta m??s alta por licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Se han exclu??do ofertas por ser anormalmente bajas por licitaci??n/lote"),
                    new CheckBoxTreeItem<>("N??mero del contrato licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Fecha formalizaci??n del contrato licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Fecha entrada en vigor del contrato de licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Adjudicatario licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Tipo de identificador de adjudicatario por licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Identificador Adjudicatario de la licitaci??n/lote"),
                    new CheckBoxTreeItem<>("El adjudicatario es o no PYME de la licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Importe adjudicaci??n sin impuestos licitaci??n/lote"),
                    new CheckBoxTreeItem<>("Importe adjudicaci??n con impuestos licitaci??n/lote"));

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

            reg.registrarAccion(debugLog, "Se comienzan a a??adir los t??tulos");
            //Se a??aden los t??tulos de los elementos que se van a insertar en las hojas
            for (SXSSFSheet hoja : hojas) {
                Row row = hoja.createRow(0);
                int cellnum = 0;
                Cell cell = row.createCell(cellnum++);
                cell.setCellValue("Identificador");
                cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                cell = row.createCell(cellnum++);
                cell.setCellValue("Link licitaci??n");
                cell.setCellStyle(SpreeadSheetManager.getCellStyleTitulo());
                cell = row.createCell(cellnum++);
                cell.setCellValue("Fecha actualizaci??n");
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
            //Se cambian los tama??os de las columnas
            spreeadSheetManager.updateColumnsSize();
            reg.registrarAccion(messageLog, "T??tulos a??adidos");

            // Se comprueba que exista el ficheroRISP a procesar
            //DSG File ficheroRISP = new File(textFieldDirOrigen.getText());
            File ficheroRISP = fileInput;

            String directorioPath = ficheroRISP.getParent();
            boolean existeFicheroRisp = ficheroRISP.exists() && ficheroRISP.isFile();

            if (existeFicheroRisp) {
                reg.registrarAccion(messageLog, "Directorio originen de ficheros RISP-PLACSP: " + directorioPath);
                reg.registrarAccion(messageLog, "Fichero r??iz: " + ficheroRISP.getName());
            } else {
                //DSG logger.error("No se puede acceder al fichero " + textFieldDirOrigen.getText());
                reg.registrarAccion(errorLog, "No se puede acceder al fichero " + fileInput.getAbsolutePath());
            }

            File[] lista_ficherosRISP = ficheroRISP.getParentFile().listFiles();
            reg.registrarAccion(messageLog, "N??mero previsto de ficheros a procesar: " + lista_ficherosRISP.length);

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

                // Se a??aden las licitaciones que han dejado de ser v??lidas
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
                    // es m??s reciente
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
                // se comprueba cu??l es el siguiente fichero a procesar
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
            reg.registrarAccion(messageLog, "N??mero de ficheros procesados " + numeroFicherosProcesados);
            reg.registrarAccion(messageLog, "N??mero de elementos entry existentes: " + numeroEntries);
            reg.registrarAccion(messageLog, "Licitaciones insertadas en el fichero: " + entriesProcesadas.size());

            spreeadSheetManager.insertarFiltro(buscadorDatosSeleecionables.size(), buscadorDatosResultadosObj.size());

            reg.registrarAccion(messageLog, "Comienzo de escritura del fichero de salida");
            spreeadSheetManager.getWorkbook().write(output_file); // write excel document to output stream
            output_file.close(); // close the file
            spreeadSheetManager.getWorkbook().close();

            reg.registrarAccion(messageLog, "Fin del proceso de generaci??n del fichero");

        } catch (JAXBException e) {// ventanas de error para las excepciones contempladas
            String auxError = "Error al procesar el fichero ATOM. No se puede continuar con el proceso.";
            reg.registrarAccion(errorLog, auxError);
            reg.registrarAccion(debugLog, e.getStackTrace().toString());

        } catch (FileNotFoundException e) {
            String auxError = "Error al generar el fichero de salida. No se pudo crear un fichero en la ruta indicada o no pudo ser abierto por alguna otra raz??n.";
            reg.registrarAccion(errorLog, auxError);
            reg.registrarAccion(debugLog, e.getStackTrace().toString());
        } catch (Exception e) {
            // error inesperado
            String auxError = "Error inesperado, revise la configuraci??n y el log...";
            reg.registrarAccion(errorLog, auxError);
            reg.registrarAccion(debugLog, e.getStackTrace().toString());
            reg.registrarAccion(debugLog, e.getMessage());
        }
    }
}
