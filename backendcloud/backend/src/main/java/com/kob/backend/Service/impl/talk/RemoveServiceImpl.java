package com.kob.backend.Service.impl.talk;

import com.kob.backend.Service.impl.util.UserDetailsImpl;
import com.kob.backend.Service.talk.RemoveService;
import com.kob.backend.mapper.TalkMapper;
import com.kob.backend.pojo.Talk;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RemoveServiceImpl implements RemoveService {

    @Autowired
    private TalkMapper talkMapper;

    @Override
    public Map<String, String> remove(Map<String, String> data) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
        User user = loginUser.getUser();
        int talk_id = Integer.parseInt(data.get("talk_id"));
        Talk talk = talkMapper.selectById(talk_id);


        Map<String, String> map = new HashMap<>();
        if(talk == null){
            map.put("error_message", "帖子不存在或已被删除");
            return map;
        }
        if(!talk.getUserId().equals(user.getId())){
            map.put("error_message", "没有权限删除");
            return map;
        }
        talkMapper.deleteById(talk_id);
        map.put("error_message", "success");
        return map;
    }
}
