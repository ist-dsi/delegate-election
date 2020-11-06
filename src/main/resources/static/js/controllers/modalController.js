angular.module('delegados').controller('modalCtrl', ['$rootScope', '$scope', '$http', '$log', 'periodEdit', 'formatDate',
                                                          function(rc, sc, http, log, periodEdit, formatDate)  {
	sc.today = function() {
		sc.dt = new Date();
	};
	sc.today();
	
	sc.clear = function () {
		sc.dt = null;
	};

	sc.openStart = function($event) {
		sc.status.startOpened = true;
	};
	
	sc.openEnd = function($event) {
		sc.status.endOpened = true;
	};
	
	sc.status = {
			startOpened: false,
			EndOpened: false
	};


	sc.start = new Date();
	
	sc.end = new Date();
	  
	sc.selectedPeriodType = function() {
		return periodEdit.getSelectedPeriodType();
	}
	  
	sc.selectedPeriodOperation = function() {
		return periodEdit.getSelectedPeriodOperation();
	}
	  
	sc.selectedDegree = function() {
		return periodEdit.getSelectedDegree();
	}
	
	sc.selectedYear = function() {
		return periodEdit.getSelectedYear();
	}
	
	sc.edit = function() {
		periodEdit.edit(formatDate(sc.start), formatDate(sc.end));
	}
		
}]);	