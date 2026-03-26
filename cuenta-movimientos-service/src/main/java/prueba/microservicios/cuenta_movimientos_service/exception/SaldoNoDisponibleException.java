package prueba.microservicios.cuenta_movimientos_service.exception;

public class SaldoNoDisponibleException extends RuntimeException {

    public SaldoNoDisponibleException(String message) {
        super(message);
    }
}
