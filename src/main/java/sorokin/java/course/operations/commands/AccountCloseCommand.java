package sorokin.java.course.operations.commands;

import org.springframework.stereotype.Component;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.console.ConsoleInput;
import sorokin.java.course.operations.ConsoleOperationType;
import sorokin.java.course.operations.OperationCommand;

@Component
public class AccountCloseCommand implements OperationCommand {

    private final AccountService accountService;
    private final ConsoleInput consoleInput;

    public AccountCloseCommand(AccountService accountService, ConsoleInput consoleInput) {
        this.accountService = accountService;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        int accountId = consoleInput.readPositiveInt("Enter account id to close:", "account id");
        accountService.closeAccount((long) accountId);
        System.out.println("Account " + accountId + " closed.");
    }

    @Override
    public ConsoleOperationType getOperationType() {
        return ConsoleOperationType.ACCOUNT_CLOSE;
    }
}
