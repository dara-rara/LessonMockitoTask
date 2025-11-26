package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
    private Customer customer1;

    ShoppingServiceTest(@Mock ProductDao productDaoMock) {
        this.productDaoMock = productDaoMock;
        shoppingService = new ShoppingServiceImpl(productDaoMock);
    }

    /**
     * Инициализация тестового окружения:
     * Создание экземляра покупателя
     */
    @BeforeEach
    void initEach() {
        this.customer1 = new Customer(1, "111");
    }

    /**
     * Проверка получения корзины покупателя
     * Проверяем на граничных данных
     * Проверяем, что продукты сохраняются в корзине
     * Проверяем, что продукты у каждого покупателя свои
     */
    @Test
    @DisplayName("getCart - позитивная проверка")
    void getCartTest() {
        Customer customer2 = new Customer(2, "222");

        Product product1 = new Product("test1", 5);
        Product product2 = new Product("test2", 5);

        shoppingService.getCart(customer1).add(product1, 1);
        shoppingService.getCart(customer2).add(product2, 5);

        Cart cart1 = shoppingService.getCart(customer1);
        Cart cart2 = shoppingService.getCart(customer2);

        Assertions.assertNotSame(cart1, cart2);

        Assertions.assertTrue(cart1.getProducts().containsKey(product1));
        Assertions.assertFalse(cart1.getProducts().containsKey(product2));

        Assertions.assertFalse(cart2.getProducts().containsKey(product1));
        Assertions.assertTrue(cart2.getProducts().containsKey(product2));
    }

    /**
     * Проверка получения корзины покупателя
     * Проверяем, что кол-во продуктов суммируется в корзине
     */
    @Test
    @DisplayName("getCart - суммирование одного продукта после нескольких добавлений")
    void getCartWithSumProductsTest() {
        Product product1 = new Product("test1", 5);

        shoppingService.getCart(customer1).add(product1, 1);
        shoppingService.getCart(customer1).add(product1, 1);

        Cart cart1 = shoppingService.getCart(customer1);

        Assertions.assertEquals(2, cart1.getProducts().get(product1));
    }

    /**
     * Проверка получения всех продуктов
     */
    @Test
    @DisplayName("getAllProducts - позитивная проверка")
    void getAllProductsTest() {
        // Внутри только вызов метода из dao
    }

    /**
     * Проверка получения продукта по имени
     */
    @Test
    @DisplayName("getProductByName - позитивная проверка")
    void getProductByNameTest() {
        // Внутри только вызов метода из dao
    }

    /**
     * Проверка покупки продуктов
     * Проверяем, что продуктов остаётся после покупки верное кол-во
     * Проверяем, что после покупки корзина пустая
     * Проверяем изменения в бд
     */
    @Test
    @DisplayName("buy - позитивная проверка")
    void buyTest() throws BuyException {
        Product product1 = new Product("test1", 5);
        shoppingService.getCart(customer1).add(product1, 1);

        Assertions.assertTrue(shoppingService.buy(shoppingService.getCart(customer1)));
        Assertions.assertEquals(4, product1.getCount());
        Assertions.assertTrue(shoppingService.getCart(customer1).getProducts().isEmpty());

        Mockito.verify(productDaoMock).save(product1);
    }

    /**
     * Проверка покупки всего кол-ва продуктов
     */
    @Test
    @DisplayName("buy - покупка всех продуктов")
    void buyAllProductsTest() {
        Product product1 = new Product("test1", 5);
        shoppingService.getCart(customer1).add(product1, 5);
        Assertions.assertEquals(0, product1.getCount());
    }

    /**
     * Проверка покупки продуктов, когда продуктов недостаточно
     * Проверяем, что 1-ую корзину можно купить
     * Проверяем, что 2-ую корзину нельзя купить
     * Проверяем, выброс исключения и читаемость ошибки
     * Проверяем изменения в бд 1 раз
     */
    @Test
    @DisplayName("buy - недостаточно продуктов")
    void buyWhenNotEnoughProductsTest() {
        Product product1 = new Product("test1", 5);
        Cart cart1 = shoppingService.getCart(customer1);

        cart1.add(product1, 1);
        product1.subtractCount(5);

        BuyException e = Assertions.assertThrows(BuyException.class, () -> shoppingService.buy(cart1));
        Assertions.assertEquals("В наличии нет необходимого количества товара 'test1'", e.getMessage());
    }

    /**
     * Проверка покупки пустой корзины
     */
    @Test
    @DisplayName("buy - пустая корзина")
    void buyEmptyCartTest() throws BuyException {
        Assertions.assertFalse(shoppingService.buy(shoppingService.getCart(customer1)));
        Mockito.verify(productDaoMock, Mockito.never()).save(Mockito.any());
    }

    /**
     * Проверка покупки null
     */
    @Test
    @DisplayName("buy - null")
    void buyNullTest() throws BuyException {
        Assertions.assertFalse(shoppingService.buy(null));
        Mockito.verify(productDaoMock, Mockito.never()).save(Mockito.any());
    }
}
