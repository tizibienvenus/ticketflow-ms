package com.boaz.ticketflow.users.domain.port.inbound;

import com.boaz.ticketflow.users.domain.model.UserEntity;

public interface  CreateUserUseCase {
    UserEntity createUser(String id);
}
