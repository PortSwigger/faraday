/*
 * Faraday Penetration Test IDE Extension for Burp
 * Copyright (C) 2019  Infobyte LLC (http://www.infobytesec.com/)
 * See the file 'LICENSE' for the license information
 */

package burp;

import burp.faraday.FaradayConnector;
import burp.faraday.FaradayExtensionUI;
import burp.faraday.VulnerabilityMapper;
import burp.faraday.exceptions.InvalidFaradayException;
import burp.faraday.exceptions.ObjectNotCreatedException;
import burp.faraday.models.ExtensionSettings;
import burp.faraday.models.vulnerability.Vulnerability;

import javax.swing.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static burp.IContextMenuInvocation.*;

public class BurpExtender implements IBurpExtender, IExtensionStateListener, IScannerListener, IContextMenuFactory {

    private static final String EXTENSION_VERSION = "2.0";

    private static final String EXTENSION_NAME = "Faraday for Burp v" + EXTENSION_VERSION;

    private IBurpExtenderCallbacks callbacks;
    private PrintWriter stdout;


    private IExtensionHelpers helpers;

    private FaradayConnector faradayConnector;
    private FaradayExtensionUI faradayExtensionUI;
    private ExtensionSettings extensionSettings;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        callbacks.setExtensionName(EXTENSION_NAME);

        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        VulnerabilityMapper.setHelpers(helpers);

        stdout = new PrintWriter(callbacks.getStdout(), true);
        this.faradayConnector = new FaradayConnector(stdout);
        this.extensionSettings = new ExtensionSettings(callbacks);
        this.faradayExtensionUI = new FaradayExtensionUI(stdout, callbacks, faradayConnector, extensionSettings);

        callbacks.addSuiteTab(faradayExtensionUI);

        log(EXTENSION_NAME + " Loaded");

        callbacks.registerScannerListener(this);
        callbacks.registerContextMenuFactory(this);
        callbacks.registerExtensionStateListener(this);
    }

    @Override
    public void extensionUnloaded() {
        log("Unloading extension");
        faradayConnector.logout();

    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        final ArrayList<JMenuItem> menu = new ArrayList<>();

        // Which part of the interface the user selects
        byte ctx = invocation.getInvocationContext();

        JMenuItem menuItem;
        switch (ctx) {
            case CONTEXT_SCANNER_RESULTS:
                menuItem = new JMenuItem("Send issue to Faraday", null);
                menuItem.addActionListener(actionEvent -> onSendVulnsToFaraday(invocation.getSelectedIssues()));
                menu.add(menuItem);

                break;

            case CONTEXT_TARGET_SITE_MAP_TABLE:
            case CONTEXT_PROXY_HISTORY:
            case CONTEXT_MESSAGE_VIEWER_REQUEST:
                menuItem = new JMenuItem("Send request to Faraday", null);
                menuItem.addActionListener(actionEvent -> onSendRequestsToFaraday(invocation.getSelectedMessages()));
                menu.add(menuItem);

                break;

        }

        return menu;
    }

    private void onSendVulnsToFaraday(IScanIssue[] issues) {
        if (issues == null) {
            return;
        }

        faradayExtensionUI.runInThread(() -> {
            final List<Vulnerability> vulnerabilities = Arrays.stream(issues).map(VulnerabilityMapper::fromIssue).collect(Collectors.toList());

            for (Vulnerability vulnerability : vulnerabilities) {
                if (!faradayExtensionUI.addVulnerability(vulnerability)) {
                    break;
                }
            }
        });
    }

    private void onSendRequestsToFaraday(IHttpRequestResponse[] messages) {
        if (messages == null) {
            return;
        }

        faradayExtensionUI.runInThread(() -> {
            final List<Vulnerability> vulnerabilities = Arrays.stream(messages).map(VulnerabilityMapper::fromRequest).collect(Collectors.toList());

            for (Vulnerability vulnerability : vulnerabilities) {
                if (!faradayExtensionUI.addVulnerability(vulnerability)) {
                    break;
                }
            }
        });
    }

    private void log(final String msg) {
        this.stdout.println("[EXTENDER] " + msg);
    }

    @Override
    public void newScanIssue(IScanIssue issue) {
        if (!extensionSettings.importNewVulns()) {
            return;
        }

        faradayExtensionUI.runInThread(() -> faradayExtensionUI.addVulnerability(VulnerabilityMapper.fromIssue(issue)));
    }


}

