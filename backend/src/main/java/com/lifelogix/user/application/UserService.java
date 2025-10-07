package com.lifelogix.user.application;

import com.lifelogix.config.jwt.JwtTokenProvider;
import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동으로 만들기 (DI)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입 비즈니스 로직
     **/
    @Transactional
    public User register(String email, String password, String username) {
        // 1. 이메일 중복 확인 -> "중복된_이메일로는_회원가입할_수_없다"
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 3. User 객체 생성 및 저장 -> "회원가입이_성공해야_한다"
        User newUser = User.builder()
                .email(email)
                .password(encodedPassword)
                .username(username)
                .build();

        return userRepository.save(newUser);
    }

    /**
     * 로그인 비즈니스 로직 (JWT 발급)
     **/
    @Transactional(readOnly = true)
    public String login(String email, String password) {
        // 1. 이메일로 사용자 조회 -> 존재하지 않는 사용자 로그인 시도 검증
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED));

        // 2. 비밀번호 일치 여부 확인 -> "잘못된_비밀번호로는_로그인할_수_없다"
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 3. 인증 성공 시 JWT 생성 및 반환 -> "올바른_정보로_로그인에_성공해야_한다"
        return jwtTokenProvider.generateToken(user);
    }
}
