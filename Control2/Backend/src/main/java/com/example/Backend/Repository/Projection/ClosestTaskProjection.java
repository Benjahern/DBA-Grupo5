package com.example.Backend.Repository.Projection;

import java.time.LocalDate;

public interface ClosestTaskProjection {
    Long getTaskId();
    String getTitle();
    String getDescription();
    LocalDate getDueDate();
    String getStatus();
    String getSectorName();
    Double getDistanceMetres();
}