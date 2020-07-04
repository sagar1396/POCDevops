'use strict';

/**
 * @ngdoc overview
 * @name angularclientApp
 * @description
 * # angularclientApp
 *
 * Main module of the application.
 */
angular
  .module('angularclientApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'formio',
    'config'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl',
        controllerAs: 'main'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .when('/task/:taskid', {
        templateUrl: 'views/task.html',
        controller: 'TaskCtrl',
        controllerAs: 'task'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
