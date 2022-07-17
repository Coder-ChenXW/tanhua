package com.tanhua.server.interceptor;

import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.User;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头
        String token = request.getHeader("Authorization");

        //使用工具类判断token是否有效
        boolean verifyToken = JwtUtils.verifyToken(token);

        if (!verifyToken) {
            response.setStatus(401);
            return false;
        }

        //正常方向

        //解析token，存入Threadlocal
        Claims claims = JwtUtils.getClaims(token);
        String mobile = (String) claims.get("mobile");

        Integer id= (Integer) claims.get("id");

        User user=new User();
        user.setId(Long.valueOf(id));
        user.setMobile(mobile);

        UserHolder.set(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }
}
