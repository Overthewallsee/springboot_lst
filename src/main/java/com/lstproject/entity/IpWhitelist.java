package com.lstproject.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ip_whitelist")
public class IpWhitelist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String ipAddress;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private boolean active = true;
}
