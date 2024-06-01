package com.nelmin.blog.common.model;

import com.nelmin.blog.common.abstracts.IUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "\"user\"")
public class User implements IUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String nickName;

    // TODO remove
    @Column(nullable = false)
    private String password;

    @CreatedDate
    private LocalDateTime registrationDate;

    @LastModifiedDate
    private LocalDateTime updateTime;

    private LocalDateTime lastLoginDate;

    private Boolean enabled = true;

    private byte [] image = new byte[0];

    @Override
    public Boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (!id.equals(user.id)) {
            return false;
        }

        if (!username.equals(user.username)) {
            return false;
        }

        if (!nickName.equals(user.nickName)) {
            return false;
        }

        if (!password.equals(user.password)) {
            return false;
        }

        return registrationDate.equals(user.registrationDate);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + nickName.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + registrationDate.hashCode();
        return result;
    }

    @Repository
    public interface Repo extends JpaRepository<User, Long> {
        Optional<User> findUserByUsername(String username);
    }
}