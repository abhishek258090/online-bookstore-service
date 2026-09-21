package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.dto.ApiDtos.CreateBookRequest;
import com.example.bookstore.dto.ApiDtos.UpdateBookStock;
import com.example.bookstore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> list(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return bookService.availableBooks(pageable);
    }

    @PostMapping
    public BookResponse create(@Valid @RequestBody CreateBookRequest request) {
        return bookService.addBook(request);
    }

    @PutMapping("/{bookId}")
    public BookResponse update(
            @PathVariable long bookId,
            @Valid @RequestBody UpdateBookStock bookStock) {
        return bookService.updateBook(bookId, bookStock);
    }
}
