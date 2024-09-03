package com.oropouche.tasks.domain.ports.out;

import com.oropouche.tasks.domain.models.*;

public interface ExternalServiceTaskPort {
    AddicionalTaskInfo getAddicionalTaskInfo(Long taskId);
}
