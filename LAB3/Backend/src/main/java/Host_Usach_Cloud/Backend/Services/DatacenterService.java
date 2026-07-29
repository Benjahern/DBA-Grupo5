package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Datacenter;
import Host_Usach_Cloud.Backend.Repository.DatacenterRepository;
import Host_Usach_Cloud.Backend.Services.DTO.DatacenterDistance;
import Host_Usach_Cloud.Backend.Services.DTO.LocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatacenterService {

    private final DatacenterRepository datacenterRepository;

    public Long createDatacenter(Datacenter datacenter) {

        if (datacenter.getCurrentInstances() == null) {
            datacenter.setCurrentInstances(0);
        }

        validateDatacenter(datacenter);

        return datacenterRepository.create(datacenter);
    }

    public List<Datacenter> getAllDatacenters() {

        return datacenterRepository.findAll();
    }

    public Datacenter getDatacenterById(Long id) {

        return datacenterRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Datacenter no encontrado con id: " + id
                        )
                );
    }

    public void updateDatacenter(
            Long id,
            Datacenter datacenter
    ) {

        getDatacenterById(id);

        validateDatacenter(datacenter);

        datacenterRepository.update(
                id,
                datacenter
        );
    }

    public void deleteDatacenter(Long id) {

        getDatacenterById(id);

        datacenterRepository.delete(id);
    }

    private void validateDatacenter(
            Datacenter datacenter
    ) {

        if (datacenter.getName() == null
                || datacenter.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        if (datacenter.getCapacity() == null
                || datacenter.getCapacity() <= 0) {

            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor a cero"
            );
        }

        if (datacenter.getLatitude() == null
                || datacenter.getLatitude() < -90
                || datacenter.getLatitude() > 90) {

            throw new IllegalArgumentException(
                    "Latitud inválida"
            );
        }

        if (datacenter.getLongitude() == null
                || datacenter.getLongitude() < -180
                || datacenter.getLongitude() > 180) {

            throw new IllegalArgumentException(
                    "Longitud inválida"
            );
        }
    }

    public LocationResponse getLocationInfo(
            Double latitude,
            Double longitude
    ) {

        LocationResponse region =
                datacenterRepository
                        .findRegionByCoordinates(
                                latitude,
                                longitude
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "No se encontró una región para las coordenadas indicadas"
                                )
                        );

        LocationResponse riskZone =
                datacenterRepository
                        .findRiskZoneByCoordinates(
                                latitude,
                                longitude
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "No se encontró una placa tectónica para las coordenadas indicadas"
                                )
                        );

        return LocationResponse.builder()
                .regionId(region.getRegionId())
                .regionName(region.getRegionName())
                .riskZoneId(riskZone.getRiskZoneId())
                .riskZoneName(riskZone.getRiskZoneName())
                .build();
    }

    public List<DatacenterDistance> getRecommendedDatacenters(
            Long instanceId
    ) {

        Datacenter instanceDatacenter =
                datacenterRepository
                        .findInstanceDatacenter(instanceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No se encontró el datacenter de la instancia"
                                ));

        return datacenterRepository.findClosestDatacenters(
                instanceDatacenter.getLatitude(),
                instanceDatacenter.getLongitude(),
                instanceDatacenter.getRiskZoneId(),
                instanceDatacenter.getId()
        );
    }
}
