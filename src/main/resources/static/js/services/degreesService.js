angular.module('delegados').factory('degrees', ['$log', '$http', function(log, http) {
	var degrees = [];
	var filteredDegrees = [];
	var lastFilter = 'Todos';
	
	function getDegrees() {
		return degrees;
	}
	
	function getFilteredDegrees() {
		return filteredDegrees;
	}

	function isLicBol(value) {
		return value.degreeType == 'Licenciatura Bolonha';
	}
	
	function isMesBol(value) {
		return value.degreeType == 'Mestrado Bolonha';
	}
	
	function isMesInt(value) {
		return value.degreeType == 'Mestrado Integrado';
	}
	
	function filterDegrees(filter) {
		lastFilter = filter;
		switch(filter) {
		case 'Todos':
			filteredDegrees = degrees;
			break;
		case 'Licenciatura Bolonha':
			filteredDegrees = degrees.filter(isLicBol);
			break;
		case 'Mestrado Bolonha':
			filteredDegrees = degrees.filter(isMesBol);
			break;
		case 'Mestrado Integrado':
			filteredDegrees = degrees.filter(isMesInt);
			break;
		}
//		log.log(filter);
	}
	
	function loadDegrees(){
		var promise = http.get('periods').success(function(data){
			degrees = data;
//			log.log(degrees);
			filterDegrees(lastFilter);
		});
		return promise;
	};
	
	return {
		getDegrees: getDegrees,
		getFilteredDegrees: getFilteredDegrees,
		filterDegrees: filterDegrees,
		loadDegrees: loadDegrees
	}
}]);