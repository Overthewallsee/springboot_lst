package com.lstproject.service;

import com.lstproject.entity.IpWhitelist;
import com.lstproject.repository.IpWhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IpWhitelistService {
    private final IpWhitelistRepository ipWhitelistRepository;
    
    @Transactional(readOnly = true)
    public boolean isIpWhitelisted(String ipAddress) {
        try {
            return ipWhitelistRepository.existsByIpAddressAndActive(ipAddress, true);
        } catch (Exception e) {
            // 如果出现数据库连接问题，记录日志并返回false（不授权访问）
            System.err.println("Database error while checking IP whitelist: " + e.getMessage());
            return false;
        }
    }
    
    @Transactional
    public IpWhitelist addIpToWhitelist(String ipAddress, String description) {
        IpWhitelist ipWhitelist = new IpWhitelist();
        ipWhitelist.setIpAddress(ipAddress);
        ipWhitelist.setDescription(description);
        ipWhitelist.setCreatedAt(LocalDateTime.now());
        ipWhitelist.setActive(true);
        return ipWhitelistRepository.save(ipWhitelist);
    }
    
    @Transactional(readOnly = true)
    public List<IpWhitelist> getAllWhitelistedIps() {
        return ipWhitelistRepository.findAll();
    }
    
    @Transactional
    public void removeIpFromWhitelist(Long id) {
        ipWhitelistRepository.deleteById(id);
    }
}