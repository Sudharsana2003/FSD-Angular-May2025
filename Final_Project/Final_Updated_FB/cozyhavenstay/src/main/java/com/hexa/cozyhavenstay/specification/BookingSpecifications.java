package com.hexa.cozyhavenstay.specification;

import com.hexa.cozyhavenstay.model.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class BookingSpecifications {

    public static Specification<Booking> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.upper(root.get("bookingStatus")), status.toUpperCase());
        };
    }

    public static Specification<Booking> hasMinFare(Double minFare) {
        return (root, query, criteriaBuilder) -> {
            if (minFare == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("totalFare"), BigDecimal.valueOf(minFare));
        };
    }

    public static Specification<Booking> hasMaxFare(Double maxFare) {
        return (root, query, criteriaBuilder) -> {
            if (maxFare == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("totalFare"), BigDecimal.valueOf(maxFare));
        };
    }

    public static Specification<Booking> hasUserId(Integer userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("userId"), userId);
        };
    }
}
