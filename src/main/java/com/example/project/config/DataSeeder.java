package com.example.project.config;

import com.example.project.entity.Booking;
import com.example.project.entity.Court;
import com.example.project.entity.CourtImage;
import com.example.project.entity.PasswordResetToken;
import com.example.project.entity.TimeSlot;
import com.example.project.entity.TokenBlacklist;
import com.example.project.entity.User;
import com.example.project.enums.BookingStatus;
import com.example.project.enums.Role;
import com.example.project.enums.UserStatus;
import com.example.project.repository.BookingRepository;
import com.example.project.repository.CourtImageRepository;
import com.example.project.repository.CourtRepository;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.TimeSlotRepository;
import com.example.project.repository.TokenBlacklistRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = seedUser("admin", "admin@gmail.com", "Administrator", "0900000001", Role.ADMIN);
        User manager = seedUser("manager", "manager@gmail.com", "Court Manager", "0900000002", Role.MANAGER);
        User customer1 = seedUser("customer1", "customer1@gmail.com", "Nguyen Van A", "0900000003", Role.CUSTOMER);
        User customer2 = seedUser("customer2", "customer2@gmail.com", "Tran Thi B", "0900000004", Role.CUSTOMER);
        User bannedCustomer = seedUser("banneduser", "banned@gmail.com", "Blocked Customer", "0900000005", Role.CUSTOMER);
        if (bannedCustomer.getStatus() != UserStatus.BANNED) {
            bannedCustomer.setStatus(UserStatus.BANNED);
            userRepository.save(bannedCustomer);
        }

        List<Court> courts = seedCourts();
        seedTimeSlots(courts);
        seedCourtImages(courts);
        seedBookings(customer1, customer2, courts);
        seedPasswordResetToken(customer1);
        seedTokenBlacklist();

        System.out.println("=== SAMPLE DATA SEEDED SUCCESSFULLY ===");
        System.out.println("Accounts: admin/123456, manager/123456, customer1/123456, customer2/123456");
        System.out.println("Reset token sample: reset-token-customer1");
    }

    private User seedUser(String username, String email, String fullName, String phoneNumber, Role role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .fullName(fullName)
                        .email(email)
                        .phoneNumber(phoneNumber)
                        .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                        .role(role)
                        .status(UserStatus.ACTIVE)
                        .build()));
    }

    private List<Court> seedCourts() {
        List<Court> courts = new ArrayList<>();
        courts.add(seedCourt("San 1", "San cau long tieu chuan, mat san chong tron", BigDecimal.valueOf(100000), true));
        courts.add(seedCourt("San 2", "San cau long VIP, anh sang tot", BigDecimal.valueOf(150000), true));
        courts.add(seedCourt("San 3", "San cau long may lanh", BigDecimal.valueOf(200000), true));
        courts.add(seedCourt("San 4", "San dang bao tri de test trang thai unavailable", BigDecimal.valueOf(120000), false));
        return courts;
    }

    private Court seedCourt(String name, String description, BigDecimal pricePerSlot, boolean available) {
        return courtRepository.findAll().stream()
                .filter(court -> court.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> courtRepository.save(Court.builder()
                        .name(name)
                        .description(description)
                        .pricePerSlot(pricePerSlot)
                        .available(available)
                        .build()));
    }

    private void seedTimeSlots(List<Court> courts) {
        for (Court court : courts) {
            if (!timeSlotRepository.findByCourtIdAndIsActiveTrue(court.getId()).isEmpty()) {
                continue;
            }

            List<TimeSlot> slots = List.of(
                    slot(court, 6, 0, 8, 0),
                    slot(court, 8, 0, 10, 0),
                    slot(court, 10, 0, 12, 0),
                    slot(court, 14, 0, 16, 0),
                    slot(court, 16, 0, 18, 0),
                    slot(court, 18, 0, 20, 0),
                    slot(court, 20, 0, 22, 0)
            );
            timeSlotRepository.saveAll(slots);
        }
    }

    private TimeSlot slot(Court court, int startHour, int startMinute, int endHour, int endMinute) {
        return TimeSlot.builder()
                .court(court)
                .startTime(LocalTime.of(startHour, startMinute))
                .endTime(LocalTime.of(endHour, endMinute))
                .isActive(true)
                .build();
    }

    private void seedCourtImages(List<Court> courts) {
        for (Court court : courts) {
            if (!courtImageRepository.findByCourtId(court.getId()).isEmpty()) {
                continue;
            }

            courtImageRepository.save(CourtImage.builder()
                    .court(court)
                    .imageUrl("https://res.cloudinary.com/demo/image/upload/sample-badminton-" + court.getId() + "-1.jpg")
                    .publicId("seed/badminton-court-" + court.getId() + "-1")
                    .build());

            courtImageRepository.save(CourtImage.builder()
                    .court(court)
                    .imageUrl("https://res.cloudinary.com/demo/image/upload/sample-badminton-" + court.getId() + "-2.jpg")
                    .publicId("seed/badminton-court-" + court.getId() + "-2")
                    .build());
        }
    }

    private void seedBookings(User customer1, User customer2, List<Court> courts) {
        if (bookingRepository.count() > 0) {
            return;
        }

        Court court1 = courts.get(0);
        Court court2 = courts.get(1);
        Court court3 = courts.get(2);

        List<TimeSlot> court1Slots = timeSlotRepository.findByCourtIdAndIsActiveTrue(court1.getId());
        List<TimeSlot> court2Slots = timeSlotRepository.findByCourtIdAndIsActiveTrue(court2.getId());
        List<TimeSlot> court3Slots = timeSlotRepository.findByCourtIdAndIsActiveTrue(court3.getId());

        bookingRepository.save(booking(customer1, court1, court1Slots.get(0), LocalDate.now().plusDays(1),
                BookingStatus.PENDING, "Seed booking pending"));
        bookingRepository.save(booking(customer1, court1, court1Slots.get(1), LocalDate.now().plusDays(2),
                BookingStatus.CONFIRMED, "Seed booking confirmed"));
        bookingRepository.save(booking(customer2, court2, court2Slots.get(2), LocalDate.now().plusDays(3),
                BookingStatus.REJECTED, "Seed booking rejected"));
        bookingRepository.save(booking(customer2, court2, court2Slots.get(3), LocalDate.now().plusDays(4),
                BookingStatus.CANCELLED, "Seed booking cancelled"));
        bookingRepository.save(booking(customer1, court3, court3Slots.get(4), LocalDate.now().minusDays(1),
                BookingStatus.CHECKED_IN, "Seed booking checked in"));
    }

    private Booking booking(User customer, Court court, TimeSlot timeSlot, LocalDate date,
                            BookingStatus status, String note) {
        return Booking.builder()
                .customer(customer)
                .court(court)
                .timeSlot(timeSlot)
                .bookingDate(date)
                .status(status)
                .totalPrice(court.getPricePerSlot())
                .note(note)
                .build();
    }

    private void seedPasswordResetToken(User customer) {
        if (passwordResetTokenRepository.findByToken("reset-token-customer1").isPresent()) {
            return;
        }

        passwordResetTokenRepository.deleteByUserId(customer.getId());
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .token("reset-token-customer1")
                .user(customer)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());
    }

    private void seedTokenBlacklist() {
        if (tokenBlacklistRepository.existsByToken("seed-revoked-token")) {
            return;
        }

        tokenBlacklistRepository.save(TokenBlacklist.builder()
                .token("seed-revoked-token")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());
    }
}
