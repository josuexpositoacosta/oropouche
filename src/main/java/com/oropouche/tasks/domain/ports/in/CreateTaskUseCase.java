package com.oropouche.tasks.domain.ports.in;

import com.oropouche.tasks.domain.models.*;

public interface CreateTaskUseCase {
  Task CreateTasks (Task tasks);
}
