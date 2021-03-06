/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.main.dao.governance;

import bisq.desktop.Navigation;
import bisq.desktop.common.view.ActivatableViewAndModel;
import bisq.desktop.common.view.CachingViewLoader;
import bisq.desktop.common.view.FxmlView;
import bisq.desktop.common.view.View;
import bisq.desktop.common.view.ViewLoader;
import bisq.desktop.common.view.ViewPath;
import bisq.desktop.components.MenuItem;
import bisq.desktop.main.MainView;
import bisq.desktop.main.dao.DaoView;
import bisq.desktop.main.dao.governance.dashboard.ProposalDashboardView;
import bisq.desktop.main.dao.governance.make.MakeProposalView;
import bisq.desktop.main.dao.governance.proposals.ProposalsView;
import bisq.desktop.main.dao.governance.result.VoteResultView;

import bisq.core.dao.DaoFacade;
import bisq.core.dao.state.period.DaoPhase;
import bisq.core.locale.Res;

import javax.inject.Inject;

import de.jensd.fx.fontawesome.AwesomeIcon;

import javafx.fxml.FXML;

import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import javafx.beans.value.ChangeListener;

import java.util.Arrays;
import java.util.List;

@FxmlView
public class GovernanceView extends ActivatableViewAndModel {

    private final ViewLoader viewLoader;
    private final Navigation navigation;
    private final DaoFacade daoFacade;

    private MenuItem dashboard, make, open, result;
    private Navigation.Listener navigationListener;

    @FXML
    private VBox leftVBox;
    @FXML
    private AnchorPane content;

    private Class<? extends View> selectedViewClass;
    private ChangeListener<DaoPhase.Phase> phaseChangeListener;

    @Inject
    private GovernanceView(CachingViewLoader viewLoader, Navigation navigation, DaoFacade daoFacade) {
        this.viewLoader = viewLoader;
        this.navigation = navigation;
        this.daoFacade = daoFacade;
    }

    @Override
    public void initialize() {
        navigationListener = viewPath -> {
            if (viewPath.size() != 4 || viewPath.indexOf(GovernanceView.class) != 2)
                return;

            selectedViewClass = viewPath.tip();
            loadView(selectedViewClass);
        };

        phaseChangeListener = (observable, oldValue, newValue) -> {
            if (newValue == DaoPhase.Phase.BLIND_VOTE)
                open.setLabelText(Res.get("dao.proposal.menuItem.vote"));
            else
                open.setLabelText(Res.get("dao.proposal.menuItem.browse"));
        };

        ToggleGroup toggleGroup = new ToggleGroup();
        final List<Class<? extends View>> baseNavPath = Arrays.asList(MainView.class, DaoView.class, GovernanceView.class);
        dashboard = new MenuItem(navigation, toggleGroup, Res.get("shared.dashboard"),
                ProposalDashboardView.class, AwesomeIcon.DASHBOARD, baseNavPath);
        make = new MenuItem(navigation, toggleGroup, Res.get("dao.proposal.menuItem.make"),
                MakeProposalView.class, AwesomeIcon.EDIT, baseNavPath);
        open = new MenuItem(navigation, toggleGroup, Res.get("dao.proposal.menuItem.browse"),
                ProposalsView.class, AwesomeIcon.LIST_UL, baseNavPath);
        result = new MenuItem(navigation, toggleGroup, Res.get("dao.proposal.menuItem.result"),
                VoteResultView.class, AwesomeIcon.LIST_ALT, baseNavPath);
        leftVBox.getChildren().addAll(dashboard, make, open, result);
    }

    @Override
    protected void activate() {
        daoFacade.phaseProperty().addListener(phaseChangeListener);

        dashboard.activate();
        make.activate();
        open.activate();
        result.activate();

        navigation.addListener(navigationListener);
        ViewPath viewPath = navigation.getCurrentPath();
        if (viewPath.size() == 3 && viewPath.indexOf(GovernanceView.class) == 2 ||
                viewPath.size() == 2 && viewPath.indexOf(DaoView.class) == 1) {
            if (selectedViewClass == null)
                selectedViewClass = MakeProposalView.class;

            loadView(selectedViewClass);

        } else if (viewPath.size() == 4 && viewPath.indexOf(GovernanceView.class) == 2) {
            selectedViewClass = viewPath.get(3);
            loadView(selectedViewClass);
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void deactivate() {
        daoFacade.phaseProperty().removeListener(phaseChangeListener);

        navigation.removeListener(navigationListener);

        dashboard.deactivate();
        make.deactivate();
        open.deactivate();
        result.deactivate();
    }

    private void loadView(Class<? extends View> viewClass) {
        View view = viewLoader.load(viewClass);
        content.getChildren().setAll(view.getRoot());

        if (view instanceof ProposalDashboardView) dashboard.setSelected(true);
        else if (view instanceof MakeProposalView) make.setSelected(true);
        else if (view instanceof ProposalsView) open.setSelected(true);
        else if (view instanceof VoteResultView) result.setSelected(true);
    }
}


