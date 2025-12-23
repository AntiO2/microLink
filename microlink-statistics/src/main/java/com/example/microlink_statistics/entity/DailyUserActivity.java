package com.example.microlink_statistics.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

/**
 * 用户日活跃度 (DAU) 统计实体类。
 *
 * @author Rolland1944
 */
@Entity
@Table(name = "daily_user_activity")
@Data
public class DailyUserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 统计日期。
     */
    @Column(unique = true, nullable = false)
    private LocalDate date;

    /**
     * 当日的活跃用户总数。
     */
    @Column(nullable = false)
    private Long dauCount;
}
