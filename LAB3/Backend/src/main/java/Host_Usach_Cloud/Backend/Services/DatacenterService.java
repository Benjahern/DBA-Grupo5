package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Datacenter;
import Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument;
import Host_Usach_Cloud.Backend.Repository.DatacenterRepository;
import Host_Usach_Cloud.Backend.Services.DTO.DatacenterDistance;
import Host_Usach_Cloud.Backend.Services.DTO.LocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatacenterService {

    private final DatacenterRepository datacenterRepository;
    private final MongoTemplate mongoTemplate;

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
            Long numericId
    ) {

        InstanceDocument instance = mongoTemplate.findOne(
                Query.query(Criteria.where("numericId").is(numericId)),
                InstanceDocument.class, "instances");
        if (instance == null) {
            throw new RuntimeException("Instancia no encontrada: " + numericId);
        }

        if (instance.getDatacenterId() == null) {
            throw new RuntimeException("La instancia " + numericId + " no tiene datacenter asignado");
        }

        Datacenter instanceDatacenter = datacenterRepository.findById(instance.getDatacenterId())
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el datacenter (id=" + instance.getDatacenterId() + ") de la instancia " + numericId));

        return datacenterRepository.findClosestDatacenters(
                instanceDatacenter.getLatitude(),
                instanceDatacenter.getLongitude(),
                instanceDatacenter.getRiskZoneId(),
                instanceDatacenter.getId()
        );
    }
}
