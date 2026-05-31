package com.example.Backend.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SeedRequest {

    private int countPerUser;
    private List<Long> targetUserIds;
    private List<Long> targetSectorIds;
    private Map<String, Integer> statusDistribution;
}