package org.example.controller;

import org.example.dto.HelloDto;
import org.example.dto.HelloResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @PostMapping("/test")
    public ResponseEntity<HelloResponse> test(@RequestBody HelloDto helloDto) {
        HelloResponse body = new HelloResponse();
        body.setName(helloDto.getName());
        body.setMsg("Hello");
        return ResponseEntity.ok(body);
    }
}
