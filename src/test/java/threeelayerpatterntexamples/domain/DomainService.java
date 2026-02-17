package threeelayerpatterntexamples.domain;

import threeelayerpatterntexamples.presentation.ViewClass;

public class DomainService {
    // Domain referencing presentation (should be flagged)
    private ViewClass view;

    public DomainService() {}

    public void process() {
        if (view != null) view.render();
    }
}
