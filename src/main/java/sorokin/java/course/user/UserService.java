package sorokin.java.course.user;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.hibernate.TransactionRunner;

import java.util.List;

@Component
public class UserService {

    private final TransactionRunner transactionRunner;
    private final AccountService accountService;

    public UserService(TransactionRunner transactionRunner, AccountService accountService) {
        this.transactionRunner = transactionRunner;
        this.accountService = accountService;
    }

    private static String normalizeLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }

    private static boolean loginExists(String login, Session session) {
        return !session.createQuery("select u.id from User u where u.login = :login", Long.class)
                .setParameter("login", login)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    public User createUser(String login) {
        String normalizedLogin = normalizeLogin(login);
        return transactionRunner.executeInTransaction(session -> {
            if (loginExists(normalizedLogin, session)) {
                throw new IllegalArgumentException("User with login=%s already exists".formatted(normalizedLogin));
            }
            User user = new User(normalizedLogin);
            session.persist(user);
            accountService.createAccount(user);
            return user;
        });
    }

    public List<User> findAll() {
        return transactionRunner.executeInTransaction(session ->
                session.createQuery("from User join fetch accountList", User.class).list()
        );
    }
}
