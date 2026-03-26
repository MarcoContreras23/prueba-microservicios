package prueba.microservicios.cuenta_movimientos_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes_local")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteLocal {

    @Id
    @Column(name = "cliente_id", unique = true, nullable = false)
    private String clienteId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = true;
}

