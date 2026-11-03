package com.ecotracker.config;

import com.ecotracker.entity.*;
import com.ecotracker.repository.*;
import com.ecotracker.service.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WasteTypeRepository wasteTypeRepository;
    private final CollectorRepository collectorRepository;
    private final PickupRequestRepository pickupRequestRepository;
    private final CollectorAssignmentRepository assignmentRepository;
    private final RequestStatusHistoryRepository statusHistoryRepository;
    private final RecyclingRecordRepository recyclingRecordRepository;
    private final UserStreakRepository userStreakRepository;
    private final PasswordEncoder passwordEncoder;
    private final RewardService rewardService;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           WasteTypeRepository wasteTypeRepository,
                           CollectorRepository collectorRepository,
                           PickupRequestRepository pickupRequestRepository,
                           CollectorAssignmentRepository assignmentRepository,
                           RequestStatusHistoryRepository statusHistoryRepository,
                           RecyclingRecordRepository recyclingRecordRepository,
                           UserStreakRepository userStreakRepository,
                           PasswordEncoder passwordEncoder,
                           RewardService rewardService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.wasteTypeRepository = wasteTypeRepository;
        this.collectorRepository = collectorRepository;
        this.pickupRequestRepository = pickupRequestRepository;
        this.assignmentRepository = assignmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.recyclingRecordRepository = recyclingRecordRepository;
        this.userStreakRepository = userStreakRepository;
        this.passwordEncoder = passwordEncoder;
        this.rewardService = rewardService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Initialize Roles
        Role roleUser = roleRepository.findByName(RoleType.ROLE_USER.name())
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_USER.name())));
        Role roleAdmin = roleRepository.findByName(RoleType.ROLE_ADMIN.name())
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_ADMIN.name())));
        Role roleCollector = roleRepository.findByName(RoleType.ROLE_COLLECTOR.name())
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_COLLECTOR.name())));

        // 2. Initialize Waste Types
        WasteType eWaste = wasteTypeRepository.findByCodeIgnoreCase("E_WASTE")
                .orElseGet(() -> wasteTypeRepository.save(new WasteType("E_WASTE", "Electronic Waste (E-Waste)", true)));
        WasteType dryWaste = wasteTypeRepository.findByCodeIgnoreCase("DRY_WASTE")
                .orElseGet(() -> wasteTypeRepository.save(new WasteType("DRY_WASTE", "Dry Recyclable Waste", true)));

        // 3. Initialize Admin User
        User adminUser = userRepository.findByEmailIgnoreCase("admin@ecotracker.com")
                .orElseGet(() -> {
                    User u = new User("EcoTracker Admin", "admin@ecotracker.com", passwordEncoder.encode("Admin@123"), "+1-555-0100");
                    u.addRole(roleAdmin);
                    u.addRole(roleUser);
                    User saved = userRepository.save(u);
                    userStreakRepository.save(new UserStreak(saved));
                    return saved;
                });

        // 4. Initialize Collector User & Collector Profile
        User collectorUser = userRepository.findByEmailIgnoreCase("collector@ecotracker.com")
                .orElseGet(() -> {
                    User u = new User("Alex Green (Collector)", "collector@ecotracker.com", passwordEncoder.encode("Collector@123"), "+1-555-0199");
                    u.addRole(roleCollector);
                    u.addRole(roleUser);
                    User saved = userRepository.save(u);
                    userStreakRepository.save(new UserStreak(saved));
                    return saved;
                });

        Collector collector = collectorRepository.findByUserId(collectorUser.getId())
                .orElseGet(() -> collectorRepository.save(new Collector(collectorUser, "Metro North & Central Zone", true)));

        // 5. Initialize Regular Demo Users
        User demoUser = userRepository.findByEmailIgnoreCase("user@ecotracker.com")
                .orElseGet(() -> {
                    User u = new User("John Doe", "user@ecotracker.com", passwordEncoder.encode("User@123"), "+1-555-0123");
                    u.addRole(roleUser);
                    User saved = userRepository.save(u);
                    UserStreak streak = new UserStreak(saved);
                    streak.setCurrentStreak(3);
                    streak.setLongestStreak(5);
                    streak.setLastQualifyingDate(LocalDate.now().minusDays(1));
                    userStreakRepository.save(streak);
                    return saved;
                });

        User janeUser = userRepository.findByEmailIgnoreCase("jane@ecotracker.com")
                .orElseGet(() -> {
                    User u = new User("Jane Smith", "jane@ecotracker.com", passwordEncoder.encode("User@123"), "+1-555-0145");
                    u.addRole(roleUser);
                    User saved = userRepository.save(u);
                    UserStreak streak = new UserStreak(saved);
                    streak.setCurrentStreak(1);
                    streak.setLongestStreak(2);
                    streak.setLastQualifyingDate(LocalDate.now());
                    userStreakRepository.save(streak);
                    return saved;
                });

        // 6. Seed Sample Pickup Requests if none exist
        if (pickupRequestRepository.count() == 0) {
            log.info("Seeding demo pickup requests and lifecycle data...");

            // Sample 1: COMPLETED request with recycling record & reward
            PickupRequest req1 = new PickupRequest(
                    demoUser,
                    eWaste,
                    "Old desktop computer, 2 faulty CRT monitors, and broken smartphone",
                    BigDecimal.valueOf(14.50),
                    "kg",
                    "742 Evergreen Terrace, Sector 4",
                    LocalDateTime.now().minusDays(2).withHour(10).withMinute(0),
                    "+1-555-0123"
            );
            req1.setStatus(PickupStatus.COMPLETED);
            req1 = pickupRequestRepository.save(req1);

            statusHistoryRepository.save(new RequestStatusHistory(req1, null, PickupStatus.PENDING, demoUser, "Pickup request submitted by user."));
            statusHistoryRepository.save(new RequestStatusHistory(req1, PickupStatus.PENDING, PickupStatus.APPROVED, adminUser, "Request approved."));
            statusHistoryRepository.save(new RequestStatusHistory(req1, PickupStatus.APPROVED, PickupStatus.ASSIGNED, adminUser, "Assigned to Alex Green."));
            statusHistoryRepository.save(new RequestStatusHistory(req1, PickupStatus.ASSIGNED, PickupStatus.PICKED_UP, collectorUser, "All items collected safely."));

            RecyclingRecord record1 = new RecyclingRecord(req1, "GreenCycle Electronics Recyclers Ltd.", LocalDateTime.now().minusDays(1), BigDecimal.valueOf(14.50), "Disassembled for precious metals and safe lead extraction.");
            recyclingRecordRepository.save(record1);
            req1.setRecyclingRecord(record1);

            statusHistoryRepository.save(new RequestStatusHistory(req1, PickupStatus.PICKED_UP, PickupStatus.COMPLETED, adminUser, "Collection confirmed at GreenCycle. Rewards issued."));
            rewardService.awardPointsForCompletedRequest(req1);

            // Sample 2: ASSIGNED request (assigned to collector)
            PickupRequest req2 = new PickupRequest(
                    demoUser,
                    dryWaste,
                    "Bundled cardboard boxes, clean newspaper, and PET water bottles",
                    BigDecimal.valueOf(8.00),
                    "kg",
                    "742 Evergreen Terrace, Sector 4",
                    LocalDateTime.now().plusDays(1).withHour(14).withMinute(30),
                    "+1-555-0123"
            );
            req2.setStatus(PickupStatus.ASSIGNED);
            req2 = pickupRequestRepository.save(req2);

            statusHistoryRepository.save(new RequestStatusHistory(req2, null, PickupStatus.PENDING, demoUser, "Pickup request submitted."));
            statusHistoryRepository.save(new RequestStatusHistory(req2, PickupStatus.PENDING, PickupStatus.APPROVED, adminUser, "Verified address and quantity."));
            CollectorAssignment assign2 = new CollectorAssignment(req2, collector, adminUser);
            assignmentRepository.save(assign2);
            statusHistoryRepository.save(new RequestStatusHistory(req2, PickupStatus.APPROVED, PickupStatus.ASSIGNED, adminUser, "Assigned to collector Alex Green."));

            // Sample 3: PENDING request awaiting admin approval
            PickupRequest req3 = new PickupRequest(
                    janeUser,
                    eWaste,
                    "3 dead laptop lithium batteries and discarded power adapters",
                    BigDecimal.valueOf(3.20),
                    "kg",
                    "108 Maple Avenue, Apartment 5B",
                    LocalDateTime.now().plusDays(2).withHour(11).withMinute(0),
                    "+1-555-0145"
            );
            req3.setStatus(PickupStatus.PENDING);
            req3 = pickupRequestRepository.save(req3);
            statusHistoryRepository.save(new RequestStatusHistory(req3, null, PickupStatus.PENDING, janeUser, "Pickup request submitted."));

            // Sample 4: PICKED_UP request awaiting recycling center completion
            PickupRequest req4 = new PickupRequest(
                    janeUser,
                    dryWaste,
                    "Clean shredded office paper and glass beverage bottles",
                    BigDecimal.valueOf(12.00),
                    "kg",
                    "108 Maple Avenue, Apartment 5B",
                    LocalDateTime.now().minusHours(3),
                    "+1-555-0145"
            );
            req4.setStatus(PickupStatus.PICKED_UP);
            req4 = pickupRequestRepository.save(req4);
            statusHistoryRepository.save(new RequestStatusHistory(req4, null, PickupStatus.PENDING, janeUser, "Submitted."));
            statusHistoryRepository.save(new RequestStatusHistory(req4, PickupStatus.PENDING, PickupStatus.APPROVED, adminUser, "Approved."));
            CollectorAssignment assign4 = new CollectorAssignment(req4, collector, adminUser);
            assignmentRepository.save(assign4);
            statusHistoryRepository.save(new RequestStatusHistory(req4, PickupStatus.APPROVED, PickupStatus.ASSIGNED, adminUser, "Assigned to collector Alex Green."));
            statusHistoryRepository.save(new RequestStatusHistory(req4, PickupStatus.ASSIGNED, PickupStatus.PICKED_UP, collectorUser, "Waste loaded into collection vehicle. En route to center."));

            log.info("Demo data seeding completed successfully.");
        }
    }
}
