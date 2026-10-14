package application;

import application.port.InmuebleRepository;
import application.port.InmuebleUseCase;
import dominio.Apartamento;
import dominio.Casa;
import dominio.Inmueble;
import dominio.InmuebleNoEncontradoException;
import dominio.Propietario;
import dominio.Sotano;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InmuebleServiceTest {

    private InmuebleRepository repository;
    private InmuebleUseCase service;
    private Propietario propietarioValido;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInmuebleRepository();
        service = new InmuebleService(repository);
        propietarioValido = new Propietario(1, "Carlos Pérez", "carlos@example.com");
    }

    @Test
    @DisplayName("Debe crear un apartamento exitosamente cuando los datos son válidos")
    void testCrearApartamentoExitoso() {
        Apartamento apto = new Apartamento(
                "CHP-001",
                LocalDate.of(2023, 5, 10),
                "Calle 100 # 15-20",
                Inmueble.Estado.DISPONIBLE,
                propietarioValido,
                5);

        service.crear(apto);

        Inmueble consultado = service.consultarPorChip("CHP-001");
        assertNotNull(consultado);
        assertEquals("CHP-001", consultado.getChip());
        assertTrue(consultado instanceof Apartamento);
        assertEquals(5, ((Apartamento) consultado).getNumeroPiso());
    }

    @Test
    @DisplayName("Debe crear una casa con sótano exitosamente")
    void testCrearCasaConSotanoExitoso() {
        Sotano sotano = new Sotano(45.5, Sotano.TipoUso.PARQUEADERO);
        Casa casa = new Casa(
                "CHP-002",
                LocalDate.of(2022, 1, 15),
                "Carrera 7 # 45-10",
                Inmueble.Estado.EN_VENTA,
                propietarioValido,
                2,
                sotano);

        service.crear(casa);

        Inmueble consultado = service.consultarPorChip("CHP-002");
        assertNotNull(consultado);
        assertTrue(consultado instanceof Casa);
        Casa casaConsultada = (Casa) consultado;
        assertEquals(2, casaConsultada.getCantidadPisos());
        assertTrue(casaConsultada.getSotano().isPresent());
        assertEquals(45.5, casaConsultada.getSotano().get().getAreaM2());
        assertEquals(Sotano.TipoUso.PARQUEADERO, casaConsultada.getSotano().get().getTipoUso());
    }

    @Test
    @DisplayName("Debe crear una casa sin sótano exitosamente y getSotano() retorna Optional vacío")
    void testCrearCasaSinSotanoExitoso() {
        Casa casa = new Casa(
                "CHP-003",
                LocalDate.of(2021, 8, 20),
                "Avenida 19 # 104-50",
                Inmueble.Estado.OCUPADO,
                propietarioValido,
                3);

        service.crear(casa);

        Inmueble consultado = service.consultarPorChip("CHP-003");
        assertNotNull(consultado);
        assertTrue(consultado instanceof Casa);
        Casa casaConsultada = (Casa) consultado;
        assertFalse(casaConsultada.getSotano().isPresent());
    }

    @Test
    @DisplayName("Debe rechazar la creación de un inmueble con CHIP duplicado")
    void testCrearChipDuplicadoFalla() {
        Apartamento apto1 = new Apartamento(
                "CHP-DUP",
                LocalDate.of(2023, 1, 1),
                "Calle 1 # 2-3",
                Inmueble.Estado.DISPONIBLE,
                propietarioValido,
                3);
        service.crear(apto1);

        Apartamento apto2 = new Apartamento(
                "CHP-DUP",
                LocalDate.of(2024, 2, 2),
                "Calle 4 # 5-6",
                Inmueble.Estado.EN_VENTA,
                propietarioValido,
                4);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(apto2));
        assertTrue(ex.getMessage().contains("Ya existe un inmueble con el CHIP"));
    }

    @Test
    @DisplayName("Debe lanzar excepción al consultar un inmueble inexistente")
    void testConsultarInexistenteLanzaExcepcion() {
        assertThrows(InmuebleNoEncontradoException.class, () -> service.consultarPorChip("INEXISTENTE"));
    }

    @Test
    @DisplayName("Debe listar todos los inmuebles creados")
    void testListarInmuebles() {
        assertTrue(service.listar().isEmpty());

        Apartamento apto = new Apartamento("CHP-A", LocalDate.of(2023, 1, 1), "Calle A", Inmueble.Estado.DISPONIBLE,
                propietarioValido, 1);
        Casa casa = new Casa("CHP-B", LocalDate.of(2023, 2, 2), "Calle B", Inmueble.Estado.DISPONIBLE,
                propietarioValido, 2);

        service.crear(apto);
        service.crear(casa);

        List<Inmueble> lista = service.listar();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Debe actualizar un inmueble existente")
    void testActualizarInmuebleExitoso() {
        Apartamento apto = new Apartamento("CHP-UPD", LocalDate.of(2023, 1, 1), "Dirección Original",
                Inmueble.Estado.DISPONIBLE, propietarioValido, 2);
        service.crear(apto);

        apto.setDireccion("Dirección Nueva 123");
        apto.setEstado(Inmueble.Estado.EN_VENTA);
        service.actualizar(apto);

        Inmueble actualizado = service.consultarPorChip("CHP-UPD");
        assertEquals("Dirección Nueva 123", actualizado.getDireccion());
        assertEquals(Inmueble.Estado.EN_VENTA, actualizado.getEstado());
    }

    @Test
    @DisplayName("Debe lanzar InmuebleNoEncontradoException al actualizar uno inexistente")
    void testActualizarInexistenteLanzaExcepcion() {
        Apartamento apto = new Apartamento("NO-EXISTE", LocalDate.of(2023, 1, 1), "Dir", Inmueble.Estado.DISPONIBLE,
                propietarioValido, 1);
        assertThrows(InmuebleNoEncontradoException.class, () -> service.actualizar(apto));
    }

    @Test
    @DisplayName("Debe eliminar un inmueble existente")
    void testEliminarInmuebleExitoso() {
        Apartamento apto = new Apartamento("CHP-DEL", LocalDate.of(2023, 1, 1), "Dir", Inmueble.Estado.DISPONIBLE,
                propietarioValido, 1);
        service.crear(apto);

        service.eliminar("CHP-DEL");

        assertThrows(InmuebleNoEncontradoException.class, () -> service.consultarPorChip("CHP-DEL"));
    }

    @Test
    @DisplayName("Debe lanzar InmuebleNoEncontradoException al eliminar uno inexistente")
    void testEliminarInexistenteLanzaExcepcion() {
        assertThrows(InmuebleNoEncontradoException.class, () -> service.eliminar("NO-EXISTE"));
    }

    // --- Pruebas de Validaciones de Dominio (IllegalArgumentException) ---

    @Test
    @DisplayName("Debe fallar si el CHIP es nulo o vacío")
    void testValidacionChipObligatorio() {
        assertThrows(IllegalArgumentException.class,
                () -> new Apartamento(null, LocalDate.now(), "Dir", Inmueble.Estado.DISPONIBLE, propietarioValido, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Apartamento("   ", LocalDate.now(), "Dir", Inmueble.Estado.DISPONIBLE, propietarioValido, 1));
    }

    @Test
    @DisplayName("Debe fallar si la dirección es nula o vacía")
    void testValidacionDireccionObligatoria() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento("CHP-DIR", LocalDate.now(), null,
                Inmueble.Estado.DISPONIBLE, propietarioValido, 1));
        assertThrows(IllegalArgumentException.class, () -> new Apartamento("CHP-DIR", LocalDate.now(), "  ",
                Inmueble.Estado.DISPONIBLE, propietarioValido, 1));
    }

    @Test
    @DisplayName("Debe fallar si el propietario es nulo")
    void testValidacionPropietarioObligatorio() {
        assertThrows(IllegalArgumentException.class,
                () -> new Apartamento("CHP-PROP", LocalDate.now(), "Dir", Inmueble.Estado.DISPONIBLE, null, 1));
    }

    @Test
    @DisplayName("Debe fallar si la fecha de compra es futura")
    void testValidacionFechaCompraNoFutura() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> new Apartamento("CHP-FUT", manana, "Dir", Inmueble.Estado.DISPONIBLE, propietarioValido, 1));
    }

    @Test
    @DisplayName("Debe fallar si el número de piso del apartamento es menor o igual a cero")
    void testValidacionNumeroPisoApartamento() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento("CHP-APTO0", LocalDate.now(), "Dir",
                Inmueble.Estado.DISPONIBLE, propietarioValido, 0));
        assertThrows(IllegalArgumentException.class, () -> new Apartamento("CHP-APTO-NEG", LocalDate.now(), "Dir",
                Inmueble.Estado.DISPONIBLE, propietarioValido, -2));
    }

    @Test
    @DisplayName("Debe fallar si la cantidad de pisos de la casa es menor o igual a cero")
    void testValidacionCantidadPisosCasa() {
        assertThrows(IllegalArgumentException.class,
                () -> new Casa("CHP-CASA0", LocalDate.now(), "Dir", Inmueble.Estado.DISPONIBLE, propietarioValido, 0));
        assertThrows(IllegalArgumentException.class, () -> new Casa("CHP-CASA-NEG", LocalDate.now(), "Dir",
                Inmueble.Estado.DISPONIBLE, propietarioValido, -1));
    }

    @Test
    @DisplayName("Debe fallar si el área del sótano es menor o igual a cero")
    void testValidacionAreaSotano() {
        assertThrows(IllegalArgumentException.class, () -> new Sotano(0.0, Sotano.TipoUso.BODEGA));
        assertThrows(IllegalArgumentException.class, () -> new Sotano(-15.0, Sotano.TipoUso.BODEGA));
    }

    @Test
    @DisplayName("Debe fallar si el tipo de uso del sótano es nulo")
    void testValidacionTipoUsoSotano() {
        assertThrows(IllegalArgumentException.class, () -> new Sotano(20.0, null));
    }
}
