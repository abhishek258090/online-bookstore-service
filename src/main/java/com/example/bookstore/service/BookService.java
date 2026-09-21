package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.dto.ApiDtos.CreateBookRequest;
import com.example.bookstore.dto.ApiDtos.UpdateBookStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    List<BookResponse> availableBooks(Pageable pageable);
    BookResponse addBook(CreateBookRequest request);
    BookResponse updateBook(long bookId, UpdateBookStock bookStock);
}
