# cart-service

Customers add products to their carts.

Cart item has: productId, productName (from Product Service), price, quantity, customerId.

Calls Product Service to get the product details (e.g., productName) when adding items.

CRUD operations on the cart.

Uses virtual threads and asynchronous operations (great for concurrency handling).
