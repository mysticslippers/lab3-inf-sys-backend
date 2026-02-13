package me.ifmo.backend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {

    private Long id;

    private Long x;

    @NotNull(message = "Location.y must not be null")
    private Long y;

    @NotNull(message = "Location.z must not be null")
    private Double z;
}
