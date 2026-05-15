package sorokin.java.course.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class TransactionRunner {

    private final SessionFactory sessionFactory;

    public TransactionRunner(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void runInTransaction(Consumer<Session> action) {
        executeInTransaction(session -> {
            action.accept(session);
            return null;
        });
    }

    public <T> T executeInTransaction(Function<Session, T> action) {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.getTransaction();
        boolean owner = (tx.getStatus() == TransactionStatus.NOT_ACTIVE);
        if (owner) {
            tx = session.beginTransaction();
        }
        try {
            T result = action.apply(session);
            if (owner) {
                tx.commit();
            }
            return result;
        } catch (RuntimeException e) {
            if (owner && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (owner && session.isOpen()) {
                session.close();
            }
        }
    }
}
