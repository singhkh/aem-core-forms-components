/*
 * Copyright 2021 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adobe.cq.forms.core.components.internal.services.formsportal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.adobe.cq.forms.core.components.models.formsportal.DraftsAndSubmissions;
import com.adobe.cq.forms.core.components.models.formsportal.PortalLister;
import com.adobe.cq.forms.core.components.models.services.formsportal.Operation;
import com.adobe.cq.forms.core.components.models.services.formsportal.OperationManager;

@Component(
    service = OperationManager.class,
    immediate = true)
public class OperationManagerImpl implements OperationManager {
    @Reference(
        service = Operation.class,
        policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.MULTIPLE,
        bind = "bindOperation",
        unbind = "unbindOperation")
    private volatile Map<String, Operation> operations = new ConcurrentHashMap<String, Operation>();

    private Map<DraftsAndSubmissions.TypeEnum, List<Operation>> operationLists = new ConcurrentHashMap<>();

    private void refreshOperationLists() {
        List<Operation> draftOperations = new ArrayList<>();
        List<Operation> submitOperations = new ArrayList<>();
        for (Map.Entry<String, Operation> entry : operations.entrySet()) {
            Operation operation = entry.getValue();
            switch (operation.getType()) {
                case DRAFT:
                    draftOperations.add(operation);
                    break;
                case SUBMISSION:
                    submitOperations.add(operation);
                    break;
            }
        }
        operationLists.put(DraftsAndSubmissions.TypeEnum.DRAFT, draftOperations.stream().sorted(Comparator.comparing(
            Operation::getTitle).reversed()).collect(Collectors.toList()));
        operationLists.put(DraftsAndSubmissions.TypeEnum.SUBMISSION, submitOperations);
    }

    protected void bindOperation(final Operation operation, Map<String, Object> config) {
        String operationName = operation.getName();
        operations.put(operationName, operation);
        refreshOperationLists();
    }

    protected void unbindOperation(final Operation operation, Map<String, Object> config) {
        String operationName = operation.getName();
        if (operations.containsKey(operationName)) {
            operations.remove(operationName);
            refreshOperationLists();
        }
    }

    public Operation getOperation(String operationName) {
        return operations.get(operationName);
    }

    public List<Operation> getOperationList(DraftsAndSubmissions.TypeEnum typeEnum, PortalLister.Item item, String requestURI) {
        List<Operation> ops = Optional.ofNullable(operationLists.get(typeEnum))
            .orElse(Collections.emptyList())
            .stream().map(x -> x.makeOperation(item, requestURI))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (ops.isEmpty()) {
            return null;
        }
        return ops;
    }
}
