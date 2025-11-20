package shopping;

import customer.Customer;
import customer.CustomerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

/**
 * Тестирование класса {@link ShoppingService}
 * @author Daria
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты ShoppingService")
class ShoppingServiceTest {

    private final ProductDao productDaoMock;
    private final ShoppingService shoppingService;
    ShoppingServiceTest(@Mock ProductDao productDaoMock) {
        this.productDaoMock = productDaoMock;
        shoppingService = new ShoppingServiceImpl(productDaoMock);
    }

    /**
     * Проверка получения корзины покупателя
     * Проверяем на граничных данных
     * Проверяем, что продукты сохраняются в корзине
     * Проверяем, что кол-во продуктов суммируется в корзине
     * Проверяем, что продукты у каждого покупателя свои
     */
    @Test
    @DisplayName("getCart - позитивная проверка")
    void getCartTest() {
        Customer customer1 = new Customer(1, "111");
        Customer customer2 = new Customer(2, "222");

        Product product1 = new Product("test1", 5);
        Product product2 = new Product("test2", 5);

        shoppingService.getCart(customer1).add(product1, 1);
        shoppingService.getCart(customer1).add(product1, 1);
        shoppingService.getCart(customer2).add(product2, 5);

        Cart cart1 = shoppingService.getCart(customer1);
        Cart cart2 = shoppingService.getCart(customer2);

        Assertions.assertTrue(cart1.getProducts().containsKey(product1));
        Assertions.assertEquals(2, cart1.getProducts().get("test1"));
        Assertions.assertFalse(cart1.getProducts().containsKey(product2));

        Assertions.assertFalse(cart2.getProducts().containsKey(product1));
        Assertions.assertTrue(cart2.getProducts().containsKey(product2));
    }

    /**
     * Проверка получения всех продуктов
     */
    @Test
    @DisplayName("getAllProducts - позитивная проверка")
    void getAllProductsTest() {
        // В методе нет логики для проверки
    }

    /**
     * Проверка получения продукта по имени
     */
    @Test
    @DisplayName("getProductByName - позитивная проверка")
    void getProductByNameTest() {
        // В методе нет логики для проверки
    }

    /**
     * Проверка покупки продуктов
     * Проверяем на граничных данных
     * Проверяем, что продуктов остаётся после покупки верное кол-во
     * Проверяем, что после покупки корзина пустая
     * Проверяем изменения в бд
     */
    @Test
    @DisplayName("buy - позитивная проверка")
    void buyTest() throws BuyException {
        Customer customer1 = new Customer(1, "111");
        Customer customer2 = new Customer(2, "222");

        Product product1 = new Product("test1", 5);
        Product product2 = new Product("test2", 5);

        shoppingService.getCart(customer1).add(product1, 1);
        shoppingService.getCart(customer2).add(product2, 5);

        Assertions.assertTrue(shoppingService.buy(shoppingService.getCart(customer1)));
        Assertions.assertTrue(shoppingService.buy(shoppingService.getCart(customer2)));

        Assertions.assertEquals(4, product1.getCount());
        Assertions.assertEquals(0, product2.getCount());

        Mockito.verify(productDaoMock).save(product1);
        Mockito.verify(productDaoMock).save(product2);

        Assertions.assertEquals(0, shoppingService.getCart(customer1).getProducts().size());
        Assertions.assertEquals(0, shoppingService.getCart(customer2).getProducts().size());
    }

    /**
     * Проверка покупки продуктов, когда продуктов недостаточно
     * Проверяем, что 1 корзину можно купить
     * Проверяем, что 2 корзину нельзя купить
     * Проверяем, выброс исключения и читаемость ошибки
     * Проверяем изменения в бд 1 раз
     */
    @Test
    @DisplayName("buy - недостаточно продуктов")
    void buyWhenNotEnoughProductsTest() throws BuyException {
        Customer customer1 = new Customer(1, "111");
        Customer customer2 = new Customer(2, "222");

        Product product1 = new Product("test1", 5);

        Cart cart1 = shoppingService.getCart(customer1);
        Cart cart2 = shoppingService.getCart(customer2);

        cart1.add(product1, 1);
        cart2.add(product1, 5);

        Assertions.assertTrue(shoppingService.buy(cart1));

        BuyException e = Assertions.assertThrows(BuyException.class, () -> shoppingService.buy(cart2));
        Assertions.assertEquals("В наличии нет необходимого количества товара 'test1'", e.getMessage());
        Assertions.assertEquals(4, product1.getCount());
        Mockito.verify(productDaoMock, Mockito.times(1)).save(Mockito.argThat(
                product -> "test1".equals(product.getName())
        ));
    }

    /**
     * Проверка покупки пустой корзины
     */
    @Test
    @DisplayName("buy - пустая корзина")
    void buyEmptyCartTest() throws BuyException {
        Customer customer1 = new Customer(1, "111");
        Assertions.assertFalse(shoppingService.buy(shoppingService.getCart(customer1)));
    }
}
