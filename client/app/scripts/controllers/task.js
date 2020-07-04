'use strict';

/**
 * @ngdoc function
 * @name angularclientApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the angularclientApp
 */
angular.module('angularclientApp')
  .controller('TaskCtrl', function ($scope,$window,$routeParams,$http,$location,ENV) {
	  $scope.taskId = $routeParams.taskid;
	  $scope.submission = {};
	  $scope.form = {};
	  var restApi = ENV.apiEndpoint;
	  
	  $scope.$on('formSubmit', function(err, submission) {
		  var url = restApi + '/task/submission/' + $scope.taskId;
		  $http.post(url,submission).then(function(response){ 
			  console.log(response);
			  if (response.data === true) {
				  $location.path('/');  
			  } else {
				  $window.alert('Validation error! Check you fields!');
			  }
	      });
	  });
	    
	  function loadTaskFormAndData(taskId) {
		  var url = restApi + '/task/' + taskId;
	      $http.get(url).then(function(response){ 
	    	  $scope.form = response.data;
	    	  $scope.submission = response.data.variables;
	      });          
	  }
	  
	  loadTaskFormAndData($scope.taskId);
  });
