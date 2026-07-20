package Host_Usach_Cloud.Backend.Services.DTO;
import java.math.BigDecimal;

public record BillingDistanceResult(
        BigDecimal baseCost,
        BigDecimal distanceKm,
        BigDecimal surchargeRate,
        BigDecimal surchargeAmount,
        BigDecimal total
) {}
