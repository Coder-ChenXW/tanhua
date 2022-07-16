package com.tanhua.dubbo.api;

import com.tanhua.model.domain.UserInfo;

public interface UserInfoApi {
    public void save(UserInfo userInfo);

    void update(UserInfo userInfo);
}
