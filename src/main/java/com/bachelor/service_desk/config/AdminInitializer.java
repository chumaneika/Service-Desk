package com.bachelor.service_desk.config;

import com.bachelor.service_desk.entity.Role;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {

        return args -> {

            boolean exists = userRepository
                    .existsByRole(Role.SUPER_ADMIN);

            if (!exists) {

                UserEntity admin = new UserEntity();

                admin.changeName("Malik");
                admin.changeSurname("Аликберов");
                admin.changeRole(Role.SUPER_ADMIN);
                admin.changeNumberPhone("+10000000000");
                admin.changeEnabled(true);
                admin.changePassword(passwordEncoder.encode("admin09012005"));

                userRepository.save(admin);

                System.out.println("SUPER_ADMIN created");
            }
        };
    }
}
