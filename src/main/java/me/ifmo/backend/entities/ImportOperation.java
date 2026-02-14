package me.ifmo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "import_operations")
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "object_type", nullable = false)
    private String objectType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImportStatus status;

    @Column(name = "imported_count")
    private Integer importedCount;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "file_object_key", length = 1000)
    private String fileObjectKey;

    @Column(name = "file_original_name", length = 500)
    private String fileOriginalName;

    @Column(name = "file_content_type", length = 200)
    private String fileContentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ImportStatus.IN_PROGRESS;
        }
    }

    @Override
    public String toString() {
        return "ImportOperation{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", objectType='" + objectType + '\'' +
                ", status=" + status +
                ", importedCount=" + importedCount +
                ", startedAt=" + startedAt +
                ", finishedAt=" + finishedAt +
                ", fileObjectKey='" + fileObjectKey + '\'' +
                ", fileOriginalName='" + fileOriginalName + '\'' +
                ", fileContentType='" + fileContentType + '\'' +
                ", fileSizeBytes=" + fileSizeBytes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportOperation that = (ImportOperation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
