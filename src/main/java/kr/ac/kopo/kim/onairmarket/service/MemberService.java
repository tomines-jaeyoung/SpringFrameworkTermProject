package kr.ac.kopo.kim.onairmarket.service;

import kr.ac.kopo.kim.onairmarket.domain.Member;
import kr.ac.kopo.kim.onairmarket.dto.SignupForm;
import kr.ac.kopo.kim.onairmarket.exception.ServiceException;
import kr.ac.kopo.kim.onairmarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회원가입과 Spring Security 로그인(DB 조회)을 담당 */
@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member signup(SignupForm form) {
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            throw new ServiceException("비밀번호 확인이 일치하지 않습니다.");
        }
        if (memberRepository.existsByUsername(form.getUsername())) {
            throw new ServiceException("이미 사용 중인 아이디입니다.");
        }
        Member member = new Member(form.getUsername(), passwordEncoder.encode(form.getPassword()), form.getName().trim());
        try {
            return memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {   // 같은 아이디로 동시에 가입한 경우 (UNIQUE)
            throw new ServiceException("이미 사용 중인 아이디입니다.");
        }
    }

    @Transactional(readOnly = true)
    public Member get(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("로그인 정보를 확인할 수 없습니다. 다시 로그인해 주세요."));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member m = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(m.getUsername()).password(m.getPassword()).roles(m.getRole()).build();
    }
}
