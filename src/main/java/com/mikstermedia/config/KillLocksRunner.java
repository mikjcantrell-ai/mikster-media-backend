package com.mikstermedia.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KillLocksRunner implements ApplicationRunner {

    private final EntityManager em;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(ApplicationArguments args) {
        log.info("STARTUP: Attempting to kill zombie MySQL connections before accepting traffic...");
        try {
            List<Object[]> processList = em.createNativeQuery("SHOW FULL PROCESSLIST").getResultList();
            for (Object[] row : processList) {
                Long id = ((Number) row[0]).longValue();
                String command = (String) row[4];
                Integer time = ((Number) row[5]).intValue();
                
                // Kill ANY connection (Query or Sleep) older than 30 seconds
                if (time > 30) {
                    try {
                        em.createNativeQuery("KILL " + id).executeUpdate();
                        log.info("Killed zombie connection ID {} (sleeping for {}s)", id, time);
                    } catch (Exception killEx) {
                        log.warn("Failed to kill connection ID {}: {}", id, killEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to read process list: {}", e.getMessage());
        }
    }
}
