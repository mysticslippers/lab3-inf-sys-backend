package me.ifmo.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "x")
    private Long x;

    @NotNull(message = "Y must not be null")
    @Column(name = "y", nullable = false)
    private Long y;

    @NotNull(message = "Z must not be null")
    @Column(name = "z", nullable = false)
    private Double z;

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return id != null && id.equals(location.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
