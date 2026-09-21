package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.dto.ApiDtos.CreateBookRequest;
import com.example.bookstore.dto.ApiDtos.UpdateBookStock;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    private BookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BookServiceImpl(bookRepository);
    }

    @Test
    void availableBooks_shouldReturnMappedBooks() {
        Book book1 = mock(Book.class);
        Book book2 = mock(Book.class);

        when(book1.getId()).thenReturn(1L);
        when(book1.getTitle()).thenReturn("Clean Code");
        when(book1.getAuthor()).thenReturn("Robert Martin");
        when(book1.getPrice()).thenReturn(new BigDecimal("50.00"));
        when(book1.getStock()).thenReturn(10);

        when(book2.getId()).thenReturn(2L);
        when(book2.getTitle()).thenReturn("Effective Java");
        when(book2.getAuthor()).thenReturn("Joshua Bloch");
        when(book2.getPrice()).thenReturn(new BigDecimal("60.00"));
        when(book2.getStock()).thenReturn(5);

        PageRequest pageable = PageRequest.of(0, 20);
        when(bookRepository.findByStockGreaterThan(0, pageable))
                .thenReturn(new PageImpl<>(List.of(book1, book2), pageable, 2));

        List<BookResponse> result = service.availableBooks(pageable);

        assertEquals(2, result.size());
        assertEquals("Clean Code", result.get(0).title());
        assertEquals("Effective Java", result.get(1).title());

        verify(bookRepository).findByStockGreaterThan(0, pageable);
    }

    @Test
    void availableBooks_shouldReturnEmptyPageWhenNoBooksAvailable() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(bookRepository.findByStockGreaterThan(0, pageable))
                .thenReturn(Page.empty(pageable));

        List<BookResponse> result = service.availableBooks(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addBook_shouldMapValidatedRequestToEntity() {
        CreateBookRequest request = CreateBookRequest.builder()
                .title("Clean Architecture")
                .author("Robert Martin")
                .price(new BigDecimal("50.00"))
                .stock(10)
                .build();

        Book saved = new Book("Clean Architecture", "Robert Martin",
                new BigDecimal("50.00"), 10);

        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        BookResponse result = service.addBook(request);

        assertEquals("Clean Architecture", result.title());
        assertEquals(new BigDecimal("50.00"), result.price());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_shouldUpdateStock() {
        Book book = new Book("Clean Code", "Robert Martin", BigDecimal.TEN, 5);
        when(bookRepository.findById(1L)).thenReturn(java.util.Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        BookResponse result = service.updateBook(1L, UpdateBookStock.builder().stock(12).build());

        assertEquals(12, result.stock());
        verify(bookRepository).save(book);
    }
}
