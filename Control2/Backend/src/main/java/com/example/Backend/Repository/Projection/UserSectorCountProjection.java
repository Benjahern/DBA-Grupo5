package com.example.Backend.Repository.Projection;

public interface UserSectorCountProjection {
    Long getUserId();
    String getUserName();
    Long getSectorId();
    String getSectorName();
    Long getTaskCount();
}