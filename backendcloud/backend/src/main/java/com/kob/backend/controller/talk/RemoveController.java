package com.kob.backend.controller.talk;

import com.kob.backend.Service.talk.RemoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RemoveController {
    @Autowired
    private RemoveService removeService;

    @PostMapping("/api/talk/remove/")
    public Map<String, String> remove(@RequestParam Map<String, String> data){

        return removeService.remove(data);
    }
}
