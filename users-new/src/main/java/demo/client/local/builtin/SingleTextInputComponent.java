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

import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class SingleTextInputComponent implements IsElement {

    @Inject
    @DataField
    private TextInput input;

    @Inject
    @DataField
    private Button submit;

    private Consumer<String> submitCallback;

    @EventHandler("submit")
    private void onSubmit( final @ForEvent("click") MouseEvent evt ) {
        if ( submitCallback != null ) submitCallback.accept( input.getValue() );
    }

    public void setSubmitCallback( final Consumer<String> callback ) {
        this.submitCallback = callback;
    }

    public void setInputValue( final String input ) {
        this.input.setValue( input );
    }

}