package com.bachelor.service_desk.security;

import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByNumberPhone(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with phone number " + username + " not found"
                ));

        return UserDetailsImpl.build(user);
    }
}
