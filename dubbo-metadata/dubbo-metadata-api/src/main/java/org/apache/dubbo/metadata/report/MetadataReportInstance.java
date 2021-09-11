/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.metadata.report;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.URLBuilder;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.rpc.model.ApplicationModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.dubbo.common.constants.CommonConstants.APPLICATION_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_DIRECTORY;
import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_KEY;
import static org.apache.dubbo.metadata.report.support.Constants.METADATA_REPORT_KEY;

/**
 * 2019-08-09
 */
public class MetadataReportInstance {

    private static final Logger logger = LoggerFactory.getLogger(MetadataReportInstance.class);

    protected volatile boolean forbidden = false;

    private AtomicBoolean init = new AtomicBoolean(false);

    private volatile Map<String, MetadataReport> metadataReports = new HashMap<>();

    public void init(MetadataReportConfig config) {
        if (init.get()) {
            return;
        }
        forbidden = false;
        Map<String, MetadataReport> localMetadataReports = metadataReports; // local reference
        ApplicationModel applicationModel = config.getApplicationModel();

        MetadataReportFactory metadataReportFactory = applicationModel.getExtensionLoader(MetadataReportFactory.class).getAdaptiveExtension();
        URL url = config.toUrl();
        if (METADATA_REPORT_KEY.equals(url.getProtocol())) {
            String protocol = url.getParameter(METADATA_REPORT_KEY, DEFAULT_DIRECTORY);
            url = URLBuilder.from(url)
                    .setProtocol(protocol)
                    .setScopeModel(config.getScopeModel())
                    .removeParameter(METADATA_REPORT_KEY)
                    .build();
        }
        url = url.addParameterIfAbsent(APPLICATION_KEY, applicationModel.getCurrentConfig().getName());
        String relatedRegistryId = config.getRegistry() == null ? DEFAULT_KEY : config.getRegistry();
//        RegistryConfig registryConfig = applicationModel.getConfigManager().getRegistry(relatedRegistryId)
//                .orElseThrow(() -> new IllegalStateException("Registry id " + relatedRegistryId + " does not exist."));
        MetadataReport metadataReport = metadataReportFactory.getMetadataReport(url);
        if (metadataReport != null) {
            localMetadataReports.put(relatedRegistryId, metadataReport);
        }
        init.set(true);
    }

    public Map<String, MetadataReport> getMetadataReports(boolean checked) {
        if (checked) {
            checkInit();
        }
        return metadataReports;
    }

    public MetadataReport getMetadataReport(String registryKey) {
        checkInit();
        Map<String, MetadataReport> localMetadataReports = metadataReports; // local reference
        MetadataReport metadataReport = localMetadataReports.get(registryKey);        
        if (metadataReport == null) {
            metadataReport = localMetadataReports.values().iterator().next();
        }
        return metadataReport;
    }


    private void checkInit() {
        if (forbidden) {
            throw new IllegalStateException("the metadata report was reset.");
        }
        if (!init.get()) {
            throw new IllegalStateException("the metadata report was not initialized.");
        }
    }

    public static void reset() {
//        metadataReports.clear();
//        init.set(false);
    }

    public void resetInstance() {
        if (!init.get()) {
            return;
        }
        try {
            forbidden = true;
            Map<String, MetadataReport> localMetadataReports = metadataReports; // local reference
            metadataReports = new HashMap<>();
            localMetadataReports.entrySet().forEach(metadataReportEntry -> {
                try {
                    metadataReportEntry.getValue().destroy();
                } catch (Exception e) {
                    logger.warn("destroy metadata report failure, relatedRegistryId: " + metadataReportEntry.getKey(), e);
                }
            });            
        } catch (Exception e1) {
            logger.warn("reset metadata report instance failure.", e1);
        }
        init.set(false);        
    }
}
