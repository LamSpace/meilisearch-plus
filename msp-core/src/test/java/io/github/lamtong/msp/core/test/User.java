package io.github.lamtong.msp.core.test;

import io.github.lamtong.msp.core.annotation.Index;

@Index(value = "user")
public class User {

    private long id;

    private String name;

    private String password;

    private String address;

    private String birth;

    private String remark;

    public User() {
    }

    public User(long id, String name, String password, String address, String birth, String remark) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.address = address;
        this.birth = birth;
        this.remark = remark;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                ", birth='" + birth + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }

}
