package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.models.AddicionalTaskInfo;
import com.oropouche.tasks.domain.ports.in.*;
import com.oropouche.tasks.domain.ports.out.*;

public class GetAddicionalTaskInfoUseCaseImpl implements GetAddicionalTaskInfoUseCase {



    private final ExternalServiceTaskPort externalServicePort;


    public GetAddicionalTaskInfoUseCaseImpl(ExternalServiceTaskPort externalServicePort) {
        this.externalServicePort = externalServicePort;
    }

    @Override
    public AddicionalTaskInfo getAddicionalTaskInfo(Long id) {
        return externalServicePort.getAddicionalTaskInfo(id);
    }
}
