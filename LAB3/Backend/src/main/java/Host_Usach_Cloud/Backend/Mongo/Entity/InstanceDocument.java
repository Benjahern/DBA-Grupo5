package Host_Usach_Cloud.Backend.Mongo.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("instances")
@CompoundIndex(name = "user_state_v2_idx", def = "{'userId': 1, 'State': 1}")
@CompoundIndex(name = "user_numeric_idx", def = "{'userId': 1, 'numericId': 1}")
public class InstanceDocument {

    @Id
    private String instanceId;

    /**
     * ID numérico sequential per-user (1, 2, 3...). Es el que ve el usuario y el
     * que va en la URL — replica el comportamiento del BIGSERIAL de Postgres
     * pre-migración pero particionado por userId para que cada usuario vea
     * su primera instancia como id=1.
     */
    @Indexed
    private Long numericId;

    @Indexed
    private Long userId;

    private String name;

    @Indexed
    @Field("State")
    private String state;

    private Long cpuId;
    private Long ramId;
    private Long storageId;
    private Long regionId;
    private Long datacenterId;

    private String containerId;

    @Field("Started_at")
    private LocalDateTime startedAt;

    @Field("Active_hours")
    private Double activeHours;

    private boolean terminated;

    private String ipAddress;

    private String color;

    /*
     * Aliases snake_case / PascalCase — el frontend post-migración todavía
     * busca las claves viejas (Dashboard.vue, InstanceContainer.vue,
     * IntanceTicket.vue, ModalResilience.vue, AdminCosts.vue) mientras se
     * actualiza a la convención camelCase.
     *
     * Desde el cambio a numericId, los consumers esperan números
     * (1, 2, 3...) en instance_id, instanceId y id. Las otras props
     * (cpu_id, ram_id, etc.) son Long FKs que ya estaban en Long.
     */
    public Long getId() { return numericId; }
    public Long getInstance_id() { return numericId; }
    public String getInstance_id_hex() { return instanceId; }
    public Long getUser_id() { return userId; }
    public Long getCpu_id() { return cpuId; }
    public Long getRam_id() { return ramId; }
    public Long getStorage_id() { return storageId; }
    public Long getRegion_id() { return regionId; }
    public Long getDatacenter_id() { return datacenterId; }
    public String getIp_address() { return ipAddress; }
    public String getContainer_id() { return containerId; }
    public Double getActive_hours() {
        return activeHours;
    }
}