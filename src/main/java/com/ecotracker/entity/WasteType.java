package com.ecotracker.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "waste_types")
public class WasteType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public WasteType() {
    }

    public WasteType(String code, String displayName, boolean enabled) {
        this.code = code;
        this.displayName = displayName;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WasteType wasteType = (WasteType) o;
        return Objects.equals(code, wasteType.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
