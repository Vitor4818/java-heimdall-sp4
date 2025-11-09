package montclio.heimdall.dto.VagaDTO;

public record VagaFilter(
        String codigo,
        Long zonaId,
        Boolean isOccupied
) {
}