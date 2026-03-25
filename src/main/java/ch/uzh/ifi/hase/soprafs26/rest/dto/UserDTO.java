package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.Date;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

public class UserDTO {
    
    private Long id;
    private String token;
    private String username;
    private String bio;
    private UserStatus status;
    private Date creationDate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public UserStatus getStatus() {
        return status;
    }
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
