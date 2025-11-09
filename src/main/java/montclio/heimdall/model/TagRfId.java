package montclio.heimdall.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import montclio.heimdall.dto.TagRfidDTO.PostTagRfidDTO;
import montclio.heimdall.dto.TagRfidDTO.PutTagRfidDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="TB_HDL_TAG_IDENTIFICACAO")
public class TagRfId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String frequencia;
    private String banda;
    private String aplicacao;
    @OneToOne
    @JoinColumn(name = "motorcycle_id", referencedColumnName = "id", unique = true)
    @ToString.Exclude
    private Motorcycle motorcycle;

    // Construtor que recebe um DTO para criação
    public TagRfId(PostTagRfidDTO dto) {
        this.id = dto.id();
        this.frequencia = dto.frequencia();
        this.banda = dto.banda();
        this.aplicacao = dto.aplicacao();

        if (dto.motorcycle() != null) {
            this.motorcycle = dto.motorcycle();
            this.motorcycle.setTag(this);
        }
    }

    // Método para atualizar os dados baseado em um DTO de update
    public void updateData(PutTagRfidDTO dto) {
        if (dto.frequencia() != null) {
            this.frequencia = dto.frequencia();
        }
        if (dto.banda() != null) {
            this.banda = dto.banda();
        }
        if (dto.aplicacao() != null) {
            this.aplicacao = dto.aplicacao();
        }
        if (dto.motorcycle() != null) {
            this.motorcycle = dto.motorcycle();
            this.motorcycle.setTag(this);
        }
    }

}
