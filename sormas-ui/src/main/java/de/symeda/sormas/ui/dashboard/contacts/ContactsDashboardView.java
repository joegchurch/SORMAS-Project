/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.dashboard.contacts;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.ui.dashboard.AbstractDashboardView;
import de.symeda.sormas.ui.dashboard.DashboardCssStyles;
import de.symeda.sormas.ui.dashboard.DashboardType;
import de.symeda.sormas.ui.dashboard.diagram.AbstractEpiCurveComponent;
import de.symeda.sormas.ui.dashboard.map.DashboardMapComponent;
import de.symeda.sormas.ui.dashboard.statistics.AbstractDashboardStatisticsComponent;
import de.symeda.sormas.ui.utils.CssStyles;

@SuppressWarnings("serial")
public class ContactsDashboardView extends AbstractDashboardView {

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/contacts";

	protected AbstractDashboardStatisticsComponent statisticsComponent;
	protected AbstractEpiCurveComponent epiCurveComponent;
	protected DashboardMapComponent mapComponent;
	protected HorizontalLayout epiCurveAndMapLayout;
	private VerticalLayout epiCurveLayout;
	private VerticalLayout mapLayout;
	
	// Case counts
	private Label minLabel = new Label();
	private Label maxLabel = new Label();
	private Label avgLabel = new Label();
	private Label sourceCasesLabel = new Label();

	public ContactsDashboardView() {
		super(VIEW_NAME, DashboardType.CONTACTS);

		filterLayout.setInfoLabelText(I18nProperties.getString(Strings.infoContactDashboard));

		// Add statistics
		statisticsComponent = new ContactsDashboardStatisticsComponent(dashboardDataProvider);
		dashboardLayout.addComponent(statisticsComponent);
		dashboardLayout.setExpandRatio(statisticsComponent, 0);
		
		HorizontalLayout caseStatisticsLayout = createCaseStatisticsLayout();
		dashboardLayout.addComponent(caseStatisticsLayout);
		dashboardLayout.setExpandRatio(caseStatisticsLayout, 0);

		epiCurveComponent = new ContactsEpiCurveComponent(dashboardDataProvider);
		mapComponent = new DashboardMapComponent(dashboardDataProvider);

		// Add epi curve and map
		epiCurveAndMapLayout = createEpiCurveAndMapLayout();
		dashboardLayout.addComponent(epiCurveAndMapLayout);
		dashboardLayout.setExpandRatio(epiCurveAndMapLayout, 1);
	}
	
	private HorizontalLayout createCaseStatisticsLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.addStyleName(DashboardCssStyles.HIGHLIGHTED_STATISTICS_COMPONENT);
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setMargin(false);
		layout.setSpacing(false);
		
		HorizontalLayout contactsPerCaseLayout = createContactsPerCaseLayout();
		HorizontalLayout sourceCasesLayout = createSourceCasesLayout();
		layout.addComponent(contactsPerCaseLayout);
		layout.addComponent(sourceCasesLayout);
		layout.setExpandRatio(contactsPerCaseLayout, 1);
		layout.setComponentAlignment(sourceCasesLayout, Alignment.MIDDLE_RIGHT);
		
		return layout;
	}
	
	private HorizontalLayout createContactsPerCaseLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(new MarginInfo(false, true, false, true));
		layout.setSpacing(false);
		
		Label caption = new Label(I18nProperties.getString(Strings.headingContactsPerCase));
		CssStyles.style(caption, CssStyles.H3, CssStyles.HSPACE_RIGHT_1, CssStyles.VSPACE_TOP_NONE);
		layout.addComponent(caption);
		
		CssStyles.style(minLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_LARGE_ALT, CssStyles.LABEL_BOLD, CssStyles.VSPACE_5, CssStyles.HSPACE_RIGHT_3);
		layout.addComponent(minLabel);
		CssStyles.style(maxLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_LARGE_ALT, CssStyles.LABEL_BOLD, CssStyles.VSPACE_5, CssStyles.HSPACE_RIGHT_3);
		layout.addComponent(maxLabel);
		CssStyles.style(avgLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_LARGE_ALT, CssStyles.LABEL_BOLD, CssStyles.VSPACE_5);
		layout.addComponent(avgLabel);
		
		return layout;
	}
	
	private HorizontalLayout createSourceCasesLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(new MarginInfo(false, true, false, true));
		layout.setSpacing(false);
		
		Label caption = new Label(I18nProperties.getString(Strings.headingNewSourceCases));
		CssStyles.style(caption, CssStyles.H3, CssStyles.HSPACE_RIGHT_1, CssStyles.VSPACE_TOP_NONE);
		layout.addComponent(caption);

		CssStyles.style(sourceCasesLabel, CssStyles.LABEL_PRIMARY, CssStyles.LABEL_LARGE_ALT, CssStyles.LABEL_BOLD, CssStyles.VSPACE_5);
		layout.addComponent(sourceCasesLabel);
		
		return layout;
	}

	private void updateCaseCountsAndSourceCasesLabels() {
		List<String> contactUuids = dashboardDataProvider.getContacts().stream().map(dto -> dto.getUuid()).collect(Collectors.toList());
		int[] counts = null;
		if (!contactUuids.isEmpty()) {
			counts = FacadeProvider.getContactFacade().getContactCountsByCasesForDashboard(contactUuids);
		} else {
			counts = new int[3];
		}
		
		int minContactCount = counts[0];
		int maxContactCount = counts[1];
		int avgContactCount = counts[2];
		
		minLabel.setValue(I18nProperties.getString(Strings.min) + ": " + minContactCount);
		maxLabel.setValue(I18nProperties.getString(Strings.max) + ": " + maxContactCount);
		avgLabel.setValue(I18nProperties.getString(Strings.average) + ": " + avgContactCount);

		List<String> caseUuids = dashboardDataProvider.getCases().stream().map(dto -> dto.getUuid()).collect(Collectors.toList());
		int nonSourceCases = 0;
		if (!caseUuids.isEmpty()) {
			nonSourceCases = FacadeProvider.getContactFacade().getNonSourceCaseCountForDashboard(caseUuids);
		}
		
		int newSourceCases = caseUuids.size() - nonSourceCases;
		int newSourceCasesPercentage = newSourceCases == 0 ? 0 : (int) ((newSourceCases * 100.0f) / caseUuids.size());
		
		sourceCasesLabel.setValue(newSourceCases + " (" + newSourceCasesPercentage + " %)");
	}
	
	protected HorizontalLayout createEpiCurveAndMapLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.addStyleName(DashboardCssStyles.CURVE_AND_MAP_LAYOUT);
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setMargin(false);
		layout.setSpacing(false);

		// Epi curve layout
		epiCurveLayout = createEpiCurveLayout();
		layout.addComponent(epiCurveLayout);

		// Map layout
		mapLayout = createMapLayout();
		layout.addComponent(mapLayout);

		return layout;
	}

	protected VerticalLayout createEpiCurveLayout() {
		if (epiCurveComponent == null) {
			throw new UnsupportedOperationException(
					"EpiCurveComponent needs to be initialized before calling createEpiCurveLayout");
		}

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setHeight(400, Unit.PIXELS);

		epiCurveComponent.setSizeFull();

		layout.addComponent(epiCurveComponent);
		layout.setExpandRatio(epiCurveComponent, 1);

		epiCurveComponent.setExpandListener(expanded -> {
			if (expanded) {
				dashboardLayout.removeComponent(statisticsComponent);
				epiCurveAndMapLayout.removeComponent(mapLayout);
				ContactsDashboardView.this.setHeight(100, Unit.PERCENTAGE);
				epiCurveAndMapLayout.setHeight(100, Unit.PERCENTAGE);
				epiCurveLayout.setSizeFull();
			} else {
				dashboardLayout.addComponent(statisticsComponent, 1);
				epiCurveAndMapLayout.addComponent(mapLayout, 1);
				epiCurveLayout.setHeight(400, Unit.PIXELS);
				ContactsDashboardView.this.setHeightUndefined();
				epiCurveAndMapLayout.setHeightUndefined();
			}
		});

		return layout;
	}

	protected VerticalLayout createMapLayout() {
		if (mapComponent == null) {
			throw new UnsupportedOperationException(
					"MapComponent needs to be initialized before calling createMapLayout");
		}
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setHeight(555, Unit.PIXELS);

		mapComponent.setSizeFull();

		layout.addComponent(mapComponent);
		layout.setExpandRatio(mapComponent, 1);

		mapComponent.setExpandListener(expanded -> {
			if (expanded) {
				dashboardLayout.removeComponent(statisticsComponent);
				epiCurveAndMapLayout.removeComponent(epiCurveLayout);
				ContactsDashboardView.this.setHeight(100, Unit.PERCENTAGE);
				epiCurveAndMapLayout.setHeight(100, Unit.PERCENTAGE);
				mapLayout.setSizeFull();
			} else {
				dashboardLayout.addComponent(statisticsComponent, 1);
				epiCurveAndMapLayout.addComponent(epiCurveLayout, 0);
				mapLayout.setHeight(400, Unit.PIXELS);
				ContactsDashboardView.this.setHeightUndefined();
				epiCurveAndMapLayout.setHeightUndefined();
			}
		});

		return layout;
	}

	public void refreshDashboard() {
		super.refreshDashboard();

		// Updates statistics
		statisticsComponent.updateStatistics(dashboardDataProvider.getDisease());
		updateCaseCountsAndSourceCasesLabels();
		
		// Update cases and contacts shown on the map
		if (mapComponent != null)
			mapComponent.refreshMap();

		// Epi curve chart has to be created again due to a canvas resizing issue when
		// simply refreshing the component
		if (epiCurveComponent != null)
			epiCurveComponent.clearAndFillEpiCurveChart();
	}

}
