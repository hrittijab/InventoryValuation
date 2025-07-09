package com.bd.spectrum.BMDInfo_server.repository;

import com.bd.spectrum.BMDInfo_server.model.BidTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BidTrackerRepository extends JpaRepository<BidTracker, UUID> {

    List<BidTracker> findBySubmissionDateBetween(LocalDate fromDate, LocalDate toDate);

    @Query(value = """
        SELECT submission AS category, COUNT(*) AS total
        FROM bid_tracker
        WHERE submission IN ('Submitted', 'Not Submitted')
        GROUP BY submission

        UNION ALL

        SELECT 
          CASE 
            WHEN result = 'Won' THEN 'Won'
            WHEN result = 'Lost' THEN 'Lost'
            WHEN result = 'Pending' THEN 'Pending'
            ELSE 'Unknown'
          END AS category,
          COUNT(*) AS total
        FROM bid_tracker
        GROUP BY 
          CASE 
            WHEN result = 'Won' THEN 'Won'
            WHEN result = 'Lost' THEN 'Lost'
            WHEN result = 'Pending' THEN 'Pending'
            ELSE 'Unknown'
          END
    """, nativeQuery = true)
    List<Object[]> getSubmissionAndResultStats();

    @Query(value = """
    WITH Categories AS (
        SELECT 1 AS ord, 'Not Submitted' AS category
        UNION ALL SELECT 2, 'Submitted'
        UNION ALL SELECT 3, 'Lost'
        UNION ALL SELECT 4, 'Pending'
        UNION ALL SELECT 5, 'Unknown'
        UNION ALL SELECT 6, 'Won'
    ),
    SubmissionCounts AS (
        SELECT 
            submission AS category,
            COUNT(*) AS total
        FROM bid_tracker
        WHERE submission IN ('Submitted', 'Not Submitted')
          AND submission_date BETWEEN :from AND :to
        GROUP BY submission
    ),
    ResultCounts AS (
        SELECT 
            CASE 
                WHEN result = 'Won' THEN 'Won'
                WHEN result = 'Lost' THEN 'Lost'
                WHEN result = 'Pending' THEN 'Pending'
                ELSE 'Unknown'
            END AS category,
            COUNT(*) AS total
        FROM bid_tracker
        WHERE submission_date BETWEEN :from AND :to
        GROUP BY 
            CASE 
                WHEN result = 'Won' THEN 'Won'
                WHEN result = 'Lost' THEN 'Lost'
                WHEN result = 'Pending' THEN 'Pending'
                ELSE 'Unknown'
            END
    ),
    CombinedCounts AS (
        SELECT * FROM SubmissionCounts
        UNION ALL
        SELECT * FROM ResultCounts
    )
    SELECT 
        c.category,
        ISNULL(SUM(cc.total), 0) AS total
    FROM Categories c
    LEFT JOIN CombinedCounts cc ON c.category = cc.category
    GROUP BY c.category, c.ord
    ORDER BY c.ord;
""", nativeQuery = true)
    List<Object[]> getSubmissionAndResultStatsByDate(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );



    @Query(value = """
        WITH Months AS (
            SELECT FORMAT(DATEADD(MONTH, -n, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)), 'yyyy-MM') AS month
            FROM (VALUES (0),(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11)) AS X(n)
        ),
        SubmissionStats AS (
            SELECT 
                FORMAT(submission_date, 'yyyy-MM') AS month,
                submission,
                COUNT(*) AS total
            FROM bid_tracker
            WHERE 
                submission IN ('Submitted', 'Not Submitted') AND
                submission_date >= DATEADD(MONTH, -11, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
            GROUP BY 
                FORMAT(submission_date, 'yyyy-MM'),
                submission
        )
        SELECT 
            m.month,
            sType.submission,
            ISNULL(ss.total, 0) AS total
        FROM Months m
        CROSS JOIN (VALUES ('Submitted'), ('Not Submitted')) AS sType(submission)
        LEFT JOIN SubmissionStats ss 
            ON m.month = ss.month AND sType.submission = ss.submission
        ORDER BY m.month ASC, sType.submission;
        """, nativeQuery = true)
    List<Object[]> getMonthlySubmissionCounts();

    @Query(value = """
    SELECT 
      client,
      SUM(CASE WHEN submission = 'Submitted' THEN 1 ELSE 0 END) AS submitted_count,
      SUM(CASE WHEN submission = 'Not Submitted' THEN 1 ELSE 0 END) AS not_submitted_count
    FROM bid_tracker
    GROUP BY client
    ORDER BY client;
""", nativeQuery = true)
    List<Object[]> getClientSubmissionSummary();

    @Query(value = """
    SELECT 
      client,
      SUM(CASE WHEN submission = 'Submitted' THEN 1 ELSE 0 END) AS submitted_count,
      SUM(CASE WHEN submission = 'Not Submitted' THEN 1 ELSE 0 END) AS not_submitted_count
    FROM bid_tracker
    WHERE submission_date BETWEEN :from AND :to
    GROUP BY client
    ORDER BY client;
""", nativeQuery = true)
    List<Object[]> getClientSubmissionSummaryByDate(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

}