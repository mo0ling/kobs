package com.kob.backend.Service.impl.talk;

import com.kob.backend.Service.impl.util.UserDetailsImpl;
import com.kob.backend.Service.talk.UpdateService;
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
public class UpdateServiceImpl implements UpdateService {

    @Autowired
    private TalkMapper talkMapper;
    @Override
    public Map<String, String> update(Map<String, String> data) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
        User user = loginUser.getUser();
        int bot_id = Integer.parseInt(data.get("bot_id"));
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
        Talk talk = talkMapper.selectById(bot_id);
        if(talk == null){
            map.put("error_message","帖子不存在或已经被删除");
            return map;
        }
        if(!talk.getUserId().equals(user.getId())){
            map.put("error_message","没有权限修改该帖子");
            return map;
        }
        Talk new_bot = new Talk(
                talk.getId(),
                user.getId(),
                title,
                content,
                talk.getCreatetime(),
                new Date()
        );
        talkMapper.updateById(new_bot);
        map.put("error_message","更新成功!");
        return map;
    }
}
