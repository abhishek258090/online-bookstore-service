package com.example.bookstore.service.impl;

import com.example.bookstore.constants.Constants;
import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.dto.ApiDtos.CreateBookRequest;
import com.example.bookstore.dto.ApiDtos.UpdateBookStock;
import com.example.bookstore.exception.ApiException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository books) {
        this.bookRepository = books;
    }

    @Override
    public List<BookResponse> availableBooks(Pageable pageable) {
        return bookRepository
                .findByStockGreaterThan(0, pageable)
                .map(BookServiceImpl::toResponse)
                .getContent();
    }

    @Override
    public BookResponse addBook(CreateBookRequest request) {
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .price(request.price())
                .stock(request.stock())
                .build();
        return toResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse updateBook(long bookId, UpdateBookStock bookStock) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, Constants.BOOK_NOT_FOUND_ERROR));
        book.setStock(bookStock.stock());
        return toResponse(bookRepository.save(book));
    }

    private static BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice(), book.getStock());
    }
}
