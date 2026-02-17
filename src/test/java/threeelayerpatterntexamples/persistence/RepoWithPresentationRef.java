package threeelayerpatterntexamples.persistence;

import threeelayerpatterntexamples.presentation.ViewClass;

public class RepoWithPresentationRef {
    // Data layer referencing presentation (should be flagged)
    private ViewClass view;

    public RepoWithPresentationRef() {}

    public void callView() {
        if (view != null) view.render();
    }
}
