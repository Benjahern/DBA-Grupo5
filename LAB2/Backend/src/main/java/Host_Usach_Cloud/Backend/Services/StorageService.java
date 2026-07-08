package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Storage;
import Host_Usach_Cloud.Backend.Repository.StorageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageService {

    private final StorageRepository storageRepository;

    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    public Storage createStorage(Storage storage) {
        return storageRepository.save(storage);
    }

    public Storage getStorageById(Long storageId) {
        return storageRepository.findById(storageId)
                .orElseThrow(() -> new IllegalArgumentException("El Id de Storage no existe"));
    }

    public List<Storage> getAllStorage() {
        return storageRepository.findAll();
    }

    public Storage updateStorage(Storage storage) {
        boolean updated = storageRepository.update(storage);
        if (!updated) {
            throw new IllegalArgumentException("El Id de Storage no existe");
        }
        return storage;
    }

    public void deleteStorage(Long storageId) {
        boolean deleted = storageRepository.deleteById(storageId);
        if (!deleted) {
            throw new IllegalArgumentException("El Id de Storage no existe");
        }
    }
}
