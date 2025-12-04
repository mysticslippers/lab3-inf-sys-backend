package me.ifmo.backend.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinatesDTO {

    private Long id;

    @NotNull(message = "Coordinates.x must not be null")
    private Double x;

    @NotNull(message = "Coordinates.y must not be null")
    @DecimalMin(value = "-976", inclusive = false, message = "Coordinates.y must be > -976")
    private Float y;
}
