package co.edu.puj.secchub_backend.integration.service;


import co.edu.puj.secchub_backend.integration.dto.AcademicRequestItemDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleDTO;
import co.edu.puj.secchub_backend.integration.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized business validations for HU01 payload.
 */
@Service
public class ValidationService {

    /**
     * Validates a single course request:
     * - startDate <= endDate
     * - requestedQuota >= 1
     * - at least one schedule
     * - schedules have start<end and no overlaps per day
     */
    public void validateItem(AcademicRequestItemDTO item) {
        if (item.getCapacity() == null || item.getCapacity() < 1) {
            throw new BusinessException("capacity must be >= 1");
        }
        if (item.getStartDate() == null || item.getEndDate() == null
                || item.getStartDate().isAfter(item.getEndDate())) {
            throw new BusinessException("Invalid dates: startDate must be <= endDate");
        }
        List<RequestScheduleDTO> schedules = item.getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            throw new BusinessException("At least one schedule is required");
        }
        validateSchedules(schedules);
    }

    private void validateSchedules(List<RequestScheduleDTO> schedules) {
        Map<String, List<int[]>> perDay = new HashMap<>();
        for (RequestScheduleDTO s : schedules) {
            int start = toIntHHmm(LocalTime.parse(s.getStartTime()));
            int end   = toIntHHmm(LocalTime.parse(s.getEndTime()));
            if (start >= end) throw new BusinessException("Schedule start must be before end");
            // Simple overlap check per day
            perDay.computeIfAbsent(s.getDay(), k -> new java.util.ArrayList<>()).add(new int[]{start, end});
        }
        perDay.values().forEach(ranges -> {
            ranges.sort(java.util.Comparator.comparingInt(a -> a[0]));
            for (int i = 1; i < ranges.size(); i++) {
                if (ranges.get(i)[0] < ranges.get(i-1)[1]) {
                    throw new BusinessException("Overlapping schedules detected in the same day");
                }
            }
        });
    }

    private int toIntHHmm(LocalTime t) {
        return t.getHour() * 100 + t.getMinute();
    }
}
