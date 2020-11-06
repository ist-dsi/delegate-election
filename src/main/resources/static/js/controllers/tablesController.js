angular.module('delegados').controller('tablesCtrl', ['$rootScope', '$scope', '$http', '$log', 'periodEdit', 'history', 'bulkEdit', 'degrees',
                                                          function(rc, sc, http, log, periodEdit, history, bulkEdit, degrees)  {
	
	sc.details = false;
	
	sc.loaded = false;
	
	sc.allSelected = false;
	
	sc.active = 'Delegados';

	sc.currentYear = '';
	
	sc.loadDegrees = function() {
		degrees.loadDegrees().then(function(data) {
			sc.loaded = true;
			if(degrees.getDegrees()){
				sc.currentYear = degrees.getDegrees()[0].academicYear;
			}
		})
		
	};
	
	sc.filteredDegrees = function() {
		return degrees.getFilteredDegrees();
	}
	
	sc.loadDegrees();
	
	sc.selection = [];

	sc.filterDegrees = function(filter) {
		sc.selection = [];
		sc.allSelected = false;
		degrees.filterDegrees(filter);
//		log.log(filter);
	}
	
	sc.toggleSelection = function(degree) {
		var index = sc.selection.indexOf(degree);
		
		if(index > -1) {
			sc.selection.splice(index, 1);
		}
		else {
			sc.selection.push(degree);
		}
	};
	
	sc.toggleSelectAll = function() {
		if(!sc.allSelected) {
			for(degree in sc.filteredDegrees()) {
				var name = sc.filteredDegrees()[degree].degree;
				var index = sc.selection.indexOf(name);
				
				if(index <= -1) {
					sc.selection.push(name);
				}
			}
			sc.allSelected = true;
		}
		else {
			for(degree in sc.filteredDegrees()) {
				var name = sc.filteredDegrees()[degree].degree;
				var index = sc.selection.indexOf(name);
				
				if(index > -1) {
					sc.selection.splice(index, 1);
				}
			}
			sc.allSelected = false;
		}
	}
	
	sc.showDetails = function(degree, year) {
		history.setAcademicYear(sc.currentYear);
		history.setAcronym(degree.degree);
		history.loadHistory(degree.degreeId, year).then(function(data) {
			history.setDegree(degree.years[year-1]);
		});
		sc.details = true;
	}
	
	sc.hideDetails = function() {
		sc.details = false;
	}
	
	sc.isEmpty = function (obj) {
	    for (var i in obj) if (obj.hasOwnProperty(i)) return false;
	    return true;
	}
	
	sc.setSelectionDetails = function(type, operation, degree, year) {
		periodEdit.setSelectedPeriodType(type);
		periodEdit.setSelectedPeriodOperation(operation);
		periodEdit.setSelectedDegree(degree);
		periodEdit.setSelectedYear(year);
	}
	
	sc.setBulkEdit = function() {
		var selectedDegrees = [];
		
		for(selectionIndex in sc.selection) {
			for(degIndex in sc.filteredDegrees()) {
				if(sc.selection[selectionIndex] == sc.filteredDegrees()[degIndex].degree) {
					selectedDegrees.push(sc.filteredDegrees()[degIndex].degreeId);
				}
			}
		}
		
		bulkEdit.setSelectedDegrees(selectedDegrees);
		bulkEdit.setYear(sc.currentYear);
//		log.log(bulkEdit.getYear());
	}
}]);