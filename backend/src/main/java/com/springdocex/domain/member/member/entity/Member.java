package com.springdocex.domain.member.member.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.springdocex.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder // 부모 클래스까지 빌더로 사용할 수 있다(부모 클래스에도 @SuperBuilder를 적용해줘야 한다)
@EntityListeners(AuditingEntityListener.class)
public class Member extends BaseTime {

    @Column(length = 100, unique = true)
    private String username;
    @Column(length = 100)
    private String password;
    @Column(length = 100, unique = true)
    private String apiKey;
    @Column(length = 100)
    private String nickname;

    public boolean isAdmin() {
        return username.equals("admin");
    }

    public Collection<? extends GrantedAuthority> getAutorities() {
        // new SimpleGrantedAuthority("ROLE_USER"); 원래는 이런 형식인데, 우리는 STRING으로 관리하고 최종적으로 줄 떄만 이렇게 처리해서 주도록 해보자
        return getMemberAuthoritiesAsString()
            .stream()
            .map(SimpleGrantedAuthority::new) // Security에서 사용하는 형식으로
            .toList();
    }

    public List<String> getMemberAuthoritiesAsString() {
        List<String> authorities = new ArrayList<>();

        if (isAdmin()) {
            authorities.add("ROLE_ADMIN");
            // ROLE_을 붙여서, 접두어로 Security가 처리할 수 있는 부분이 있다. ADMIN 역할을 한다는 뜻
            // 기존 ADMIN_ACT 를 사용했을 때에서 바뀐 설정
            // hasAuthority("ADMIN_ACT") -> hasRole("ADMIN"): config에서 hasRole()로 설정할 수 있다.
            // 접두어 ROLE_ 은 없애서 적용해야 한다.
        }

        return authorities;
    }
}