package com.github.hiroshi85.bankx_reactive_ledger.transaction.application;

import com.github.hiroshi85.bankx_reactive_ledger.account.domain.Account;
import com.github.hiroshi85.bankx_reactive_ledger.account.infrastructure.AccountRepository;
import com.github.hiroshi85.bankx_reactive_ledger.common.error.BusinessException;
import com.github.hiroshi85.bankx_reactive_ledger.risk.application.RiskEvaluationService;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskDecision;
import com.github.hiroshi85.bankx_reactive_ledger.risk.domain.RiskRequest;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.CreateTransactionCommand;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.LedgerTransaction;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionResult;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.domain.TransactionType;
import com.github.hiroshi85.bankx_reactive_ledger.transaction.infrastructure.TransactionRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final RiskEvaluationService risk;
    private final TransactionEventPublisher events;
    private final Clock clock;

    @Autowired
    public TransactionService(AccountRepository accounts, TransactionRepository transactions,
            RiskEvaluationService risk, TransactionEventPublisher events) {
        this(accounts, transactions, risk, events, Clock.systemUTC());
    }
    TransactionService(AccountRepository accounts, TransactionRepository transactions,
            RiskEvaluationService risk, TransactionEventPublisher events, Clock clock) {
        this.accounts = accounts; this.transactions = transactions; this.risk = risk; this.events = events; this.clock = clock;
    }

    public Mono<TransactionResult> create(CreateTransactionCommand command) {
        log.info("Starting transaction type={} account={}", command.type(), command.accountNumber());
        return accounts.findByAccountNumber(command.accountNumber())
            .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
            .flatMap(account -> evaluateAndApply(account, command));
    }

    private Mono<TransactionResult> evaluateAndApply(Account account, CreateTransactionCommand command) {
        RiskRequest request = new RiskRequest(command.accountNumber(), command.type(), command.amount(), command.simulate());

        return risk.evaluate(request).flatMap(decision -> {
            if (decision == RiskDecision.REJECTED) {
                log.info("Risk rejected transaction for account={}", command.accountNumber());
                return Mono.error(new BusinessException("risk_rejected"));
            }

            if (command.type() == TransactionType.DEBIT && account.getBalance().compareTo(command.amount()) < 0) {
                return Mono.error(new BusinessException("insufficient_funds"));
            }

            var balance = command.type() == TransactionType.DEBIT
                ? account.getBalance().subtract(command.amount()) : account.getBalance().add(command.amount());

            account.setBalance(balance);
            
            return accounts.save(account)
                .flatMap(saved -> transactions.save(new LedgerTransaction(null, command.accountNumber(), command.type(),
                    command.amount(), "OK", Instant.now(clock))))
                .map(transaction -> complete(transaction, balance));
        });
    }

    private TransactionResult complete(LedgerTransaction transaction, java.math.BigDecimal balance) {
        events.publish(new TransactionEvent(transaction.id(), transaction.accountNumber(), transaction.type(), transaction.amount(), transaction.status()));
        log.info("Transaction completed id={} account={}", transaction.id(), transaction.accountNumber());
        return new TransactionResult(transaction.id(), transaction.accountNumber(), transaction.type(), transaction.amount(), transaction.status(), balance);
    }
}
