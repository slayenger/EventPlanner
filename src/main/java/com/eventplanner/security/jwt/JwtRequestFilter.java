package com.eventplanner.security.jwt;


import com.eventplanner.dtos.CustomUserDetailsDTO;
import com.eventplanner.services.impl.UsersServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final UsersServiceImpl usersService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer "))
        {
            jwt = authHeader.substring(7);
            try
            {
                username = jwtTokenUtils.getUsername(jwt);

                CustomUserDetailsDTO customUserDetailsDTO = (CustomUserDetailsDTO) usersService.loadUserByUsername(username);
                System.out.println(customUserDetailsDTO);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
                {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            customUserDetailsDTO,
                            null,
                            jwtTokenUtils.getRoles(jwt).stream()
                                    .map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                    );
                    SecurityContextHolder.getContext().setAuthentication(token);
                }
            }
            catch (ExpiredJwtException e)
            {
                //TODO если время жизни вышло, то - (?)
                log.debug("Token expired");
            }
            catch (SignatureException e)
            {
                log.debug("Incorrect signature");
            }
        }

        filterChain.doFilter(request,response);
    }
}
