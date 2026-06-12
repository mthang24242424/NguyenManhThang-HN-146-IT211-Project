package com.example.project.config;

import com.example.project.dto.response.BookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // ===== Pointcut: Bao quanh phương thức createBooking tại Service layer =====
    @Pointcut("execution(* com.example.project.service.impl.BookingServiceImpl.createBooking(..))")
    public void bookingCreationPointcut() {}

    // ===== @AfterReturning: Ghi log khi đặt sân THÀNH CÔNG (UC-04) =====
    @AfterReturning(
            pointcut = "bookingCreationPointcut()",
            returning = "result"
    )
    public void logBookingSuccess(JoinPoint joinPoint, Object result) {
        if (result instanceof BookingResponse booking) {
            log.info("[AUDIT - SUCCESS] Khách hàng {} đặt thành công {} vào ngày {}, Khung giờ {}-{}",
                    booking.getCustomerName(),
                    booking.getCourtName(),
                    booking.getBookingDate(),
                    booking.getStartTime(),
                    booking.getEndTime());
        }
    }

    // ===== @AfterThrowing: Ghi log khi đặt sân THẤT BẠI (UC-04) =====
    @AfterThrowing(
            pointcut = "bookingCreationPointcut()",
            throwing = "ex"
    )
    public void logBookingFailure(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        // args[0] = BookingRequest, args[1] = username
        String username = args.length > 1 ? String.valueOf(args[1]) : "unknown";

        log.warn("[AUDIT - FAILED] Khách hàng {} cố gắng đặt sân nhưng thất bại. Lý do: {}",
                username, ex.getMessage());
    }

    // ===== Pointcut tổng quát: Log tất cả các API call (Request/Response logging) =====
    @Pointcut("within(com.example.project.controller..*)")
    public void controllerLayer() {}

    @Before("controllerLayer()")
    public void logRequest(JoinPoint joinPoint) {
        log.debug("[REQUEST] Calling: {}.{}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "controllerLayer()", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        log.debug("[RESPONSE] {}.{} completed successfully",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "controllerLayer()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        log.error("[EXCEPTION] {}.{} threw: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }
}

