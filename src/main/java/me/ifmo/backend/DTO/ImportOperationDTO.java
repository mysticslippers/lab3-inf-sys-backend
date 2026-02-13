package me.ifmo.backend.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportOperationDTO {

    private Long id;
    private String username;
    private String objectType;
    private String status;
    private Integer importedCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
