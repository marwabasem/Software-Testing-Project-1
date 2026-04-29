
public class BookingManager {
    // interfaces for dependencies
    private final IPaymentGateway paymentGateway;
    private final INotificationService notificationService;
    private final IEventRepository eventRepository;

    // constructor injection for dependencies
    public BookingManager(IPaymentGateway paymentGateway,
            INotificationService notificationService,
            IEventRepository eventRepository) {
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }

    /*
     * US-01: Happy Path
     * US-02: Invalid Paths
     * US-03: Sold Out Path
     */
    public void processBooking(String userId, String eventId, double TicketPrice) {

        // 1.(US-02)
        // checking if the user input valid befor doing anything
        if (userId == null || eventId == null || TicketPrice <= 0) {
            return;
        }

        // 2.(US-03)
        // Check that tickets are available
        // if the event is sold out, we should not proceed with payment or booking
        if (eventRepository.isSoldOut(eventId)) {
            return;
        }

        // 3.(US-01)
        // if the input is valid and tickets are available, we save the booking and
        // notify the user
        String transactionId = paymentGateway.processPayment(userId, TicketPrice);

        if (transactionId != null) {
            eventRepository.saveBooking(userId, eventId);
            notificationService.sendConfirmation(userId, "Done! Your seat is reserved.");
        }
    }

    // Interfaces for dependencies
    interface IPaymentGateway {
        String processPayment(String userId, double TicketPrice);
    }

    interface INotificationService {
        void sendConfirmation(String userId, String message);
    }

    interface IEventRepository {
        boolean isSoldOut(String eventId);

        void saveBooking(String userId, String eventId);
    }
}