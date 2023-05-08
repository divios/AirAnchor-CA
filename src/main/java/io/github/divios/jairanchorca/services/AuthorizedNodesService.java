package io.github.divios.jairanchorca.services;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AuthorizedNodesService {

    @Value("${authorized.file.path}")
    private String authorizedFile;

    private Set<String> authorizedList;

    @PostConstruct
    private void init() {
        var path = Path.of(authorizedFile);

        try {
            authorizedList = new HashSet<>(Files.readAllLines(path));
        } catch (IOException e) {
            log.error("Could no read authorized keys file: {}", e.getMessage());
            System.exit(-1);
        }

        CompletableFuture.runAsync(() -> createFileWatcher(path));
    }

    public boolean isAuthorized(String pubKey) {
        return authorizedList.contains(pubKey);
    }

    @SneakyThrows
    public void createFileWatcher(Path path) {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                if (String.valueOf(event.context()).equals(path.toFile().getName())) {
                    log.info("Reloaded authorized keys");
                    authorizedList = new HashSet<>(Files.readAllLines(path));
                }
            }
            key.reset();
        }
    }

}
