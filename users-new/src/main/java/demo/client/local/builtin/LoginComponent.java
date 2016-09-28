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

import java.util.function.BiConsumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.common.client.dom.PasswordInput;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class LoginComponent implements IsElement {

    @Inject
    @DataField
    private TextInput username;

    @Inject
    @DataField
    private PasswordInput password;

    @Inject
    @DataField
    private Button submit;

    private BiConsumer<String, String> submitCallback;

    @EventHandler("submit")
    private void onSubmit( @ForEvent("click") final MouseEvent evt ) {
        if ( submitCallback != null ) submitCallback.accept( username.getValue(), password.getValue() );
    }

    public void setCallback( final BiConsumer<String, String> callback ) {
        this.submitCallback = callback;
    }

}
