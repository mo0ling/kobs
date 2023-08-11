package com.kob.backend.Service.impl.talk;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.Service.impl.util.UserDetailsImpl;
import com.kob.backend.Service.talk.AddService;
import com.kob.backend.mapper.TalkMapper;
import com.kob.backend.pojo.Talk;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {

    @Autowired
    private TalkMapper talkMapper;
    @Override
    public Map<String, String> add(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();


        String title = data.get("title");
        String content = data.get("content");

        Map<String, String> map = new HashMap<>();
        if(title == null || title.length() == 0){
            map.put("error_message", "标题不能为空!");
            return map;
        }
        if(title.length() > 100){
            map.put("error_message", "标题长度不能大于100!");
            return map;
        }

        if(content.length() > 10000){
            map.put("error_message", "内容长度不能超过10000!");
            return map;
        }

        Date now = new Date();
        Talk talk = new Talk(null, user.getId(), title, content, now, now);
        talkMapper.insert(talk);
        map.put("error_message", "success");
        return map;
    }
}
