package com.lstproject.service;

import com.lstproject.entity.IpWhitelist;
import com.lstproject.repository.IpWhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IpWhitelistService {
    private final IpWhitelistRepository ipWhitelistRepository;
    
    public boolean isIpWhitelisted(String ipAddress) {
        return ipWhitelistRepository.existsByIpAddressAndActive(ipAddress, true);
    }
    
    public IpWhitelist addIpToWhitelist(String ipAddress, String description) {
        IpWhitelist ipWhitelist = new IpWhitelist();
        ipWhitelist.setIpAddress(ipAddress);
        ipWhitelist.setDescription(description);
        ipWhitelist.setCreatedAt(LocalDateTime.now());
        ipWhitelist.setActive(true);
        return ipWhitelistRepository.save(ipWhitelist);
    }
    
    public List<IpWhitelist> getAllWhitelistedIps() {
        return ipWhitelistRepository.findAll();
    }
    
    public void removeIpFromWhitelist(Long id) {
        ipWhitelistRepository.deleteById(id);
    }
}
