/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.coronawarn.verification.service;

import app.coronawarn.verification.VerificationApplication;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = VerificationApplication.class)
public class JwTServiceTest {
  
  public JwTServiceTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of validateToken method, of class JwTService.
   */
  @Test
  public void testValidateToken() {
  
  }

  /**
   * Test of getRoleNameFromToken method, of class JwTService.
   */
  @Test
  public void testGetRoleNameFromToken() {
  }

  /**
   * Test of getClaimFromToken method, of class JwTService.
   */
  @Test
  public void testGetClaimFromToken() {
  }
  
}
