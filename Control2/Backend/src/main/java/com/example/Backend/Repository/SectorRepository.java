package com.example.Backend.Repository;

import com.example.Backend.Entity.SectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.locationtech.jts.geom.Point;
import java.util.List;

@Repository
public interface SectorRepository extends JpaRepository<SectorEntity, Long>{

    @Query(value = "SELECT * FROM sectors s WHERE ST_DistanceSphere(s.geo_location, :userLocation) <= :radiusInMetres", nativeQuery = true)
    List<SectorEntity> findSectorsWithinRadius(@Param("userLocation") Point userLocation, @Param("radiusInMetres") double radiusInMetres);
}