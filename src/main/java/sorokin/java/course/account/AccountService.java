package sorokin.java.course.account;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import sorokin.java.course.hibernate.TransactionRunner;
import sorokin.java.course.user.User;

import java.util.List;
import java.util.Objects;

@Component
public class AccountService {

    private final AccountProperties accountProperties;
    private final TransactionRunner transactionRunner;

    public AccountService(AccountProperties accountProperties, TransactionRunner transactionRunner) {
        this.accountProperties = accountProperties;
        this.transactionRunner = transactionRunner;
    }

    private static void validatePositiveAccountId(Long id) {
        validatePositiveId(id, "account id");
    }

    private static void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    private static void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }

    private static Account getAccountById(Long id, Session session) {
        return session.byId(Account.class)
                .loadOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(id)));
    }

    private static User getUserById(Long id, Session session) {
        return session.byId(User.class)
                .loadOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("No such user: id=%s".formatted(id)));
    }

    private static Account findAccountToTransfer(List<Account> userAccounts, Long accountToRemoveId) {
        return userAccounts.stream()
                .filter(a -> !Objects.equals(a.getId(), accountToRemoveId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Can't close the only one account"));
    }

    public Account createAccount(Long userId) {
        return transactionRunner.executeInTransaction(session -> {
            User user = getUserById(userId, session);
            return createAccount(user);
        });
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        return transactionRunner.executeInTransaction(session -> {
            Account account = new Account();
            account.setUser(user);
            account.setMoneyAmount(accountProperties.getDefaultAmount());
            user.getAccountList().add(account);
            session.persist(account);
            return account;
        });
    }

    public void closeAccount(Long accountId) {
        validatePositiveAccountId(accountId);
        transactionRunner.runInTransaction(session -> {
            Account accountToClose = getAccountById(accountId, session);
            var user = accountToClose.getUser();
            var userAccounts = user.getAccountList();
            var accountToTransferMoney = findAccountToTransfer(userAccounts, accountId);
            transfer(accountToClose, accountToTransferMoney, accountToClose.getMoneyAmount());
            userAccounts.remove(accountToClose);
            session.remove(accountToClose);
        });
    }

    public void withdraw(Long fromAccountId, Integer amount) {
        validatePositiveAccountId(fromAccountId);
        validatePositiveAmount(amount);
        transactionRunner.runInTransaction(session -> {
            Account account = getAccountById(fromAccountId, session);
            withdraw(account, amount);
        });
    }

    private void withdraw(Account account, int amount) {
        if (amount > account.getMoneyAmount()) {
            throw new IllegalArgumentException(
                    "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                            .formatted(account.getId(), account.getMoneyAmount(), amount)
            );
        }
        account.setMoneyAmount(account.getMoneyAmount() - amount);
    }

    public void deposit(Long toAccountId, Integer amount) {
        validatePositiveAccountId(toAccountId);
        validatePositiveAmount(amount);
        transactionRunner.runInTransaction(session -> {
            Account account = getAccountById(toAccountId, session);
            deposit(account, amount);
        });
    }

    private void deposit(Account toAccount, int amount) {
        toAccount.setMoneyAmount(toAccount.getMoneyAmount() + amount);
    }

    public void transfer(Long fromAccountId, Long toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        if (Objects.equals(fromAccountId, toAccountId)) {
            throw new IllegalArgumentException("source and target account id must be different");
        }
        transactionRunner.runInTransaction(session -> {
            Account accountFrom = getAccountById(fromAccountId, session);
            Account accountTo = getAccountById(toAccountId, session);
            transfer(accountFrom, accountTo, amount);
        });
    }

    private void transfer(Account fromAccount, Account toAccount, int amount) {
        withdraw(fromAccount, amount);
        int amountToDeposit = Objects.equals(fromAccount.getUser().getId(), toAccount.getUser().getId())
                ? amount
                : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));
        deposit(toAccount, amountToDeposit);
    }
}
