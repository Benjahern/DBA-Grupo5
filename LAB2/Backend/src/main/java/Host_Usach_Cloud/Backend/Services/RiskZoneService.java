package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Repository.RiskZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiskZoneService {

    @Autowired
    private RiskZoneRepository riskZoneRepository; // Nombre de clase actualizado

    /**
     * Obtiene todas las zonas de riesgo (placas y otras)
     */
    public String obtenerTodasLasZonasGeoJson() {
        return riskZoneRepository.findAllAsGeoJson();
    }

    /**
     * Obtiene zonas filtradas por tipo (ej. 'TECTONICO')
     */
    public String obtenerZonasPorTipoGeoJson(String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            return obtenerTodasLasZonasGeoJson();
        }
        // Llamamos al método actualizado del repositorio
        return riskZoneRepository.findByTypeAsGeoJson(tipo.toUpperCase());
    }
}
