package Host_Usach_Cloud.Backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Region {

    @JsonProperty("region_id")
    private Long Region_id;

    @JsonProperty("Name")
    private String Name;

    @JsonIgnore
    private Polygon Geom;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Polygon getGeom() {
        return Geom;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setGeom(Polygon geom) {
        this.Geom = geom;
    }

    /**
     * DTO matching Control2's CoordinateDTO: wire format for input is a list of
     * {"latitude", "longitude"} objects.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDTO {
        private Double latitude;
        private Double longitude;
    }

    /**
     * Exterior ring vertices as [lng, lat] pairs (GeoJSON order, EPSG:4326).
     * First and last vertices are equal (closed ring).
     */
    public double[][] getCoordinates() {
        if (Geom == null || Geom.isEmpty()) return null;
        Coordinate[] coords = Geom.getExteriorRing().getCoordinates();
        double[][] out = new double[coords.length][2];
        for (int i = 0; i < coords.length; i++) {
            out[i] = new double[]{coords[i].getX(), coords[i].getY()};
        }
        return out;
    }

    /** [lng, lat] of the polygon's centroid. */
    public double[] getCentroid() {
        if (Geom == null || Geom.isEmpty()) return null;
        Point c = Geom.getCentroid();
        return new double[]{c.getX(), c.getY()};
    }

    /**
     * Accepts "coordinates": List<{latitude, longitude}>. Internally stored as
     * [longitude, latitude] (GeoJSON order) to match getCoordinates() output.
     * Auto-closes ring by repeating the first vertex (JTS requirement).
     */
    @JsonSetter("coordinates")
    public void setCoordinatesFromWire(List<CoordinateDTO> coords) {
        if (coords == null || coords.size() < 3) {
            throw new IllegalArgumentException("Polygon requires at least 3 vertices");
        }
        Coordinate[] c = new Coordinate[coords.size() + 1];
        for (int i = 0; i < coords.size(); i++) {
            CoordinateDTO p = coords.get(i);
            c[i] = new Coordinate(p.getLongitude(), p.getLatitude());
        }
        c[coords.size()] = c[0];
        this.Geom = new GeometryFactory().createPolygon(c);
    }
}