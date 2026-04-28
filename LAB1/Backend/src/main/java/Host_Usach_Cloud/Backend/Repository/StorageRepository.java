package Host_Usach_Cloud.Backend.Repository;

import Host_Usach_Cloud.Backend.Entity.Storage;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class StorageRepository {

    private final JdbcTemplate jdbcTemplate;

    public StorageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Storage save(Storage storage) {
        String sql = "INSERT INTO \"Storage\" (\"Quantity\", \"Cost_ph\") VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, storage.getQuantity());
            ps.setFloat(2, storage.getCost_ph());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            storage.setStorage_id(keyHolder.getKey().longValue());
        }

        return storage;
    }

    public Optional<Storage> findById(Long id) {
        String sql = "SELECT * FROM \"Storage\" WHERE \"Storage_id\" = ?";

        try {
            Storage storage = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    Storage.builder()
                            .Storage_id(rs.getLong("Storage_id"))
                            .Quantity(rs.getInt("Quantity"))
                            .Cost_ph(rs.getFloat("Cost_ph"))
                            .build()
            , id);
            return Optional.ofNullable(storage);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Storage> findAll() {
        String sql = "SELECT * FROM \"Storage\"";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Storage.builder()
                .Storage_id(rs.getLong("Storage_id"))
                .Quantity(rs.getInt("Quantity"))
                .Cost_ph(rs.getFloat("Cost_ph"))
                .build());
    }

    public boolean update(Storage storage) {
        String sql = "UPDATE \"Storage\" SET \"Quantity\" = ?, \"Cost_ph\" = ? WHERE \"Storage_id\" = ?";
        int updated = jdbcTemplate.update(sql, storage.getQuantity(), storage.getCost_ph(), storage.getStorage_id());
        return updated > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM \"Storage\" WHERE \"Storage_id\" = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
