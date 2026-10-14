package infrastructure.ui;

import application.port.InmuebleUseCase;
import dominio.Apartamento;
import dominio.Casa;
import dominio.Inmueble;
import dominio.Propietario;
import dominio.Sotano;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Adaptador de entrada: controlador JavaFX de la vista {@code inmueble-view.fxml}.
 * <p>Traduce las acciones del usuario (guardar, consultar, eliminar, limpiar) en llamadas
 * al puerto {@link application.port.InmuebleUseCase}. No conoce repositorios ni JDBC.</p>
 */
public class InmuebleController {

    private static final String APARTAMENTO = "Apartamento";
    private static final String CASA = "Casa";

    @FXML private TextField txtChip;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<Inmueble.Estado> cbEstado;
    @FXML private ComboBox<Propietario> cbPropietario;
    @FXML private ComboBox<String> cbTipo;
    @FXML private VBox boxApartamento;
    @FXML private TextField txtNumeroPiso;
    @FXML private VBox boxCasa;
    @FXML private TextField txtCantidadPisos;
    @FXML private CheckBox chkSotano;
    @FXML private VBox boxSotano;
    @FXML private TextField txtAreaSotano;
    @FXML private ComboBox<Sotano.TipoUso> cbTipoUsoSotano;
    @FXML private Label lblModo;

    @FXML private TableView<Inmueble> tblInmuebles;
    @FXML private TableColumn<Inmueble, String> colChip;
    @FXML private TableColumn<Inmueble, String> colTipo;
    @FXML private TableColumn<Inmueble, String> colDireccion;
    @FXML private TableColumn<Inmueble, String> colFecha;
    @FXML private TableColumn<Inmueble, String> colEstado;
    @FXML private TableColumn<Inmueble, String> colPropietario;
    @FXML private TableColumn<Inmueble, String> colDetalle;

    private InmuebleUseCase useCase;
    private boolean modoEdicion = false;

    /**
     * Configura combos, columnas de la tabla y listeners. JavaFX lo invoca al cargar el FXML.
     */
    @FXML
    private void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList(Inmueble.Estado.values()));
        cbTipo.setItems(FXCollections.observableArrayList(APARTAMENTO, CASA));
        cbTipoUsoSotano.setItems(FXCollections.observableArrayList(Sotano.TipoUso.values()));

        cbTipo.valueProperty().addListener((o, a, b) -> actualizarVisibilidad());
        chkSotano.selectedProperty().addListener((o, a, b) -> actualizarVisibilidad());

        colChip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getChip()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue() instanceof Casa ? CASA : APARTAMENTO));
        colDireccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccion()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaCompra().toString()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        colPropietario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPropietario().getNombre()));
        colDetalle.setCellValueFactory(c -> new SimpleStringProperty(detalle(c.getValue())));

        tblInmuebles.getSelectionModel().selectedItemProperty().addListener((o, a, sel) -> {
            if (sel != null) {
                cargarEnFormulario(sel);
            }
        });

        limpiar();
    }

    /**
     * Inyecta el caso de uso, carga los propietarios en el combo y llena la tabla.
     *
     * @param useCase puerto de entrada de inmuebles
     */
    public void setInmuebleUseCase(InmuebleUseCase useCase) {
        this.useCase = useCase;
        cbPropietario.setItems(FXCollections.observableArrayList(useCase.listarPropietarios()));
        refrescarTabla();
    }

    /**
     * Crea el inmueble, o lo actualiza si el formulario está en modo edición.
     */
    @FXML
    private void onGuardar() {
        try {
            Inmueble inmueble = construirInmueble();
            if (modoEdicion) {
                useCase.actualizar(inmueble);
            } else {
                useCase.crear(inmueble);
            }
            refrescarTabla();
            limpiar();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Inmueble guardado correctamente.");
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos inválidos", "Revisa los campos numéricos.");
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se pudo guardar", e.getMessage());
        }
    }

    /**
     * Busca por el CHIP escrito en el formulario y carga el resultado para editarlo.
     */
    @FXML
    private void onConsultar() {
        try {
            Inmueble inmueble = useCase.consultarPorChip(txtChip.getText());
            tblInmuebles.getSelectionModel().select(inmueble);
            cargarEnFormulario(inmueble);
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Consulta", e.getMessage());
        }
    }

    /**
     * Elimina el inmueble seleccionado en la tabla, previa confirmación.
     */
    @FXML
    private void onEliminar() {
        Inmueble sel = tblInmuebles.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminar", "Selecciona un inmueble de la tabla.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el inmueble con CHIP " + sel.getChip() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        try {
            useCase.eliminar(sel.getChip());
            refrescarTabla();
            limpiar();
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar", e.getMessage());
        }
    }

    /**
     * Vacía el formulario y vuelve al modo creación.
     */
    @FXML
    private void onLimpiar() {
        limpiar();
    }

    /**
     * Restablece todos los campos y el modo de creación.
     */
    private void limpiar() {
        tblInmuebles.getSelectionModel().clearSelection();
        txtChip.clear();
        dpFecha.setValue(null);
        txtDireccion.clear();
        cbEstado.setValue(null);
        cbPropietario.setValue(null);
        txtNumeroPiso.clear();
        txtCantidadPisos.clear();
        chkSotano.setSelected(false);
        txtAreaSotano.clear();
        cbTipoUsoSotano.setValue(null);
        cbTipo.setValue(APARTAMENTO);
        cbTipo.setDisable(false);
        txtChip.setDisable(false);
        modoEdicion = false;
        actualizarVisibilidad();
        actualizarLabelModo();
    }

    /**
     * Muestra los datos del inmueble en el formulario y pasa a modo edición (CHIP y tipo bloqueados).
     */
    private void cargarEnFormulario(Inmueble i) {
        txtChip.setText(i.getChip());
        dpFecha.setValue(i.getFechaCompra());
        txtDireccion.setText(i.getDireccion());
        cbEstado.setValue(i.getEstado());
        cbPropietario.setValue(i.getPropietario());
        if (i instanceof Apartamento a) {
            cbTipo.setValue(APARTAMENTO);
            txtNumeroPiso.setText(String.valueOf(a.getNumeroPiso()));
        } else if (i instanceof Casa c) {
            cbTipo.setValue(CASA);
            txtCantidadPisos.setText(String.valueOf(c.getCantidadPisos()));
            Sotano s = c.getSotano().orElse(null);
            chkSotano.setSelected(s != null);
            txtAreaSotano.setText(s != null ? String.valueOf(s.getAreaM2()) : "");
            cbTipoUsoSotano.setValue(s != null ? s.getTipoUso() : null);
        }
        txtChip.setDisable(true);
        cbTipo.setDisable(true);
        modoEdicion = true;
        actualizarVisibilidad();
        actualizarLabelModo();
    }

    /**
     * Recarga la tabla con los inmuebles del caso de uso.
     */
    private void refrescarTabla() {
        tblInmuebles.setItems(FXCollections.observableArrayList(useCase.listar()));
    }

    /**
     * Muestra u oculta los campos de apartamento, casa y sótano según el tipo elegido.
     */
    private void actualizarVisibilidad() {
        boolean esCasa = CASA.equals(cbTipo.getValue());
        boolean esApto = APARTAMENTO.equals(cbTipo.getValue());
        boxApartamento.setVisible(esApto);
        boxApartamento.setManaged(esApto);
        boxCasa.setVisible(esCasa);
        boxCasa.setManaged(esCasa);
        boolean conSotano = esCasa && chkSotano.isSelected();
        boxSotano.setVisible(conSotano);
        boxSotano.setManaged(conSotano);
    }

    /**
     * Construye el {@link Inmueble} a partir de los campos del formulario.
     *
     * @throws NumberFormatException    si un campo numérico no es válido
     * @throws IllegalArgumentException si el dominio rechaza algún dato
     */
    private Inmueble construirInmueble() {
        String chip = txtChip.getText();
        LocalDate fecha = dpFecha.getValue();
        String direccion = txtDireccion.getText();
        Inmueble.Estado estado = cbEstado.getValue();
        Propietario propietario = cbPropietario.getValue();
        String tipo = cbTipo.getValue();

        if (APARTAMENTO.equals(tipo)) {
            int piso = Integer.parseInt(txtNumeroPiso.getText().trim());
            return new Apartamento(chip, fecha, direccion, estado, propietario, piso);
        } else if (CASA.equals(tipo)) {
            int pisos = Integer.parseInt(txtCantidadPisos.getText().trim());
            Sotano sotano = null;
            if (chkSotano.isSelected()) {
                double area = Double.parseDouble(txtAreaSotano.getText().trim().replace(',', '.'));
                sotano = new Sotano(area, cbTipoUsoSotano.getValue());
            }
            return new Casa(chip, fecha, direccion, estado, propietario, pisos, sotano);
        }
        throw new IllegalArgumentException("Selecciona el tipo de inmueble.");
    }

    /**
     * @return texto resumen de los datos propios de apartamento o casa, para la tabla
     */
    private String detalle(Inmueble i) {
        if (i instanceof Apartamento a) {
            return "Piso " + a.getNumeroPiso();
        }
        if (i instanceof Casa c) {
            return c.getCantidadPisos() + " pisos" + c.getSotano()
                    .map(s -> ", sótano " + s.getAreaM2() + " m² (" + s.getTipoUso() + ")")
                    .orElse("");
        }
        return "";
    }

    /**
     * Muestra una alerta modal con el título y el mensaje indicados.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Actualiza la etiqueta que indica si el formulario está en modo creación o edición.
     */
    private void actualizarLabelModo() {
        if (modoEdicion) {
            lblModo.setText("Modo: Edición");
            lblModo.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");
        } else {
            lblModo.setText("Modo: Creación");
            lblModo.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
        }
    }
}
