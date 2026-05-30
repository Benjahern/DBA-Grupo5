package com.example.Backend.Repository.Projection;

public interface SectorCountProjection {
    Long getSectorId();
    String getSectorName();
    Object getTaskCount();
}