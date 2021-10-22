/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.appservice.serviceplan;

import com.microsoft.azure.toolkit.eclipse.appservice.PricingTierCombobox;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.List;

public class ServicePlanCreationDialog extends Dialog {
    private Text text;
    private PricingTierCombobox pricingTierCombobox;
    private DraftServicePlan data;

    public ServicePlanCreationDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
    }


    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.numColumns = 2;

        Label lblNewLabel = new Label(container, SWT.WRAP);
        GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_lblNewLabel.widthHint = 160;
        lblNewLabel.setLayoutData(gd_lblNewLabel);
        lblNewLabel.setText("App Service plan pricing tier determines the location, features, cost and compute resources associated with your app.");

        Label lblNewLabel_1 = new Label(container, SWT.NONE);
        lblNewLabel_1.setText("Name:");

        text = new Text(container, SWT.BORDER);
        GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_text.widthHint = 257;
        text.setLayoutData(gd_text);

        Label lblNewLabel_1_1 = new Label(container, SWT.NONE);
        lblNewLabel_1_1.setText("Pricing tier:");

        pricingTierCombobox = new PricingTierCombobox(container, this.pricingTiers);
        pricingTierCombobox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        return container;
    }


    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(420, 158);
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setMinimumSize(new Point(360, 220));
        super.configureShell(newShell);
        newShell.setText("New App Service plan");
    }

    public void setPricingTier(List<PricingTier> pricingTiers) {
        this.pricingTiers = pricingTiers;
        if (pricingTierCombobox != null && pricingTierCombobox.isEnabled()) {
            pricingTierCombobox.refreshItems();
        }

    }

    private List<PricingTier> pricingTiers = null;

    public DraftServicePlan getData() {
        return data;
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {

            this.data = new DraftServicePlan(text.getText(), pricingTierCombobox.getValue());
        }
        super.buttonPressed(buttonId);
    }
}
