package com.github.lamtong.msp.mapper;

import com.github.lamtong.msp.entity.User;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import io.github.lamtong.msp.core.annotation.Mapper;
import io.github.lamtong.msp.core.mapper.BaseMapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    default User getUserById(Long id) {
        Index index = this.getIndex();
        User user;
        try {
            user = index.getDocument(String.valueOf(id), User.class);
        } catch (MeilisearchException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

}
