'use strict';

/**
 * @ngdoc overview
 * @name clientApp
 * @description
 * # clientApp
 *
 * Main module of the application.
 */
angular
  .module('codebriefcaseApp', [
    'codebriefcaseApp.constants',
    'ngCookies',
    'ngMaterial',
    'ngResource',
    'ngSanitize',
    'ui.ace',
    'ui.router'
  ])
  .config(function($stateProvider,$httpProvider, $locationProvider) {
    $stateProvider.state('List', {
      url: '/',
      templateUrl: 'app/list/list.html',
      controller: 'ListCtrl',
      data: {title: 'Code Briefcase'}
    }).state('Add', {
      url: '/items/add',
      templateUrl: 'app/add/add.html',
      controller: 'AddCtrl',
      data: {title: 'Add', }
    }).state('Detail', {
      url: '/items/:_id',
      templateUrl: 'app/detail/detail.html',
      controller: 'DetailCtrl',
      data: {title: 'Details'}
    });
    $locationProvider.html5Mode(true);
  })
  .config(function($mdThemingProvider) {
    $mdThemingProvider.theme('default')
      .primaryPalette('indigo')
      .accentPalette('red');
  });

// ace path fix for minify
/* global ace */
ace.config.set('basePath', 'bower_components/ace-builds/src-min-noconflict');
ace.config.set('modePath', 'bower_components/ace-builds/src-min-noconflict');
ace.config.set('themePath', 'bower_components/ace-builds/src-min-noconflict');
ace.config.set('workerPath', 'bower_components/ace-builds/src-min-noconflict');
