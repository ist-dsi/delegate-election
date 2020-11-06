angular.module('delegados').factory('history', ['$log', '$http', function(log, http){
	var academicYear = "";
	
	var acronym = "";
	
	var degree = {}
	
	var history = {};
	
	var candidates = [];
	
	var inspect = {};
	
	var inspectCandidates = [];
	
	function getAcronym() {
		return acronym;
	}
	
	function setAcronym(a) {
		acronym = a; 
	}
	
	function getHistory() {
		return history;
	}
	
	function inspectPeriod(index) {
		inspect = history.periods[index];
//		log.log(history.periods[index]);
//		log.log(inspect);
		loadInspectCandidates(inspect.periodId);
	}
	
	function getInspectedPeriod() {
		return inspect;
	}
	
	function getInspectedPeriodCandidates() {
		return inspectCandidates;
	}
	
	function getCandidates() {
		return candidates;
	}
	
	function setDegree(d) {
		degree = d;
		loadCandidates();
	}
	
	function setAcademicYear(year) {
//////		log.log('setter');
////		log.log('year: ' + year);
//		log.log('prev: ' + academicYear);
		academicYear = year;
//		log.log('new: ' + academicYear);
	}
	
	function getCurrentApplication() {
		return degree.applicationPeriod;
	}
	
	function getCurrentElection() {
		return degree.electionPeriod;
	}
	
	function loadCandidates(){
		if(history.periods.length == 0) {
			candidates = [];
			return;
		} 
		else if(history.periods[0].academicYear != academicYear) {
			candidates = [];
//			log.log(history.periods[0].academicYear);
//			log.log(academicYear);
//			log.log(history.periods[0].academicYear != degree.academicYear);
			return;
		}
		else {
			http.get('periods/' + history.periods[0].periodId + '/candidates').success(function(data) {
				if(!(data == 'No Period with that Id')) {
					candidates = data;
					var result = {usernames: []};
					for (index in candidates) {
						result.usernames.push(candidates[index].username); 
					}
					http.post('periods/' + history.periods[0].periodId + '/selfProposed', result)
						.success(function(data) {
						for(index in candidates) {
							candidates[index].selfProposed = data[candidates[index].username];
						}
					});
					http.get('periods/' + history.periods[0].periodId + '/votes', result).success(function(data) {
						for(index in candidates) {
							candidates[index].votes = data[candidates[index].username];
						}
						if(data.branco) {
							candidates.blank = data.branco;
						}
					});
				}
			}).then(function(data) {
//				log.log(candidates);
				candidates = candidates.sort(function(a, b){
					return a - b;
				});
//				log.log(candidates);
			});
		}
	}
	
	function loadInspectCandidates(id) {
		http.get('periods/' + id + '/candidates').success(function(data) {
			if(!(data == 'No Period with that Id')) {
				inspectCandidates = data;
				var result = {usernames: []};
				for (index in inspectCandidates) {
					result.usernames.push(inspectCandidates[index].username); 
				}
				http.post('periods/' + id + '/selfProposed', result)
					.success(function(data) {
					for(index in inspectCandidates) {
						inspectCandidates[index].selfProposed = data[inspectCandidates[index].username];
					}
				});
				http.get('periods/' + id + '/votes', result).success(function(data) {
					for(index in inspectCandidates) {
						inspectCandidates[index].votes = data[inspectCandidates[index].username];
					}
					if(data.branco) {
						inspectCandidates.blank = data.branco;
					}
				});
			}
		});
	}
	
	function loadHistory(degreeId, year) {
		var promise = http.get('degrees/' + degreeId + '/years/' + year + '/history').success(function(data) {
			history = data;
//			log.log(data.periods);
		});
		
		return promise;
	}
	
	return {
		getCandidates: getCandidates,
		getAcronym: getAcronym,
		setAcronym: setAcronym,
		getInspectedPeriod: getInspectedPeriod,
		getInspectedPeriodCandidates: getInspectedPeriodCandidates,
		getHistory: getHistory,
		getCurrentApplication: getCurrentApplication,
		getCurrentElection: getCurrentElection,
		setDegree: setDegree,
		setAcademicYear: setAcademicYear,
		loadHistory: loadHistory,
		loadCandidates: loadCandidates,
		loadInspectCandidates: loadInspectCandidates,
		inspectPeriod: inspectPeriod
	}
}]);