package com.example.demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.MessageRequest;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.service.MessageService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/messages")
public class MessageController {
  private final MessageService messageService;
  public MessageController(MessageService messageService) { this.messageService = messageService; }

  @GetMapping
  public List<MessageResponse> listMine() {
    return messageService.listMine();
  }

  @PostMapping
  public MessageResponse post(@Valid @RequestBody MessageRequest req) {
    return messageService.create(req);
  }
}