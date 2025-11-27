package com.tns.newscrawler.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*") // Lọc tất cả các request
public class SameSiteCookieFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // Thêm SameSite vào cookie thông qua header "Set-Cookie"
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    // Tạo header Set-Cookie với SameSite=None
                    String cookieHeader = cookie.getName() + "=" + cookie.getValue()
                            + "; Path=" + cookie.getPath()
                            + "; Max-Age=" + cookie.getMaxAge()
                            + "; HttpOnly=true; Secure=true; SameSite=None";
                    httpServletResponse.addHeader("Set-Cookie", cookieHeader);
                }
            }
        }

        chain.doFilter(request, response); // Tiếp tục chuỗi filter
    }

    @Override
    public void destroy() {
    }
}
