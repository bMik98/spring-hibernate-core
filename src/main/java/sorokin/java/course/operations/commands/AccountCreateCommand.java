package sorokin.java.course.operations.commands;

import org.springframework.stereotype.Component;
import sorokin.java.course.account.Account;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.console.ConsoleInput;
import sorokin.java.course.operations.ConsoleOperationType;
import sorokin.java.course.operations.OperationCommand;

@Component
public class AccountCreateCommand implements OperationCommand {

    private final AccountService accountService;
    private final ConsoleInput consoleInput;

    public AccountCreateCommand(AccountService accountService, ConsoleInput consoleInput) {
        this.accountService = accountService;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        long userId = consoleInput.readPositiveInt("Enter user id:", "user id");
        Account account = accountService.createAccount(userId);
        System.out.println("Account created: " + account);
    }

    @Override
    public ConsoleOperationType getOperationType() {
        return ConsoleOperationType.ACCOUNT_CREATE;
    }
}
