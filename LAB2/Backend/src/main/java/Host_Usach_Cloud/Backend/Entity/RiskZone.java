package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RiskZone {
    private Long ogcFid;      // PRIMARY KEY creada por ogr2ogr
    private String layer;
    private String code;
    private String platename;
    private Geometry wkbGeometry;
}