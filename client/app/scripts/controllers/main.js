'use strict';

/**
 * @ngdoc function
 * @name angularclientApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the angularclientApp
 */
angular.module('angularclientApp')
  .controller('MainCtrl', function ($scope,$location,$http,ENV) {
	  
	  var restApi = ENV.apiEndpoint;
	  
		function loadProcesses() {
			var url = restApi + '/engine-rest/engine/default/process-definition';
			$http.get(url).then(function(response) {
				$scope.processes = response.data;
			});
		}
		
		loadProcesses();
	    
		function loadTasks() {
			  var url = restApi + '/engine-rest/engine/default/task';
		      $http.get(url).then(function(response){ 
		    	  $scope.tasks = response.data;
		      }); 
		}
		loadTasks();
	    
	    $scope.startProcess = function(processId) {
	    	var url = restApi + '/engine-rest/engine/default/process-definition/' + processId + '/start';
		      $http.post(url).then(function(response){ 
		    	  $location.path('/task/' + response.data.id);
		      }); 
	    };
	    
  });
