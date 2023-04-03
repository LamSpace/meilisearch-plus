package com.github.lamtong.msp.entity;

import io.github.lamtong.msp.core.annotation.Index;
import io.github.lamtong.msp.core.annotation.PrimaryKey;

@Index(value = "role")
public class Role {

    @PrimaryKey
    private long id;

    private String name;

    private String description;

    public Role() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

}
