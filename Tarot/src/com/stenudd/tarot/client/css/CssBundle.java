package com.stenudd.tarot.client.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface CssBundle extends ClientBundle {

  public static final CssBundle INSTANCE = GWT.create(CssBundle.class); 

  @Source("../../tarot.css")
  @CssResource.NotStrict
  CssResource css();
}
