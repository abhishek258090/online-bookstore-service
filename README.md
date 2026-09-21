# Bookstore API

A Spring Boot 3 / Java 17 REST API demonstrating an OO bookstore domain, JWT authentication, H2 persistence, transactional checkout, structured logging, validation, and meaningful tests.

## Prerequisites

- Java 17
- Maven 3.9+ (or use an IDE with Maven support)

## API and local H2 run details.

The API starts at `http://localhost:8081`. The H2 console is disabled by default. For local development, start with the `local` profile to enable `/h2-console`; it is never exposed by the default security configuration.

Start locally with `SPRING_PROFILES_ACTIVE=local`. If an admin account is needed locally, also provide `BOOKSTORE_ADMIN_EMAIL` and `BOOKSTORE_ADMIN_PASSWORD` environment variables before starting. No admin password or bcrypt hash is committed to source control.

## API workflow

1. Register with `POST /api/auth/register`, this accept Credentials like `{"email":"reader@example.com","password":"strong-pass"}`.
2. Log in using `POST /api/auth/login` with Authorization type as basic auth, login using UserName and Password . Login returns the JWT.
3. For every authenticated state-changing request, send the JWT as `Authorization: Bearer <token>`.

Endpoints:

| Method     | Path                           | Auth | Purpose                                                                                         |
|------------|--------------------------------|------|-------------------------------------------------------------------------------------------------|
| GET        | `/api/books?page=0&size=20`    | JWT  | Paginated list of available books                                                               |
| POST       | `/api/books`                   | ADMIN| Add a book using a validated `CreateBookRequest`                                                |
| PUT        | `/api/books/{bookId}`          | ADMIN| Update stock using a validated request DTO                                                      |
| GET/POST   | `/api/cart`, `/api/cart/items` | JWT  | Read cart / add `{ "bookId": 1, "quantity": 2 }`                                                |
| PUT/DELETE | `/api/cart/items/{bookId}`     | JWT  | Set quantity with `{ "quantity": 3 }` / remove item                                             |
| GET        | `/api/orders?page=0&size=20`   | JWT  | Paginated order history                                                                          |
| POST       | `/api/orders`                  | JWT  | Checkout; optional body `{ "paymentMethod": "CARD" }` or `{ "paymentMethod": "CASH_ON_DELIVERY" }` |

`POST /api/orders` returns `201`; registration also returns `201`. Validation, missing resources, empty carts, insufficient stock, and bad credentials return appropriate `400`, `401`, or `404` responses. Insufficient stock returns `409 Conflict`. Checkout locks the customer's cart before validation, preventing the same cart from being processed twice concurrently. It then acquires book write locks in deterministic ID order before reserving stock.

## Design notes

- The domain is expressed through models `Book`, `Cart`, `CartItem`, `PurchaseOrder`, and `OrderItem`; invariants such as stock reservation live with the entities.
- Service interfaces separate controllers from implementations, while DTOs keep HTTP concerns out of the domain. JPA repositories are persistence abstractions.
- `PurchaseOrder` builds immutable purchase-line snapshots; checkout runs inside one transaction, so stock deductions and order persistence are atomic. Checkout acquires pessimistic database write locks in book-ID order, preventing concurrent checkouts from overselling stock without requiring a client retry.
- Entities and request DTOs expose builders where object construction has multiple fields. `PurchaseOrder.createFrom(...)` is a domain factory that assembles immutable order-line snapshots. Payment is represented by a `PaymentStrategy` abstraction with card and cash-on-delivery implementations.
- Tests cover the happy path, empty-cart boundary, insufficient-stock and other failure cases:


Change `app.jwt.secret` through an environment-specific configuration before deploying; the default is strictly for local development.
