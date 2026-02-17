package threeelayerpatterntexamples.presentation;

import threeelayerpatterntexamples.persistence.DataRepository;

public class PresentationController {
    // Field referencing Data layer directly (should be flagged)
    private DataRepository repo;

    public PresentationController() {}

    public void doWork() {
        if (repo != null) repo.query();
    }
}
