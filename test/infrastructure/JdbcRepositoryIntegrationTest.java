package infrastructure;

import application.port.InmuebleUseCase;
import dominio.Apartamento;
import dominio.Casa;
import dominio.Inmueble;
import dominio.Propietario;
import dominio.Sotano;
import infrastructure.config.AppConfig;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcRepositoryIntegrationTest {

    private static InmuebleUseCase service;
    private static boolean dbAvailable = false;

    @BeforeAll
    static void setUpAll() {
        try {
            service = AppConfig.getInmuebleService();
            dbAvailable = true;
        } catch (Exception e) {
            System.err.println("Base de datos no disponible para pruebas de integración: " + e.getMessage());
            dbAvailable = false;
        }
    }

    @Test
    @DisplayName("Debe conectar a la BD, ejecutar schema/data y listar los propietarios precargados")
    void testListarPropietariosPrecargados() {
        Assumptions.assumeTrue(dbAvailable, "MySQL no está disponible o credenciales no coinciden");

        List<Propietario> propietarios = service.listarPropietarios();
        assertNotNull(propietarios);
        assertFalse(propietarios.isEmpty(), "Deben existir propietarios precargados por data.sql");
        assertTrue(propietarios.size() >= 4, "Debe haber al menos 4 propietarios");
    }

    @Test
    @DisplayName("Debe persistir, consultar, actualizar y eliminar un Apartamento en MySQL")
    void testCrudApartamentoJdbc() {
        Assumptions.assumeTrue(dbAvailable, "MySQL no está disponible");

        List<Propietario> propietarios = service.listarPropietarios();
        Propietario prop = propietarios.get(0);

        String chip = "CHIP-TEST-APT-01";
        // Limpiar previo si existiera
        try {
            service.eliminar(chip);
        } catch (Exception ignored) {
        }

        Apartamento apto = new Apartamento(
                chip,
                LocalDate.of(2023, 6, 15),
                "Carrera 15 # 85-30",
                Inmueble.Estado.DISPONIBLE,
                prop,
                8);

        // Guardar
        service.crear(apto);

        // Consultar
        Inmueble encontrado = service.consultarPorChip(chip);
        assertNotNull(encontrado);
        assertEquals(chip, encontrado.getChip());
        assertTrue(encontrado instanceof Apartamento);
        assertEquals(8, ((Apartamento) encontrado).getNumeroPiso());

        // Actualizar
        encontrado.setDireccion("Carrera 15 # 85-30 Apto 801");
        encontrado.setEstado(Inmueble.Estado.OCUPADO);
        service.actualizar(encontrado);

        Inmueble actualizado = service.consultarPorChip(chip);
        assertEquals("Carrera 15 # 85-30 Apto 801", actualizado.getDireccion());
        assertEquals(Inmueble.Estado.OCUPADO, actualizado.getEstado());

        // Eliminar
        service.eliminar(chip);
        assertFalse(service.listar().stream().anyMatch(i -> i.getChip().equals(chip)));
    }

    @Test
    @DisplayName("Debe persistir una Casa con Sótano en la misma transacción y cascada al eliminar")
    void testCrudCasaConSotanoTransaccionJdbc() {
        Assumptions.assumeTrue(dbAvailable, "MySQL no está disponible");

        List<Propietario> propietarios = service.listarPropietarios();
        Propietario prop = propietarios.get(1);

        String chip = "CHIP-TEST-CASA-01";
        try {
            service.eliminar(chip);
        } catch (Exception ignored) {
        }

        Sotano sotano = new Sotano(50.0, Sotano.TipoUso.PARQUEADERO);
        Casa casa = new Casa(
                chip,
                LocalDate.of(2022, 3, 20),
                "Calle 127 # 45-12",
                Inmueble.Estado.EN_VENTA,
                prop,
                2,
                sotano);

        // Guardar en transacción (Casa + Sótano)
        service.crear(casa);

        // Consultar
        Inmueble encontrado = service.consultarPorChip(chip);
        assertNotNull(encontrado);
        assertTrue(encontrado instanceof Casa);
        Casa casaBD = (Casa) encontrado;
        assertTrue(casaBD.getSotano().isPresent());
        assertEquals(50.0, casaBD.getSotano().get().getAreaM2());
        assertEquals(Sotano.TipoUso.PARQUEADERO, casaBD.getSotano().get().getTipoUso());

        // Actualizar cambiando sótano
        casaBD.setSotano(new Sotano(60.0, Sotano.TipoUso.BODEGA));
        service.actualizar(casaBD);

        Casa casaActualizada = (Casa) service.consultarPorChip(chip);
        assertTrue(casaActualizada.getSotano().isPresent());
        assertEquals(60.0, casaActualizada.getSotano().get().getAreaM2());
        assertEquals(Sotano.TipoUso.BODEGA, casaActualizada.getSotano().get().getTipoUso());

        // Eliminar
        service.eliminar(chip);
        assertFalse(service.listar().stream().anyMatch(i -> i.getChip().equals(chip)));
    }
}
