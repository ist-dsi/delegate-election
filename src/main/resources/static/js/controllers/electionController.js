angular.module('delegados').controller('electionCtrl', ['$rootScope', '$scope', '$http', '$log',
                                                          function($rootScope, $scope, $http, $log)  {
	
	/***
	 * INIT
	 */
	
	$http.get('user').success(function(data) {
		if (data) {
//			$log.log(data);
			$rootScope.degree = null;
			
			$rootScope.authenticated = true;
			
			$rootScope.id = data.name;
			
			$rootScope.credentials = data;
			
			$rootScope.degrees = [];
			
			$rootScope.reloadDegrees();
			
		} else {
			$rootScope.authenticated = false;
		}
	}).error(function() {
		$rootScope.authenticated = false;
	});
	
	$scope.subTitle = 'START'
		
	$rootScope.debug = true;
	
	$rootScope.voted = false;
	
	$rootScope.applied = false;
	
	$rootScope.loaded = false;
	
	$rootScope.nextPeriod = false;
	
	$rootScope.showEmpty = false;

	/***
	 * DEGREES
	 */
	$rootScope.reloadDegrees = function() {
		$http.get('students/'+$rootScope.credentials.username+'/degrees').success(function(data){
			$rootScope.degrees = data;
//			$log.log(data);
//			$log.log($rootScope.degrees);
			if($rootScope.degrees.length == 1) {
				$scope.selection = 0;
				$scope.setDegree();
			}
			
			$scope.checkNextPeriod();
		});
	}
	
	$rootScope.reloadCandidates = function() {
		$http.get('degrees/'+$rootScope.degree.id+'/years/'+$rootScope.degree.curricularYear+'/candidates')
		.success(function(data) { 
			$rootScope.candidatos = data;
		});
//		$log.log('got candidates');
		$http.get('degrees/'+$rootScope.degree.id+'/years/'+$rootScope.degree.curricularYear+'/candidates/'+$rootScope.credentials.username)
		.success(function(data) { 
			if(data != '') {
				$rootScope.applied = true;
			}
		});
//		$log.log('checked application');
		$http.get('students/'+$rootScope.credentials.username+'/degrees/'+$rootScope.degree.id+'/votes')
		.success(function(data) {
			if(data != '' && data != "Student hasn't voted." && data != 'No election period.' ) {
				$rootScope.voted = true;
				$rootScope.voto = data;
			}
			$rootScope.loaded = true;	
		}).error(function(data) {		
			$rootScope.loaded = true;	
		});
//		$log.log('checked voted');
	}
	
	$scope.setDegree = function() {
		$rootScope.showEmpty = true;
		$rootScope.loaded = false;
		$rootScope.degree = $rootScope.degrees[$scope.selection];
		$rootScope.candidatos=[];
		$rootScope.reloadCandidates();
		$scope.setSubtitle();
	}
	
	$scope.setSubtitle = function() {
		if($rootScope.degree.currentPeriod.type == 'APPLICATION') {
			$scope.subTitle = 'APPLICATION';
		}
		else if($rootScope.degree.currentPeriod.type == 'ELECTION')  {
			$scope.subTitle = 'ELECTION';
		}
	}
	
	$scope.checkNextPeriod = function() {
//		$log.log('checking next period')
		if($rootScope.degree != null) {
			$http.get('/degrees/' + $rootScope.degree.id + '/years/' + $rootScope.degree.curricularYear + '/periods')
				.success(function(data) {
					if (data.start && data.end) {
//						$log.log('exists');
						$scope.start = data.start;
						$scope.end = data.end;
//						$log.log('start: ' + $scope.start + ' end: ' + $scope.end);
						$rootScope.nextPeriod = true;
					}
				});
		}
	}
	
	$rootScope.isEmpty = function (obj) {
	    for (var i in obj) if (obj.hasOwnProperty(i)) return false;
	    return true;
	}
	/***
	 * DEBUG
	 */
//	$scope.debugVotePeriod = function() {
//		$rootScope.votePeriod = !$rootScope.votePeriod;
//	}
//	
//	$rootScope.toggleDebug = function() {
//		$rootScope.debug = !$rootScope.debug;
//	}
//	
//	
//	$scope.specialLogin = function(){		//debug
//		$rootScope.loaded = false;
//		$http.post('get-user', $scope.special_username)
//			.success(function(data) {
//				$rootScope.credentials = data;
//				$rootScope.id = data.name;
//			});
//		$rootScope.authenticated = true;
//	}
//	
}]);

