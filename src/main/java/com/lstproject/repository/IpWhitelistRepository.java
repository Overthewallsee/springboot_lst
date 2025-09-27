package com.lstproject.repository;

import com.lstproject.entity.IpWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpWhitelistRepository extends JpaRepository<IpWhitelist, Long> {
    boolean existsByIpAddressAndActive(String ipAddress, boolean active);
}
