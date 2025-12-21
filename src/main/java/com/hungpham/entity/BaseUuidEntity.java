package com.hungpham.entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseUuidEntity {
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private byte[] id;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedDate;
}
