/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2022 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.forms.core.components.internal.models.v2.form;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.adobe.cq.export.json.SlingModelFilter;
import com.adobe.cq.forms.core.Utils;
import com.adobe.cq.forms.core.components.models.form.FormContainer;
import com.adobe.cq.forms.core.context.FormsCoreComponentTestContext;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.msm.api.MSMNameConstants;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ExtendWith(AemContextExtension.class)
public class FormContainerImplTest {
    private static final String BASE = "/form/formcontainer";
    private static final String CONTENT_ROOT = "/content";
    private static final String PATH_FORM_1 = CONTENT_ROOT + "/formcontainerv2";

    private final AemContext context = FormsCoreComponentTestContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.load().json(BASE + FormsCoreComponentTestContext.TEST_CONTENT_JSON, CONTENT_ROOT);
        context.registerService(SlingModelFilter.class, new SlingModelFilter() {

            private final Set<String> IGNORED_NODE_NAMES = new HashSet<String>() {
                {
                    add(NameConstants.NN_RESPONSIVE_CONFIG);
                    add(MSMNameConstants.NT_LIVE_SYNC_CONFIG);
                    add("cq:annotations");
                }
            };

            @Override
            public Map<String, Object> filterProperties(Map<String, Object> map) {
                return map;
            }

            @Override
            public Iterable<Resource> filterChildResources(Iterable<Resource> childResources) {
                return StreamSupport
                    .stream(childResources.spliterator(), false)
                    .filter(r -> !IGNORED_NODE_NAMES.contains(r.getName()))
                    .collect(Collectors.toList());
            }
        });
    }

    @Test
    void testExportedType() throws Exception {
        FormContainer formContainer = getFormContainerUnderTest(PATH_FORM_1);
        assertEquals("core/fd/components/form/container/v2/container", formContainer.getExportedType());
        FormContainer formContainerMock = Mockito.mock(FormContainer.class);
        Mockito.when(formContainerMock.getExportedType()).thenCallRealMethod();
        assertEquals("", formContainerMock.getExportedType());
    }

    @Test
    void testGetId() throws Exception {
        FormContainer formContainer = getFormContainerUnderTest(PATH_FORM_1);
        assertNotNull(formContainer.getId());
    }

    @Test
    void testGetAction() throws Exception {
        FormContainer formContainer = getFormContainerUnderTest(PATH_FORM_1);
        assertEquals("/a/b", formContainer.getMetaData().getAction());
    }

    @Test
    void testGetDataUrl() throws Exception {
        FormContainer formContainer = getFormContainerUnderTest(PATH_FORM_1);
        assertEquals("/c/d", formContainer.getMetaData().getDataUrl());
    }

    @Test
    void testJSONExport() throws Exception {
        FormContainer formContainer = getFormContainerUnderTest(PATH_FORM_1);
        Utils.testJSONExport(formContainer, Utils.getTestExporterJSONPath(BASE, PATH_FORM_1));
    }

    private FormContainer getFormContainerUnderTest(String resourcePath) throws Exception {
        context.currentResource(resourcePath);
        MockSlingHttpServletRequest request = context.request();
        return request.adaptTo(FormContainer.class);
    }
}
