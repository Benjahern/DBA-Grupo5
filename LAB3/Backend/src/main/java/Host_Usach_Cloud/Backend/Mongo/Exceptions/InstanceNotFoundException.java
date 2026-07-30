package Host_Usach_Cloud.Backend.Mongo.Exceptions;

public class InstanceNotFoundException extends RuntimeException {
    public InstanceNotFoundException(String message) {
        super(message);
    }
}