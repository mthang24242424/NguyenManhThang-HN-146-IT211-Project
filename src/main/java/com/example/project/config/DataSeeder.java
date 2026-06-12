package com.example.project.config;

import com.example.project.entity.Court;
import com.example.project.entity.TimeSlot;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.repository.CourtRepository;
import com.example.project.repository.TimeSlotRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Seed Admin
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

            User admin = User.builder()
                    .username("admin")
                    .fullName("Administrator")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);

            System.out.println("=== ADMIN CREATED ===");
        }

        // Seed Manager
        if (userRepository.findByEmail("manager@gmail.com").isEmpty()) {

            User manager = User.builder()
                    .username("manager")
                    .fullName("Court Manager")
                    .email("manager@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.MANAGER)
                    .build();

            userRepository.save(manager);

            System.out.println("=== MANAGER CREATED ===");
        }

        // Seed Court + TimeSlot
        if (courtRepository.count() > 0) {
            return;
        }

        List<Court> courts = List.of(
                Court.builder()
                        .name("San 1")
                        .description("San cau long tieu chuan")
                        .pricePerSlot(BigDecimal.valueOf(100000))
                        .available(true)
                        .build(),

                Court.builder()
                        .name("San 2")
                        .description("San cau long VIP")
                        .pricePerSlot(BigDecimal.valueOf(150000))
                        .available(true)
                        .build(),

                Court.builder()
                        .name("San 3")
                        .description("San cau long may lanh")
                        .pricePerSlot(BigDecimal.valueOf(200000))
                        .available(true)
                        .build()
        );

        courtRepository.saveAll(courts);

        List<TimeSlot> slots = new ArrayList<>();

        for (Court court : courtRepository.findAll()) {

            slots.add(TimeSlot.builder()
                    .court(court)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(10, 0))
                    .isActive(true)
                    .build());

            slots.add(TimeSlot.builder()
                    .court(court)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(12, 0))
                    .isActive(true)
                    .build());

            slots.add(TimeSlot.builder()
                    .court(court)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(16, 0))
                    .isActive(true)
                    .build());

            slots.add(TimeSlot.builder()
                    .court(court)
                    .startTime(LocalTime.of(18, 0))
                    .endTime(LocalTime.of(20, 0))
                    .isActive(true)
                    .build());
        }

        timeSlotRepository.saveAll(slots);

        System.out.println("=== SAMPLE DATA SEEDED SUCCESSFULLY ===");
    }
}