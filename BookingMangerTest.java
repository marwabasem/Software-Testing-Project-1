import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

public class BookingManagerTest {

    // Mock dependencies
    private BookingManager.IPaymentGateway paymentGateway;
    private BookingManager.INotificationService notificationService;
    private BookingManager.IEventRepository eventRepository;

    // Class to test
    private BookingManager bookingManager;
@BeforeEach
void setUp() {
    // Create mock objects
    paymentGateway = Mockito.mock(BookingManager.IPaymentGateway.class);
    notificationService = Mockito.mock(BookingManager.INotificationService.class);
    eventRepository = Mockito.mock(BookingManager.IEventRepository.class);

    // Create BookingManager with mocks
    bookingManager = new BookingManager(paymentGateway, notificationService, eventRepository);
}
// US-01: Happy Path
@Test
void testHappyPath() {
    // Event is not sold out
    when(eventRepository.isSoldOut("event1")).thenReturn(false);
    
    // Payment succeeded
    when(paymentGateway.processPayment("user1", 100.0)).thenReturn("12345");
    
    // Run the booking
    bookingManager.processBooking("user1", "event1", 100.0);
    
    // Verify
    verify(eventRepository, times(1)).saveBooking("user1", "event1");
    verify(notificationService, times(1)).sendConfirmation(eq("user1"), anyString());
}
// US-02: Null userId
@Test
void testInvalidPath_nullUserId() {
    bookingManager.processBooking(null, "event1", 100.0);

    verify(paymentGateway, never()).processPayment(any(), anyDouble());
    verify(eventRepository, never()).saveBooking(any(), any());
    verify(notificationService, never()).sendConfirmation(any(), any());
}

// US-02: Null eventId
@Test
void testInvalidPath_nullEventId() {
    bookingManager.processBooking("user1", null, 100.0);

    verify(paymentGateway, never()).processPayment(any(), anyDouble());
    verify(eventRepository, never()).saveBooking(any(), any());
    verify(notificationService, never()).sendConfirmation(any(), any());
}

// US-02: Invalid price
@Test
void testInvalidPath_zeroPrice() {
    bookingManager.processBooking("user1", "event1", 0);

    verify(paymentGateway, never()).processPayment(any(), anyDouble());
    verify(eventRepository, never()).saveBooking(any(), any());
    verify(notificationService, never()).sendConfirmation(any(), any());
}
// US-03: Sold Out Path
@Test
void testSoldOutPath() {
    // Event is sold out
    when(eventRepository.isSoldOut("event1")).thenReturn(true);

    // Run the booking
    bookingManager.processBooking("user1", "event1", 100.0);

    // Verify isSoldOut was called
    verify(eventRepository, times(1)).isSoldOut("event1");

    // Verify nothing else was called
    verify(paymentGateway, never()).processPayment(any(), anyDouble());
    verify(eventRepository, never()).saveBooking(any(), any());
    verify(notificationService, never()).sendConfirmation(any(), any());
}

}