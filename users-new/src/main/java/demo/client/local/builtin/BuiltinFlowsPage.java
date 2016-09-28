/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package demo.client.local.builtin;

import static org.jboss.errai.common.client.dom.DOMUtil.removeAllElementChildren;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.Option;
import org.jboss.errai.common.client.dom.Select;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowExecutor;
import org.livespark.flow.api.Unit;
import org.livespark.flow.api.descriptor.conversion.Converter;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;

import demo.client.AppFlowDescriptorInfo;
import demo.client.DescriptorService;

@Templated
@Page
@Dependent
public class BuiltinFlowsPage {

    @Inject
    private Converter converter;

    @Inject
    private Caller<DescriptorService> descriptorService;

    @Inject
    private DescriptorRegistry registry;

    @Inject
    private AppFlowExecutor executor;

    @Inject
    @DataField
    private Select flows;

    @Inject
    private Document doc;

    @Inject
    @DataField
    private Div flowContainer;

    private final Map<String, AppFlow<Unit, ?>> flowMap = new HashMap<>();

    @PageShowing
    public void showing() {
        descriptorService.call( (final AppFlowDescriptorInfo flowDescriptors) -> {
            flowDescriptors
                .getMap()
                .forEach( (name, descriptor) -> {
                    final AppFlow<?, ?> flow = converter.convert( registry, descriptor );
                    flowMap.put( name, (AppFlow<Unit, ?>) flow );
                    final Option option = (Option) doc.createElement( "option" );
                    option.setTextContent( name );
                    option.setValue( name );
                    flows.appendChild( option );
                } );
        } )
        .getAppFlowDescriptors();
    }

    @EventHandler("flows")
    private void onSelect( @ForEvent("change") final Event evt) {
        final AppFlow<Unit, ?> flow = Optional.ofNullable( flowMap.get( flows.getValue() ) ).orElseThrow( () -> new IllegalArgumentException() );
        executor.execute( flow );
    }

    public void display( @Observes final IsElement view ) {
        removeAllElementChildren( flowContainer );
        flowContainer.appendChild( view.getElement() );
    }

}
