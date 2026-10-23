package common;

/** Bọc SQLException để các lớp service/ui không phải xử lý checked exception. */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
