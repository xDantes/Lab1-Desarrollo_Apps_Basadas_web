package cr.ac.una.lab1;

import org.testcontainers.DockerClientFactory;

public final class DockerUtils {

    private DockerUtils() {
    }

    public static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }
}
