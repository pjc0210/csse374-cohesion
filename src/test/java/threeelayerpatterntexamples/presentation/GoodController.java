package threeelayerpatterntexamples.presentation;

import threeelayerpatterntexamples.domain.DomainService;

public class GoodController {
    // Field referencing Domain layer (allowed)
    private DomainService service;

    public GoodController() {}

    public void handle() {
        if (service != null) service.process();
    }
}
