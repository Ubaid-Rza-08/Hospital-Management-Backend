package com.ubaid.Auth_service.service;



import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import com.ubaid.Auth_service.config.TwilioConfig;
import com.ubaid.Auth_service.dto.LoginResponseDto;
import com.ubaid.Auth_service.entity.User;
import com.ubaid.Auth_service.entity.type.AuthProviderType;
import com.ubaid.Auth_service.entity.type.RoleType;
import com.ubaid.Auth_service.error.InvalidOtpException;
import com.ubaid.Auth_service.repository.UserRepository;
import com.ubaid.Auth_service.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {


    private final UserRepository userRepository;
    private final TwilioConfig twilioConfig;
     private final StringRedisTemplate redisTemplate;
     private final AuthUtil authUtil;

    private static final SecureRandom secureRandom = new SecureRandom();
    public String generateAndSendOtp(String phoneNumber) {
        try {
            String otp = generateOtp();

            sendOtpSms(phoneNumber, otp);
            redisTemplate.opsForValue().set(phoneNumber,otp,5, TimeUnit.MINUTES);
            return "OTP sent successfully to " + phoneNumber;

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP: " + e.getMessage());
        }
    }
    public LoginResponseDto verifyOtpAndGenerateToken(String phoneNumber, String otp) {

        String storedOtp = redisTemplate.opsForValue().get(phoneNumber);
        boolean isValid = storedOtp != null && storedOtp.equals(otp);

        if (isValid) {
            User user = userRepository.findByPhone(phoneNumber);
            if (user == null) {
                user = User.builder()
                        .providerType(AuthProviderType.PHONE)
                        .phone(phoneNumber)
                        .build();
                user = userRepository.save(user);
            }
            String jwt = authUtil.generateAccessToken(user);

            // Convert roles to Set<String> - FIXED
            Set<String> roles = user.getRoles().stream()
                    .map(RoleType::name)
                    .collect(Collectors.toSet());

            return new LoginResponseDto(jwt, user.getId(), user.getUsername(), roles);
        }
        else {
            throw new InvalidOtpException("OTP is not valid");
        }

    }

    private String generateOtp() {
        int otp = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
    private void sendOtpSms(String phoneNumber, String otp) {
        String messageBody = String.format(
                "Hi , your verification code is: %s. This code will expire in %d minutes. " +
                        "Please do not share this code with anyone.",
                otp, 5
        );
        Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioConfig.getTwilioPhoneNumber()),
                messageBody
        ).create();
    }
}