package com.oropouche.tasks.domain.ports.in;

import com.oropouche.tasks.domain.models.*;

public interface GetAddicionalTaskInfoUseCase {

    AddicionalTaskInfo getAddicionalTaskInfo(Long id);

}
