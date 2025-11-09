package montclio.heimdall.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import montclio.heimdall.dto.MotorcycleDTO.PostMotorcycleDTO;
import montclio.heimdall.dto.MotorcycleDTO.PutMotorcycleDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="TB_HDL_MOTO")
public class Motorcycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String plate;
    private String chassiNumber;
    private MotorcycleType motorcycleType;
    @OneToOne(mappedBy = "motorcycle")
    @ToString.Exclude
    private TagRfId tag;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", unique = true)
    private Vaga vaga;

    public Motorcycle(PostMotorcycleDTO motorcycleDTO) {
        this.id = motorcycleDTO.id();
        this.motorcycleType = motorcycleDTO.motorcycleType();
        this.chassiNumber = motorcycleDTO.chassiNumber();
        this.plate = motorcycleDTO.plate();

        if (motorcycleDTO.tagId() != null) {
            TagRfId tag = new TagRfId();
            tag.setId(motorcycleDTO.tagId());
            tag.setMotorcycle(this); // Define o vínculo 1:1
            this.tag = tag;
        }
    }

    public void updateData(PutMotorcycleDTO dto) {
        if (dto.plate() != null) {
            this.plate = dto.plate();
        }
        if (dto.motorcycleType() != null) {
            this.motorcycleType = dto.motorcycleType();
        }
        if (dto.tagId() != null) {
            TagRfId tag = new TagRfId();
            tag.setId(dto.tagId());
            tag.setMotorcycle(this);   // RELAÇÃO sendo "setada" do lado correto
            this.tag = tag;
        }
    }
}
