angular.module('delegados').controller('historyCtrl', ['$rootScope', '$scope', '$http', '$log', 'history',
                                                          function(rc, sc, http, log, history)  {
	sc.history = function () {
		return history.getHistory();
	};
	
	sc.inspectedPeriod = function() {
		return history.getInspectedPeriod();
	};
	
	sc.candidates = function() {
		return history.getInspectedPeriodCandidates();
	}
}]);