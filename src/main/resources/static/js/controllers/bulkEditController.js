angular.module('delegados').controller('bulkCtrl', ['$rootScope', '$scope', '$http', '$log', 'bulkEdit', 'formatDate',
                                                          function(rc, sc, http, log, bulkEdit, formatDate)  {
	
    sc.today = function() {
	    sc.dt = new Date();
	};
	sc.today();
	
    sc.clear = function () {
    	sc.dt = null;
	};
	
	sc.openAppStart = function($event) {
	  sc.status.AppStartOpened = true;
	};
	
	sc.openAppEnd = function($event) {
		sc.status.AppEndOpened = true;
	};
	
	sc.openElecStart = function($event) {
		sc.status.ElecStartOpened = true;
	};
			
	sc.openElecEnd = function($event) {
		sc.status.ElecEndOpened = true;
	};
		
	sc.status = {
	  AppStartOpened: false,
	  AppEndOpened: false,
	  ElecStartOpened: false,
	  ElecEndOpened: false
	};	
	
	sc.applicationStart = new Date();
	sc.applicationEnd = new Date();
	sc.electionStart = new Date();
	sc.electionEnd = new Date();
	
	sc.years = [false, false, false, false, false];
		
	sc.academicYear = function() {
//		log.log(bulkEdit.getYear());
		return bulkEdit.getYear();
	}
	
	sc.bulkEdit = function() {
		bulkEdit.edit(sc.years, formatDate(sc.applicationStart), formatDate(sc.applicationEnd), formatDate(sc.electionStart), formatDate(sc.electionEnd));
	}
}]);
