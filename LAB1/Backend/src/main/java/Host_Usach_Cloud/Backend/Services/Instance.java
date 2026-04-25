package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Instance {
    public final JdbcTemplate jdbc;

    public Instance findById(Long Id){
        String sql = "SELECT * FROM Instance WHERE Instance_id = ?";

        return quer
    }


    // Solicitado por enunciado
    public Instance updateStateByid(Long InstanceId, String State){
        Instance

        return instanceUpdates;
    }
}
