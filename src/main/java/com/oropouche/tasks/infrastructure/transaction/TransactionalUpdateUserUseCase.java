package com.oropouche.tasks.infrastructure.transaction;

import java.util.Optional;

import org.springframework.transaction.support.TransactionTemplate;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.UpdateUserUseCase;

/**
 * Decorador de infraestructura: ejecuta el caso de uso dentro de una transacción
 * para que la lectura y el guardado sean atómicos, sin acoplar la capa de aplicación a Spring.
 */
public class TransactionalUpdateUserUseCase implements UpdateUserUseCase {

    private final UpdateUserUseCase delegate;
    private final TransactionTemplate transactionTemplate;

    public TransactionalUpdateUserUseCase(UpdateUserUseCase delegate, TransactionTemplate transactionTemplate) {
        this.delegate = delegate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Optional<User> updateUser(User datos) {
        return transactionTemplate.execute(status -> delegate.updateUser(datos));
    }
}
