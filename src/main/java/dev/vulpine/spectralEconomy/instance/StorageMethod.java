package pro.vulpine.vCrates.instance;

public enum StorageMethod {

    H2,
    MYSQL;

    public static StorageMethod fromString(String string) {
        for (StorageMethod method : values()) {
            if (method.name().equalsIgnoreCase(string)) {
                return method;
            }
        }
        return null;
    }

}