package com.example.microlink_statistics.repository;

import com.example.microlink_statistics.entity.DailyUserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyUserActivityRepository extends JpaRepository<DailyUserActivity, Long> {

    Optional<DailyUserActivity> findByDate(LocalDate date);
}
