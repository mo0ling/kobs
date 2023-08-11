package com.kob.backend.Service.impl.talk;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kob.backend.Service.impl.util.UserDetailsImpl;
import com.kob.backend.Service.talk.GetListService;
import com.kob.backend.mapper.TalkMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.Talk;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class GetListServiceImpl implements GetListService {
    @Autowired
    private TalkMapper talkMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject getList(Integer page) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
        User user = loginUser.getUser();
        IPage<Talk> talkIPage = new Page<>(page, 10);
        QueryWrapper<Talk> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.orderByDesc("modifytime");
        List<Talk> talks = talkMapper.selectPage(talkIPage, queryWrapper).getRecords();
        JSONObject resp = new JSONObject();
        List<JSONObject> items = new LinkedList<>();

        for(Talk talk : talks){
            JSONObject item = new JSONObject();
            item.put("talk", talk);
            items.add(item);
        }

        resp.put("talks", items);
        resp.put("talks_count", talkMapper.selectCount(queryWrapper));
        return resp;
    }

    @Override
    public JSONObject getAllList(Integer page) {
        IPage<Talk> talkIPage = new Page<>(page, 10);
        QueryWrapper<Talk> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("modifytime");
        List<Talk> talks = talkMapper.selectPage(talkIPage, queryWrapper).getRecords();
        JSONObject resp = new JSONObject();
        List<JSONObject> items = new LinkedList<>();

        for(Talk talk : talks){
            User user = userMapper.selectById(talk.getUserId());
            JSONObject item = new JSONObject();
            item.put("photo", user.getPhoto());
            item.put("username", user.getUsername());
            item.put("talk", talk);
            items.add(item);
        }
        resp.put("talks", items);
        resp.put("talks_count", talkMapper.selectCount(null));
        return resp;
    }
}
