angular.module('delegados').factory('bulkEdit', ['$rootScope', '$log', '$http', 'degrees', function(rc, log, http, degrees) {
	var selectedPeriodOperation = 'none';
	
	var selectedDegrees = [];
	var year = '';
	var result = {};
	
	function getResult() {
		return result;
	}
	
	function getYear() {
		return year;
	}
	
	function setYear(y) {
		year = y;
	}
	
	function setSelectedDegrees(degrees) {
		selectedDegrees = degrees;
//		log.log(selectedDegrees);
	}
	
	function edit(years, applicationStart, applicationEnd, electionStart, electionEnd) {
		var request = [];
		var dates = {electionPeriodStart:electionStart, 
					 electionPeriodEnd:electionEnd,
					 applicationPeriodStart:applicationStart,
					 applicationPeriodEnd:applicationEnd};
		
//		console.log(selectedDegrees);
		
		selectedDegrees.forEach(function(degree) {
			var degreeJson = {};
			degreeJson.degreeId = degree;
			var yearsJson = [];
			for (var i = 1; i <= 5; i++) {
				if (years[i]) {
					var yearJson = {};
					yearJson.degreeYear = i;
					yearJson.electionPeriod = {};
					yearJson.applicationPeriod = {};
					if (electionEnd && electionStart) {
						yearJson.electionPeriod.electionPeriodStart = electionStart;
						yearJson.electionPeriod.electionPeriodEnd = electionEnd;
					}
					if (applicationStart && applicationEnd) {
						yearJson.applicationPeriod.applicationPeriodStart = applicationStart;
						yearJson.applicationPeriod.applicationPeriodEnd = applicationEnd;
					}
					yearsJson.push(yearJson);
				}
			}
			degreeJson.years = yearsJson;
			request.push(degreeJson);
		});
		console.log(request);
//		log.log('start: ' + start + ' end ' + end);
		var requestUrl = 'periods';
		http.put(requestUrl, request).success(function(data) {
//			log.log('success edit apply');
		}).then(function(data) {
			degrees.loadDegrees();
		});
	};
	
	return {
		getResult: getResult,
		getYear: getYear,
		setYear: setYear,
		setSelectedDegrees: setSelectedDegrees,
		edit: edit
	};
}]);