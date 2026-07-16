package com.smartroad.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartroad.backend.model.AccidentReport;

public interface AccidentReportRepository
        extends JpaRepository<AccidentReport, Long> {

    List<AccidentReport> findByFullName(String fullName);

    @Query("""
    		SELECT
    		ROUND(a.latitude, 3),
    		ROUND(a.longitude, 3),
    		COUNT(a.id)
    		FROM AccidentReport a
    		WHERE a.latitude IS NOT NULL
    		AND a.longitude IS NOT NULL
    		GROUP BY ROUND(a.latitude,3), ROUND(a.longitude,3)
    		""")
    		List<Object[]> getHotspots();
    		
    		@Query("""
    				SELECT SUBSTRING(a.date, 1, 7), COUNT(a)
    				FROM AccidentReport a
    				GROUP BY SUBSTRING(a.date, 1, 7)
    				ORDER BY SUBSTRING(a.date, 1, 7)
    				""")
    				List<Object[]> getMonthlyReports();
}