package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.dto.ApiDtos.CreateBookRequest;
import com.example.bookstore.dto.ApiDtos.UpdateBookStock;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    private BookController controller;

    @BeforeEach
    void setUp() {
        controller = new BookController(bookService);
    }

    @Test
    void list_shouldReturnAvailableBooks() {
        PageRequest pageable = PageRequest.of(0, 20);
        List<BookResponse> books = List.of(
                new BookResponse(1L, "Clean Code", "Robert Martin",
                        new BigDecimal("50.00"), 10));

        when(bookService.availableBooks(pageable)).thenReturn(books);

        List<BookResponse> result = controller.list(pageable);

        assertSame(books, result);
        verify(bookService).availableBooks(pageable);
    }

    @Test
    void list_shouldPropagateServiceException() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(bookService.availableBooks(pageable))
                .thenThrow(new RuntimeException("Database unavailable"));

        assertThrows(RuntimeException.class, () -> controller.list(pageable));
    }

    @Test
    void create_shouldDelegateToService() {
        CreateBookRequest request = CreateBookRequest.builder()
                .title("Clean Code")
                .author("Robert Martin")
                .price(BigDecimal.TEN)
                .stock(10)
                .build();
        BookResponse response = new BookResponse(
                1L, "Clean Code", "Robert Martin", BigDecimal.TEN, 10);

        when(bookService.addBook(request)).thenReturn(response);

        assertSame(response, controller.create(request));
        verify(bookService).addBook(request);
    }

    @Test
    void update_shouldDelegateToService() {
        UpdateBookStock request = UpdateBookStock.builder().stock(15).build();
        BookResponse response = new BookResponse(
                1L, "Clean Code", "Robert Martin", BigDecimal.TEN, 15);

        when(bookService.updateBook(1L, request)).thenReturn(response);

        assertSame(response, controller.update(1L, request));
        verify(bookService).updateBook(1L, request);
    }
}
