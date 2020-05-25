#!/usr/bin/env groovy 
@Library('sbs-jenkinslib') _

/* 
 * T-Systems SBS pipeline build see below link for internal (sorry) 
 * documentation details. 
 * https://sbs.t-systems.com/wiki/Job+Type:+Jenkins+Pipeline+Build 
 */
sbsBuild(
  jdk: 'jdk11',
  dockerAlternateRegistries: [
    'MTR_SBS@mtr.external.otc.telekomcloud.com/sbs/cwa-verification-server'
  ]
)
