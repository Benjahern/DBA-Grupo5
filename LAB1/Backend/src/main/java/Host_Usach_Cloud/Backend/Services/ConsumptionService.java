package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Repository.ConsumptionRepository;
import Host_Usach_Cloud.Backend.Services.DTO.ConsumptionProjection;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ConsumptionService {

    private static final int SAMPLES_PER_DAY = 144; // cada 10 minutos

    private final ConsumptionRepository consumptionRepository;

    public ConsumptionService(ConsumptionRepository consumptionRepository) {
        this.consumptionRepository = consumptionRepository;
    }

    public ConsumptionProjection getProjectedMonthlyConsumptionByUser(Long userId) {
        ConsumptionRepository.MonthlyConsumptionStats stats = consumptionRepository.findMonthlyStatsByUser(userId);
        if (stats == null || stats.getSamples() <= 0) {
            return ConsumptionProjection.builder()
                    .projectedCpu(0.0)
                    .projectedRam(0.0)
                    .projectedStorage(0.0)
                    .samplesSoFar(0)
                    .samplesProjected(0)
                    .build();
        }

        LocalDate today = LocalDate.now();
        int daysInMonth = today.lengthOfMonth();
        int samplesProjected = daysInMonth * SAMPLES_PER_DAY;
        int samplesSoFar = stats.getSamples();

        double projectedCpu = (stats.getCpuSum() / samplesSoFar) * samplesProjected;
        double projectedRam = (stats.getRamSum() / samplesSoFar) * samplesProjected;
        double projectedStorage = (stats.getStorageSum() / samplesSoFar) * samplesProjected;

        return ConsumptionProjection.builder()
                .projectedCpu(projectedCpu)
                .projectedRam(projectedRam)
                .projectedStorage(projectedStorage)
                .samplesSoFar(samplesSoFar)
                .samplesProjected(samplesProjected)
                .build();
    }
}
