package kr.ac.kopo.kim.onairmarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupForm {
    @NotBlank(message = "아이디를 입력해 주세요.")
    @Pattern(regexp = "^[a-z0-9_]{4,20}$", message = "아이디는 영문 소문자·숫자·밑줄 4~20자로 입력해 주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해 주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름(닉네임)을 입력해 주세요.")
    @Size(min = 2, max = 20, message = "이름은 2~20자로 입력해 주세요.")
    private String name;
}
