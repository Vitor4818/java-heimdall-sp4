package montclio.heimdall.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_HDL_CATEGORIA_USUARIO")
public class UserCategory {
    @Id
    private Long id;
    @NotBlank
    private String category;
    @OneToMany(mappedBy = "userCategory")
    @ToString.Exclude
    @JsonIgnore
    private List<User> users;
}
