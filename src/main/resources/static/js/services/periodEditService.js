angular.module('delegados').factory('periodEdit', ['$log', '$http', 'degrees', function(log, http, degrees){
	var selectedPeriodType = 'none';
	
	var selectedPeriodOperation = 'none';
	
	var selectedDegree = {};
	
	var selectedYear = {};
	
	function getSelectedPeriodType() {
		return selectedPeriodType;
	}
	
	function getSelectedPeriodOperation() {
		return selectedPeriodOperation;
	}
	
	function getSelectedDegree() {
		return selectedDegree;
	}
	
	function getSelectedYear() {
		return selectedYear;
	}
	
	function setSelectedPeriodType(type) {
		selectedPeriodType = type;
	};
	
	function setSelectedPeriodOperation(operation) {
		selectedPeriodOperation = operation;
	};
	
	function setSelectedDegree(degree) {
		selectedDegree = degree;
	};
	
	function setSelectedYear(year) {
		selectedYear = year;
	};
	
	function edit(start, end) {
		var dates = {start:start, end:end};
		
		if(selectedPeriodOperation == 'Estender') {
			
			var requestUrl = 'periods/';
			if(selectedPeriodType == 'Votação') {
				requestUrl += selectedYear.electionPeriod.electionPeriodId;
			}
			else if(selectedPeriodType == 'Candidatura') {
				requestUrl += selectedYear.applicationPeriod.applicationPeriodId;
			}
			
			http.put(requestUrl, dates).success(function(data) {
				degrees.loadDegrees();
			});
		}
		else if(selectedPeriodOperation == 'Criar') {
			
			var changes = {degreeId:selectedDegree.degreeId, degreeYear:selectedYear.degreeYear, periodType:'', start:start, end:end};
			
			if(selectedPeriodType == 'Votação') {
				changes.periodType = 'ELECTION';
			}
			else if(selectedPeriodType == 'Candidatura') {
				changes.periodType = 'APPLICATION'
			}
			
			http.post('periods', changes).success(function(data) {
				degrees.loadDegrees();
			});
		}
	};
	
	function remove() {
		
		var requestUrl = 'periods/';
		
		if(selectedPeriodType == 'Votação') {
			requestUrl += selectedYear.electionPeriod.electionPeriodId;
		}
		else if(selectedPeriodType == 'Candidatura') {
			requestUrl += selectedYear.applicationPeriod.applicationPeriodId;
		}
		
		http.delete(requestUrl).success(function(data) {
			degrees.loadDegrees();
		});
	};
	
	return {
		getSelectedPeriodType: getSelectedPeriodType,
		getSelectedPeriodOperation: getSelectedPeriodOperation,
		getSelectedDegree: getSelectedDegree,
		getSelectedYear: getSelectedYear,
		setSelectedPeriodType: setSelectedPeriodType,
		setSelectedPeriodOperation: setSelectedPeriodOperation,
		setSelectedDegree: setSelectedDegree,
		setSelectedYear: setSelectedYear,
		edit: edit,
		remove: remove
	};
}]);