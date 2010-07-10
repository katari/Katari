package com.globant.katari.gadgetcontainer.domain;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.UUID;

import org.junit.Test;

/**
 * Test for the bean  {@link GadgetInstance}
 * 
 * @author waabox (emiliano[dot]arango[at]globant[dot]com)
 *
 */
public class GadgetInstanceTest {
  
  @Test
  public void testConstructorOk() { 
    GadgetInstance gi = new GadgetInstance("url", "position");
    assertTrue(gi.getUrl().equals("url"));
    assertTrue(gi.getGadgetPosition().equals("position"));
    assertTrue(gi.getId() == 0);
  }
  
  @Test
  public void testConstructorWithNullGadgetUrl() {
    try {
      new GadgetInstance(null, "1");
      fail("Should be an illegal argument exception because gadget url is null");
    } catch (IllegalArgumentException e) {
      
    }
  }
  
  @Test
  public void testConstructorWithEmptyGadgetUrl() {
    try {
      new GadgetInstance("", "1");
      fail("Should be an illegal argument exception because gadget url is empty");
    } catch (IllegalArgumentException e) {
      
    }
  }
  
  @Test
  public void testConstructorWithNullPosition() {
    try {
      new GadgetInstance("1", null);
      fail("Should be an illegal argument exception because position is null");
    } catch (IllegalArgumentException e) {
      
    }
  }
  
  @Test
  public void testConstructorWithEmptyPosition() {
    try {
      new GadgetInstance("1", "");
      fail("Should be an illegal argument exception because position is empty");
    } catch (IllegalArgumentException e) {
      
    }
  }
  
  @Test
  public void testSetSecurityToken() {
    GadgetInstance gi = new GadgetInstance("1", "1");
    try {
      gi.associateToViewer("", 1);
      fail("Should be an illegal argument exception because securityToken is empty");
    } catch (IllegalArgumentException e) {
      // nothing
    }
    try {
      gi.associateToViewer(null, 1);
      fail("Should be an illegal argument exception because securityToken is null");
    } catch (IllegalArgumentException e) {
      // nothing
    }
    
    String st = "theToken";
    gi.associateToViewer(st, 1);
    
    assertTrue(gi.getSecurityToken().equals(st));
  }
  
  @Test
  public void testSetViewer() {
    GadgetInstance gi = new GadgetInstance("1", "1");
    try {
      gi.associateToViewer("token",0);
      fail("Should be an illegal argument exception because viewer is empty");
    } catch (IllegalArgumentException e) {
      // nothing
    }
    
    gi.associateToViewer("token", 1);
    
    assertThat(gi.getViewer(), is(1l));
  }
  
  @Test
  public void testChangePosition() {
    GadgetInstance gi = new GadgetInstance("1", "1");
    try {
      gi.move("");
      fail("Should be an illegal argument exception because position is empty");
    } catch (IllegalArgumentException e) {
      // nothing
    }
    try {
      gi.move(null);
      fail("Should be an illegal argument exception because position is null");
    } catch (IllegalArgumentException e) {
      // nothing
    }
    String position = UUID.randomUUID().toString();
    gi.move(position);
    assertTrue(gi.getGadgetPosition().equals(position));
  }
}

