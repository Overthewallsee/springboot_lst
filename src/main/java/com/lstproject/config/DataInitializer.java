package com.lstproject.config;

import com.lstproject.entity.IpWhitelist;
import com.lstproject.repository.IpWhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final IpWhitelistRepository ipWhitelistRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在本地IP白名单记录
        if (!ipWhitelistRepository.existsByIpAddressAndActive("127.0.0.1", true)) {
            // 添加本地IP到白名单
            IpWhitelist localIp = new IpWhitelist();
            localIp.setIpAddress("127.0.0.1");
            localIp.setDescription("本地开发环境IP");
            localIp.setCreatedAt(LocalDateTime.now());
            localIp.setActive(true);
            ipWhitelistRepository.save(localIp);

            logger.info("已添加本地IP 127.0.0.1 到白名单");
        }
        
        // 检查是否已存在localhost IP白名单记录
        if (!ipWhitelistRepository.existsByIpAddressAndActive("0:0:0:0:0:0:0:1", true)) {
            // 添加localhost IPv6到白名单
            IpWhitelist localIp = new IpWhitelist();
            localIp.setIpAddress("0:0:0:0:0:0:0:1");
            localIp.setDescription("本地开发环境IPv6");
            localIp.setCreatedAt(LocalDateTime.now());
            localIp.setActive(true);
            ipWhitelistRepository.save(localIp);

            logger.info("已添加本地IP 0:0:0:0:0:0:0:1 到白名单");
        }
    }
}