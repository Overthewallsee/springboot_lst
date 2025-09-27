package com.lstproject.filter;

import com.lstproject.service.IpWhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class IpWhitelistFilter extends OncePerRequestFilter {
    private final IpWhitelistService ipWhitelistService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        
        // 检查IP是否在白名单中
        if (!ipWhitelistService.isIpWhitelisted(clientIp)) {
            log.warn("Blocked request from non-whitelisted IP: {}", clientIp);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access denied: IP not whitelisted");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
