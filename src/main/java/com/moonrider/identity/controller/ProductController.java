package com.moonrider.identity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private  static  final List<String> PRODUCTS= List.of("Books","Pen","NoteBooks");
    @GetMapping
    public ResponseEntity<List<String>> getAllProducts() {
        return ResponseEntity.ok(PRODUCTS);
    }
}
