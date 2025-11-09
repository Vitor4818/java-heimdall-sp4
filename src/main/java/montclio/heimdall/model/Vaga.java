package montclio.heimdall.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_HDL_VAGA")
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private Boolean ocupada = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    @ToString.Exclude
    private Zona zona;
    @OneToOne(mappedBy = "vaga", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private Motorcycle moto;

}