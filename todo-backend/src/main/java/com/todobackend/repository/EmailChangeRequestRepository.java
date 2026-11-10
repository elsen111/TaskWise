package com.todobackend.repository;

import com.todobackend.entity.EmailChangeRequestEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequestEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT e FROM EmailChangeRequestEntity e
    WHERE e.tokenHash = :tokenHash
""")
    Optional<EmailChangeRequestEntity> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    Optional<EmailChangeRequestEntity> findByUserIdAndUsedAtNull(UUID userId);

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
    UPDATE EmailChangeRequestEntity e
    SET e.usedAt = :now
    WHERE e.user.id = :userId
      AND e.usedAt IS NULL
""")
    int invalidatePreviousRequests(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );
}
